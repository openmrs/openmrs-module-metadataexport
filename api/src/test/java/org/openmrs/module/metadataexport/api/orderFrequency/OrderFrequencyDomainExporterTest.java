/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.orderFrequency;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.OrderFrequency;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderFrequencyDomainExporterTest {
	
	private final OrderFrequencyDomainExporter exporter = new OrderFrequencyDomainExporter();
	
	@Test
	void getDependencies_includesConcept() {
		Concept concept = new Concept();
		concept.setUuid("hourly-concept-uuid");
		
		OrderFrequency frequency = new OrderFrequency();
		frequency.setConcept(concept);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(frequency);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(concept), "referenced concept must be pulled into the closure");
	}
	
	@Test
	void getDependencies_emptyWhenNoConcept() {
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(new OrderFrequency());
		
		assertTrue(dependencies.isEmpty());
	}
}
