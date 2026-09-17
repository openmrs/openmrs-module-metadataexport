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
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CashPointDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private final CashPointDomainExporter exporter = new CashPointDomainExporter();
	
	@Test
	void shouldGetCashPointAllInstances() {
		seedOneRetiredAndOneNonRetiredCashPoint();
		
		Collection<CashPoint> cashPoints = exporter.getAllInstances();
		
		assertNotNull(cashPoints);
		assertEquals(2, cashPoints.size());
		
		List<String> uuids = cashPoints.stream().map(CashPoint::getUuid).collect(Collectors.toList());
		assertTrue(uuids.contains(LIVE_UUID));
		assertTrue(uuids.contains(RETIRED_UUID));
	}
	
	@Test
	void shouldGetEmptyCashPointsIfAllInstancesEmpty() {
		Collection<CashPoint> cashPoints = exporter.getAllInstances();
		assertEquals(0, cashPoints.size());
	}
	
	private void seedOneRetiredAndOneNonRetiredCashPoint() {
		CashPointService service = Context.getService(CashPointService.class);
		
		CashPoint live = createCashPoint(LIVE_UUID, "Main Desk");
		service.saveCashPoint(live);
		
		CashPoint retired = createCashPoint(RETIRED_UUID, "Old Desk");
		service.saveCashPoint(retired);
		service.retireCashPoint(retired, "Discontinued");
		
		Context.flushSession();
	}
	
	private CashPoint createCashPoint(String uuid, String name) {
		CashPoint cashPoint = new CashPoint();
		cashPoint.setUuid(uuid);
		cashPoint.setName(name);
		return cashPoint;
	}
}
