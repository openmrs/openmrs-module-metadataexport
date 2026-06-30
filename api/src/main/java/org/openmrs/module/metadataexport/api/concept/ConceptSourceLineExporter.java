/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.concept;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.ConceptSource;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import static org.openmrs.module.initializer.api.c.ConceptSourceLineProcessor.HEADER_HL7_CODE;
import static org.openmrs.module.initializer.api.c.ConceptSourceLineProcessor.HEADER_UNIQUE_ID;

public class ConceptSourceLineExporter extends BaseLineExporter<ConceptSource> {
	
	@Override
	public void export(ConceptSource conceptSource, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_UUID, conceptSource.getUuid());
		if (BooleanUtils.isTrue(conceptSource.getRetired())) {
			line.put(BaseLineProcessor.HEADER_VOID_RETIRE, "true");
			return;
		}
		line.put(BaseLineProcessor.HEADER_NAME, conceptSource.getName());
		line.put(BaseLineProcessor.HEADER_DESC, conceptSource.getDescription());
		
		if (StringUtils.isNotEmpty(conceptSource.getHl7Code())) {
			line.put(HEADER_HL7_CODE, conceptSource.getHl7Code());
		}
		if (StringUtils.isNotEmpty(conceptSource.getUniqueId())) {
			line.put(HEADER_UNIQUE_ID, conceptSource.getUniqueId());
		}
	}
}
