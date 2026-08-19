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
import org.openmrs.api.APIException;
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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Identifier sources are written as one file per source type, mirroring Iniz's own fixture layout,
 * so each file carries an {@code _order:} header — pools reference their backing source by uuid, so
 * the pool file must load last. Sources Iniz cannot import — custom {@link IdentifierSource}
 * subclasses, remotes without a user, pools without an importable backing source — are skipped with
 * a warning.
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
				continue;
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
		IdentifierSource real = HibernateUtil.getRealObjectFromProxy(source);
		Set<IdentifierSource> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		while (real instanceof IdentifierPool) {
			if (!seen.add(real)) {
				return false;
			}
			real = HibernateUtil.getRealObjectFromProxy(((IdentifierPool) real).getSource());
		}
		if (real instanceof RemoteIdentifierSource) {
			return StringUtils.isNotBlank(((RemoteIdentifierSource) real).getUser());
		}
		return real instanceof SequentialIdentifierGenerator;
	}
	
	@Override
	public Collection<IdentifierSource> getAllInstances() {
		List<IdentifierSource> sources = new ArrayList<>();
		for (IdentifierSource source : Context.getService(IdentifierSourceService.class).getAllIdentifierSources(true)) {
			IdentifierSource real = HibernateUtil.getRealObjectFromProxy(source);
			if (handles(real)) {
				sources.add(source);
			} else {
				log.warn(
				    "Idgen: skipping identifier source {} of type {}; Iniz cannot import it (unsupported type,"
				            + " blank remote user, missing/unimportable backing source, or a pool cycle)",
				    real.getUuid(), real.getClass().getName());
			}
		}
		return sources;
	}
	
	@Override
	public Collection<IdentifierSource> getInstancesByUuids(Collection<String> uuids) {
		Set<String> wanted = new HashSet<>(uuids);
		List<IdentifierSource> found = new ArrayList<>();
		for (IdentifierSource source : getAllInstances()) {
			if (wanted.remove(source.getUuid())) {
				found.add(source);
			}
		}
		if (!wanted.isEmpty()) {
			// getAllInstances() filters out unimportable sources, so a leftover uuid may name a
			// source that exists — say so instead of misreporting it as unknown
			IdentifierSourceService service = Context.getService(IdentifierSourceService.class);
			List<String> skipped = wanted.stream().filter(uuid -> service.getIdentifierSourceByUuid(uuid) != null)
			        .collect(Collectors.toList());
			if (!skipped.isEmpty()) {
				throw new APIException("Identifier sources exist but cannot be imported by Initializer (unsupported"
				        + " type, blank remote user, missing/unimportable backing source, or a pool cycle): " + skipped);
			}
			throw new APIException("Unknown uuids in domain " + getDomain() + ": " + wanted);
		}
		return found;
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(IdentifierSource instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		if (instance.getIdentifierType() != null) {
			dependencies.add(instance.getIdentifierType());
		}
		IdentifierSource real = HibernateUtil.getRealObjectFromProxy(instance);
		if (real instanceof IdentifierPool && ((IdentifierPool) real).getSource() != null) {
			dependencies.add(((IdentifierPool) real).getSource());
		}
		return dependencies;
	}
}
