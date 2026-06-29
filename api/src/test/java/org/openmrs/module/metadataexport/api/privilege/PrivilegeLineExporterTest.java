/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.privilege;

import org.junit.jupiter.api.Test;
import org.openmrs.Privilege;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PrivilegeLineExporterTest {
	
	@Test
	void exportsUuidNameAndDescription() {
		Privilege privilege = new Privilege("App: stockmanagement.dashboard", "Able to view stock management dashboard");
		privilege.setUuid("cc9b0e0c-ecaf-479d-9d7c-3071c0c1bff2");
		
		ExportLine line = new ExportLine();
		new PrivilegeLineExporter().export(privilege, line);
		
		assertEquals("cc9b0e0c-ecaf-479d-9d7c-3071c0c1bff2", line.get("uuid"));
		assertEquals("App: stockmanagement.dashboard", line.get("Privilege name"));
		assertEquals("Able to view stock management dashboard", line.get("description"));
	}
	
	@Test
	void retiredPrivilegeEmitsUuidAndFlagOnly() {
		Privilege privilege = new Privilege("Old Privilege");
		privilege.setUuid("old-uuid");
		privilege.setRetired(true);
		
		ExportLine line = new ExportLine();
		new PrivilegeLineExporter().export(privilege, line);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("Privilege name"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void privilegeWithNoDescriptionOmitsDescriptionColumn() {
		Privilege privilege = new Privilege("Task: stockmanagement.stockItems.mutate");
		privilege.setUuid("4a61b6db-03dd-43f2-969c-17dfa66ad41e");
		
		ExportLine line = new ExportLine();
		new PrivilegeLineExporter().export(privilege, line);
		
		assertEquals("Task: stockmanagement.stockItems.mutate", line.get("Privilege name"));
		assertNull(line.get("description"));
	}
}
