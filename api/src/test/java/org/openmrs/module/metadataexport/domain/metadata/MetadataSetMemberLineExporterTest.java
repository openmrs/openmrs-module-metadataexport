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
import org.openmrs.module.metadatamapping.MetadataSet;
import org.openmrs.module.metadatamapping.MetadataSetMember;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MetadataSetMemberLineExporterTest {
	
	private static MetadataSet metadataSet(String uuid) {
		MetadataSet metadataSet = new MetadataSet();
		metadataSet.setUuid(uuid);
		return metadataSet;
	}
	
	@Test
	void exportsAllFieldsForActiveMember() {
		MetadataSetMember member = new MetadataSetMember();
		member.setUuid("dbfd899d-e9e1-4059-8992-73737c924f88");
		member.setName("Outpatient Id");
		member.setDescription("IdentifierType for OPD");
		member.setMetadataSet(metadataSet("f0ebcb99-7618-41b7-b0bf-8ff93de67b9e"));
		member.setMetadataClass("org.openmrs.PatientIdentifierType");
		member.setMetadataUuid("7b0f5697-27e3-40c4-8bae-f4049abfb4ed");
		member.setSortWeight(34d);
		
		ExportLine line = new ExportLine();
		new MetadataSetMemberLineExporter().writeLine(member, line);
		
		assertEquals("dbfd899d-e9e1-4059-8992-73737c924f88", line.get("uuid"));
		assertEquals("Outpatient Id", line.get("name"));
		assertEquals("IdentifierType for OPD", line.get("description"));
		assertEquals("f0ebcb99-7618-41b7-b0bf-8ff93de67b9e", line.get("metadata set uuid"));
		assertEquals("org.openmrs.PatientIdentifierType", line.get("metadata class"));
		assertEquals("7b0f5697-27e3-40c4-8bae-f4049abfb4ed", line.get("metadata uuid"));
		assertEquals("34.0", line.get("sort weight"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void retiredMemberEmitsUuidAndFlagOnly() {
		MetadataSetMember member = new MetadataSetMember();
		member.setUuid("f0ebcb99-272d-41b7-4c67-078de9342492");
		member.setName("Old OpenMRS Id");
		member.setMetadataSet(metadataSet("f0ebcb99-7618-41b7-b0bf-8ff93de67b9e"));
		member.setMetadataClass("org.openmrs.PatientIdentifierType");
		member.setMetadataUuid("8f6ed8bb-0cbe-4c67-bc45-c5c0320e1324");
		member.setRetired(true);
		
		ExportLine line = new ExportLine();
		new MetadataSetMemberLineExporter().writeLine(member, line);
		
		assertEquals("f0ebcb99-272d-41b7-4c67-078de9342492", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
		assertNull(line.get("metadata set uuid"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void omitsSortWeightAndMetadataSetWhenAbsent() {
		MetadataSetMember member = new MetadataSetMember();
		member.setUuid("ee00777d-0cbe-41b7-4c67-8ff93de67b9e");
		member.setName("Legacy Id");
		member.setMetadataClass("org.openmrs.PatientIdentifierType");
		member.setMetadataUuid("n0ebcb90-m618-n1b1-b0bf-kff93de97b9j");
		
		ExportLine line = new ExportLine();
		new MetadataSetMemberLineExporter().writeLine(member, line);
		
		assertEquals("Legacy Id", line.get("name"));
		assertNull(line.get("sort weight"));
		assertNull(line.get("metadata set uuid"));
	}
}
