/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.encounter;

import org.openmrs.EncounterRole;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class EncounterRoleLineExporter extends MetadataLineExporter<EncounterRole> {
	
	@Override
	public void export(EncounterRole encounterRole, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, encounterRole.getName());
		line.put(BaseLineProcessor.HEADER_DESC, encounterRole.getDescription());
	}
	
}
