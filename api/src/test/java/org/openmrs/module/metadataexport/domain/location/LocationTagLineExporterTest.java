/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.location;

import org.junit.jupiter.api.Test;
import org.openmrs.LocationTag;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LocationTagLineExporterTest {
	
	@Test
	void exportsUuidNameAndDescription() {
		LocationTag tag = new LocationTag();
		tag.setUuid("tag");
		tag.setName("Login Location");
		tag.setDescription("Locations a user may log in to");
		
		ExportLine line = new ExportLine();
		new LocationTagLineExporter().writeLine(tag, line);
		
		assertEquals("tag", line.get("uuid"));
		assertEquals("Login Location", line.get("name"));
		assertEquals("Locations a user may log in to", line.get("description"));
	}
	
	@Test
	void retiredTagEmitsUuidAndFlagOnly() {
		LocationTag tag = new LocationTag();
		tag.setUuid("old");
		tag.setName("Old");
		tag.setRetired(true);
		
		ExportLine line = new ExportLine();
		new LocationTagLineExporter().writeLine(tag, line);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
	}
}
