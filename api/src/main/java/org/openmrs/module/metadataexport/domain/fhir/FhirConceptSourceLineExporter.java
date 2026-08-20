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

import org.openmrs.module.fhir2.model.FhirConceptSource;
import org.openmrs.module.initializer.api.fhir.cs.FhirConceptSourceCsvParser;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

/**
 * Iniz has no name/description columns for this domain — the row's name is set from the referenced
 * concept source when Iniz first creates it, and the description never round-trips.
 */
public class FhirConceptSourceLineExporter extends MetadataLineExporter<FhirConceptSource> {
	
	public static final String URL_HEADER = "url";
	
	@Override
	public void export(FhirConceptSource source, ExportLine line) {
		if (source.getConceptSource() != null) {
			line.put(FhirConceptSourceCsvParser.CONCEPT_SOURCE_HEADER, source.getConceptSource().getUuid());
		}
		line.put(URL_HEADER, source.getUrl());
	}
	
	/**
	 * Iniz matches existing rows by concept source (not uuid) and requires that column — plus the url
	 * when the target has no row for that concept source yet — even on retire rows.
	 */
	@Override
	protected void writeRetiredDiscriminators(FhirConceptSource source, ExportLine line) {
		export(source, line);
	}
}
