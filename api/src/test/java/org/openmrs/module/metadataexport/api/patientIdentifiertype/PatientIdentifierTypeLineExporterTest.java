/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.patientIdentifiertype;

import org.junit.jupiter.api.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PatientIdentifierType.LocationBehavior;
import org.openmrs.PatientIdentifierType.UniquenessBehavior;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.metadataexport.api.patientIdentifierType.PatientIdentifierTypeLineExporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PatientIdentifierTypeLineExporterTest {
	
	@Test
	void exportsUuidNameDescriptionAndFormatFields() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setUuid("pit");
		type.setName("OpenMRS ID");
		type.setDescription("Primary identifier");
		type.setRequired(true);
		type.setFormat("[0-9]+");
		type.setFormatDescription("Digits only");
		type.setValidator("org.openmrs.module.idgen.validator.LuhnMod30IdentifierValidator");
		type.setLocationBehavior(LocationBehavior.REQUIRED);
		type.setUniquenessBehavior(UniquenessBehavior.UNIQUE);
		
		ExportLine line = new ExportLine();
		new PatientIdentifierTypeLineExporter().writeLine(type, line);
		
		assertEquals("pit", line.get("uuid"));
		assertEquals("OpenMRS ID", line.get("name"));
		assertEquals("Primary identifier", line.get("description"));
		assertEquals("true", line.get("required"));
		assertEquals("[0-9]+", line.get("format"));
		assertEquals("Digits only", line.get("format description"));
		assertEquals("org.openmrs.module.idgen.validator.LuhnMod30IdentifierValidator", line.get("validator"));
		assertEquals("REQUIRED", line.get("location behavior"));
		assertEquals("UNIQUE", line.get("uniqueness behavior"));
	}
	
	@Test
	void omitsNullEnumBehaviors() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setUuid("pit");
		type.setName("Local ID");
		type.setRequired(false);
		
		ExportLine line = new ExportLine();
		new PatientIdentifierTypeLineExporter().writeLine(type, line);
		
		assertNull(line.get("location behavior"), "null location behavior is not emitted");
		assertNull(line.get("uniqueness behavior"), "null uniqueness behavior is not emitted");
	}
	
	@Test
	void retiredTypeEmitsUuidAndFlagOnly() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setUuid("old");
		type.setName("Old");
		type.setRetired(true);
		
		ExportLine line = new ExportLine();
		new PatientIdentifierTypeLineExporter().writeLine(type, line);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
	}
}
