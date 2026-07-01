/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.visitType;

import org.junit.jupiter.api.Test;
import org.openmrs.VisitType;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VisitTypeLineExporterTest {
	
	@Test
	void exportsUuidNameAndDescription() {
		VisitType visitType = new VisitType("HIV", "HIV Description");
		visitType.setUuid("2bcf7212-d218-4572-88e9-25c4b5b71934");
		
		ExportLine line = new ExportLine();
		new VisitTypeLineExporter().writeLine(visitType, line);
		
		assertEquals("2bcf7212-d218-4572-88e9-25c4b5b71934", line.get("uuid"));
		assertEquals("HIV", line.get("name"));
		assertEquals("HIV Description", line.get("description"));
	}
	
	@Test
	void exportsUuidAndNameWhenDescriptionIsAbsent() {
		VisitType visitType = new VisitType("General", null);
		visitType.setUuid("abcf7209-d218-4572-8893-25c4b5b71934");
		
		ExportLine line = new ExportLine();
		new VisitTypeLineExporter().writeLine(visitType, line);
		
		assertEquals("General", line.get("name"));
		assertNull(line.get("description"));
	}
	
	@Test
	void retiredVisitTypeEmitsUuidAndFlagOnly() {
		VisitType visitType = new VisitType("Old Type", null);
		visitType.setUuid("old-uuid");
		visitType.setRetired(true);
		
		ExportLine line = new ExportLine();
		new VisitTypeLineExporter().writeLine(visitType, line);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
	}
}
