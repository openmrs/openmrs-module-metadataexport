/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.cohort;

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.ExportLine;

public class CohortTypeLineExporter extends BaseLineExporter<CohortType> {
	
	/**
	 * CohortType is Voidable, not Retireable, so this can't extend MetadataLineExporter; a voided row
	 * keeps its full columns because Iniz's CohortTypeCsvParser bootstraps and fills a voided row whose
	 * uuid is unknown on the target before voiding it.
	 */
	@Override
	public void writeLine(CohortType instance, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_UUID, instance.getUuid());
		
		if (BooleanUtils.isTrue(instance.getVoided())) {
			line.put(BaseLineProcessor.HEADER_VOID_RETIRE, "true");
		}
		
		export(instance, line);
	}
	
	@Override
	public void export(CohortType instance, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, instance.getName());
		line.put(BaseLineProcessor.HEADER_DESC, instance.getDescription());
	}
}
