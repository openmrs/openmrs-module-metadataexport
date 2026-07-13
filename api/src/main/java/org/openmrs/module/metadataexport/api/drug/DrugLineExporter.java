/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.drug;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.metadataexport.api.export.MetadataLineExporter;

public class DrugLineExporter extends MetadataLineExporter<Drug> {
	
	public final static String HEADER_STRENGTH = "strength";
	
	public final static String HEADER_DOSAGE_FORM = "concept dosage form";
	
	public final static String HEADER_CONCEPT_DRUG = "concept drug";
	
	@Override
	public void export(Drug drug, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, drug.getName());
		line.put(BaseLineProcessor.HEADER_DESC, drug.getDescription());
		line.put(HEADER_STRENGTH, drug.getStrength());
		
		Concept drugConcept = drug.getConcept();
		if (drugConcept != null) {
			line.put(HEADER_CONCEPT_DRUG, drugConcept.getUuid());
		}
		
		Concept dosageForm = drug.getDosageForm();
		if (dosageForm != null) {
			line.put(HEADER_DOSAGE_FORM, dosageForm.getUuid());
		}
	}
}
