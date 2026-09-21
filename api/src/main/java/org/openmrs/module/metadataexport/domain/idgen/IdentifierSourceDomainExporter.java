/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.idgen;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.idgen.IdentifierPool;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.RemoteIdentifierSource;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Identifier sources are written as one file per source type, mirroring Iniz's own fixture layout,
 * so each file carries an {@code _order:} header — pools reference their backing source by uuid, so
 * the pool file must load last. Sources Iniz cannot import — custom {@link IdentifierSource}
 * subclasses, remotes without a user, pools without an importable backing source — are left out and
 * reported as exclusions.
 */
@Slf4j
@Component
@OpenmrsProfile(modules = { "idgen:4.6.* - 9.*" })
public class IdentifierSourceDomainExporter extends CsvDomainExporter<IdentifierSource> {
	
	public static final String FILE_SEQUENTIAL = "idgen_sequential.csv";
	
	public static final String FILE_REMOTE = "idgen_remote.csv";
	
	public static final String FILE_POOL = "idgen_pool.csv";
	
	@Override
	protected List<BaseLineExporter<IdentifierSource>> chain() {
		return Arrays.asList(new IdentifierSourceLineExporter(), new SequentialIdentifierGeneratorLineExporter(),
		    new RemoteIdentifierSourceLineExporter(), new IdentifierPoolLineExporter());
	}
	
	@Override
	protected String fileName() {
		throw new UnsupportedOperationException("idgen writes one file per source type; see partition()");
	}
	
	@Override
	protected Map<String, Collection<IdentifierSource>> partition(Collection<IdentifierSource> instances) {
		Map<String, Collection<IdentifierSource>> files = new LinkedHashMap<>();
		for (IdentifierSource instance : instances) {
			IdentifierSource real = HibernateUtil.getRealObjectFromProxy(instance);
			if (!handles(real)) {
				// the manifest only holds sources handles() accepted; anything else is a routing bug, and
				// dropping it silently would leave the auto generation options that reference it dangling
				throw new IllegalStateException("Idgen: identifier source " + real.getUuid() + " of type "
				        + real.getClass().getName() + " reached the writer although it is not exportable");
			}
			if (real.getReservedIdentifiers() != null && !real.getReservedIdentifiers().isEmpty()) {
				// Iniz has no column for reserved identifiers, so a bootstrapped copy of this
				// source would hand out exactly the identifiers this server was told to skip;
				// warned here, on the write path, so dependency-closure sources are covered too
				log.warn("Idgen: identifier source {} has {} reserved identifier(s), which are not exported", real.getUuid(),
				    real.getReservedIdentifiers().size());
			}
			if (real instanceof IdentifierPool) {
				files.computeIfAbsent(FILE_POOL, f -> new ArrayList<>()).add(instance);
			} else if (real instanceof SequentialIdentifierGenerator) {
				files.computeIfAbsent(FILE_SEQUENTIAL, f -> new ArrayList<>()).add(instance);
			} else {
				files.computeIfAbsent(FILE_REMOTE, f -> new ArrayList<>()).add(instance);
			}
		}
		return files;
	}
	
	@Override
	protected Integer order(String fileName) {
		// pools must load after the sources they reference
		switch (fileName) {
			case FILE_SEQUENTIAL:
				return 1000;
			case FILE_REMOTE:
				return 2000;
			case FILE_POOL:
				return 3000;
			default:
				throw new IllegalArgumentException("Not an idgen export file: " + fileName);
		}
	}
	
	@Override
	public Domain getDomain() {
		return Domain.IDGEN;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof IdentifierSource && exports((IdentifierSource) instance);
	}
	
	/**
	 * Whether this domain writes an importable row for the source: a supported type, a non-blank user
	 * for remotes (nullable in idgen, required by Iniz), and for pools a backing chain ending in an
	 * importable source. Also consulted by the auto generation option exporter so an exported option
	 * can never reference a source this domain drops.
	 */
	static boolean exports(IdentifierSource source) {
		return importProblem(source) == null;
	}
	
	/**
	 * Why Iniz cannot import the source, as a clause following its uuid, or null when it can. Names the
	 * one cause that applies, and the backing source when the problem is down a pool chain.
	 */
	static String importProblem(IdentifierSource source) {
		IdentifierSource self = HibernateUtil.getRealObjectFromProxy(source);
		IdentifierSource real = self;
		Set<IdentifierSource> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		while (real instanceof IdentifierPool) {
			if (!seen.add(real)) {
				return "is a pool whose backing chain is a cycle";
			}
			IdentifierSource backing = ((IdentifierPool) real).getSource();
			if (backing == null) {
				return real == self ? "is a pool without a backing source"
				        : "is backed by pool " + real.getUuid() + ", which has no backing source";
			}
			real = HibernateUtil.getRealObjectFromProxy(backing);
		}
		String problem;
		if (real instanceof SequentialIdentifierGenerator) {
			return null;
		} else if (real instanceof RemoteIdentifierSource) {
			if (StringUtils.isNotBlank(((RemoteIdentifierSource) real).getUser())) {
				return null;
			}
			problem = "a remote source without a user (Initializer requires one)";
		} else {
			problem = "of type " + real.getClass().getName() + ", which Initializer cannot import";
		}
		return real == self ? "is " + problem : "is backed by " + real.getUuid() + ", " + problem;
	}
	
	@Override
	public Collection<IdentifierSource> getAllInstances() {
		List<IdentifierSource> sources = new ArrayList<>();
		for (IdentifierSource source : allSources()) {
			if (exports(source)) {
				sources.add(source);
			}
		}
		return sources;
	}
	
	@Override
	public Map<String, String> exclusions() {
		return exclusionsOf(allSources());
	}
	
	/** The sources among the given ones that Iniz cannot import, each with its name and the cause. */
	static Map<String, String> exclusionsOf(Collection<IdentifierSource> sources) {
		return DomainExporter.exclusions(sources, source -> !exports(source),
		    source -> "('" + source.getName() + "') " + importProblem(source));
	}
	
	private static List<IdentifierSource> allSources() {
		return Context.getService(IdentifierSourceService.class).getAllIdentifierSources(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(IdentifierSource instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		dependencies.add(instance.getIdentifierType());
		IdentifierSource real = HibernateUtil.getRealObjectFromProxy(instance);
		if (real instanceof IdentifierPool && ((IdentifierPool) real).getSource() != null) {
			dependencies.add(((IdentifierPool) real).getSource());
		}
		return dependencies;
	}
}
