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

import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.metadataexport.api.export.MetadataLineExporter;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.module.patientflags.Tag;

import java.util.stream.Collectors;

public class FlagLineExporter extends MetadataLineExporter<Flag> {
	
	static final String HEADER_CRITERIA = "criteria";
	
	static final String HEADER_EVALUATOR = "evaluator";
	
	static final String HEADER_MESSAGE = "message";
	
	static final String HEADER_PRIORITY = "priority";
	
	static final String HEADER_ENABLED = "enabled";
	
	static final String HEADER_TAGS = "tags";
	
	@Override
	public void export(Flag flag, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, flag.getName());
		line.put(HEADER_CRITERIA, flag.getCriteria());
		line.put(HEADER_EVALUATOR, flag.getEvaluator());
		
		line.put(HEADER_MESSAGE, flag.getMessage());
		
		if (flag.getPriority() != null) {
			line.put(HEADER_PRIORITY, flag.getPriority().getName());
		}
		
		if (flag.getEnabled() != null) {
			line.put(HEADER_ENABLED, String.valueOf(flag.getEnabled()));
		}
		
		if (flag.getTags() != null) {
			line.put(HEADER_TAGS, flag.getTags().stream().map(Tag::getName).sorted().collect(Collectors.joining(";")));
		}
		
		line.put(BaseLineProcessor.HEADER_DESC, flag.getDescription());
		
	}
}
