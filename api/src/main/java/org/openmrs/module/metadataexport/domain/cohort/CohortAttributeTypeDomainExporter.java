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
import org.openmrs.module.cohort.CohortAttributeType;
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
public class CohortAttributeTypeDomainExporter extends CsvDomainExporter<CohortAttributeType> {
	
	@Override
	protected List<BaseLineExporter<CohortAttributeType>> chain() {
		return Collections.singletonList(new CohortAttributeTypeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "cohortAttributeTypes.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.COHORT_ATTRIBUTE_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof CohortAttributeType;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<CohortAttributeType> getAllInstances() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		return sessionFactory.getCurrentSession().createQuery("from CohortAttributeType where retired = false").list();
	}
	
	@Override
	public Collection<CohortAttributeType> getInstancesByUuids(Collection<String> uuids) {
		Set<String> wanted = new HashSet<>(uuids);
		List<CohortAttributeType> found = new ArrayList<>();
		for (CohortAttributeType type : getAllInstances()) {
			if (wanted.remove(type.getUuid())) {
				found.add(type);
			}
		}
		if (!wanted.isEmpty()) {
			List<String> retired = allRows().stream().map(CohortAttributeType::getUuid).filter(wanted::contains)
			        .collect(Collectors.toList());
			if (!retired.isEmpty()) {
				throw new APIException("Cohort attribute types exist but are retired,"
				        + " and Iniz cannot resolve a retired cohort attribute type: " + retired);
			}
			throw new APIException("Unknown uuids in domain " + getDomain() + ": " + wanted);
		}
		return found;
	}
	
	@SuppressWarnings("unchecked")
	private static List<CohortAttributeType> allRows() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		return sessionFactory.getCurrentSession().createQuery("from CohortAttributeType").list();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(CohortAttributeType instance) {
		return Collections.emptyList();
	}
}
