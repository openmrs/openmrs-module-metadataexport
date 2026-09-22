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

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class BillableServiceLineExporter extends MetadataLineExporter<BillableService> {
	
	public static final String HEADER_SERVICE_NAME = "Service Name";
	
	public static final String HEADER_SHORT_NAME = "Short Name";
	
	public static final String HEADER_CONCEPT = "Concept";
	
	public static final String HEADER_SERVICE_TYPE = "Service Type";
	
	public static final String HEADER_SERVICE_STATUS = "Service Status";
	
	@Override
	public void export(BillableService instance, ExportLine line) {
		line.put(HEADER_SERVICE_NAME, instance.getName());
		line.put(HEADER_SHORT_NAME, instance.getShortName());
		if (instance.getConcept() != null) {
			line.put(HEADER_CONCEPT, instance.getConcept().getUuid());
		}
		if (instance.getServiceType() != null) {
			Concept serviceType = instance.getServiceType();
			ConceptName serviceTypeName = serviceType.getPreferredName(Context.getLocale());
			if (serviceTypeName != null) {
				line.put(HEADER_SERVICE_TYPE, serviceTypeName.getName());
			}
		}
		if (instance.getServiceStatus() != null) {
			line.put(HEADER_SERVICE_STATUS, instance.getServiceStatus().name());
		}
	}
}
