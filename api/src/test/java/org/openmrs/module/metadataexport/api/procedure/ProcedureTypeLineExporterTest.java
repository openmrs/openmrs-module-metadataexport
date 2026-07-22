/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.procedure;

import org.junit.jupiter.api.Test;
import org.openmrs.module.emrapi.procedure.ProcedureType;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProcedureTypeLineExporterTest {
	
	@Test
	void exportsUuidNameAndDescription() {
		ProcedureType procedureType = new ProcedureType("Appendectomy", "Surgical removal of the appendix");
		procedureType.setUuid("aaa1a367-3047-4833-af27-b30e2dac9028");
		
		ExportLine line = new ExportLine();
		new ProcedureTypeLineExporter().writeLine(procedureType, line);
		
		assertEquals("aaa1a367-3047-4833-af27-b30e2dac9028", line.get("uuid"));
		assertEquals("Appendectomy", line.get("name"));
		assertEquals("Surgical removal of the appendix", line.get("description"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void liveTypeWithoutDescriptionOmitsDescriptionColumn() {
		ProcedureType procedureType = new ProcedureType();
		procedureType.setUuid("aaa1a367-3047-4833-af27-b30e2dac9028");
		procedureType.setName("Appendectomy");
		
		ExportLine line = new ExportLine();
		new ProcedureTypeLineExporter().writeLine(procedureType, line);
		
		assertEquals("Appendectomy", line.get("name"));
		assertNull(line.get("description"), "empty description is not written as a column");
	}
	
	@Test
	void retiredTypeEmitsUuidAndFlagOnly() {
		ProcedureType procedureType = new ProcedureType("Discontinued Procedure", "Procedure no longer offered");
		procedureType.setUuid("439559c2-a3a4-4a25-b4b2-1a0299e287ee");
		procedureType.setRetired(true);
		
		ExportLine line = new ExportLine();
		new ProcedureTypeLineExporter().writeLine(procedureType, line);
		
		assertEquals("439559c2-a3a4-4a25-b4b2-1a0299e287ee", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
		assertNull(line.get("description"), "retired rows carry only uuid + flag");
	}
}
