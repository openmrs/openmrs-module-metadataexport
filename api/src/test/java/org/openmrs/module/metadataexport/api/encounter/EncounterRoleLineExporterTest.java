/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.encounter;

import org.junit.jupiter.api.Test;
import org.openmrs.EncounterRole;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EncounterRoleLineExporterTest {
	
	@Test
	void exportsUuidNameAndDescription() {
		EncounterRole role = new EncounterRole();
		role.setUuid("a0b8f83c-1f3b-4c2a-9d1e-2f4a6b8c0d2e");
		role.setName("Clinician");
		role.setDescription("The clinician for an encounter");
		
		ExportLine line = new ExportLine();
		new EncounterRoleLineExporter().writeLine(role, line);
		
		assertEquals("a0b8f83c-1f3b-4c2a-9d1e-2f4a6b8c0d2e", line.get("uuid"));
		assertEquals("Clinician", line.get("name"));
		assertEquals("The clinician for an encounter", line.get("description"));
	}
	
	@Test
	void liveRoleWithoutDescriptionOmitsDescriptionColumn() {
		EncounterRole role = new EncounterRole();
		role.setUuid("a0b8f83c-1f3b-4c2a-9d1e-2f4a6b8c0d2e");
		role.setName("Clinician");
		
		ExportLine line = new ExportLine();
		new EncounterRoleLineExporter().writeLine(role, line);
		
		assertEquals("Clinician", line.get("name"));
		assertNull(line.get("description"), "empty description is not written as a column");
	}
	
	@Test
	void retiredRoleEmitsUuidAndFlagOnly() {
		EncounterRole role = new EncounterRole();
		role.setUuid("old");
		role.setName("Old Role");
		role.setDescription("no longer used");
		role.setRetired(true);
		
		ExportLine line = new ExportLine();
		new EncounterRoleLineExporter().writeLine(role, line);
		
		assertEquals("old", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
		assertNull(line.get("description"), "retired rows carry only uuid + flag");
	}
}
