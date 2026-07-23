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

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.Concept;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class ProgramWorkflowStateLineExporter extends MetadataLineExporter<ProgramWorkflowState> {
	
	public static final String HEADER_WORKFLOW = "Workflow";
	
	public static final String HEADER_STATE_CONCEPT = "State concept";
	
	public static final String HEADER_INITIAL = "Initial";
	
	public static final String HEADER_TERMINAL = "Terminal";
	
	@Override
	public void export(ProgramWorkflowState state, ExportLine line) {
		ProgramWorkflow workflow = state.getProgramWorkflow();
		if (workflow != null) {
			line.put(HEADER_WORKFLOW, workflow.getUuid());
		}
		
		Concept concept = state.getConcept();
		if (concept != null) {
			line.put(HEADER_STATE_CONCEPT, concept.getUuid());
		}
		
		if (BooleanUtils.isTrue(state.getInitial())) {
			line.put(HEADER_INITIAL, "true");
		}
		
		if (BooleanUtils.isTrue(state.getTerminal())) {
			line.put(HEADER_TERMINAL, "true");
		}
	}
}
