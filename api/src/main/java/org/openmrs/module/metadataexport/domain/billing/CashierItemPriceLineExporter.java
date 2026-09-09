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

import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class CashierItemPriceLineExporter extends MetadataLineExporter<CashierItemPrice> {
	
	public static final String HEADER_PRICE = "Price";
	
	public static final String HEADER_PAYMENT_MODE = "Payment Mode";
	
	public static final String HEADER_STOCK_ITEM = "Stock Item";
	
	public static final String HEADER_BILLABLE_SERVICE = "Billable Service";
	
	@Override
	public void export(CashierItemPrice itemPrice, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, itemPrice.getName());
		line.put(HEADER_PRICE, itemPrice.getPrice());
		
		if (itemPrice.getPaymentMode() != null) {
			line.put(HEADER_PAYMENT_MODE, itemPrice.getPaymentMode().getUuid());
		}
		if (itemPrice.getItem() != null) {
			line.put(HEADER_STOCK_ITEM, itemPrice.getItem().getUuid());
		}
		if (itemPrice.getBillableService() != null) {
			line.put(HEADER_BILLABLE_SERVICE, itemPrice.getBillableService().getUuid());
		}
	}
}
