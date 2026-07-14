/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.flag;

import org.junit.jupiter.api.Test;
import org.openmrs.Role;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.patientflags.DisplayPoint;
import org.openmrs.module.patientflags.Tag;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FlagTagLineExporterTest {
	
	@Test
	void exportsAllFieldsForActiveTag() {
		Tag tag = new Tag();
		tag.setUuid("627bf278-ba81-4436-b867-c2f6641d060b");
		tag.setName("Clinical");
		tag.setDescription("General clinical flags");
		
		Role clinician = new Role();
		clinician.setRole("Clinician");
		Role nurse = new Role();
		nurse.setRole("Nurse");
		Set<Role> roles = new HashSet<>();
		roles.add(clinician);
		roles.add(nurse);
		tag.setRoles(roles);
		
		DisplayPoint summary = new DisplayPoint();
		summary.setName("Patient Summary");
		DisplayPoint dashboard = new DisplayPoint();
		dashboard.setName("Patient Dashboard");
		Set<DisplayPoint> displayPoints = new HashSet<>();
		displayPoints.add(summary);
		displayPoints.add(dashboard);
		tag.setDisplayPoints(displayPoints);
		
		ExportLine line = new ExportLine();
		new FlagTagLineExporter().writeLine(tag, line);
		
		assertEquals("627bf278-ba81-4436-b867-c2f6641d060b", line.get("uuid"));
		assertEquals("Clinical", line.get("name"));
		assertEquals("Clinician;Nurse", line.get("roles"));
		assertEquals("Patient Dashboard;Patient Summary", line.get("display points"));
		assertEquals("General clinical flags", line.get("description"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void retiredTagEmitsUuidAndFlagOnly() {
		Tag tag = new Tag();
		tag.setUuid("829bf278-ba81-4436-b867-c2f6641d060d");
		tag.setName("Deprecated");
		tag.setDescription("Deprecated flags");
		tag.setRetired(true);
		
		ExportLine line = new ExportLine();
		new FlagTagLineExporter().writeLine(tag, line);
		
		assertEquals("829bf278-ba81-4436-b867-c2f6641d060d", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
		assertNull(line.get("description"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void tagWithNoRolesOrDisplayPointsOmitsThoseColumns() {
		Tag tag = new Tag();
		tag.setUuid("526bf278-ba81-4436-b867-c2f6641d060a");
		tag.setName("HIV");
		tag.setDescription("Tags for HIV-related flags");
		tag.setRoles(new HashSet<>());
		tag.setDisplayPoints(new HashSet<>());
		
		ExportLine line = new ExportLine();
		new FlagTagLineExporter().writeLine(tag, line);
		
		assertEquals("HIV", line.get("name"));
		assertNull(line.get("roles"));
		assertNull(line.get("display points"));
	}
}
