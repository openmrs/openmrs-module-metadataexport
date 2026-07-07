/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.personAttributeType;

import org.junit.jupiter.api.Test;
import org.openmrs.PersonAttributeType;
import org.openmrs.Privilege;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PersonAttributeTypeLineExporterTest {
	
	@Test
	void exportsCoreFields() {
		PersonAttributeType type = new PersonAttributeType();
		type.setUuid("pat");
		type.setName("Birthplace");
		type.setDescription("Where the person was born");
		type.setSearchable(true);
		type.setFormat("java.lang.String");
		type.setEditPrivilege(new Privilege("Edit Birthplace"));
		
		ExportLine line = new ExportLine();
		new PersonAttributeTypeLineExporter().writeLine(type, line);
		
		assertEquals("pat", line.get("uuid"));
		assertEquals("Birthplace", line.get("name"));
		assertEquals("Where the person was born", line.get("description"));
		assertEquals("true", line.get("searchable"));
		assertEquals("java.lang.String", line.get("format"));
		assertEquals("Edit Birthplace", line.get("edit privilege"));
	}
	
	@Test
	void omitsEmptyFormatAndNullEditPrivilege() {
		PersonAttributeType type = new PersonAttributeType();
		type.setUuid("pat");
		type.setName("Notes");
		type.setSearchable(false);
		
		ExportLine line = new ExportLine();
		new PersonAttributeTypeLineExporter().writeLine(type, line);
		
		assertNull(line.get("format"), "empty format is not emitted");
		assertNull(line.get("edit privilege"), "null edit privilege is not emitted");
	}
	
	@Test
	void omitsForeignKeyWhenFormatIsNotConcept() {
		// The foreign key is only resolvable (and only meaningful) for a Concept format. For any
		// other format the guard must short-circuit before touching the ConceptService, so a stray
		// foreign key is dropped rather than exported against the wrong domain.
		PersonAttributeType type = new PersonAttributeType();
		type.setUuid("pat");
		type.setName("Birthplace");
		type.setFormat("java.lang.String");
		type.setForeignKey(42);
		
		ExportLine line = new ExportLine();
		new PersonAttributeTypeLineExporter().writeLine(type, line);
		
		assertNull(line.get("foreign uuid"), "foreign key is not emitted for a non-Concept format");
	}
	
	@Test
	void retiredTypeEmitsUuidAndFlagOnly() {
		PersonAttributeType type = new PersonAttributeType();
		type.setUuid("old");
		type.setName("Old");
		type.setRetired(true);
		
		ExportLine line = new ExportLine();
		new PersonAttributeTypeLineExporter().writeLine(type, line);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
	}
}
