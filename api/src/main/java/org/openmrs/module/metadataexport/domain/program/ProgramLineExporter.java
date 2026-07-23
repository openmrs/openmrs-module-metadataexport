/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.program;

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class ProgramLineExporter extends MetadataLineExporter<Program> {
	
	public static final String HEADER_CONCEPT_PROGRAM = "program concept";
	
	public static final String HEADER_OUTCOMES_CONCEPT = "outcomes concept";
	
	@Override
	public void export(Program program, ExportLine line) {
		Concept concept = program.getConcept();
		if (concept != null) {
			line.put(HEADER_CONCEPT_PROGRAM, concept.getUuid());
		}
		
		Concept outcomesConcept = program.getOutcomesConcept();
		if (outcomesConcept != null) {
			line.put(HEADER_OUTCOMES_CONCEPT, outcomesConcept.getUuid());
		}
	}
}
