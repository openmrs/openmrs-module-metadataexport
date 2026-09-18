/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.personattributetype;

import org.junit.jupiter.api.Test;
import org.openmrs.OpenmrsObject;
import org.openmrs.PersonAttributeType;
import org.openmrs.Privilege;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plain unit tests: there is no OpenMRS context here, so any path that reaches the ConceptService
 * fails. That is deliberate for the non-concept cases below, which must not resolve the foreign key
 * at all. The concept-format case needs a context and lives in
 * {@link PersonAttributeTypeDomainExporterIntegrationTest}.
 */
class PersonAttributeTypeDomainExporterTest {
	
	private final PersonAttributeTypeDomainExporter exporter = new PersonAttributeTypeDomainExporter();
	
	@Test
	void getDependencies_includesEditPrivilege() {
		Privilege privilege = new Privilege("Edit Birthplace");
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("java.lang.String");
		type.setEditPrivilege(privilege);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(type);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(privilege));
	}
	
	@Test
	void getDependencies_ignoresForeignKeyOfNonConceptFormat() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.Location");
		type.setForeignKey(42);
		
		assertTrue(exporter.getDependencies(type).isEmpty());
	}
	
	@Test
	void getDependencies_ignoresConceptFormatWithoutForeignKey() {
		PersonAttributeType type = new PersonAttributeType();
		type.setFormat("org.openmrs.Concept");
		
		assertTrue(exporter.getDependencies(type).isEmpty());
	}
}
