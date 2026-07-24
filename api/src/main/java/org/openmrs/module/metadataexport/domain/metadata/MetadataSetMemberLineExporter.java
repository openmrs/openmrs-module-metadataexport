/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.metadata;

import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.mds.MetadataSetMemberLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;
import org.openmrs.module.metadatamapping.MetadataSet;
import org.openmrs.module.metadatamapping.MetadataSetMember;

public class MetadataSetMemberLineExporter extends MetadataLineExporter<MetadataSetMember> {
	
	public static final String HEADER_METADATA_UUID = "metadata uuid";
	
	@Override
	public void export(MetadataSetMember member, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, member.getName());
		line.put(BaseLineProcessor.HEADER_DESC, member.getDescription());
		
		if (member.getSortWeight() != null) {
			line.put(MetadataSetMemberLineProcessor.SORT_WEIGHT, String.valueOf(member.getSortWeight()));
		}
		
		line.put(MetadataSetMemberLineProcessor.METADATA_CLASS, member.getMetadataClass());
		line.put(HEADER_METADATA_UUID, member.getMetadataUuid());
		
		MetadataSet metadataSet = member.getMetadataSet();
		if (metadataSet != null) {
			line.put(MetadataSetMemberLineProcessor.METADATA_SET_UUID, metadataSet.getUuid());
		}
	}
}
