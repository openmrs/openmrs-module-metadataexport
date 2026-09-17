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
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentModeDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private final PaymentModeDomainExporter exporter = new PaymentModeDomainExporter();
	
	@Test
	void shouldGetPaymentModeAllInstances() {
		seedOneRetiredAndOneNonRetiredPaymentMode();
		
		Collection<PaymentMode> paymentModes = exporter.getAllInstances();
		
		assertNotNull(paymentModes);
		assertEquals(2, paymentModes.size());
		
		List<String> uuids = paymentModes.stream().map(PaymentMode::getUuid).collect(Collectors.toList());
		assertTrue(uuids.contains(LIVE_UUID));
		assertTrue(uuids.contains(RETIRED_UUID));
	}
	
	@Test
	void shouldGetEmptyPaymentModesIfAllInstancesEmpty() {
		Collection<PaymentMode> paymentModes = exporter.getAllInstances();
		assertEquals(0, paymentModes.size());
	}
	
	private void seedOneRetiredAndOneNonRetiredPaymentMode() {
		PaymentModeService service = Context.getService(PaymentModeService.class);
		
		PaymentMode live = createPaymentMode(LIVE_UUID, "Cash");
		service.savePaymentMode(live);
		
		PaymentMode retired = createPaymentMode(RETIRED_UUID, "Cheque");
		service.savePaymentMode(retired);
		service.retirePaymentMode(retired, "Discontinued");
		
		Context.flushSession();
	}
	
	private PaymentMode createPaymentMode(String uuid, String name) {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid(uuid);
		paymentMode.setName(name);
		return paymentMode;
	}
}
