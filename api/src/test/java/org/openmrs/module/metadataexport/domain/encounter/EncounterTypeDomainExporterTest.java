/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.encounter;

import org.junit.jupiter.api.Test;
import org.openmrs.EncounterType;
import org.openmrs.OpenmrsObject;
import org.openmrs.Privilege;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncounterTypeDomainExporterTest {
	
	private final EncounterTypeDomainExporter exporter = new EncounterTypeDomainExporter();
	
	@Test
	void getDependencies_includesViewAndEditPrivileges() {
		Privilege view = new Privilege("View Encounters");
		Privilege edit = new Privilege("Edit Encounters");
		EncounterType type = new EncounterType();
		type.setViewPrivilege(view);
		type.setEditPrivilege(edit);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(type);
		
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(view));
		assertTrue(dependencies.contains(edit));
	}
	
	@Test
	void getDependencies_isEmptyWithoutPrivileges() {
		assertTrue(exporter.getDependencies(new EncounterType()).isEmpty());
	}
}
