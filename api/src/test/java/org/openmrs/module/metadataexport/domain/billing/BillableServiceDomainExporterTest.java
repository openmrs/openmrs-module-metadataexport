/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.billing;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.initializer.Domain;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillableServiceDomainExporterTest {
	
	private BillableServiceDomainExporter exporter = new BillableServiceDomainExporter();
	
	@Test
	void getDomainShouldContainBillableServiceDomain() {
		assertEquals(Domain.BILLABLE_SERVICES, exporter.getDomain());
	}
	
	@Test
	void fileNameShouldReturnBillableServiceCsv() {
		assertEquals("billableService.csv", exporter.fileName());
	}
	
	@Test
	void chainShouldContainBillableServiceLineExporter() {
		assertEquals(1, exporter.chain().size());
		assertTrue(exporter.chain().get(0) instanceof BillableServiceLineExporter);
	}
	
	@Test
	void shouldHandleOnlyBillableServiceDomain() {
		assertTrue(exporter.handles(new BillableService()));
		assertFalse(exporter.handles(new PaymentMode()));
		assertFalse(exporter.handles(null));
	}
	
	@Test
	void shouldGetAllDependencies() {
		Concept concept = new Concept();
		concept.setUuid("123e4567-e89b-12d3-a456-426614174000");
		
		Concept serviceType = new Concept();
		serviceType.setUuid("123e4567-e89b-12d3-a456-426614175100");
		
		Concept serviceCategory = new Concept();
		serviceCategory.setUuid("323e4567-e89b-12d3-a456-426614174010");
		
		BillableService billableService = new BillableService();
		billableService.setConcept(concept);
		billableService.setServiceType(serviceType);
		billableService.setServiceCategory(serviceCategory);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(billableService);
		assertEquals(3, dependencies.size());
		assertTrue(dependencies.contains(concept));
		assertTrue(dependencies.contains(serviceType));
		assertTrue(dependencies.contains(serviceCategory));
	}
	
	@Test
	void shouldGetOnlyConceptDependency() {
		Concept concept = new Concept();
		concept.setUuid("c1d8a345-3f10-11e4-adec-0800271c1b75");
		
		BillableService billableService = new BillableService();
		billableService.setConcept(concept);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(billableService);
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(concept));
	}
	
	@Test
	void shouldGetOnlyServiceTypeDependency() {
		Concept serviceType = new Concept();
		serviceType.setUuid("123e4567-e89b-12d3-a456-426614175100");
		
		BillableService billableService = new BillableService();
		billableService.setServiceType(serviceType);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(billableService);
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(serviceType));
	}
	
	@Test
	void shouldGetOnlyServiceCategoryDependency() {
		Concept serviceCategory = new Concept();
		serviceCategory.setUuid("323e4567-e89b-12d3-a456-426614174010");
		
		BillableService billableService = new BillableService();
		billableService.setServiceCategory(serviceCategory);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(billableService);
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(serviceCategory));
	}
	
	@Test
	void shouldReturnEmptyDependenciesWhenNoneConfigured() {
		BillableService billableService = new BillableService();
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(billableService);
		assertTrue(dependencies.isEmpty());
	}
}
