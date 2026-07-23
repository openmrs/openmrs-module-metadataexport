/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.program;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.Program;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgramDomainExporterTest {
	
	private final ProgramDomainExporter exporter = new ProgramDomainExporter();
	
	@Test
	void getDependencies_includesConceptAndOutcomesConcept() {
		Concept concept = new Concept();
		concept.setUuid("tb-program-concept-uuid");
		Concept outcomesConcept = new Concept();
		outcomesConcept.setUuid("tb-program-outcomes-concept-uuid");
		
		Program program = new Program();
		program.setConcept(concept);
		program.setOutcomesConcept(outcomesConcept);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(program);
		
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(concept), "referenced program concept must be pulled into the closure");
		assertTrue(dependencies.contains(outcomesConcept), "referenced outcomes concept must be pulled into the closure");
	}
	
	@Test
	void getDependencies_conceptOnlyWhenNoOutcomesConcept() {
		Concept concept = new Concept();
		concept.setUuid("aids-program-concept-uuid");
		
		Program program = new Program();
		program.setConcept(concept);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(program);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(concept));
	}
	
	@Test
	void getDependencies_emptyWhenNoConcepts() {
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(new Program());
		
		assertTrue(dependencies.isEmpty());
	}
}
