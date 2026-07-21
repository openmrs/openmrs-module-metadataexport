/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.concept;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.OpenmrsObject;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConceptSetDomainExporterTest extends BaseModuleContextSensitiveTest {
	
	private final ConceptSetDomainExporter exporter = new ConceptSetDomainExporter();
	
	private static Concept concept(String uuid) {
		Concept concept = new Concept();
		concept.setUuid(uuid);
		return concept;
	}
	
	@Test
	void getAllInstances_returnsEverySetMembershipFromTheDatabase() {
		Collection<ConceptSet> instances = exporter.getAllInstances();
		
		// The nine concept_set rows seeded by the standard test dataset.
		assertEquals(9, instances.size());
		
		Set<String> uuids = instances.stream().map(ConceptSet::getUuid).collect(Collectors.toSet());
		assertTrue(uuids.contains("1a111827-639f-4cb4-961f-1e025bf88d90"), "expected the seeded set membership");
	}
	
	@Test
	void getDependencies_includesBothParentSetAndMember() {
		Concept parentSet = concept("parent-set-uuid");
		Concept member = concept("member-uuid");
		
		ConceptSet conceptSet = new ConceptSet(member, 1.0);
		conceptSet.setConceptSet(parentSet);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(conceptSet);
		
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(parentSet), "parent set concept must be pulled into the closure");
		assertTrue(dependencies.contains(member), "member concept must be pulled into the closure");
	}
}
