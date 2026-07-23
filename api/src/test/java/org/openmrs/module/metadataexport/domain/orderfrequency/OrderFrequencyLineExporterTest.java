/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.orderfrequency;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.OrderFrequency;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderFrequencyLineExporterTest {
	
	private static Concept concept(String uuid) {
		Concept concept = new Concept();
		concept.setUuid(uuid);
		return concept;
	}
	
	@Test
	void exportsAllFieldsForActiveFrequency() {
		OrderFrequency frequency = new OrderFrequency();
		frequency.setUuid("136ebdb7-e989-47cf-8ec2-4e8b2ffe0ab3");
		frequency.setFrequencyPerDay(0.5);
		frequency.setConcept(concept("bidaily-concept-uuid"));
		
		ExportLine line = new ExportLine();
		new OrderFrequencyLineExporter().writeLine(frequency, line);
		
		assertEquals("136ebdb7-e989-47cf-8ec2-4e8b2ffe0ab3", line.get("uuid"));
		assertEquals("0.5", line.get("frequency per day"));
		assertEquals("bidaily-concept-uuid", line.get("concept frequency"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void retiredFrequencyEmitsUuidAndFlagOnly() {
		OrderFrequency frequency = new OrderFrequency();
		frequency.setUuid("4b33b729-1fe3-4fa5-acc4-084beb069b68");
		frequency.setFrequencyPerDay(24d);
		frequency.setConcept(concept("hourly-concept-uuid"));
		frequency.setRetired(true);
		
		ExportLine line = new ExportLine();
		new OrderFrequencyLineExporter().writeLine(frequency, line);
		
		assertEquals("4b33b729-1fe3-4fa5-acc4-084beb069b68", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("frequency per day"), "retired rows carry only uuid + flag");
		assertNull(line.get("concept frequency"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void omitsColumnsWhenFrequencyOrConceptIsNull() {
		OrderFrequency frequency = new OrderFrequency();
		frequency.setUuid("no-concept-uuid");
		
		ExportLine line = new ExportLine();
		new OrderFrequencyLineExporter().writeLine(frequency, line);
		
		assertNull(line.get("frequency per day"));
		assertNull(line.get("concept frequency"));
	}
}
