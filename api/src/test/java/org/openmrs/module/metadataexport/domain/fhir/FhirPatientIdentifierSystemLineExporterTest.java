/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.fhir;

import org.junit.jupiter.api.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.model.FhirPatientIdentifierSystem;
import org.openmrs.module.initializer.api.fhir.pis.FhirPatientIdentifierSystemCsvParser;
import org.openmrs.module.initializer.api.fhir.pis.FhirPatientIdentifierSystemLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FhirPatientIdentifierSystemLineExporterTest {
	
	private static PatientIdentifierType identifierType(String uuid) {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setUuid(uuid);
		return type;
	}
	
	@Test
	void exportsUuidIdentifierTypeAndUrl() {
		FhirPatientIdentifierSystem system = new FhirPatientIdentifierSystem();
		system.setUuid("c1d8a345-3f10-11e4-adec-0800271c1b75");
		system.setPatientIdentifierType(identifierType("a5d38e09-efcb-4d91-a526-50ce1ba5011a"));
		system.setUrl("http://openmrs.example.org/identifier");
		
		ExportLine line = new ExportLine();
		new FhirPatientIdentifierSystemLineExporter().writeLine(system, line);
		
		assertEquals("c1d8a345-3f10-11e4-adec-0800271c1b75", line.get("uuid"));
		assertEquals("a5d38e09-efcb-4d91-a526-50ce1ba5011a", line.get("Patient identifier type"));
		assertEquals("http://openmrs.example.org/identifier", line.get("url"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void liveSystemWithoutIdentifierTypeOrUrlOmitsThoseColumns() {
		FhirPatientIdentifierSystem system = new FhirPatientIdentifierSystem();
		system.setUuid("c1d8a345-3f10-11e4-adec-0800271c1b75");
		
		ExportLine line = new ExportLine();
		new FhirPatientIdentifierSystemLineExporter().writeLine(system, line);
		
		assertEquals("c1d8a345-3f10-11e4-adec-0800271c1b75", line.get("uuid"));
		assertNull(line.get("Patient identifier type"), "missing identifier type is not written as a column");
		assertNull(line.get("url"), "empty url is not written as a column");
	}
	
	@Test
	void retiredSystemEmitsFullRowPlusFlag() {
		FhirPatientIdentifierSystem system = new FhirPatientIdentifierSystem();
		system.setUuid("439559c2-a3a4-4a25-b4b2-1a0299e287ee");
		system.setPatientIdentifierType(identifierType("a5d38e09-efcb-4d91-a526-50ce1ba5011a"));
		system.setUrl("http://openmrs.example.org/identifier");
		system.setRetired(true);
		
		ExportLine line = new ExportLine();
		new FhirPatientIdentifierSystemLineExporter().writeLine(system, line);
		
		assertEquals("439559c2-a3a4-4a25-b4b2-1a0299e287ee", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertEquals("a5d38e09-efcb-4d91-a526-50ce1ba5011a", line.get("Patient identifier type"),
		    "Iniz matches retire rows by identifier type, not uuid, so the column is required");
		assertEquals("http://openmrs.example.org/identifier", line.get("url"),
		    "Iniz needs the url whenever the target has no row for this identifier type yet — retire rows included");
	}
	
	@Test
	void retiredOrphanStillWritesUuidAndFlagWithoutFailing() {
		FhirPatientIdentifierSystem system = new FhirPatientIdentifierSystem();
		system.setUuid("439559c2-a3a4-4a25-b4b2-1a0299e287ee");
		system.setRetired(true);
		
		ExportLine line = new ExportLine();
		new FhirPatientIdentifierSystemLineExporter().writeLine(system, line);
		
		assertEquals("439559c2-a3a4-4a25-b4b2-1a0299e287ee", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("Patient identifier type"),
		    "the domain exporter filters orphans out of exports; the line exporter just stays NPE-safe");
	}
	
	@Test
	void headerLiteralsStayInSyncWithInitializer() throws Exception {
		Field type = FhirPatientIdentifierSystemCsvParser.class.getDeclaredField("PATIENT_IDENTIFIER_TYPE_HEADER");
		type.setAccessible(true);
		assertEquals(FhirPatientIdentifierSystemLineExporter.PATIENT_IDENTIFIER_TYPE_HEADER, type.get(null),
		    "PATIENT_IDENTIFIER_TYPE_HEADER drifted from Iniz's FhirPatientIdentifierSystemCsvParser");
		
		Field url = FhirPatientIdentifierSystemLineProcessor.class.getDeclaredField("URL_HEADER");
		url.setAccessible(true);
		assertEquals(FhirPatientIdentifierSystemLineExporter.URL_HEADER, url.get(null),
		    "URL_HEADER drifted from Iniz's FhirPatientIdentifierSystemLineProcessor");
	}
}
