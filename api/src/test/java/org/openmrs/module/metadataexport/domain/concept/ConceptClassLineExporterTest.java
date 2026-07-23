/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.concept;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptClass;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConceptClassLineExporterTest {
	
	@Test
	void exportsUuidNameAndDescription() {
		ConceptClass conceptClass = new ConceptClass();
		conceptClass.setUuid("cc");
		conceptClass.setName("Diagnosis");
		conceptClass.setDescription("Concept classified as a diagnosis");
		
		ExportLine line = new ExportLine();
		new ConceptClassLineExporter().writeLine(conceptClass, line);
		
		assertEquals("cc", line.get("uuid"));
		assertEquals("Diagnosis", line.get("name"));
		assertEquals("Concept classified as a diagnosis", line.get("description"));
	}
	
	@Test
	void retiredClassEmitsUuidAndFlagOnly() {
		ConceptClass conceptClass = new ConceptClass();
		conceptClass.setUuid("old");
		conceptClass.setName("Old");
		conceptClass.setRetired(true);
		
		ExportLine line = new ExportLine();
		new ConceptClassLineExporter().writeLine(conceptClass, line);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
	}
}
