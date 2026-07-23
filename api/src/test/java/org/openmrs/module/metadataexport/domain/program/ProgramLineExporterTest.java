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
import org.openmrs.Program;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProgramLineExporterTest {
	
	private static Concept concept(String uuid) {
		Concept concept = new Concept();
		concept.setUuid(uuid);
		return concept;
	}
	
	@Test
	void exportsAllFieldsForActiveProgram() {
		Program program = new Program();
		program.setUuid("eae98b4c-e195-403b-b34a-82d94103b2c0");
		program.setConcept(concept("tb-program-concept-uuid"));
		program.setOutcomesConcept(concept("tb-program-outcomes-concept-uuid"));
		
		ExportLine line = new ExportLine();
		new ProgramLineExporter().writeLine(program, line);
		
		assertEquals("eae98b4c-e195-403b-b34a-82d94103b2c0", line.get("uuid"));
		assertEquals("tb-program-concept-uuid", line.get("program concept"));
		assertEquals("tb-program-outcomes-concept-uuid", line.get("outcomes concept"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void retiredProgramEmitsUuidAndFlagOnly() {
		Program program = new Program();
		program.setUuid("28f3da50-3f56-4e4e-93cd-66f334970480");
		program.setConcept(concept("ayurvedic-program-concept-uuid"));
		program.setOutcomesConcept(concept("ayurvedic-program-outcomes-concept-uuid"));
		program.setRetired(true);
		
		ExportLine line = new ExportLine();
		new ProgramLineExporter().writeLine(program, line);
		
		assertEquals("28f3da50-3f56-4e4e-93cd-66f334970480", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("program concept"), "retired rows carry only uuid + flag");
		assertNull(line.get("outcomes concept"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void omitsOutcomesConceptColumnWhenNull() {
		Program program = new Program();
		program.setUuid("d7477c21-bfc3-4922-9591-e89d8b9c8efa");
		program.setConcept(concept("aids-program-concept-uuid"));
		
		ExportLine line = new ExportLine();
		new ProgramLineExporter().writeLine(program, line);
		
		assertEquals("aids-program-concept-uuid", line.get("program concept"));
		assertNull(line.get("outcomes concept"));
	}
}
