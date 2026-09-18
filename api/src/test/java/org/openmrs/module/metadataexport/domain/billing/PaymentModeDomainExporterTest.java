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
import org.openmrs.OpenmrsObject;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.initializer.Domain;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentModeDomainExporterTest {
	
	private final PaymentModeDomainExporter exporter = new PaymentModeDomainExporter();
	
	@Test
	void getDomainShouldContainPaymentModeDomain() {
		assertEquals(Domain.PAYMENT_MODES, exporter.getDomain());
	}
	
	@Test
	void fileNameShouldReturnPaymentModesCsv() {
		assertEquals("paymentModes.csv", exporter.fileName());
	}
	
	@Test
	void chainShouldContainPaymentModeLineExporter() {
		assertEquals(1, exporter.chain().size());
		assertTrue(exporter.chain().get(0) instanceof PaymentModeLineExporter);
	}
	
	@Test
	void shouldHandleOnlyPaymentModeDomain() {
		assertTrue(exporter.handles(new PaymentMode()));
		assertFalse(exporter.handles(new CashPoint()));
		assertFalse(exporter.handles(null));
	}
	
	@Test
	void shouldReturnEmptyDependencies() {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("123e4567-e89b-12d3-a456-426614174000");
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(paymentMode);
		assertTrue(dependencies.isEmpty());
	}
}
