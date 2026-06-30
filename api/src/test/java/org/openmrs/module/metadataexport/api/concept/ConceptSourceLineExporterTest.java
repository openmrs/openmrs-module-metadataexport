/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.concept;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptSource;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConceptSourceLineExporterTest {
	
	@Test
	void exportsUuidNameDescriptionHl7CodeAndUniqueId() {
		ConceptSource source = new ConceptSource();
		source.setUuid("cs");
		source.setName("SNOMED CT");
		source.setDescription("SNOMED Clinical Terms");
		source.setHl7Code("SCT");
		source.setUniqueId("2.16.840.1.113883.6.96");
		
		ExportLine line = new ExportLine();
		new ConceptSourceLineExporter().export(source, line);
		
		assertEquals("cs", line.get("uuid"));
		assertEquals("SNOMED CT", line.get("name"));
		assertEquals("SNOMED Clinical Terms", line.get("description"));
		assertEquals("SCT", line.get("HL7 Code"));
		assertEquals("2.16.840.1.113883.6.96", line.get("Unique ID"));
	}
	
	@Test
	void omitsBlankHl7CodeAndUniqueId() {
		ConceptSource source = new ConceptSource();
		source.setUuid("cs");
		source.setName("Local");
		
		ExportLine line = new ExportLine();
		new ConceptSourceLineExporter().export(source, line);
		
		assertNull(line.get("HL7 Code"), "blank HL7 code is not emitted");
		assertNull(line.get("Unique ID"), "blank unique id is not emitted");
	}
	
	@Test
	void retiredSourceEmitsUuidAndFlagOnly() {
		ConceptSource source = new ConceptSource();
		source.setUuid("old");
		source.setName("Old");
		source.setRetired(true);
		
		ExportLine line = new ExportLine();
		new ConceptSourceLineExporter().export(source, line);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
	}
}
