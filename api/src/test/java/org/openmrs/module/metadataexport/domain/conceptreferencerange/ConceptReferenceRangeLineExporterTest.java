/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.conceptreferencerange;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConceptReferenceRangeLineExporterTest {
	
	private static ConceptNumeric conceptNumeric(String uuid) {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid(uuid);
		return conceptNumeric;
	}
	
	@Test
	void exportsAllFields() {
		ConceptReferenceRange range = new ConceptReferenceRange();
		range.setConceptNumeric(conceptNumeric("bp-concept-uuid"));
		range.setLowAbsolute(0d);
		range.setHiAbsolute(300d);
		range.setLowCritical(40d);
		range.setHiCritical(200d);
		range.setLowNormal(80d);
		range.setHiNormal(120d);
		range.setCriteria("$patient.getAge() > 18");
		
		ExportLine line = new ExportLine();
		new ConceptReferenceRangeLineExporter().writeLine(range, line);
		
		assertEquals("bp-concept-uuid", line.get("Concept Numeric uuid"));
		assertEquals("0.0", line.get("Absolute Low"));
		assertEquals("300.0", line.get("Absolute High"));
		assertEquals("40.0", line.get("Critical Low"));
		assertEquals("200.0", line.get("Critical High"));
		assertEquals("80.0", line.get("Normal Low"));
		assertEquals("120.0", line.get("Normal High"));
		assertEquals("$patient.getAge() > 18", line.get("Criteria"));
	}
	
	@Test
	void omitsUnsetBounds() {
		ConceptReferenceRange range = new ConceptReferenceRange();
		range.setConceptNumeric(conceptNumeric("no-bounds-concept-uuid"));
		
		ExportLine line = new ExportLine();
		new ConceptReferenceRangeLineExporter().writeLine(range, line);
		
		assertEquals("no-bounds-concept-uuid", line.get("Concept Numeric uuid"));
		assertNull(line.get("Absolute Low"));
		assertNull(line.get("Criteria"));
	}
}
