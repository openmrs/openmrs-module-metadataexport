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
import org.openmrs.ConceptMapType;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConceptMapTypeLineExporterTest {
	
	@Test
	void exportsUuidNameDescriptionAndHiddenFlag() {
		ConceptMapType type = new ConceptMapType();
		type.setUuid("cmt");
		type.setName("Direct device");
		type.setDescription("SNOMED direct device relationship");
		type.setIsHidden(true);
		
		ExportLine line = new ExportLine();
		new ConceptMapTypeLineExporter().writeLine(type, line);
		
		assertEquals("cmt", line.get("uuid"));
		assertEquals("Direct device", line.get("name"));
		assertEquals("SNOMED direct device relationship", line.get("description"));
		assertEquals("true", line.get("Is hidden"));
	}
	
	@Test
	void visibleTypeEmitsFalseHiddenFlag() {
		ConceptMapType type = new ConceptMapType();
		type.setUuid("cmt");
		type.setName("SAME-AS");
		
		ExportLine line = new ExportLine();
		new ConceptMapTypeLineExporter().writeLine(type, line);
		
		assertEquals("false", line.get("Is hidden"));
		assertNull(line.get("description"), "blank description is not emitted");
	}
	
	@Test
	void retiredTypeEmitsFullRowPlusFlag() {
		ConceptMapType type = new ConceptMapType();
		type.setUuid("old");
		type.setName("Old");
		type.setRetired(true);
		
		ExportLine line = new ExportLine();
		new ConceptMapTypeLineExporter().writeLine(type, line);
		
		assertEquals("true", line.get("void/retire"));
		assertEquals("Old", line.get("name"),
		    "retired rows carry the full row so a fresh target can create the object before retiring it");
	}
}
