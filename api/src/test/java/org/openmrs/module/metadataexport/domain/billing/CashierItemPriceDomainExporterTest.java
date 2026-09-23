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
import org.openmrs.OpenmrsObject;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.initializer.Domain;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CashierItemPriceDomainExporterTest {
	
	private final CashierItemPriceDomainExporter exporter = new CashierItemPriceDomainExporter();
	
	@Test
	void getDomainShouldContainCashierItemPriceDomain() {
		assertEquals(Domain.CASHIER_ITEM_PRICES, exporter.getDomain());
	}
	
	@Test
	void fileNameShouldReturnCashierItemPriceCsv() {
		assertEquals("cashierItemPrices.csv", exporter.fileName());
	}
	
	@Test
	void chainShouldContainCashierItemPriceLineExporter() {
		assertEquals(1, exporter.chain().size());
		assertTrue(exporter.chain().get(0) instanceof CashierItemPriceLineExporter);
	}
	
	@Test
	void shouldHandleOnlyCashierItemPriceDomain() {
		assertTrue(exporter.handles(new CashierItemPrice()));
		assertFalse(exporter.handles(new BillableService()));
	}
	
	@Test
	void shouldGetAllDependencies() {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("123e4567-e89b-12d3-a456-426614174000");
		
		BillableService billableService = new BillableService();
		billableService.setUuid("123e4567-e89b-12d3-a456-426614174001");
		
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setUuid("123e4567-e89b-12d3-a456-426614174003");
		cashierItemPrice.setPaymentMode(paymentMode);
		cashierItemPrice.setBillableService(billableService);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(cashierItemPrice);
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(billableService));
		assertTrue(dependencies.contains(paymentMode));
	}
	
	@Test
	void shouldGetOnlyGetPaymentModeDependency() {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("123e4567-e89b-12d3-a456-426614174000");
		
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setUuid("123e4567-e89b-12d3-a456-426614174003");
		cashierItemPrice.setPaymentMode(paymentMode);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(cashierItemPrice);
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(paymentMode));
	}
	
	@Test
	void shouldGetOnlyGetBillableServiceDependency() {
		BillableService billableService = new BillableService();
		billableService.setUuid("123e4567-e89b-12d3-a456-426614174001");
		
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setUuid("123e4567-e89b-12d3-a456-426614174003");
		cashierItemPrice.setBillableService(billableService);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(cashierItemPrice);
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(billableService));
	}
	
	@Test
	void shouldExcludeRetiredBillableServiceFromDependencies() {
		BillableService billableService = new BillableService();
		billableService.setUuid("123e4567-e89b-12d3-a456-426614174001");
		billableService.setRetired(true);
		
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setUuid("123e4567-e89b-12d3-a456-426614174003");
		cashierItemPrice.setBillableService(billableService);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(cashierItemPrice);
		assertEquals(0, dependencies.size());
		assertFalse(dependencies.contains(billableService));
	}
	
	@Test
	void shouldExcludeRetiredBillableServiceButRetainPaymentModeDependency() {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("123e4567-e89b-12d3-a456-426614174000");
		
		BillableService billableService = new BillableService();
		billableService.setUuid("123e4567-e89b-12d3-a456-426614174001");
		billableService.setRetired(true);
		
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setUuid("123e4567-e89b-12d3-a456-426614174003");
		cashierItemPrice.setPaymentMode(paymentMode);
		cashierItemPrice.setBillableService(billableService);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(cashierItemPrice);
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(paymentMode));
		assertFalse(dependencies.contains(billableService));
	}
}
