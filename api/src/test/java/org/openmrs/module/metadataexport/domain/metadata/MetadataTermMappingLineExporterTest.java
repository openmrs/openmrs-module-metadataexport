/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.metadata;

import org.junit.jupiter.api.Test;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadatamapping.MetadataSource;
import org.openmrs.module.metadatamapping.MetadataTermMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MetadataTermMappingLineExporterTest {
	
	private static MetadataSource source(String name) {
		MetadataSource source = new MetadataSource();
		source.setName(name);
		return source;
	}
	
	@Test
	void exportsAllFieldsForActiveMapping() {
		MetadataTermMapping mapping = new MetadataTermMapping();
		mapping.setUuid("21e24b36-f9e3-4b0e-986d-9899665597f7");
		mapping.setMetadataSource(source("org.openmrs.module.emrapi"));
		mapping.setCode("emr.primaryIdentifierType");
		mapping.setMetadataClass("org.openmrs.PatientIdentifierType");
		mapping.setMetadataUuid("264c9e75-77da-486a-8361-31558e051930");
		
		ExportLine line = new ExportLine();
		new MetadataTermMappingLineExporter().writeLine(mapping, line);
		
		assertEquals("21e24b36-f9e3-4b0e-986d-9899665597f7", line.get("uuid"));
		assertEquals("emr.primaryIdentifierType", line.get("mapping code"));
		assertEquals("org.openmrs.module.emrapi", line.get("mapping source"));
		assertEquals("org.openmrs.PatientIdentifierType", line.get("metadata class name"));
		assertEquals("264c9e75-77da-486a-8361-31558e051930", line.get("metadata uuid"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void retiredMappingEmitsUuidAndFlagOnly() {
		MetadataTermMapping mapping = new MetadataTermMapping();
		mapping.setUuid("5f84b986-232d-475b-aad2-2094306bd655");
		mapping.setMetadataSource(source("org.openmrs.module.emrapi"));
		mapping.setCode("emr.extraPatientIdentifierTypes");
		mapping.setMetadataClass("org.openmrs.module.metadatamapping.MetadataSet");
		mapping.setMetadataUuid("05a29f94-c0ed-11e2-94be-8c13b969e334");
		mapping.setRetired(true);
		
		ExportLine line = new ExportLine();
		new MetadataTermMappingLineExporter().writeLine(mapping, line);
		
		assertEquals("5f84b986-232d-475b-aad2-2094306bd655", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("mapping code"), "retired rows carry only uuid + flag");
		assertNull(line.get("mapping source"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void omitsMappingSourceColumnWhenNull() {
		MetadataTermMapping mapping = new MetadataTermMapping();
		mapping.setUuid("dbfd899d-e9e1-4059-8992-73737c924f84");
		mapping.setCode("emr.admissionEncounterType");
		mapping.setMetadataClass("org.openmrs.EncounterType");
		mapping.setMetadataUuid("e22e39fd-7db2-45e7-80f1-60fa0d5a4378");
		
		ExportLine line = new ExportLine();
		new MetadataTermMappingLineExporter().writeLine(mapping, line);
		
		assertEquals("emr.admissionEncounterType", line.get("mapping code"));
		assertNull(line.get("mapping source"));
	}
}
