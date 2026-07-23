/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.ordertype;

import org.openmrs.OpenmrsObject;
import org.openmrs.OrderType;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class OrderTypeDomainExporter extends CsvDomainExporter<OrderType> {
	
	@Override
	protected List<BaseLineExporter<OrderType>> chain() {
		return Collections.singletonList(new OrderTypeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "orderTypes.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.ORDER_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof OrderType;
	}
	
	@Override
	public Collection<OrderType> getAllInstances() {
		return Context.getOrderService().getOrderTypes(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(OrderType orderType) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		if (orderType.getParent() != null) {
			dependencies.add(orderType.getParent());
		}
		dependencies.addAll(orderType.getConceptClasses());
		return dependencies;
	}
}
