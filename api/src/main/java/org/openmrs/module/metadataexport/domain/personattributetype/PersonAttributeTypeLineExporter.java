/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.personattributetype;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.Privilege;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class PersonAttributeTypeLineExporter extends MetadataLineExporter<PersonAttributeType> {
	
	public static final String HEADER_FORMAT = "format";
	
	public static final String HEADER_FOREIGN_UUID = "foreign uuid";
	
	public static final String HEADER_SEARCHABLE = "searchable";
	
	public static final String HEADER_EDITPRIVILEGE = "edit privilege";
	
	@Override
	public void export(PersonAttributeType personAttributeType, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, personAttributeType.getName());
		line.put(BaseLineProcessor.HEADER_DESC, personAttributeType.getDescription());
		line.put(HEADER_SEARCHABLE, String.valueOf(personAttributeType.getSearchable()));
		
		String format = personAttributeType.getFormat();
		if (StringUtils.isNotEmpty(format)) {
			line.put(HEADER_FORMAT, format);
		}
		
		Integer foreignId = personAttributeType.getForeignKey();
		if (foreignId != null && "org.openmrs.Concept".equals(format)) {
			Concept foreignConcept = Context.getConceptService().getConcept(foreignId);
			if (foreignConcept != null) {
				line.put(HEADER_FOREIGN_UUID, foreignConcept.getUuid());
			}
		}
		
		Privilege editPrivilege = personAttributeType.getEditPrivilege();
		if (editPrivilege != null) {
			line.put(HEADER_EDITPRIVILEGE, editPrivilege.getPrivilege());
		}
	}
}
