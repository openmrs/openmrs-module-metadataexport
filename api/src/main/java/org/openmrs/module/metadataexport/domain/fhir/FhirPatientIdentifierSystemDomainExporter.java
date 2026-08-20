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
import org.openmrs.module.fhir2.model.FhirPatientIdentifierSystem;
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
public class FhirPatientIdentifierSystemDomainExporter extends CsvDomainExporter<FhirPatientIdentifierSystem> {
	
	@Override
	protected List<BaseLineExporter<FhirPatientIdentifierSystem>> chain() {
		return Collections.singletonList(new FhirPatientIdentifierSystemLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "fhirPatientIdentifierSystems.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.FHIR_PATIENT_IDENTIFIER_SYSTEMS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof FhirPatientIdentifierSystem && exports((FhirPatientIdentifierSystem) instance);
	}
	
	/** Iniz resolves rows by their patient identifier type, so a row without one can never import. */
	static boolean exports(FhirPatientIdentifierSystem system) {
		return system.getPatientIdentifierType() != null;
	}
	
	@Override
	public Collection<FhirPatientIdentifierSystem> getAllInstances() {
		return exportable(allRows());
	}
	
	/** The subset of rows that can round-trip through Iniz. */
	static List<FhirPatientIdentifierSystem> exportable(Collection<FhirPatientIdentifierSystem> rows) {
		Map<String, FhirPatientIdentifierSystem> byIdentifierType = new LinkedHashMap<>();
		for (FhirPatientIdentifierSystem row : rows) {
			if (!exports(row)) {
				log.warn(
				    "Fhir: skipping FHIR patient identifier system {} — it has no patient identifier type, and Iniz requires that column",
				    row.getUuid());
				continue;
			}
			FhirPatientIdentifierSystem kept = byIdentifierType.get(row.getPatientIdentifierType().getUuid());
			if (kept == null) {
				byIdentifierType.put(row.getPatientIdentifierType().getUuid(), row);
			} else if (BooleanUtils.isTrue(kept.getRetired()) && !BooleanUtils.isTrue(row.getRetired())) {
				byIdentifierType.put(row.getPatientIdentifierType().getUuid(), row);
				log.warn("Fhir: skipping FHIR patient identifier system {} — unretired row {} shares its identifier type,"
				        + " and Iniz keeps one row per identifier type",
				    kept.getUuid(), row.getUuid());
			} else {
				log.warn("Fhir: skipping FHIR patient identifier system {} — row {} shares its identifier type,"
				        + " and Iniz keeps one row per identifier type",
				    row.getUuid(), kept.getUuid());
			}
		}
		return new ArrayList<>(byIdentifierType.values());
	}
	
	@SuppressWarnings("unchecked")
	private static List<FhirPatientIdentifierSystem> allRows() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		return sessionFactory.getCurrentSession().createQuery("from FhirPatientIdentifierSystem").list();
	}
	
	@Override
	public Collection<FhirPatientIdentifierSystem> getInstancesByUuids(Collection<String> uuids) {
		Set<String> wanted = new HashSet<>(uuids);
		List<FhirPatientIdentifierSystem> found = new ArrayList<>();
		for (FhirPatientIdentifierSystem row : getAllInstances()) {
			if (wanted.remove(row.getUuid())) {
				found.add(row);
			}
		}
		if (!wanted.isEmpty()) {
			List<String> skipped = allRows().stream().map(FhirPatientIdentifierSystem::getUuid).filter(wanted::contains)
			        .collect(Collectors.toList());
			if (!skipped.isEmpty()) {
				throw new APIException("FHIR patient identifier systems exist but Initializer cannot import them"
				        + " (no patient identifier type, or an exported row shares their identifier type): " + skipped);
			}
			throw new APIException("Unknown uuids in domain " + getDomain() + ": " + wanted);
		}
		return found;
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(FhirPatientIdentifierSystem instance) {
		if (instance.getPatientIdentifierType() == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(instance.getPatientIdentifierType());
	}
}
