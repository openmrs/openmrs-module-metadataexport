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
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// Headers are asserted as string literals (not the exporter's own constants) so a change to a
// header value that would break Initializer import is caught here rather than passing silently.
class LocationLineExporterTest {
	
	private static ExportLine export(Location location) {
		ExportLine line = new ExportLine();
		new LocationLineExporter().writeLine(location, line);
		return line;
	}
	
	@Test
	void exportsUuidNameAndDescription() {
		Location location = new Location();
		location.setUuid("loc");
		location.setName("Registration Desk");
		location.setDescription("Front desk");
		
		ExportLine line = export(location);
		
		assertEquals("loc", line.get("uuid"));
		assertEquals("Registration Desk", line.get("name"));
		assertEquals("Front desk", line.get("description"));
	}
	
	@Test
	void exportsAllAddressFields() {
		Location location = new Location();
		location.setUuid("loc");
		location.setName("Registration Desk");
		location.setCityVillage("Boston");
		location.setCountyDistrict("Suffolk");
		location.setStateProvince("MA");
		location.setPostalCode("02115");
		location.setCountry("USA");
		location.setAddress1("addr1");
		location.setAddress2("addr2");
		location.setAddress3("addr3");
		location.setAddress4("addr4");
		location.setAddress5("addr5");
		location.setAddress6("addr6");
		
		ExportLine line = export(location);
		
		assertEquals("Boston", line.get("city/village"));
		assertEquals("Suffolk", line.get("county/district"));
		assertEquals("MA", line.get("state/province"));
		assertEquals("02115", line.get("postal code"));
		assertEquals("USA", line.get("country"));
		assertEquals("addr1", line.get("address 1"));
		assertEquals("addr2", line.get("address 2"));
		assertEquals("addr3", line.get("address 3"));
		assertEquals("addr4", line.get("address 4"));
		assertEquals("addr5", line.get("address 5"));
		assertEquals("addr6", line.get("address 6"));
	}
	
	@Test
	void exportsEveryTagAsItsOwnBooleanColumn() {
		Location location = new Location();
		location.setUuid("loc");
		location.setName("Registration Desk");
		location.addTag(namedTag("Login Location"));
		location.addTag(namedTag("Visit Location"));
		
		ExportLine line = export(location);
		
		assertEquals("TRUE", line.get("tag|Login Location"));
		assertEquals("TRUE", line.get("tag|Visit Location"));
	}
	
	@Test
	void skipsRetiredTags() {
		LocationTag retired = namedTag("Old Tag");
		retired.setRetired(true);
		
		Location location = new Location();
		location.setUuid("loc");
		location.setName("Registration Desk");
		location.addTag(namedTag("Login Location"));
		location.addTag(retired);
		
		ExportLine line = export(location);
		
		assertEquals("TRUE", line.get("tag|Login Location"));
		assertNull(line.get("tag|Old Tag"),
		    "retired tags cannot resolve by name on import, so they must not be emitted as tag| columns");
	}
	
	@Test
	void referencesParentByUuidNotName() {
		Location parent = new Location();
		parent.setUuid("parent-uuid");
		parent.setName("Hospital");
		
		Location child = new Location();
		child.setUuid("child");
		child.setName("Ward");
		child.setParentLocation(parent);
		
		ExportLine line = export(child);
		
		assertEquals("parent-uuid", line.get("parent"),
		    "parent must be referenced by uuid so a retired parent (emitted as uuid-only) still resolves on import");
	}
	
	@Test
	void omitsParentWhenAbsent() {
		Location location = new Location();
		location.setUuid("loc");
		location.setName("Standalone");
		
		ExportLine line = export(location);
		
		assertNull(line.get("parent"));
	}
	
	@Test
	void retiredLocationEmitsUuidAndFlagOnly() {
		Location location = new Location();
		location.setUuid("old");
		location.setName("Closed Clinic");
		location.setRetired(true);
		
		ExportLine line = export(location);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
	}
	
	private static LocationTag namedTag(String name) {
		LocationTag tag = new LocationTag();
		tag.setName(name);
		return tag;
	}
}
