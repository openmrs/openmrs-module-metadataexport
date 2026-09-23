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

import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.CashierItemPriceService;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@OpenmrsProfile(modules = "billing:2.4.0")
public class CashierItemPriceDomainExporter extends CsvDomainExporter<CashierItemPrice> {
	
	@Override
	protected List<BaseLineExporter<CashierItemPrice>> chain() {
		return Collections.singletonList(new CashierItemPriceLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "cashierItemPrices.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.CASHIER_ITEM_PRICES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof CashierItemPrice;
	}
	
	@Override
	public Collection<CashierItemPrice> getAllInstances() {
		return allPrices().stream().filter(p -> !isUnimportable(p)).collect(Collectors.toList());
	}
	
	@Override
	public Map<String, String> exclusions() {
		return DomainExporter.exclusions(allPrices(), CashierItemPriceDomainExporter::isUnimportable,
		    CashierItemPriceDomainExporter::unimportableReason);
	}
	
	private static boolean isUnimportable(CashierItemPrice p) {
		boolean noPaymentMode = p.getPaymentMode() == null;
		boolean hasItem = p.getItem() != null;
		boolean hasService = p.getBillableService() != null && !p.getBillableService().getRetired();
		boolean badItemServiceCombo = hasItem == hasService;
		return noPaymentMode || badItemServiceCombo;
	}
	
	private static String unimportableReason(CashierItemPrice p) {
		if (p.getPaymentMode() == null) {
			return "('" + p.getName() + "') has no payment mode and cannot be imported";
		}
		boolean hasItem = p.getItem() != null;
		boolean hasService = p.getBillableService() != null && !p.getBillableService().getRetired();
		if (hasItem && hasService) {
			return "('" + p.getName() + "') references both a stock item and a billable service; exactly one is required";
		}
		return "('" + p.getName() + "') references neither a stock item nor a billable service; exactly one is required";
	}
	
	private static List<CashierItemPrice> allPrices() {
		CashierItemPriceService cashierItemPriceService = Context.getService(CashierItemPriceService.class);
		return new ArrayList<>(cashierItemPriceService.getCashierItemPrices(true));
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(CashierItemPrice instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		if (instance.getPaymentMode() != null) {
			dependencies.add(instance.getPaymentMode());
		}
		if (instance.getBillableService() != null && !instance.getBillableService().getRetired()) {
			dependencies.add(instance.getBillableService());
		}
		return dependencies;
	}
}
