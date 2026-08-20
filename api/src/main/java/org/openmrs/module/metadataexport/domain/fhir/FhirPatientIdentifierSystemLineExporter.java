/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.fhir;

import org.openmrs.module.fhir2.model.FhirPatientIdentifierSystem;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

/**
 * Iniz has no name/description columns for this domain — the row's name is overwritten with the
 * referenced patient identifier type's name on every import, and the description never round-trips.
 */
public class FhirPatientIdentifierSystemLineExporter extends MetadataLineExporter<FhirPatientIdentifierSystem> {
	
	public static final String PATIENT_IDENTIFIER_TYPE_HEADER = "Patient identifier type";
	
	public static final String URL_HEADER = "url";
	
	@Override
	public void export(FhirPatientIdentifierSystem system, ExportLine line) {
		if (system.getPatientIdentifierType() != null) {
			line.put(PATIENT_IDENTIFIER_TYPE_HEADER, system.getPatientIdentifierType().getUuid());
		}
		line.put(URL_HEADER, system.getUrl());
	}
	
	/**
	 * Iniz matches existing rows by patient identifier type (not uuid) and requires that column — plus
	 * the url when the target has no row for that identifier type yet — even on retire rows.
	 */
	@Override
	protected void writeRetiredDiscriminators(FhirPatientIdentifierSystem system, ExportLine line) {
		export(system, line);
	}
}
