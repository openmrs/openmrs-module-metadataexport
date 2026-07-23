/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.attributetype;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptAttributeType;
import org.openmrs.LocationAttributeType;
import org.openmrs.ProgramAttributeType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.VisitAttributeType;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AttributeTypeLineExporterTest {
	
	@Test
	void exportsAllFieldsForLocationAttributeType() {
		LocationAttributeType at = new LocationAttributeType();
		at.setUuid("0bb29984-3193-11e7-93ae-92367f002671");
		at.setName("Location Height");
		at.setDescription("Location Height's description");
		at.setMinOccurs(1);
		at.setMaxOccurs(1);
		at.setDatatypeClassname("org.openmrs.customdatatype.datatype.FloatDatatype");
		
		ExportLine line = new ExportLine();
		new AttributeTypeLineExporter().writeLine(at, line);
		
		assertEquals("0bb29984-3193-11e7-93ae-92367f002671", line.get("uuid"));
		assertEquals("Location", line.get("Entity name"));
		assertEquals("Location Height", line.get("name"));
		assertEquals("Location Height's description", line.get("description"));
		assertEquals("1", line.get("Min occurs"));
		assertEquals("1", line.get("Max occurs"));
		assertEquals("org.openmrs.customdatatype.datatype.FloatDatatype", line.get("Datatype classname"));
	}
	
	@Test
	void omitsMaxOccursWhenNull() {
		ProgramAttributeType at = new ProgramAttributeType();
		at.setUuid("3884c889-35f5-47b4-a6b7-5b1165cee218");
		at.setName("Program Assessment");
		at.setDescription("Program Assessment's description");
		at.setMinOccurs(1);
		at.setMaxOccurs(null);
		at.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		
		ExportLine line = new ExportLine();
		new AttributeTypeLineExporter().writeLine(at, line);
		
		assertEquals("Program", line.get("Entity name"));
		assertEquals("1", line.get("Min occurs"));
		assertNull(line.get("Max occurs"));
	}
	
	@Test
	void retiredAttributeTypeEmitsUuidFlagEntityNameAndName() {
		ProviderAttributeType at = new ProviderAttributeType();
		at.setUuid("some-uuid");
		at.setName("Provider Rating");
		at.setRetired(true);
		
		ExportLine line = new ExportLine();
		new AttributeTypeLineExporter().writeLine(at, line);
		
		assertEquals("true", line.get("void/retire"));
		assertEquals("Provider", line.get("Entity name"), "Initializer needs entity name to route the retire");
		assertEquals("Provider Rating", line.get("name"), "Initializer needs name to locate the record");
		assertNull(line.get("description"), "other domain columns must be absent on retired rows");
	}
	
	@Test
	void entityNameMappingCoversAllTypes() {
		assertEquals("Location", entityNameFor(new LocationAttributeType()));
		assertEquals("Visit", entityNameFor(new VisitAttributeType()));
		assertEquals("Provider", entityNameFor(new ProviderAttributeType()));
		assertEquals("Concept", entityNameFor(new ConceptAttributeType()));
		assertEquals("Program", entityNameFor(new ProgramAttributeType()));
	}
	
	private String entityNameFor(org.openmrs.attribute.BaseAttributeType<?> at) {
		at.setUuid("test");
		at.setName("test");
		at.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		ExportLine line = new ExportLine();
		new AttributeTypeLineExporter().writeLine(at, line);
		return line.get("Entity name");
	}
}
