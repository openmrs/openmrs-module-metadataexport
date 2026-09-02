/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.cohort;

import org.hibernate.SessionFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@OpenmrsProfile(modules = "cohort:3.5.0")
public class CohortTypeDomainExporter extends CsvDomainExporter<CohortType> {
	
	@Override
	protected List<BaseLineExporter<CohortType>> chain() {
		return Collections.singletonList(new CohortTypeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "cohortTypes.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.COHORT_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof CohortType;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<CohortType> getAllInstances() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		// Dropped because CohortType.getId() unboxes a primitive int, so Iniz's shouldFill is never
		// true on a void/retire line: it bootstraps the row, skips the fill and saves it with no
		// name or description. Iniz itself does not drop voided rows.
		return sessionFactory.getCurrentSession().createQuery("from CohortType where voided = false").list();
	}
	
	@Override
	public Collection<CohortType> getInstancesByUuids(Collection<String> uuids) {
		Set<String> wanted = new HashSet<>(uuids);
		List<CohortType> found = new ArrayList<>();
		for (CohortType type : getAllInstances()) {
			if (wanted.remove(type.getUuid())) {
				found.add(type);
			}
		}
		if (!wanted.isEmpty()) {
			List<String> voided = allRows().stream().map(CohortType::getUuid).filter(wanted::contains)
			        .collect(Collectors.toList());
			if (!voided.isEmpty()) {
				throw new APIException(
				        "Cohort types exist but are voided, and Iniz cannot import a voided cohort type: " + voided);
			}
			throw new APIException("Unknown uuids in domain " + getDomain() + ": " + wanted);
		}
		return found;
	}
	
	@SuppressWarnings("unchecked")
	private static List<CohortType> allRows() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		return sessionFactory.getCurrentSession().createQuery("from CohortType").list();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(CohortType instance) {
		return Collections.emptyList();
	}
}
