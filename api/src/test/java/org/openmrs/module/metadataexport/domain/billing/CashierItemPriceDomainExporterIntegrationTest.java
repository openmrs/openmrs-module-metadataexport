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
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.CashierItemPriceService;
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.stockmanagement.api.StockManagementService;
import org.openmrs.module.stockmanagement.api.model.StockItem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CashierItemPriceDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String VALID_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String VALID_PM_UUID = "a1b2c3d4-1111-1111-1111-000000000001";
	
	private static final String VALID_SVC_UUID = "a1b2c3d4-1111-1111-1111-000000000002";
	
	private static final String NO_PAYMENT_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private static final String NO_PAYMENT_SVC_UUID = "a1b2c3d4-2222-2222-2222-000000000001";
	
	private static final String BOTH_SET_UUID = "7e3f4d5a-3f10-11e4-adec-0800271c1b75";
	
	private static final String BOTH_SET_PM_UUID = "a1b2c3d4-3333-3333-3333-000000000001";
	
	private static final String BOTH_SET_SVC_UUID = "a1b2c3d4-3333-3333-3333-000000000002";
	
	private static final String BOTH_SET_SI_UUID = "a1b2c3d4-3333-3333-3333-000000000003";
	
	private static final String NEITHER_SET_UUID = "9b2a6c1e-3f10-11e4-adec-0800271c1b75";
	
	private static final String NEITHER_SET_PM_UUID = "a1b2c3d4-4444-4444-4444-000000000001";
	
	private static final String RETIRED_SVC_UUID = "b4f6a1c2-3d10-11e4-adec-0800271c1b99";
	
	private static final String RETIRED_SVC_PM_UUID = "a1b2c3d4-5555-5555-5555-000000000001";
	
	private static final String RETIRED_SVC_SVC_UUID = "a1b2c3d4-5555-5555-5555-000000000002";
	
	private final CashierItemPriceDomainExporter exporter = new CashierItemPriceDomainExporter();
	
	@Test
	void getAllInstances_includesValidPrice() {
		seedValidPrice(VALID_UUID, "valid-price");
		
		Collection<CashierItemPrice> itemPrices = exporter.getAllInstances();
		
		assertNotNull(itemPrices);
		assertEquals(1, itemPrices.size());
		assertEquals(VALID_UUID, itemPrices.iterator().next().getUuid());
	}
	
	@Test
	void getAllInstances_excludesPriceWithNoPaymentMode() {
		seedValidPrice(VALID_UUID, "valid-price");
		seedPriceWithNoPaymentMode(NO_PAYMENT_UUID, "no-payment-price");
		
		Collection<CashierItemPrice> itemPrices = exporter.getAllInstances();
		
		assertEquals(1, itemPrices.size());
		assertEquals(VALID_UUID, itemPrices.iterator().next().getUuid());
	}
	
	@Test
	void getAllInstances_excludesPriceWithBothStockItemAndBillableService() {
		seedValidPrice(VALID_UUID, "valid-price");
		seedPriceWithBothItemAndService(BOTH_SET_UUID, "both-set-price");
		
		Collection<CashierItemPrice> itemPrices = exporter.getAllInstances();
		
		assertEquals(1, itemPrices.size());
		assertEquals(VALID_UUID, itemPrices.iterator().next().getUuid());
	}
	
	@Test
	void getAllInstances_excludesPriceWithNeitherStockItemNorBillableService() {
		seedValidPrice(VALID_UUID, "valid-price");
		seedPriceWithNeitherItemNorService(NEITHER_SET_UUID, "neither-set-price");
		
		Collection<CashierItemPrice> itemPrices = exporter.getAllInstances();
		
		assertEquals(1, itemPrices.size());
		assertEquals(VALID_UUID, itemPrices.iterator().next().getUuid());
	}
	
	@Test
	void getAllInstances_excludesPriceWhoseBillableServiceIsRetired() {
		seedValidPrice(VALID_UUID, "valid-price");
		seedPriceWithRetiredBillableService(RETIRED_SVC_UUID, "retired-svc-price");
		
		Collection<CashierItemPrice> itemPrices = exporter.getAllInstances();
		
		assertNotNull(itemPrices);
		assertEquals(1, itemPrices.size());
		assertEquals(VALID_UUID, itemPrices.iterator().next().getUuid());
	}
	
	@Test
	void exclusions_isEmptyWhenAllPricesAreImportable() {
		seedValidPrice(VALID_UUID, "valid-price");
		
		Map<String, String> exclusions = exporter.exclusions();
		
		assertTrue(exclusions.isEmpty());
	}
	
	@Test
	void exclusions_containsPriceWithNoPaymentModeWithReason() {
		seedPriceWithNoPaymentMode(NO_PAYMENT_UUID, "no-payment-price");
		
		Map<String, String> exclusions = exporter.exclusions();
		
		assertEquals(1, exclusions.size());
		assertTrue(exclusions.containsKey(NO_PAYMENT_UUID));
		assertTrue(exclusions.get(NO_PAYMENT_UUID).contains("no payment mode"));
	}
	
	@Test
	void exclusions_containsPriceWithBothSetWithReason() {
		seedPriceWithBothItemAndService(BOTH_SET_UUID, "both-set-price");
		
		Map<String, String> exclusions = exporter.exclusions();
		
		assertEquals(1, exclusions.size());
		assertTrue(exclusions.containsKey(BOTH_SET_UUID));
		assertTrue(exclusions.get(BOTH_SET_UUID).contains("both a stock item and a billable service"));
	}
	
	@Test
	void exclusions_containsPriceWithNeitherSetWithReason() {
		seedPriceWithNeitherItemNorService(NEITHER_SET_UUID, "neither-set-price");
		
		Map<String, String> exclusions = exporter.exclusions();
		
		assertEquals(1, exclusions.size());
		assertTrue(exclusions.containsKey(NEITHER_SET_UUID));
		assertTrue(exclusions.get(NEITHER_SET_UUID).contains("neither a stock item nor a billable service"));
	}
	
	@Test
	void exclusions_containsAllInvalidPricesAndNoValidOnes() {
		seedValidPrice(VALID_UUID, "valid-price");
		seedPriceWithNoPaymentMode(NO_PAYMENT_UUID, "no-payment-price");
		seedPriceWithNeitherItemNorService(NEITHER_SET_UUID, "neither-set-price");
		
		Map<String, String> exclusions = exporter.exclusions();
		
		assertEquals(2, exclusions.size());
		assertTrue(exclusions.containsKey(NO_PAYMENT_UUID));
		assertTrue(exclusions.containsKey(NEITHER_SET_UUID));
		assertFalse(exclusions.containsKey(VALID_UUID));
	}
	
	@Test
	void shouldGetEmptyCashierItemPricesIfAllInstancesEmpty() {
		Collection<CashierItemPrice> itemPrices = exporter.getAllInstances();
		assertEquals(0, itemPrices.size());
	}
	
	private void seedValidPrice(String uuid, String name) {
		PaymentMode paymentMode = savePaymentMode(VALID_PM_UUID, "Mode for " + name);
		BillableService service = saveLiveBillableService(VALID_SVC_UUID, "Service for " + name);
		
		CashierItemPrice price = createCashierItemPrice(uuid, name, new BigDecimal("50.00"));
		price.setPaymentMode(paymentMode);
		price.setBillableService(service);
		getCashierItemPriceService().saveCashierItemPrice(price);
	}
	
	private void seedPriceWithNoPaymentMode(String uuid, String name) {
		BillableService service = saveLiveBillableService(NO_PAYMENT_SVC_UUID, "Service for " + name);
		
		CashierItemPrice price = createCashierItemPrice(uuid, name, new BigDecimal("50.00"));
		price.setBillableService(service);
		getCashierItemPriceService().saveCashierItemPrice(price);
	}
	
	private void seedPriceWithBothItemAndService(String uuid, String name) {
		PaymentMode paymentMode = savePaymentMode(BOTH_SET_PM_UUID, "Mode for " + name);
		StockItem stockItem = saveStockItem(BOTH_SET_SI_UUID);
		BillableService service = saveLiveBillableService(BOTH_SET_SVC_UUID, "Service for " + name);
		
		CashierItemPrice price = createCashierItemPrice(uuid, name, new BigDecimal("50.00"));
		price.setPaymentMode(paymentMode);
		price.setItem(stockItem);
		price.setBillableService(service);
		getCashierItemPriceService().saveCashierItemPrice(price);
	}
	
	private void seedPriceWithNeitherItemNorService(String uuid, String name) {
		PaymentMode paymentMode = savePaymentMode(NEITHER_SET_PM_UUID, "Mode for " + name);
		
		CashierItemPrice price = createCashierItemPrice(uuid, name, new BigDecimal("50.00"));
		price.setPaymentMode(paymentMode);
		getCashierItemPriceService().saveCashierItemPrice(price);
	}
	
	private void seedPriceWithRetiredBillableService(String uuid, String name) {
		PaymentMode paymentMode = savePaymentMode(RETIRED_SVC_PM_UUID, "Mode for " + name);
		
		BillableServiceService billableServiceService = Context.getService(BillableServiceService.class);
		BillableService retiredService = new BillableService();
		retiredService.setUuid(RETIRED_SVC_SVC_UUID);
		retiredService.setName("Retired service for " + name);
		billableServiceService.saveBillableService(retiredService);
		billableServiceService.retireBillableService(retiredService, "Discontinued");
		
		CashierItemPrice price = createCashierItemPrice(uuid, name, new BigDecimal("50.00"));
		price.setPaymentMode(paymentMode);
		price.setBillableService(retiredService);
		getCashierItemPriceService().saveCashierItemPrice(price);
	}
	
	private PaymentMode savePaymentMode(String uuid, String name) {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid(uuid);
		paymentMode.setName(name);
		Context.getService(PaymentModeService.class).savePaymentMode(paymentMode);
		return paymentMode;
	}
	
	private BillableService saveLiveBillableService(String uuid, String name) {
		BillableService service = new BillableService();
		service.setUuid(uuid);
		service.setName(name);
		Context.getService(BillableServiceService.class).saveBillableService(service);
		return service;
	}
	
	private StockItem saveStockItem(String uuid) {
		StockItem stockItem = new StockItem();
		stockItem.setUuid(uuid);
		stockItem.setIsDrug(false);
		Context.getService(StockManagementService.class).saveStockItem(stockItem);
		return stockItem;
	}
	
	private CashierItemPrice createCashierItemPrice(String uuid, String name, BigDecimal price) {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setUuid(uuid);
		cashierItemPrice.setName(name);
		cashierItemPrice.setPrice(price);
		return cashierItemPrice;
	}
	
	private CashierItemPriceService getCashierItemPriceService() {
		return Context.getService(CashierItemPriceService.class);
	}
}
