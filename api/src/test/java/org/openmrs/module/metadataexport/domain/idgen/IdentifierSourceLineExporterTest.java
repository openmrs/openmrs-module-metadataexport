/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.idgen;

import org.junit.jupiter.api.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.initializer.api.idgen.IdentifierSourceLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdentifierSourceLineExporterTest {
	
	private static PatientIdentifierType identifierType(String uuid) {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setUuid(uuid);
		return type;
	}
	
	@Test
	void exportsUuidIdentifierTypeNameAndDescription() {
		SequentialIdentifierGenerator source = new SequentialIdentifierGenerator();
		source.setUuid("c1d8a345-3f10-11e4-adec-0800271c1b75");
		source.setIdentifierType(identifierType("a5d38e09-efcb-4d91-a526-50ce1ba5011a"));
		source.setName("OpenMRS ID Generator");
		source.setDescription("Generates OpenMRS IDs");
		
		ExportLine line = new ExportLine();
		new IdentifierSourceLineExporter().writeLine(source, line);
		
		assertEquals("c1d8a345-3f10-11e4-adec-0800271c1b75", line.get("uuid"));
		assertEquals("a5d38e09-efcb-4d91-a526-50ce1ba5011a", line.get("Identifier type"));
		assertEquals("OpenMRS ID Generator", line.get("name"));
		assertEquals("Generates OpenMRS IDs", line.get("description"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void liveSourceWithoutDescriptionOrTypeOmitsThoseColumns() {
		SequentialIdentifierGenerator source = new SequentialIdentifierGenerator();
		source.setUuid("c1d8a345-3f10-11e4-adec-0800271c1b75");
		source.setName("OpenMRS ID Generator");
		
		ExportLine line = new ExportLine();
		new IdentifierSourceLineExporter().writeLine(source, line);
		
		assertEquals("OpenMRS ID Generator", line.get("name"));
		assertNull(line.get("description"), "empty description is not written as a column");
		assertNull(line.get("Identifier type"), "missing identifier type is not written as a column");
	}
	
	@Test
	void retiredSourceEmitsFullCommonRowPlusFlag() {
		SequentialIdentifierGenerator source = new SequentialIdentifierGenerator();
		source.setUuid("439559c2-a3a4-4a25-b4b2-1a0299e287ee");
		source.setIdentifierType(identifierType("a5d38e09-efcb-4d91-a526-50ce1ba5011a"));
		source.setName("Old Generator");
		source.setDescription("No longer used");
		source.setRetired(true);
		
		ExportLine line = new ExportLine();
		new IdentifierSourceLineExporter().writeLine(source, line);
		
		assertEquals("439559c2-a3a4-4a25-b4b2-1a0299e287ee", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertEquals("Old Generator", line.get("name"),
		    "Iniz bootstraps retired rows with unknown uuids, so they carry the full row");
		assertEquals("No longer used", line.get("description"));
		assertEquals("a5d38e09-efcb-4d91-a526-50ce1ba5011a", line.get("Identifier type"));
	}
	
	@Test
	void headerLiteralsStayInSyncWithInitializer() throws Exception {
		int checked = 0;
		for (Field ours : IdentifierSourceLineExporter.class.getDeclaredFields()) {
			if (!ours.getName().startsWith("HEADER_")) {
				continue;
			}
			Field theirs = IdentifierSourceLineProcessor.class.getDeclaredField(ours.getName());
			theirs.setAccessible(true);
			assertEquals(theirs.get(null), ours.get(null),
			    ours.getName() + " drifted from Iniz's IdentifierSourceLineProcessor");
			checked++;
		}
		assertEquals(15, checked, "every re-declared header literal is checked against Iniz");
		
		long inizHeaders = Arrays.stream(IdentifierSourceLineProcessor.class.getDeclaredFields())
		        .filter(field -> field.getName().startsWith("HEADER_")).count();
		assertEquals(checked, inizHeaders, "Iniz declares a header this exporter does not know about");
	}
}
