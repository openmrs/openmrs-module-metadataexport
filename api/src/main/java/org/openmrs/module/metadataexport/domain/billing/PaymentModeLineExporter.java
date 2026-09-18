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

import org.apache.commons.lang.BooleanUtils;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class PaymentModeLineExporter extends MetadataLineExporter<PaymentMode> {
	
	public static final String HEADER_ATTRIBUTES = "attributes";
	
	@Override
	public void export(PaymentMode paymentMode, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, paymentMode.getName());
		
		if (CollectionUtils.isEmpty(paymentMode.getAttributeTypes())) {
			return;
		}
		
		List<String> attributes = new ArrayList<>();
		for (PaymentModeAttributeType pt : paymentMode.getAttributeTypes()) {
			if (pt != null) {
				if (BooleanUtils.isTrue(pt.getRetired())) {
					continue;
				}
				
				String name = pt.getName() == null ? "" : pt.getName();
				String format = pt.getFormat() == null ? "" : pt.getFormat();
				String regex = pt.getRegExp() == null ? "" : pt.getRegExp();
				String required = BooleanUtils.isTrue(pt.getRequired()) ? "True" : "False";
				attributes.add(name + " :: " + format + " :: " + regex + " :: " + required);
			}
		}
		
		if (!attributes.isEmpty()) {
			line.put(HEADER_ATTRIBUTES, String.join(";", attributes));
		}
	}
}
