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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Identifier sources are written as one file per source type, mirroring Iniz's own fixture layout,
 * so each file carries an {@code _order:} header — pools reference their backing source by uuid, so
 * the pool file must load last. Custom {@link IdentifierSource} subclasses have no Iniz
 * representation and are skipped with a warning.
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
			if (!exportable(real)) {
				continue;
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
	
	/**
	 * A source Iniz can import. Pools need a backing source that is itself exported: Iniz reads
	 * {@code pool identifier source} as required and resolves it by uuid, so a pool without one (legal
	 * in idgen's schema) or backed by a skipped custom type fails on import.
	 */
	private boolean exportable(IdentifierSource real) {
		if (!handles(real)) {
			log.warn("Idgen: skipping identifier source {} of unsupported type {} — no Iniz representation", real.getUuid(),
			    real.getClass().getName());
			return false;
		}
		if (real instanceof IdentifierPool) {
			IdentifierSource backing = HibernateUtil.getRealObjectFromProxy(((IdentifierPool) real).getSource());
			if (backing == null) {
				log.warn("Idgen: skipping identifier pool {} with no backing source; Iniz requires one on import",
				    real.getUuid());
				return false;
			}
			if (!handles(backing)) {
				log.warn("Idgen: skipping identifier pool {} — its backing source {} has unsupported type {}",
				    real.getUuid(), backing.getUuid(), backing.getClass().getName());
				return false;
			}
		}
		return true;
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
		return instance instanceof SequentialIdentifierGenerator || instance instanceof RemoteIdentifierSource
		        || instance instanceof IdentifierPool;
	}
	
	@Override
	public Collection<IdentifierSource> getAllInstances() {
		List<IdentifierSource> sources = new ArrayList<>();
		for (IdentifierSource source : Context.getService(IdentifierSourceService.class).getAllIdentifierSources(true)) {
			if (exportable(HibernateUtil.getRealObjectFromProxy(source))) {
				sources.add(source);
			}
		}
		return sources;
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
