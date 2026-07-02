/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.role;

import org.junit.jupiter.api.Test;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RoleLineExporterTest {
	
	@Test
	void exportsUuidNameDescriptionInheritedRolesAndPrivileges() {
		Role role = new Role("Organizational: Doctor", "Doctor role");
		role.setUuid("d2fcb604-2700-102b-80cb-0017a47871b2");
		
		Set<Role> inherited = new HashSet<>();
		inherited.add(new Role("Application: Records Allergies"));
		inherited.add(new Role("Application: Uses Patient Summary"));
		role.setInheritedRoles(inherited);
		
		Set<Privilege> privileges = new HashSet<>();
		privileges.add(new Privilege("Add Allergies"));
		privileges.add(new Privilege("Add Patient"));
		role.setPrivileges(privileges);
		
		ExportLine line = new ExportLine();
		new RoleLineExporter().writeLine(role, line);
		
		assertEquals("d2fcb604-2700-102b-80cb-0017a47871b2", line.get("uuid"));
		assertEquals("Organizational: Doctor", line.get("Role name"));
		assertEquals("Doctor role", line.get("description"));
		assertEquals("Application: Records Allergies; Application: Uses Patient Summary", line.get("Inherited roles"));
		assertEquals("Add Allergies; Add Patient", line.get("Privileges"));
	}
	
	@Test
	void roleWithNoInheritedRolesOrPrivilegesOmitsThoseColumns() {
		Role role = new Role("Organizational: Nurse", "Nurse role");
		role.setUuid("abcf7209-d218-4572-8893-25c4b5b71934");
		role.setInheritedRoles(new HashSet<>());
		role.setPrivileges(new HashSet<>());
		
		ExportLine line = new ExportLine();
		new RoleLineExporter().export(role, line);
		
		assertEquals("Organizational: Nurse", line.get("Role name"));
		assertNull(line.get("Inherited roles"));
		assertNull(line.get("Privileges"));
	}
	
	@Test
	void retiredRoleEmitsUuidAndFlagOnly() {
		Role role = new Role("Organizational: Doctor", "Doctor role");
		role.setUuid("d2fcb604-2700-102b-80cb-0017a47871b2");
		role.setRetired(true);
		
		ExportLine line = new ExportLine();
		new RoleLineExporter().writeLine(role, line);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("Role name"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void roleWithSinglePrivilegeProducesNoSemicolon() {
		Role role = new Role("Organizational: Clerk");
		role.setUuid("some-uuid");
		role.setInheritedRoles(new HashSet<>());
		
		Set<Privilege> privileges = new HashSet<>();
		privileges.add(new Privilege("Add Orders"));
		role.setPrivileges(privileges);
		
		ExportLine line = new ExportLine();
		new RoleLineExporter().export(role, line);
		
		assertEquals("Add Orders", line.get("Privileges"));
	}
}
