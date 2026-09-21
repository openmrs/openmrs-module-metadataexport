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
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BillableServiceDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private static final String UNKNOWN_UUID = "7e3f4d5a-3f10-11e4-adec-0800271c1b75";
	
	private final BillableServiceDomainExporter exporter = new BillableServiceDomainExporter();
	
	@BeforeEach
	void seedBillableServices() {
		BillableServiceService service = Context.getService(BillableServiceService.class);
		
		service.saveBillableService(createBillableService(LIVE_UUID, "General Consultation", "Gen Con"));
		
		BillableService retired = createBillableService(RETIRED_UUID, "Discontinued Service", "Disc Serv");
		service.saveBillableService(retired);
		service.retireBillableService(retired, "Discontinued");
		
		Context.flushSession();
	}
	
	@Test
	void getAllInstances_excludesRetiredServices() {
		Collection<BillableService> instances = exporter.getAllInstances();
		
		assertEquals(1, instances.size());
		assertEquals(LIVE_UUID, instances.iterator().next().getUuid());
	}
	
	@Test
	void getInstancesByUuids_returnsALiveRow() {
		Collection<BillableService> found = exporter.getInstancesByUuids(Collections.singletonList(LIVE_UUID));
		
		assertEquals(1, found.size());
		assertEquals(LIVE_UUID, found.iterator().next().getUuid());
	}
	
	@Test
	void getInstancesByUuids_reportsARetiredRowAsNotImportableRatherThanUnknown() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Collections.singletonList(RETIRED_UUID)));
		
		assertTrue(e.getMessage().contains("retired"), "a retired row must not be reported as unknown");
		assertTrue(e.getMessage().contains(RETIRED_UUID));
		assertFalse(e.getMessage().contains("Unknown uuids"));
	}
	
	@Test
	void getInstancesByUuids_stillReportsUnknownUuids() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Collections.singletonList(UNKNOWN_UUID)));
		
		assertTrue(e.getMessage().contains("Unknown uuids"));
		assertTrue(e.getMessage().contains(UNKNOWN_UUID));
	}
	
	@Test
	void getInstancesByUuids_reportsRetiredAndUnknownUuidsInOneMessage() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Arrays.asList(LIVE_UUID, RETIRED_UUID, UNKNOWN_UUID)));
		
		assertTrue(e.getMessage().contains(RETIRED_UUID), "the retired uuid must be reported");
		assertTrue(e.getMessage().contains(UNKNOWN_UUID), "the unknown uuid must be reported in the same round");
		assertFalse(e.getMessage().contains(LIVE_UUID), "a resolvable uuid is not a problem");
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
