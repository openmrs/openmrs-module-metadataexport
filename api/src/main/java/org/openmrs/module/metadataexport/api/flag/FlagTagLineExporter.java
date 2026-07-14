/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.flag;

import org.openmrs.Role;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.metadataexport.api.export.MetadataLineExporter;
import org.openmrs.module.patientflags.DisplayPoint;
import org.openmrs.module.patientflags.Tag;

import java.util.stream.Collectors;

public class FlagTagLineExporter extends MetadataLineExporter<Tag> {
	
	static final String HEADER_ROLES = "roles";
	
	static final String HEADER_DISPLAY_POINTS = "display points";
	
	@Override
	public void export(Tag tag, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, tag.getName());
		
		if (tag.getRoles() != null) {
			line.put(HEADER_ROLES, tag.getRoles().stream().map(Role::getRole).sorted().collect(Collectors.joining(";")));
		}
		
		if (tag.getDisplayPoints() != null) {
			line.put(HEADER_DISPLAY_POINTS,
			    tag.getDisplayPoints().stream().map(DisplayPoint::getName).sorted().collect(Collectors.joining(";")));
		}
		
		line.put(BaseLineProcessor.HEADER_DESC, tag.getDescription());
	}
}
