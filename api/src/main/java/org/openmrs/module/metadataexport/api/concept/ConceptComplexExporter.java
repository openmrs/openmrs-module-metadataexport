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
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.ExportLine;

public class ConceptComplexExporter extends BaseLineExporter<Concept> {
	
	// Duplicated from the protected ConceptComplexLineProcessor.HEADER_HANDLER.
	private static final String HEADER_HANDLER = "complex data handler";
	
	@Override
	public void export(Concept concept, ExportLine line) {
		if (BooleanUtils.isTrue(concept.getRetired()) || !(concept instanceof ConceptComplex)) {
			return;
		}
		line.put(HEADER_HANDLER, ((ConceptComplex) concept).getHandler());
	}
}
