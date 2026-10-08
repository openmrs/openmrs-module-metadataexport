/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.conceptreferencerange;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.domain.concept.ConceptDomainExporter;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class ConceptReferenceRangeDomainExporter extends CsvDomainExporter<ConceptReferenceRange> {
	
	@Override
	protected List<BaseLineExporter<ConceptReferenceRange>> chain() {
		return Collections.singletonList(new ConceptReferenceRangeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "conceptReferenceRanges.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.CONCEPT_REFERENCE_RANGE;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof ConceptReferenceRange;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<ConceptReferenceRange> getAllInstances() {
		// feels like this needs to be in core
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		return sessionFactory.getCurrentSession().createQuery("from ConceptReferenceRange").list();
	}
	
	/**
	 * A range has no name; it is the numeric concept's name and the criteria that selects the range.
	 */
	@Override
	public String display(ConceptReferenceRange instance) {
		String concept = ConceptDomainExporter.displayName(instance.getConceptNumeric());
		return StringUtils.isBlank(instance.getCriteria()) ? concept : concept + " [" + instance.getCriteria() + "]";
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(ConceptReferenceRange instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		dependencies.add(instance.getConceptNumeric());
		return dependencies;
	}
}
