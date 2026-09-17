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
import org.openmrs.module.billing.api.CashierItemPriceService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CashierItemPriceDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private final CashierItemPriceDomainExporter exporter = new CashierItemPriceDomainExporter();
	
	@Test
	void shouldGetCashierItemPricesAllInstances() {
		seedOneRetiredAndOneNonRetiredCashierItemPrice();
		
		Collection<CashierItemPrice> itemPrices = exporter.getAllInstances();
		
		assertNotNull(itemPrices);
		assertEquals(2, itemPrices.size());
		
		List<String> uuids = itemPrices.stream().map(CashierItemPrice::getUuid).collect(Collectors.toList());
		assertTrue(uuids.contains(LIVE_UUID));
		assertTrue(uuids.contains(RETIRED_UUID));
	}
	
	@Test
	void shouldGetEmptyCashierItemPricesIfAllInstancesEmpty() {
		Collection<CashierItemPrice> itemPrices = exporter.getAllInstances();
		assertEquals(0, itemPrices.size());
	}
	
	private void seedOneRetiredAndOneNonRetiredCashierItemPrice() {
		CashierItemPriceService itemPriceService = Context.getService(CashierItemPriceService.class);
		
		CashierItemPrice live = createCashierItemPrice(LIVE_UUID, "name1", new BigDecimal("123.45"));
		itemPriceService.saveCashierItemPrice(live);
		
		CashierItemPrice retired = createCashierItemPrice(RETIRED_UUID, "name2", new BigDecimal("123.45"));
		itemPriceService.saveCashierItemPrice(retired);
	}
	
	private CashierItemPrice createCashierItemPrice(String uuid, String name, BigDecimal price) {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setUuid(uuid);
		cashierItemPrice.setName(name);
		cashierItemPrice.setPrice(price);
		
		return cashierItemPrice;
	}
}
