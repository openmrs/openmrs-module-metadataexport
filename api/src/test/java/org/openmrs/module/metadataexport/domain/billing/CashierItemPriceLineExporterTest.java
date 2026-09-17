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
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.stockmanagement.api.model.StockItem;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CashierItemPriceLineExporterTest {
	
	@Test
	void exportAllColumns() {
		
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("123e4567-e89b-12d3-a456-426614174000");
		
		StockItem stockItem = new StockItem();
		stockItem.setUuid("123e4567-e89b-12d3-a456-4266141740101");
		
		BillableService billableService = new BillableService();
		billableService.setUuid("123e4567-e89b-12d3-a456-4266141740141");
		
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setUuid("123e4567-e89b-12d3-a456-4266141740102");
		cashierItemPrice.setName("test-name");
		BigDecimal price = new BigDecimal("100.00");
		cashierItemPrice.setPrice(price);
		cashierItemPrice.setPaymentMode(paymentMode);
		cashierItemPrice.setItem(stockItem);
		cashierItemPrice.setBillableService(billableService);
		
		ExportLine line = new ExportLine();
		new CashierItemPriceLineExporter().writeLine(cashierItemPrice, line);
		
		assertEquals("123e4567-e89b-12d3-a456-4266141740102", line.get("uuid"));
		assertEquals("test-name", line.get("name"));
		assertEquals("100.00", line.get("Price"));
		assertEquals(paymentMode.getUuid(), line.get("Payment Mode"));
		assertEquals(stockItem.getUuid(), line.get("Stock Item"));
		assertEquals(billableService.getUuid(), line.get("Billable Service"));
	}
	
	@Test
	void omitsOptionalColumnsWhenNull() {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setUuid("123e4567-e89b-12d3-a456-4266141740102");
		cashierItemPrice.setName("test-name");
		BigDecimal price = new BigDecimal("100.00");
		cashierItemPrice.setPrice(price);
		
		ExportLine line = new ExportLine();
		new CashierItemPriceLineExporter().writeLine(cashierItemPrice, line);
		
		assertEquals("123e4567-e89b-12d3-a456-4266141740102", line.get("uuid"));
		assertEquals("test-name", line.get("name"));
		assertEquals("100.00", line.get("Price"));
		assertNull(line.get("Payment Mode"));
		assertNull(line.get("Stock Item"));
		assertNull(line.get("Billable Service"));
	}
	
	@Test
	void exportsRetiredInstanceWithVoidRetireFlagOnly() {
		CashierItemPrice cashierItemPrice = new CashierItemPrice();
		cashierItemPrice.setUuid("123e4567-e89b-12d3-a456-4266141740102");
		cashierItemPrice.setName("test-name");
		BigDecimal price = new BigDecimal("100.00");
		cashierItemPrice.setPrice(price);
		cashierItemPrice.setRetired(true);
		
		ExportLine line = new ExportLine();
		new CashierItemPriceLineExporter().writeLine(cashierItemPrice, line);
		
		assertEquals("123e4567-e89b-12d3-a456-4266141740102", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"));
		assertNull(line.get("Price"));
		assertNull(line.get("Payment Mode"));
		assertNull(line.get("Stock Item"));
		assertNull(line.get("Billable Service"));
	}
}
