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

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Drug;
import org.openmrs.DrugReferenceMap;
import org.openmrs.module.initializer.api.drugs.MappingsDrugLineProcessor;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inverse of {@link MappingsDrugLineProcessor}. Emits one {@code mappings|<mapType>|<source>}
 * column per (map type, source) pair, value = the reference term code(s) joined by {@code "; "}
 * (the loader splits on {@code ;} and trims).
 */
public class DrugMappingsExporter extends BaseLineExporter<Drug> {
	
	private static final String HEADER_SEPARATOR = "|";
	
	private static final String LIST_SEPARATOR = "; ";
	
	@Override
	public void export(Drug drug, ExportLine line) {
		if (BooleanUtils.isTrue(drug.getRetired())) {
			return;
		}
		
		Map<String, String> codesByHeader = new LinkedHashMap<>();
		for (DrugReferenceMap map : drug.getDrugReferenceMaps()) {
			ConceptReferenceTerm term = map.getConceptReferenceTerm();
			if (term == null || term.getConceptSource() == null || map.getConceptMapType() == null) {
				continue;
			}
			String header = MappingsDrugLineProcessor.MAPPING_HEADER_PREFIX + HEADER_SEPARATOR
			        + map.getConceptMapType().getName() + HEADER_SEPARATOR + term.getConceptSource().getName();
			codesByHeader.merge(header, term.getCode(), (existing, code) -> existing + LIST_SEPARATOR + code);
		}
		
		for (Map.Entry<String, String> entry : codesByHeader.entrySet()) {
			line.put(entry.getKey(), entry.getValue());
		}
	}
}
