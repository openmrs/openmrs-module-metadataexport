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
import org.openmrs.module.idgen.RemoteIdentifierSource;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SequentialIdentifierGeneratorLineExporterTest {
	
	@Test
	void exportsAllSequentialColumns() {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();
		generator.setPrefix("PRE-");
		generator.setSuffix("-SUF");
		generator.setFirstIdentifierBase("1000");
		generator.setMinLength(6);
		generator.setMaxLength(10);
		generator.setBaseCharacterSet("0123456789ACDEFGHJKLMNPRTUVWXY");
		
		ExportLine line = new ExportLine();
		new SequentialIdentifierGeneratorLineExporter().writeLine(generator, line);
		
		assertEquals("PRE-", line.get("prefix"));
		assertEquals("-SUF", line.get("suffix"));
		assertEquals("1000", line.get("first identifier base"));
		assertEquals("6", line.get("min length"));
		assertEquals("10", line.get("max length"));
		assertEquals("0123456789ACDEFGHJKLMNPRTUVWXY", line.get("base character set"));
	}
	
	@Test
	void omitsUnsetOptionalColumns() {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();
		generator.setFirstIdentifierBase("1");
		generator.setBaseCharacterSet("0123456789");
		
		ExportLine line = new ExportLine();
		new SequentialIdentifierGeneratorLineExporter().writeLine(generator, line);
		
		assertNull(line.get("prefix"), "unset prefix is not written as a column");
		assertNull(line.get("suffix"), "unset suffix is not written as a column");
		assertNull(line.get("min length"), "unset min length is not written as a column");
		assertNull(line.get("max length"), "unset max length is not written as a column");
	}
	
	@Test
	void neverExportsNextSequenceValue() {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();
		generator.setFirstIdentifierBase("1000");
		generator.setBaseCharacterSet("0123456789");
		generator.setNextSequenceValue(4711L);
		
		ExportLine line = new ExportLine();
		new SequentialIdentifierGeneratorLineExporter().writeLine(generator, line);
		
		for (String header : line.getHeaders()) {
			assertNotEquals("4711", line.get(header), "next sequence value must not leak into column " + header);
		}
	}
	
	@Test
	void skipsNonSequentialSources() {
		RemoteIdentifierSource remote = new RemoteIdentifierSource();
		remote.setUrl("https://idgen.example.org/generate");
		
		ExportLine line = new ExportLine();
		new SequentialIdentifierGeneratorLineExporter().writeLine(remote, line);
		
		assertTrue(line.getHeaders().isEmpty(), "non-sequential sources contribute no columns");
	}
	
	@Test
	void retiredGeneratorStillExportsItsColumns() {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();
		generator.setFirstIdentifierBase("1000");
		generator.setBaseCharacterSet("0123456789");
		generator.setRetired(true);
		
		ExportLine line = new ExportLine();
		new SequentialIdentifierGeneratorLineExporter().writeLine(generator, line);
		
		assertEquals("1000", line.get("first identifier base"),
		    "Iniz requires this column even when it bootstraps a retired row");
		assertEquals("0123456789", line.get("base character set"));
	}
}
