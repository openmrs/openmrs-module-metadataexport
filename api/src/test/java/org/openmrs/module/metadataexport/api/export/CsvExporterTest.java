/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.export;

import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.Concept;
import org.openmrs.module.initializer.Domain;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExporterTest {
	
	@TempDir
	File outDir;
	
	private static Concept concept(String uuid) {
		Concept c = new Concept();
		c.setUuid(uuid);
		return c;
	}
	
	// Emits colA for every concept, and colB only for "c2" — so the union header must include both.
	private static final BaseLineExporter<Concept> VARYING_COLUMNS = new BaseLineExporter<Concept>() {
		
		@Override
		public void export(Concept concept, ExportLine line) {
			line.put("colA", "A-" + concept.getUuid());
			if ("c2".equals(concept.getUuid())) {
				line.put("colB", "B-c2");
			}
		}
	};
	
	@Test
	void writeCsv_buildsUnionHeaderAndAlignsRows() throws Exception {
		CsvExporter<Concept> exporter = new CsvExporter<>(Collections.singletonList(VARYING_COLUMNS), Domain.CONCEPTS);
		
		exporter.writeCsv(Arrays.asList(concept("c1"), concept("c2")), outDir, "test.csv");
		
		File csv = new File(new File(outDir, "configuration"), Domain.CONCEPTS.getName() + "/test.csv");
		assertTrue(csv.exists(), "expected " + csv);
		
		try (CSVReader reader = new CSVReader(new FileReader(csv))) {
			List<String[]> rows = reader.readAll();
			
			// header = first-seen union (colA, colB) + the _version metadata column
			assertArrayEquals(new String[] { "colA", "colB", "_version:1" }, rows.get(0));
			// c1 has no colB -> blank, aligned to the union
			assertArrayEquals(new String[] { "A-c1", "", "" }, rows.get(1));
			assertArrayEquals(new String[] { "A-c2", "B-c2", "" }, rows.get(2));
			assertEquals(3, rows.size());
		}
	}
}
