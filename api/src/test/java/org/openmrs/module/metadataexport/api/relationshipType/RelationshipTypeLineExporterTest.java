/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.relationshipType;

import org.junit.jupiter.api.Test;
import org.openmrs.RelationshipType;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RelationshipTypeLineExporterTest {
	
	@Test
	void exportAllTheFields() {
		RelationshipType relationshipType = new RelationshipType();
		relationshipType.setUuid("c86d9979-b8ac-4d8c-85cf-cc04e7f16315");
		relationshipType.setName("Uncle -> Nephew");
		relationshipType.setDescription("A relationship of an uncle and his nephew");
		relationshipType.setaIsToB("Uncle");
		relationshipType.setbIsToA("Nephew");
		relationshipType.setPreferred(true);
		relationshipType.setWeight(1);
		
		ExportLine exportLine = new ExportLine();
		new RelationshipTypeLineExporter().writeLine(relationshipType, exportLine);
		
		assertEquals(relationshipType.getUuid(), exportLine.get("uuid"));
		assertEquals(relationshipType.getDescription(), exportLine.get("description"));
		assertEquals(relationshipType.getaIsToB(), exportLine.get("a is to b"));
		assertEquals(relationshipType.getbIsToA(), exportLine.get("b is to a"));
		assertEquals(String.valueOf(relationshipType.getPreferred()), exportLine.get("preferred"));
		assertEquals(String.valueOf(relationshipType.getWeight()), exportLine.get("weight"));
	}
	
	@Test
	void retiredTypeEmitsUuidAndFlagOnly() {
		RelationshipType relationshipType = new RelationshipType();
		relationshipType.setUuid("c86d9979-b8ac-4d8c-85cf-cc04e7f16315");
		relationshipType.setName("Nephew");
		relationshipType.setDescription("A relationship of an uncle and his nephew");
		relationshipType.setRetired(true);
		
		ExportLine exportLine = new ExportLine();
		new RelationshipTypeLineExporter().writeLine(relationshipType, exportLine);
		
		assertEquals(relationshipType.getUuid(), exportLine.get("uuid"));
		assertEquals("true", exportLine.get("void/retire"));
		assertNull(exportLine.get("description"));
	}
}
