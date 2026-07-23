/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.conceptreferencerange;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.OpenmrsObject;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConceptReferenceRangeDomainExporterTest extends BaseModuleContextSensitiveTest {
	
	private final ConceptReferenceRangeDomainExporter exporter = new ConceptReferenceRangeDomainExporter();
	
	@Test
	void getAllInstances_returnsEveryReferenceRangeFromTheDatabase() {
		Collection<ConceptReferenceRange> instances = exporter.getAllInstances();
		
		// The six rows seeded by the standard test dataset.
		assertEquals(6, instances.size());
		
		Set<String> uuids = instances.stream().map(ConceptReferenceRange::getUuid).collect(Collectors.toSet());
		assertTrue(uuids.contains("2c5972e8-aee5-468c-8216-369a1b60723d"), "expected the WEIGHT reference range");
	}
	
	@Test
	void getDependencies_includesConceptNumeric() {
		ConceptNumeric conceptNumeric = new ConceptNumeric();
		conceptNumeric.setUuid("weight-concept-uuid");
		
		ConceptReferenceRange range = new ConceptReferenceRange();
		range.setConceptNumeric(conceptNumeric);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(range);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(conceptNumeric), "referenced concept must be pulled into the closure");
	}
}
