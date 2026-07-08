/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api;

import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.encounter.EncounterTypeDomainExporter;
import org.openmrs.module.metadataexport.api.export.DomainExporterRegistry;
import org.openmrs.module.metadataexport.api.impl.ExporterServiceImpl;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataExportIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String SCHEDULED_UUID = "61ae96f4-6afe-4351-b6f8-cd4fc383cce1";
	
	private static final String RETIRED_LABORATORY_UUID = "02c533ab-b74b-4ee4-b6e5-ffb6d09a0ac8";
	
	@Test
	public void export_writesEncounterTypesFromTheRealDatabase(@TempDir File outDir) throws Exception {
		ExporterService service = new ExporterServiceImpl(
		        new DomainExporterRegistry(Collections.singletonList(new EncounterTypeDomainExporter())));
		
		service.export(outDir, Collections.singletonList(Domain.ENCOUNTER_TYPES));
		
		File csv = new File(new File(outDir, "configuration"), Domain.ENCOUNTER_TYPES.getName() + "/encounterTypes.csv");
		assertTrue(csv.exists(), "expected " + csv);
		
		try (CSVReader reader = new CSVReader(new FileReader(csv))) {
			List<String[]> rows = reader.readAll();
			Map<String, Integer> col = headerIndex(rows.get(0));
			Map<String, String[]> byUuid = new HashMap<>();
			for (int i = 1; i < rows.size(); i++) {
				byUuid.put(rows.get(i)[col.get("uuid")], rows.get(i));
			}
			
			assertEquals(3, byUuid.size());
			
			assertEquals("Scheduled", byUuid.get(SCHEDULED_UUID)[col.get("name")]);
			
			String[] laboratory = byUuid.get(RETIRED_LABORATORY_UUID);
			assertEquals("true", laboratory[col.get("void/retire")]);
			assertEquals("", laboratory[col.get("name")]);
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
