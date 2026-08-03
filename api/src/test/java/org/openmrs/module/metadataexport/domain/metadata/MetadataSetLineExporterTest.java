/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.metadata;

import org.junit.jupiter.api.Test;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadatamapping.MetadataSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MetadataSetLineExporterTest {
	
	@Test
	void exportsAllFieldsForActiveSet() {
		MetadataSet metadataSet = new MetadataSet();
		metadataSet.setUuid("f0ebcb99-7618-41b7-b0bf-8ff93de67b9e");
		metadataSet.setName("Extra Patient Identifiers Set");
		metadataSet.setDescription("A set of extra patient identifiers");
		
		ExportLine line = new ExportLine();
		new MetadataSetLineExporter().writeLine(metadataSet, line);
		
		assertEquals("f0ebcb99-7618-41b7-b0bf-8ff93de67b9e", line.get("uuid"));
		assertEquals("Extra Patient Identifiers Set", line.get("name"));
		assertEquals("A set of extra patient identifiers", line.get("description"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void retiredSetEmitsUuidAndFlagOnly() {
		MetadataSet metadataSet = new MetadataSet();
		metadataSet.setUuid("f0ebcb99-272d-41b7-4c67-078de9342492");
		metadataSet.setName("Old Set");
		metadataSet.setDescription("No longer used");
		metadataSet.setRetired(true);
		
		ExportLine line = new ExportLine();
		new MetadataSetLineExporter().writeLine(metadataSet, line);
		
		assertEquals("f0ebcb99-272d-41b7-4c67-078de9342492", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
		assertNull(line.get("description"), "retired rows carry only uuid + flag");
	}
}
