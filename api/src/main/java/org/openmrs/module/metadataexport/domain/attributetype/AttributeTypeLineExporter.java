/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.attributetype;

import org.openmrs.ConceptAttributeType;
import org.openmrs.LocationAttributeType;
import org.openmrs.ProgramAttributeType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.VisitAttributeType;
import org.openmrs.attribute.BaseAttributeType;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class AttributeTypeLineExporter extends MetadataLineExporter<BaseAttributeType<?>> {
	
	static final String HEADER_ENTITY_NAME = "Entity name";
	
	static final String HEADER_MIN_OCCURS = "Min occurs";
	
	static final String HEADER_MAX_OCCURS = "Max occurs";
	
	static final String HEADER_DATATYPE_CLASSNAME = "Datatype classname";
	
	static final String HEADER_DATATYPE_CONFIG = "Datatype config";
	
	static final String HEADER_PREFERRED_HANDLER = "Preferred handler classname";
	
	static final String HEADER_HANDLER_CONFIG = "Handler config";
	
	@Override
	protected void writeRetiredDiscriminators(BaseAttributeType<?> attributeType, ExportLine line) {
		line.put(HEADER_ENTITY_NAME, entityName(attributeType));
		line.put(BaseLineProcessor.HEADER_NAME, attributeType.getName());
	}
	
	@Override
	public void export(BaseAttributeType<?> attributeType, ExportLine line) {
		line.put(HEADER_ENTITY_NAME, entityName(attributeType));
		line.put(BaseLineProcessor.HEADER_NAME, attributeType.getName());
		line.put(BaseLineProcessor.HEADER_DESC, attributeType.getDescription());
		if (attributeType.getMinOccurs() != null) {
			line.put(HEADER_MIN_OCCURS, String.valueOf(attributeType.getMinOccurs()));
		}
		
		if (attributeType.getMaxOccurs() != null) {
			line.put(HEADER_MAX_OCCURS, String.valueOf(attributeType.getMaxOccurs()));
		}
		if (attributeType.getDatatypeClassname() != null) {
			line.put(HEADER_DATATYPE_CLASSNAME, attributeType.getDatatypeClassname());
		}
		
		if (attributeType.getDatatypeConfig() != null) {
			line.put(HEADER_DATATYPE_CONFIG, attributeType.getDatatypeConfig());
		}
		
		if (attributeType.getPreferredHandlerClassname() != null) {
			line.put(HEADER_PREFERRED_HANDLER, attributeType.getPreferredHandlerClassname());
		}
		
		if (attributeType.getHandlerConfig() != null) {
			line.put(HEADER_HANDLER_CONFIG, attributeType.getHandlerConfig());
		}
	}
	
	private static String entityName(BaseAttributeType<?> attributeType) {
		if (attributeType instanceof LocationAttributeType)
			return "Location";
		if (attributeType instanceof VisitAttributeType)
			return "Visit";
		if (attributeType instanceof ProviderAttributeType)
			return "Provider";
		if (attributeType instanceof ConceptAttributeType)
			return "Concept";
		if (attributeType instanceof ProgramAttributeType)
			return "Program";
		throw new IllegalArgumentException("Unknown attribute type: " + attributeType.getClass().getName());
	}
}
