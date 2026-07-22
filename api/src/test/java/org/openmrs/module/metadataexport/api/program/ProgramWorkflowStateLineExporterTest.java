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
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProgramWorkflowStateLineExporterTest {
	
	private static Concept concept(String uuid) {
		Concept concept = new Concept();
		concept.setUuid(uuid);
		return concept;
	}
	
	private static ProgramWorkflow workflow(String uuid) {
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setUuid(uuid);
		return workflow;
	}
	
	@Test
	void exportsAllFieldsForActiveState() {
		ProgramWorkflowState state = new ProgramWorkflowState();
		state.setUuid("88b717c0-f580-497a-8d2b-026b60dd6bfd");
		state.setProgramWorkflow(workflow("tb-treatment-status-workflow-uuid"));
		state.setConcept(concept("deceased-concept-uuid"));
		state.setInitial(true);
		state.setTerminal(true);
		
		ExportLine line = new ExportLine();
		new ProgramWorkflowStateLineExporter().writeLine(state, line);
		
		assertEquals("88b717c0-f580-497a-8d2b-026b60dd6bfd", line.get("uuid"));
		assertEquals("tb-treatment-status-workflow-uuid", line.get("Workflow"));
		assertEquals("deceased-concept-uuid", line.get("State concept"));
		assertEquals("true", line.get("Initial"));
		assertEquals("true", line.get("Terminal"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void omitsInitialAndTerminalColumnsWhenFalse() {
		ProgramWorkflowState state = new ProgramWorkflowState();
		state.setUuid("cfa244b0-2700-102b-80cb-0017a47871b2");
		state.setProgramWorkflow(workflow("extended-discharge-workflow-uuid"));
		state.setConcept(concept("defaulted-concept-uuid"));
		state.setInitial(false);
		state.setTerminal(false);
		
		ExportLine line = new ExportLine();
		new ProgramWorkflowStateLineExporter().writeLine(state, line);
		
		assertNull(line.get("Initial"));
		assertNull(line.get("Terminal"));
	}
	
	@Test
	void retiredStateEmitsUuidAndFlagOnly() {
		ProgramWorkflowState state = new ProgramWorkflowState();
		state.setUuid("cfa24690-2700-102b-80cb-0017a47871b2");
		state.setProgramWorkflow(workflow("extended-discharge-workflow-uuid"));
		state.setConcept(concept("moribund-concept-uuid"));
		state.setInitial(true);
		state.setRetired(true);
		
		ExportLine line = new ExportLine();
		new ProgramWorkflowStateLineExporter().writeLine(state, line);
		
		assertEquals("cfa24690-2700-102b-80cb-0017a47871b2", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("Workflow"), "retired rows carry only uuid + flag");
		assertNull(line.get("State concept"), "retired rows carry only uuid + flag");
		assertNull(line.get("Initial"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void omitsColumnsWhenWorkflowOrConceptIsNull() {
		ProgramWorkflowState state = new ProgramWorkflowState();
		state.setUuid("no-workflow-or-concept-uuid");
		
		ExportLine line = new ExportLine();
		new ProgramWorkflowStateLineExporter().writeLine(state, line);
		
		assertNull(line.get("Workflow"));
		assertNull(line.get("State concept"));
	}
}
