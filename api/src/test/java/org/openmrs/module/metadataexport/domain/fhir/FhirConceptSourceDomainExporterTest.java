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

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.ConceptSource;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.CsvLine;
import org.openmrs.module.metadataexport.export.ExportContext;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FhirConceptSourceDomainExporterTest {
	
	private final FhirConceptSourceDomainExporter exporter = new FhirConceptSourceDomainExporter();
	
	private static FhirConceptSource fhirConceptSource(String uuid, String conceptSourceUuid, String url) {
		ConceptSource conceptSource = new ConceptSource();
		conceptSource.setUuid(conceptSourceUuid);
		FhirConceptSource source = new FhirConceptSource();
		source.setUuid(uuid);
		source.setConceptSource(conceptSource);
		source.setUrl(url);
		return source;
	}
	
	@Test
	void handlesOnlyImportableFhirConceptSources() {
		assertTrue(exporter.handles(fhirConceptSource("c1d8a345-3f10-11e4-adec-0800271c1b75",
		    "a5d38e09-efcb-4d91-a526-50ce1ba5011a", "http://snomed.info/sct")));
		assertFalse(exporter.handles(new FhirConceptSource()), "a row without a concept source cannot be imported by Iniz");
		assertFalse(exporter.handles(new ConceptSource()));
	}
	
	@Test
	void exportableSkipsRowsWithoutAConceptSource() {
		FhirConceptSource good = fhirConceptSource("c1d8a345-3f10-11e4-adec-0800271c1b75",
		    "a5d38e09-efcb-4d91-a526-50ce1ba5011a", "http://snomed.info/sct");
		FhirConceptSource orphan = new FhirConceptSource();
		orphan.setUuid("439559c2-a3a4-4a25-b4b2-1a0299e287ee");
		
		List<FhirConceptSource> result = FhirConceptSourceDomainExporter.exportable(Arrays.asList(good, orphan));
		
		assertEquals(1, result.size(), "a row missing its required reference column can never import");
		assertEquals(good.getUuid(), result.get(0).getUuid());
	}
	
	@Test
	void exportableKeepsOneRowPerConceptSourcePreferringTheUnretiredOne() {
		FhirConceptSource retired = fhirConceptSource("439559c2-a3a4-4a25-b4b2-1a0299e287ee",
		    "a5d38e09-efcb-4d91-a526-50ce1ba5011a", "http://snomed.info/old");
		retired.setRetired(true);
		FhirConceptSource live = fhirConceptSource("c1d8a345-3f10-11e4-adec-0800271c1b75",
		    "a5d38e09-efcb-4d91-a526-50ce1ba5011a", "http://snomed.info/sct");
		FhirConceptSource other = fhirConceptSource("9e1a2b3c-3f10-11e4-adec-0800271c1b75",
		    "7e3f4d5a-3f10-11e4-adec-0800271c1b75", "http://loinc.org");
		
		List<FhirConceptSource> result = FhirConceptSourceDomainExporter.exportable(Arrays.asList(retired, live, other));
		
		assertEquals(2, result.size(), "duplicate rows for one concept source collapse nondeterministically on import");
		assertEquals(live.getUuid(), result.get(0).getUuid(), "the unretired duplicate wins");
		assertEquals(other.getUuid(), result.get(1).getUuid());
	}
	
	@Test
	void dependenciesIncludeTheConceptSource() {
		FhirConceptSource source = fhirConceptSource("c1d8a345-3f10-11e4-adec-0800271c1b75",
		    "a5d38e09-efcb-4d91-a526-50ce1ba5011a", "http://snomed.info/sct");
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(source);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(source.getConceptSource()));
	}
	
	@Test
	void dependenciesAreNullSafe() {
		assertTrue(exporter.getDependencies(new FhirConceptSource()).isEmpty());
	}
	
	@Test
	void export_writesACsvInizCanRead(@TempDir File outDir) throws Exception {
		FhirConceptSource live = fhirConceptSource("c1d8a345-3f10-11e4-adec-0800271c1b75",
		    "a5d38e09-efcb-4d91-a526-50ce1ba5011a", "http://snomed.info/sct");
		FhirConceptSource retired = fhirConceptSource("439559c2-a3a4-4a25-b4b2-1a0299e287ee",
		    "9e1a2b3c-3f10-11e4-adec-0800271c1b75", "http://loinc.org");
		retired.setRetired(true);
		
		exporter.export(Arrays.asList(live, retired), new ExportContext(outDir));
		
		File csv = outDir.toPath()
		        .resolve(Paths.get("configuration", Domain.FHIR_CONCEPT_SOURCES.getName(), "fhirConceptSources.csv"))
		        .toFile();
		assertTrue(csv.exists(), "expected " + csv);
		try (CSVReader reader = new CSVReader(new FileReader(csv))) {
			List<String[]> rows = reader.readAll();
			assertEquals(3, rows.size(), "one header row plus both fixtures, retired included");
			String[] header = rows.get(0);
			assertEquals("1", BaseLineProcessor.getVersion(header));
			for (String[] cells : rows.subList(1, rows.size())) {
				for (int i = 0; i < cells.length; i++) {
					if (StringUtils.isBlank(cells[i])) {
						cells[i] = null;
					}
				}
				CsvLine line = new CsvLine(header, cells);
				assertNotNull(line.get("Concept source", true), "row " + line.getUuid() + " must fill 'Concept source'");
				assertNotNull(line.get("url", true), "row " + line.getUuid() + " must fill 'url'");
			}
		}
	}
}
