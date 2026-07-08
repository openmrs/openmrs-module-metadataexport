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
import org.openmrs.OpenmrsObject;
import org.openmrs.Privilege;
import org.openmrs.Role;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleDomainExporterDependenciesTest {
	
	private final RoleDomainExporter exporter = new RoleDomainExporter();
	
	@Test
	void getDependencies_includesInheritedRolesAndPrivileges() {
		Role clinician = new Role("Clinician");
		Role nurse = new Role("Nurse");
		Privilege view = new Privilege("View Patients");
		Privilege edit = new Privilege("Edit Patients");
		clinician.setInheritedRoles(new HashSet<>(Collections.singletonList(nurse)));
		clinician.setPrivileges(new HashSet<>(Arrays.asList(view, edit)));
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(clinician);
		
		assertEquals(3, dependencies.size());
		assertTrue(dependencies.contains(nurse));
		assertTrue(dependencies.contains(view) && dependencies.contains(edit));
	}
	
	@Test
	void getDependencies_isEmptyForRoleWithNoInheritanceOrPrivileges() {
		Role loner = new Role("Loner");
		loner.setInheritedRoles(new HashSet<>());
		loner.setPrivileges(new HashSet<>());
		
		assertTrue(exporter.getDependencies(loner).isEmpty());
	}
}
