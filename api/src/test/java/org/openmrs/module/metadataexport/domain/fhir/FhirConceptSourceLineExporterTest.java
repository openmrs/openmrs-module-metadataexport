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
import org.openmrs.ConceptSource;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.openmrs.module.initializer.api.fhir.cs.FhirConceptSourceLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FhirConceptSourceLineExporterTest {
	
	private static ConceptSource conceptSource(String uuid) {
		ConceptSource source = new ConceptSource();
		source.setUuid(uuid);
		return source;
	}
	
	@Test
	void exportsUuidConceptSourceAndUrl() {
		FhirConceptSource source = new FhirConceptSource();
		source.setUuid("c1d8a345-3f10-11e4-adec-0800271c1b75");
		source.setConceptSource(conceptSource("a5d38e09-efcb-4d91-a526-50ce1ba5011a"));
		source.setUrl("http://snomed.info/sct");
		
		ExportLine line = new ExportLine();
		new FhirConceptSourceLineExporter().writeLine(source, line);
		
		assertEquals("c1d8a345-3f10-11e4-adec-0800271c1b75", line.get("uuid"));
		assertEquals("a5d38e09-efcb-4d91-a526-50ce1ba5011a", line.get("Concept source"));
		assertEquals("http://snomed.info/sct", line.get("url"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void liveSourceWithoutConceptSourceOrUrlOmitsThoseColumns() {
		FhirConceptSource source = new FhirConceptSource();
		source.setUuid("c1d8a345-3f10-11e4-adec-0800271c1b75");
		
		ExportLine line = new ExportLine();
		new FhirConceptSourceLineExporter().writeLine(source, line);
		
		assertEquals("c1d8a345-3f10-11e4-adec-0800271c1b75", line.get("uuid"));
		assertNull(line.get("Concept source"), "missing concept source is not written as a column");
		assertNull(line.get("url"), "empty url is not written as a column");
	}
	
	@Test
	void retiredSourceEmitsFullRowPlusFlag() {
		FhirConceptSource source = new FhirConceptSource();
		source.setUuid("439559c2-a3a4-4a25-b4b2-1a0299e287ee");
		source.setConceptSource(conceptSource("a5d38e09-efcb-4d91-a526-50ce1ba5011a"));
		source.setUrl("http://snomed.info/sct");
		source.setRetired(true);
		
		ExportLine line = new ExportLine();
		new FhirConceptSourceLineExporter().writeLine(source, line);
		
		assertEquals("439559c2-a3a4-4a25-b4b2-1a0299e287ee", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertEquals("a5d38e09-efcb-4d91-a526-50ce1ba5011a", line.get("Concept source"),
		    "Iniz matches retire rows by concept source, not uuid, so the column is required");
		assertEquals("http://snomed.info/sct", line.get("url"),
		    "Iniz needs the url whenever the target has no row for this concept source yet — retire rows included");
	}
	
	@Test
	void retiredOrphanStillWritesUuidAndFlagWithoutFailing() {
		FhirConceptSource source = new FhirConceptSource();
		source.setUuid("439559c2-a3a4-4a25-b4b2-1a0299e287ee");
		source.setRetired(true);
		
		ExportLine line = new ExportLine();
		new FhirConceptSourceLineExporter().writeLine(source, line);
		
		assertEquals("439559c2-a3a4-4a25-b4b2-1a0299e287ee", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("Concept source"),
		    "the domain exporter filters orphans out of exports; the line exporter just stays NPE-safe");
	}
	
	@Test
	void headerLiteralsStayInSyncWithInitializer() throws Exception {
		// the "Concept source" header is written via Iniz's public constant, so only the
		// re-declared url header (private in Iniz) can drift
		Field theirs = FhirConceptSourceLineProcessor.class.getDeclaredField("URL_HEADER");
		theirs.setAccessible(true);
		assertEquals(FhirConceptSourceLineExporter.URL_HEADER, theirs.get(null),
		    "URL_HEADER drifted from Iniz's FhirConceptSourceLineProcessor");
	}
}
