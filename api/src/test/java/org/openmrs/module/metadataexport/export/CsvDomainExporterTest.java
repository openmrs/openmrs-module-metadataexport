/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.export;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvDomainExporterTest {
	
	private static final String DOMAIN_DIR = Domain.CONCEPTS.getName();
	
	private static class NameExporter extends MetadataLineExporter<Concept> {
		
		@Override
		public void export(Concept concept, ExportLine line) {
			line.put("name", "N-" + concept.getUuid());
		}
	}
	
	private static class FlavorExporter extends BaseLineExporter<Concept> {
		
		@Override
		public void export(Concept concept, ExportLine line) {
			if (BooleanUtils.isTrue(concept.getRetired())) {
				return;
			}
			if ("c2".equals(concept.getUuid())) {
				line.put("flavor", "F-c2");
			}
		}
	}
	
	private static class TestDomainExporter extends CsvDomainExporter<Concept> {
		
		@Override
		protected List<BaseLineExporter<Concept>> chain() {
			return Arrays.asList(new NameExporter(), new FlavorExporter());
		}
		
		@Override
		protected String fileName() {
			return "test.csv";
		}
		
		@Override
		public Domain getDomain() {
			return Domain.CONCEPTS;
		}
		
		@Override
		public boolean handles(OpenmrsObject instance) {
			return instance instanceof Concept;
		}
		
		@Override
		public Collection<Concept> getAllInstances() {
			return Collections.emptyList();
		}
		
		@Override
		public Collection<? extends OpenmrsObject> getDependencies(Concept instance) {
			return Collections.emptyList();
		}
	}
	
	private static Concept concept(String uuid) {
		Concept c = new Concept();
		c.setUuid(uuid);
		return c;
	}
	
	@Test
	void export_mergesChainIntoUnionHeaderAndAlignsRows(@TempDir File outDir) throws Exception {
		Concept c1 = concept("c1");
		Concept c2 = concept("c2");
		Concept retired = concept("r");
		retired.setRetired(true);
		
		new TestDomainExporter().export(Arrays.asList(c1, c2, retired), new ExportContext(outDir));
		
		File csv = outDir.toPath().resolve(Paths.get("configuration", DOMAIN_DIR, "test.csv")).toFile();
		assertTrue(csv.exists(), "expected " + csv);
		
		try (CSVReader reader = new CSVReader(new FileReader(csv))) {
			List<String[]> rows = reader.readAll();
			assertEquals(4, rows.size());
			assertArrayEquals(new String[] { "uuid", "name", "flavor", "void/retire", "_version:1" }, rows.get(0));
			
			Map<String, Integer> col = headerIndex(rows.get(0));
			String[] row1 = rows.get(1), row2 = rows.get(2), row3 = rows.get(3);
			
			assertEquals("c1", row1[col.get("uuid")]);
			assertEquals("N-c1", row1[col.get("name")]);
			assertEquals("", row1[col.get("flavor")]);
			
			assertEquals("F-c2", row2[col.get("flavor")]);
			
			assertEquals("r", row3[col.get("uuid")]);
			assertEquals("true", row3[col.get("void/retire")]);
			assertEquals("", row3[col.get("name")]);
			assertEquals("", row3[col.get("flavor")]);
		}
	}
	
	private static Map<String, Integer> headerIndex(String[] header) {
		Map<String, Integer> index = new HashMap<>();
		for (int i = 0; i < header.length; i++) {
			index.put(header[i], i);
		}
		return index;
	}
}
