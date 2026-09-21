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

import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class CashPointLineExporter extends MetadataLineExporter<CashPoint> {
	
	public static final String HEADER_LOCATION = "location";
	
	@Override
	public void export(CashPoint cashPoint, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, cashPoint.getName());
		line.put(BaseLineProcessor.HEADER_DESC, cashPoint.getDescription());
		
		if (cashPoint.getLocation() != null) {
			line.put(HEADER_LOCATION, cashPoint.getLocation().getUuid());
		}
	}
	
	@Override
	protected void writeRetiredDiscriminators(CashPoint cashPoint, ExportLine line) {
		export(cashPoint, line);
	}
}
