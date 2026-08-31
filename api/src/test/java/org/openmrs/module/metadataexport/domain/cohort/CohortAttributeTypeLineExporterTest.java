/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.cohort;

import org.junit.jupiter.api.Test;
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CohortAttributeTypeLineExporterTest {
	
	@Test
	void exportsAllColumnsInitializerReads() {
		CohortAttributeType attributeType = new CohortAttributeType();
		attributeType.setUuid("aaa1a367-3047-4833-af27-b30e2dac9028");
		attributeType.setName("Sponsor");
		attributeType.setDescription("Sponsoring organization");
		attributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		attributeType.setPreferredHandlerClassname("org.openmrs.web.attribute.handler.LongFreeTextTextareaHandler");
		attributeType.setHandlerConfig("some config");
		attributeType.setMinOccurs(0);
		attributeType.setMaxOccurs(1);
		
		ExportLine line = new ExportLine();
		new CohortAttributeTypeLineExporter().writeLine(attributeType, line);
		
		assertEquals("aaa1a367-3047-4833-af27-b30e2dac9028", line.get("uuid"));
		assertEquals("Sponsor", line.get("name"));
		assertEquals("Sponsoring organization", line.get("description"));
		assertEquals("org.openmrs.customdatatype.datatype.FreeTextDatatype", line.get("Datatype classname"));
		assertEquals("org.openmrs.web.attribute.handler.LongFreeTextTextareaHandler",
		    line.get("Preferred handler classname"));
		assertEquals("some config", line.get("Handler config"));
		assertEquals("0", line.get("Min occurs"));
		assertEquals("1", line.get("Max occurs"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void omitsOptionalColumnsWhenUnset() {
		CohortAttributeType attributeType = new CohortAttributeType();
		attributeType.setUuid("aaa1a367-3047-4833-af27-b30e2dac9028");
		attributeType.setName("Sponsor");
		attributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		
		ExportLine line = new ExportLine();
		new CohortAttributeTypeLineExporter().writeLine(attributeType, line);
		
		assertEquals("Sponsor", line.get("name"));
		assertNull(line.get("description"), "empty description is not written as a column");
		assertNull(line.get("Preferred handler classname"), "empty handler is not written as a column");
		assertNull(line.get("Handler config"), "empty handler config is not written as a column");
		assertNull(line.get("Max occurs"), "unset max occurs is not written as a column");
	}
	
	@Test
	void retiredTypeEmitsUuidAndFlagOnly() {
		CohortAttributeType attributeType = new CohortAttributeType();
		attributeType.setUuid("439559c2-a3a4-4a25-b4b2-1a0299e287ee");
		attributeType.setName("Old Attribute");
		attributeType.setRetired(true);
		
		ExportLine line = new ExportLine();
		new CohortAttributeTypeLineExporter().writeLine(attributeType, line);
		
		assertEquals("439559c2-a3a4-4a25-b4b2-1a0299e287ee", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
		assertNull(line.get("Datatype classname"), "retired rows carry only uuid + flag");
	}
}
