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

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptSet;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class ConceptDomainExporter extends CsvDomainExporter<Concept> {
	
	@Override
	public Domain getDomain() {
		return Domain.CONCEPTS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof Concept;
	}
	
	@Override
	public Collection<Concept> getAllInstances() {
		return Context.getConceptService().getAllConcepts();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(Concept concept) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		for (ConceptAnswer answer : concept.getAnswers()) {
			if (answer.getAnswerConcept() != null) {
				dependencies.add(answer.getAnswerConcept());
			}
		}
		for (ConceptSet member : concept.getConceptSets()) {
			if (member.getConcept() != null) {
				dependencies.add(member);
			}
		}
		return dependencies;
	}
	
	@Override
	protected List<BaseLineExporter<Concept>> chain() {
		return Arrays.asList(new ConceptLineExporter(), new ConceptNumericExporter(), new ConceptComplexExporter(),
		    new NestedConceptExporter(), new MappingsConceptExporter(), new ConceptAttributeExporter());
	}
	
	@Override
	protected String fileName() {
		return "concepts.csv";
	}
}
