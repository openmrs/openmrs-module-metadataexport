/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.flag;

import org.junit.jupiter.api.Test;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.patientflags.Priority;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FlagPriorityLineExporterTest {
	
	@Test
	void exportsAllFieldsForActivePriority() {
		Priority priority = new Priority();
		priority.setUuid("a168c141-f5fd-4eec-bd3e-633bed1c9601");
		priority.setName("High Priority");
		priority.setDescription("Requires immediate attention");
		priority.setStyle("danger");
		priority.setRank(1);
		
		ExportLine line = new ExportLine();
		new FlagPriorityLineExporter().writeLine(priority, line);
		
		assertEquals("a168c141-f5fd-4eec-bd3e-633bed1c9601", line.get("uuid"));
		assertEquals("High Priority", line.get("name"));
		assertEquals("Requires immediate attention", line.get("description"));
		assertEquals("danger", line.get("style"));
		assertEquals("1", line.get("rank"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void retiredPriorityEmitsUuidAndFlagOnly() {
		Priority priority = new Priority();
		priority.setUuid("b279d252-g6ge-5ffd-ce4f-744cef2d0718");
		priority.setName("Retired Priority");
		priority.setDescription("This priority should be retired");
		priority.setStyle("default");
		priority.setRank(3);
		priority.setRetired(true);
		
		ExportLine line = new ExportLine();
		new FlagPriorityLineExporter().writeLine(priority, line);
		
		assertEquals("b279d252-g6ge-5ffd-ce4f-744cef2d0718", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
		assertNull(line.get("style"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void priorityWithNullRankOmitsRankColumn() {
		Priority priority = new Priority();
		priority.setUuid("c379d252-g6ge-5ffd-ce4f-744cef2d0719");
		priority.setName("Unranked Priority");
		priority.setDescription("No rank set");
		priority.setStyle("info");
		
		ExportLine line = new ExportLine();
		new FlagPriorityLineExporter().writeLine(priority, line);
		
		assertEquals("Unranked Priority", line.get("name"));
		assertNull(line.get("rank"));
	}
}
