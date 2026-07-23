/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.concept;

import org.hibernate.SessionFactory;
import org.openmrs.ConceptSet;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class ConceptSetDomainExporter extends CsvDomainExporter<ConceptSet> {
	
	@Override
	protected List<BaseLineExporter<ConceptSet>> chain() {
		return Collections.singletonList(new ConceptSetLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "conceptSet.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.CONCEPT_SETS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof ConceptSet;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<ConceptSet> getAllInstances() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		return sessionFactory.getCurrentSession().createQuery("from ConceptSet").list();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(ConceptSet conceptSet) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		dependencies.add(conceptSet.getConceptSet());
		dependencies.add(conceptSet.getConcept());
		return dependencies;
	}
}
