/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.patientIdentifierType;

import org.openmrs.PatientIdentifierType;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.pit.PatientIdentifierTypeLineProcessor;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.metadataexport.api.export.MetadataLineExporter;

public class PatientIdentifierTypeLineExporter extends MetadataLineExporter<PatientIdentifierType> {
	
	@Override
	public void export(PatientIdentifierType patientIdentifierType, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, patientIdentifierType.getName());
		line.put(BaseLineProcessor.HEADER_DESC, patientIdentifierType.getDescription());
		
		line.put(PatientIdentifierTypeLineProcessor.HEADER_REQUIRED, String.valueOf(patientIdentifierType.getRequired()));
		line.put(PatientIdentifierTypeLineProcessor.HEADER_FORMAT, patientIdentifierType.getFormat());
		line.put(PatientIdentifierTypeLineProcessor.HEADER_FORMAT_DESCRIPTION, patientIdentifierType.getFormatDescription());
		line.put(PatientIdentifierTypeLineProcessor.HEADER_VALIDATOR, patientIdentifierType.getValidator());
		if (patientIdentifierType.getLocationBehavior() != null) {
			line.put(PatientIdentifierTypeLineProcessor.HEADER_LOCATION_BEHAVIOR,
			    patientIdentifierType.getLocationBehavior().name());
		}
		if (patientIdentifierType.getUniquenessBehavior() != null) {
			line.put(PatientIdentifierTypeLineProcessor.HEADER_UNIQUENESS_BEHAVIOR,
			    patientIdentifierType.getUniquenessBehavior().name());
		}
	}
}
