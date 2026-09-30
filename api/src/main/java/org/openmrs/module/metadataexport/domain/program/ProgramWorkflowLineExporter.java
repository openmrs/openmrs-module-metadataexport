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

import org.openmrs.ProgramWorkflow;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class ProgramWorkflowLineExporter extends MetadataLineExporter<ProgramWorkflow> {
	
	public static final String HEADER_PROGRAM = "program";
	
	public static final String HEADER_WORKFLOW_CONCEPT = "workflow concept";
	
	@Override
	public void export(ProgramWorkflow workflow, ExportLine line) {
		line.put(HEADER_PROGRAM, workflow.getProgram().getUuid());
		line.put(HEADER_WORKFLOW_CONCEPT, workflow.getConcept().getUuid());
	}
}
