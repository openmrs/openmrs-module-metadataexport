/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.flag;

import org.junit.jupiter.api.Test;
import org.openmrs.OpenmrsObject;
import org.openmrs.Role;
import org.openmrs.module.patientflags.Tag;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlagTagDomainExporterTest {
	
	private final FlagTagDomainExporter exporter = new FlagTagDomainExporter();
	
	@Test
	void getDependencies_includesRoles() {
		Role clinician = new Role("Clinician");
		Role nurse = new Role("Nurse");
		Set<Role> roles = new HashSet<>();
		roles.add(clinician);
		roles.add(nurse);
		Tag tag = new Tag();
		tag.setRoles(roles);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(tag);
		
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(clinician));
		assertTrue(dependencies.contains(nurse));
	}
	
	@Test
	void getDependencies_isEmptyWithoutRoles() {
		Tag tag = new Tag();
		tag.setRoles(new HashSet<>());
		
		assertTrue(exporter.getDependencies(tag).isEmpty());
	}
}
