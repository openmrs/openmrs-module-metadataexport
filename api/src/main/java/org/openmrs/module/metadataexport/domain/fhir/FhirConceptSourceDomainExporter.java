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

import org.apache.commons.lang3.BooleanUtils;
import org.hibernate.SessionFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
		return exportable(rows, new LinkedHashMap<>());
	}
	
	/**
	 * Splits the rows into those exported and, in {@code exclusions}, those left out with the reason:
	 * Iniz resolves a row by its concept source and keeps one row per concept source, preferring an
	 * unretired one.
	 */
	static List<FhirConceptSource> exportable(Collection<FhirConceptSource> rows, Map<String, String> exclusions) {
		Map<String, FhirConceptSource> byKey = new LinkedHashMap<>();
		for (FhirConceptSource row : rows) {
			if (!exports(row)) {
				exclusions.put(row.getUuid(), row.getUuid() + " has no concept source, which Initializer requires");
				continue;
			}
			String key = row.getConceptSource().getUuid();
			FhirConceptSource kept = byKey.get(key);
			if (kept == null) {
				byKey.put(key, row);
			} else if (BooleanUtils.isTrue(kept.getRetired()) && !BooleanUtils.isTrue(row.getRetired())) {
				byKey.put(key, row);
				exclusions.put(kept.getUuid(), kept.getUuid() + " shares its concept source with unretired row "
				        + row.getUuid() + ", and Initializer keeps one row per concept source");
			} else {
				exclusions.put(row.getUuid(), row.getUuid() + " shares its concept source with exported row "
				        + kept.getUuid() + ", and Initializer keeps one row per concept source");
			}
		}
		return new ArrayList<>(byKey.values());
	}
	
	@Override
	public Map<String, String> exclusions() {
		Map<String, String> exclusions = new LinkedHashMap<>();
		exportable(allRows(), exclusions);
		return exclusions;
	}
	
	@SuppressWarnings("unchecked")
	private static List<FhirConceptSource> allRows() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		return sessionFactory.getCurrentSession().createQuery("from FhirConceptSource").list();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(FhirConceptSource instance) {
		if (instance.getConceptSource() == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(instance.getConceptSource());
	}
}
