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
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProgramWorkflowLineExporterTest {
	
	private static Concept concept(String uuid) {
		Concept concept = new Concept();
		concept.setUuid(uuid);
		return concept;
	}
	
	private static Program program(String uuid) {
		Program program = new Program();
		program.setUuid(uuid);
		return program;
	}
	
	@Test
	void exportsAllFieldsForActiveWorkflow() {
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setUuid("2b98bc76-245c-11e1-9cf0-00248140a5eb");
		workflow.setProgram(program("tb-program-uuid"));
		workflow.setConcept(concept("tb-treatment-status-concept-uuid"));
		
		ExportLine line = new ExportLine();
		new ProgramWorkflowLineExporter().writeLine(workflow, line);
		
		assertEquals("2b98bc76-245c-11e1-9cf0-00248140a5eb", line.get("uuid"));
		assertEquals("tb-program-uuid", line.get("program"));
		assertEquals("tb-treatment-status-concept-uuid", line.get("workflow concept"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void retiredWorkflowEmitsUuidAndFlagOnly() {
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setUuid("45a28ee9-20a3-4065-9955-9cb7a0c6a24b");
		workflow.setProgram(program("mental-health-program-uuid"));
		workflow.setRetired(true);
		
		ExportLine line = new ExportLine();
		new ProgramWorkflowLineExporter().writeLine(workflow, line);
		
		assertEquals("45a28ee9-20a3-4065-9955-9cb7a0c6a24b", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("program"), "retired rows carry only uuid + flag");
		assertNull(line.get("workflow concept"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void omitsColumnsWhenProgramOrConceptIsNull() {
		ProgramWorkflow workflow = new ProgramWorkflow();
		workflow.setUuid("1b42d0e8-20ad-4bd8-b05d-fbad80a3b665");
		
		ExportLine line = new ExportLine();
		new ProgramWorkflowLineExporter().writeLine(workflow, line);
		
		assertNull(line.get("program"));
		assertNull(line.get("workflow concept"));
	}
}
