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
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@OpenmrsProfile(modules = "billing:2.4.0")
public class PaymentModeDomainExporter extends CsvDomainExporter<PaymentMode> {
	
	@Override
	protected List<BaseLineExporter<PaymentMode>> chain() {
		return Collections.singletonList(new PaymentModeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "paymentModes.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.PAYMENT_MODES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof PaymentMode;
	}
	
	@Override
	public Collection<PaymentMode> getAllInstances() {
		PaymentModeService paymentModeService = Context.getService(PaymentModeService.class);
		return paymentModeService.getPaymentModes(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(PaymentMode instance) {
		return Collections.emptyList();
	}
}
