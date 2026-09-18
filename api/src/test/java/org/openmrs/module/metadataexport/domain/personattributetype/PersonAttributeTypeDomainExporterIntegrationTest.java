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
import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonAttributeTypeDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String CIVIL_STATUS_UUID = "a0f5521c-dbbd-4c10-81b2-1b7ab18330df";
	
	private final PersonAttributeTypeDomainExporter exporter = new PersonAttributeTypeDomainExporter();
	
	@Test
	void getDependencies_includesForeignConceptOfConceptFormat() {
		Concept concept = Context.getConceptService().getConcept(4);
		PersonAttributeType type = Context.getPersonService().getPersonAttributeTypeByUuid(CIVIL_STATUS_UUID);
		type.setForeignKey(concept.getConceptId());
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(type);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(concept));
	}
}
