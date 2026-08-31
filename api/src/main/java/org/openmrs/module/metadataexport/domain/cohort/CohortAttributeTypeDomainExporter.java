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
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
	public Collection<? extends OpenmrsObject> getDependencies(CohortAttributeType instance) {
		return Collections.emptyList();
	}
}
