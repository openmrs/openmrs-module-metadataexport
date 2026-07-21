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

import org.openmrs.ConceptSet;
import org.openmrs.module.initializer.api.c.ConceptSetLineProcessor;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.ExportLine;

public class ConceptSetLineExporter extends BaseLineExporter<ConceptSet> {
	
	@Override
	public void export(ConceptSet conceptSet, ExportLine line) {
		line.put(ConceptSetLineProcessor.HEADER_CONCEPT, conceptSet.getConceptSet().getUuid());
		line.put(ConceptSetLineProcessor.HEADER_MEMBER, conceptSet.getConcept().getUuid());
		line.put(ConceptSetLineProcessor.HEADER_MEMBER_TYPE, ConceptSetLineProcessor.HEADER_MEMBER_TYPE_CONCEPT_SET);
		
		if (conceptSet.getSortWeight() != null) {
			line.put(ConceptSetLineProcessor.HEADER_SORT_WEIGHT, conceptSet.getSortWeight().toString());
		}
	}
}
