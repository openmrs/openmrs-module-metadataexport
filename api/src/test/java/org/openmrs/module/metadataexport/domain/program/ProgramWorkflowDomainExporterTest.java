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
import org.openmrs.ProgramWorkflow;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgramWorkflowDomainExporterTest {
	
	private final ProgramWorkflowDomainExporter exporter = new ProgramWorkflowDomainExporter();
	
	@Test
	void getDependencies_includesProgramAndConcept() {
		Program program = new Program();
		program.setUuid("tb-program-uuid");
		Concept concept = new Concept();
		concept.setUuid("tb-treatment-status-concept-uuid");
		
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setProgram(program);
		workflow.setConcept(concept);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(workflow);
		
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(program), "referenced program must be pulled into the closure");
		assertTrue(dependencies.contains(concept), "referenced workflow concept must be pulled into the closure");
	}
	
	@Test
	void getDependencies_programOnlyWhenNoConcept() {
		Program program = new Program();
		program.setUuid("aids-program-uuid");
		
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setProgram(program);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(workflow);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(program));
	}
	
	@Test
	void getDependencies_emptyWhenNoProgramOrConcept() {
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(new ProgramWorkflow());
		
		assertTrue(dependencies.isEmpty());
	}
}
