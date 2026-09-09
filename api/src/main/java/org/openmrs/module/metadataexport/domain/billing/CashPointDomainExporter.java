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
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@OpenmrsProfile(modules = "billing:2.4.0")
public class CashPointDomainExporter extends CsvDomainExporter<CashPoint> {
	
	@Override
	protected List<BaseLineExporter<CashPoint>> chain() {
		return Collections.singletonList(new CashPointLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "cashPoints.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.CASH_POINTS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof CashPoint;
	}
	
	@Override
	public Collection<CashPoint> getAllInstances() {
		CashPointService cashPointService = Context.getService(CashPointService.class);
		return cashPointService.getAllCashPoints(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(CashPoint instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		if (instance.getLocation() != null) {
			dependencies.add(instance.getLocation());
		}
		return dependencies;
	}
}
