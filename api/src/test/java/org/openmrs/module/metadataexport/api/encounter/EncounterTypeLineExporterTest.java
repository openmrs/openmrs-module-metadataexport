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
import org.openmrs.EncounterType;
import org.openmrs.Privilege;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EncounterTypeLineExporterTest {
	
	@Test
	void exportsUuidNameDescriptionAndPrivileges() {
		EncounterType type = new EncounterType();
		type.setUuid("et");
		type.setName("Vitals");
		type.setDescription("Vitals encounter");
		type.setViewPrivilege(new Privilege("View Vitals"));
		type.setEditPrivilege(new Privilege("Edit Vitals"));
		
		ExportLine line = new ExportLine();
		new EncounterTypeLineExporter().export(type, line);
		
		assertEquals("et", line.get("uuid"));
		assertEquals("Vitals", line.get("name"));
		assertEquals("Vitals encounter", line.get("description"));
		assertEquals("View Vitals", line.get("view privilege"));
		assertEquals("Edit Vitals", line.get("edit privilege"));
	}
	
	@Test
	void retiredTypeEmitsUuidAndFlagOnly() {
		EncounterType type = new EncounterType();
		type.setUuid("old");
		type.setName("Old");
		type.setRetired(true);
		
		ExportLine line = new ExportLine();
		new EncounterTypeLineExporter().export(type, line);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
	}
}
