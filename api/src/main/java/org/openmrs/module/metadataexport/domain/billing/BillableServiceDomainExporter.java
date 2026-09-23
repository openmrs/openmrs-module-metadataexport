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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Component
@OpenmrsProfile(modules = "billing:2.4.0")
public class BillableServiceDomainExporter extends CsvDomainExporter<BillableService> {
	
	@Override
	protected List<BaseLineExporter<BillableService>> chain() {
		return Collections.singletonList(new BillableServiceLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "billableServices.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.BILLABLE_SERVICES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof BillableService;
	}
	
	@Override
	public Collection<BillableService> getAllInstances() {
		List<BillableService> live = new ArrayList<>();
		for (BillableService service : allServices()) {
			if (BooleanUtils.isTrue(service.getRetired())) {
				log.warn(
				    "BillableServices: skipping retired service {} ({}); BillableService.getId() unboxes a"
				            + " primitive int so Initializer's shouldFill is never true on a void/retire row,"
				            + " causing an empty BillableService to be saved on a fresh target",
				    service.getUuid(), service.getName());
			} else {
				live.add(service);
			}
		}
		return live;
	}
	
	@Override
	public Map<String, String> exclusions() {
		return DomainExporter.exclusions(allServices(), BillableServiceDomainExporter::isRetired, service -> "('"
		        + service.getName()
		        + "') is retired, and Initializer cannot import a retired billable service (unretire them on this server or remove them from the package)");
	}
	
	private static boolean isRetired(BillableService service) {
		return BooleanUtils.isTrue(service.getRetired());
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(BillableService instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		if (instance.getConcept() != null) {
			dependencies.add(instance.getConcept());
		}
		if (instance.getServiceType() != null) {
			dependencies.add(instance.getServiceType());
		}
		return dependencies;
	}
	
	private static List<BillableService> allServices() {
		BillableServiceService billableServiceService = Context.getService(BillableServiceService.class);
		return new ArrayList<>(billableServiceService
		        .getBillableServices(new BillableServiceSearch(null, null, null, null, null, true), null));
	}
}
