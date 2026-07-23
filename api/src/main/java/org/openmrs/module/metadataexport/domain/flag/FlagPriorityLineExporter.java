/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.flag;

import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;
import org.openmrs.module.patientflags.Priority;

public class FlagPriorityLineExporter extends MetadataLineExporter<Priority> {
	
	static final String HEADER_STYLE = "style";
	
	static final String HEADER_RANK = "rank";
	
	@Override
	public void export(Priority priority, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, priority.getName());
		line.put(BaseLineProcessor.HEADER_DESC, priority.getDescription());
		line.put(HEADER_STYLE, priority.getStyle());
		
		if (priority.getRank() != null) {
			line.put(HEADER_RANK, String.valueOf(priority.getRank()));
		}
	}
}
