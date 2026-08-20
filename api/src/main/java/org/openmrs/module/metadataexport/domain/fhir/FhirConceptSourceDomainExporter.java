/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.fhir;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.SessionFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@OpenmrsProfile(modules = { "fhir2:1.6.* - 9.*" })
public class FhirConceptSourceDomainExporter extends CsvDomainExporter<FhirConceptSource> {
	
	@Override
	protected List<BaseLineExporter<FhirConceptSource>> chain() {
		return Collections.singletonList(new FhirConceptSourceLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "fhirConceptSources.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.FHIR_CONCEPT_SOURCES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof FhirConceptSource && exports((FhirConceptSource) instance);
	}
	
	/** Iniz resolves rows by their concept source, so a row without one can never import. */
	static boolean exports(FhirConceptSource source) {
		return source.getConceptSource() != null;
	}
	
	@Override
	public Collection<FhirConceptSource> getAllInstances() {
		return exportable(allRows());
	}
	
	/** The subset of rows that can round-trip through Iniz. */
	static List<FhirConceptSource> exportable(Collection<FhirConceptSource> rows) {
		Map<String, FhirConceptSource> byConceptSource = new LinkedHashMap<>();
		for (FhirConceptSource row : rows) {
			if (!exports(row)) {
				log.warn("Fhir: skipping FHIR concept source {} — it has no concept source, and Iniz requires that column",
				    row.getUuid());
				continue;
			}
			FhirConceptSource kept = byConceptSource.get(row.getConceptSource().getUuid());
			if (kept == null) {
				byConceptSource.put(row.getConceptSource().getUuid(), row);
			} else if (BooleanUtils.isTrue(kept.getRetired()) && !BooleanUtils.isTrue(row.getRetired())) {
				byConceptSource.put(row.getConceptSource().getUuid(), row);
				log.warn("Fhir: skipping FHIR concept source {} — unretired row {} shares its concept source,"
				        + " and Iniz keeps one row per concept source",
				    kept.getUuid(), row.getUuid());
			} else {
				log.warn("Fhir: skipping FHIR concept source {} — row {} shares its concept source,"
				        + " and Iniz keeps one row per concept source",
				    row.getUuid(), kept.getUuid());
			}
		}
		return new ArrayList<>(byConceptSource.values());
	}
	
	@SuppressWarnings("unchecked")
	private static List<FhirConceptSource> allRows() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		return sessionFactory.getCurrentSession().createQuery("from FhirConceptSource").list();
	}
	
	@Override
	public Collection<FhirConceptSource> getInstancesByUuids(Collection<String> uuids) {
		Set<String> wanted = new HashSet<>(uuids);
		List<FhirConceptSource> found = new ArrayList<>();
		for (FhirConceptSource row : getAllInstances()) {
			if (wanted.remove(row.getUuid())) {
				found.add(row);
			}
		}
		if (!wanted.isEmpty()) {
			List<String> skipped = allRows().stream().map(FhirConceptSource::getUuid).filter(wanted::contains)
			        .collect(Collectors.toList());
			if (!skipped.isEmpty()) {
				throw new APIException("FHIR concept sources exist but Initializer cannot import them"
				        + " (no concept source, or an exported row shares their concept source): " + skipped);
			}
			throw new APIException("Unknown uuids in domain " + getDomain() + ": " + wanted);
		}
		return found;
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(FhirConceptSource instance) {
		if (instance.getConceptSource() == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(instance.getConceptSource());
	}
}
