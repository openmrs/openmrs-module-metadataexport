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
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConceptSetLineExporterTest {
	
	private static Concept concept(String uuid) {
		Concept concept = new Concept();
		concept.setUuid(uuid);
		return concept;
	}
	
	private static ConceptSet conceptSet(Concept set, Concept member, Double sortWeight) {
		ConceptSet conceptSet = new ConceptSet(member, sortWeight);
		conceptSet.setConceptSet(set);
		return conceptSet;
	}
	
	@Test
	void exportsMembershipAsConceptSetRow() {
		ConceptSet conceptSet = conceptSet(concept("parent-set-uuid"), concept("member-uuid"), 2.0);
		
		ExportLine line = new ExportLine();
		new ConceptSetLineExporter().writeLine(conceptSet, line);
		
		assertEquals("parent-set-uuid", line.get("concept"));
		assertEquals("member-uuid", line.get("member"));
		assertEquals("concept-set", line.get("member type"));
		assertEquals("2.0", line.get("sort weight"));
	}
	
	@Test
	void omitsSortWeightWhenUnset() {
		ConceptSet conceptSet = conceptSet(concept("parent-set-uuid"), concept("member-uuid"), null);
		
		ExportLine line = new ExportLine();
		new ConceptSetLineExporter().writeLine(conceptSet, line);
		
		assertEquals("member-uuid", line.get("member"));
		assertNull(line.get("sort weight"));
	}
}
