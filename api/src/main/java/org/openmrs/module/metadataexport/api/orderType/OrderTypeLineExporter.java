/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.orderType;

import org.openmrs.ConceptClass;
import org.openmrs.OrderType;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.metadataexport.api.export.MetadataLineExporter;

import java.util.Collection;
import java.util.stream.Collectors;

public class OrderTypeLineExporter extends MetadataLineExporter<OrderType> {
	
	public final static String JAVA_CLASS_NAME = "java class name";
	
	public final static String HEADER_CONCEPT_CLASSES = "concept classes";
	
	@Override
	public void export(OrderType orderType, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, orderType.getName());
		line.put(BaseLineProcessor.HEADER_DESC, orderType.getDescription());
		line.put(JAVA_CLASS_NAME, orderType.getJavaClassName());
		
		OrderType parent = orderType.getParent();
		if (parent != null) {
			line.put(BaseLineProcessor.PARENT, parent.getUuid());
		}
		
		line.put(HEADER_CONCEPT_CLASSES, orderType.getConceptClasses().stream().map(ConceptClass::getUuid)
		        .collect(Collectors.joining(BaseLineProcessor.LIST_SEPARATOR)));
	}
}
