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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillableServiceDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private final BillableServiceDomainExporter domainExporter = new BillableServiceDomainExporter();
	
	@BeforeEach
	void seedOneRetiredAndOneNonRetiredBillableService() {
		BillableServiceService service = Context.getService(BillableServiceService.class);
		
		BillableService live = createBillableService(LIVE_UUID, "General Consultation", "Gen Con");
		service.saveBillableService(live);
		
		BillableService retired = createBillableService(RETIRED_UUID, "Discontinued Service", "Disc Serv");
		service.saveBillableService(retired);
		service.retireBillableService(retired, "Discontinued");
		
		Context.flushSession();
	}
	
	@Test
	void shouldGetBillableServiceAllInstances() {
		Collection<BillableService> billableServices = domainExporter.getAllInstances();
		
		assertNotNull(billableServices);
		assertEquals(2, billableServices.size());
		
		List<String> uuids = billableServices.stream().map(BillableService::getUuid).collect(Collectors.toList());
		assertTrue(uuids.contains(LIVE_UUID));
		assertTrue(uuids.contains(RETIRED_UUID));
	}
	
	private BillableService createBillableService(String uuid, String name, String shortName) {
		BillableService billableService = new BillableService();
		billableService.setUuid(uuid);
		billableService.setName(name);
		billableService.setShortName(shortName);
		billableService.setServiceStatus(BillableServiceStatus.ENABLED);
		return billableService;
	}
}
