/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.program;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgramWorkflowStateDomainExporterTest {
	
	private final ProgramWorkflowStateDomainExporter exporter = new ProgramWorkflowStateDomainExporter();
	
	@Test
	void getDependencies_includesWorkflowAndConcept() {
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setUuid("tb-treatment-status-workflow-uuid");
		Concept concept = new Concept();
		concept.setUuid("deceased-concept-uuid");
		
		ProgramWorkflowState state = new ProgramWorkflowState();
		state.setProgramWorkflow(workflow);
		state.setConcept(concept);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(state);
		
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(workflow), "referenced workflow must be pulled into the closure");
		assertTrue(dependencies.contains(concept), "referenced state concept must be pulled into the closure");
	}
	
	@Test
	void getDependencies_workflowOnlyWhenNoConcept() {
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setUuid("standard-treatment-status-workflow-uuid");
		
		ProgramWorkflowState state = new ProgramWorkflowState();
		state.setProgramWorkflow(workflow);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(state);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(workflow));
	}
	
	@Test
	void getDependencies_emptyWhenNoWorkflowOrConcept() {
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(new ProgramWorkflowState());
		
		assertTrue(dependencies.isEmpty());
	}
}
