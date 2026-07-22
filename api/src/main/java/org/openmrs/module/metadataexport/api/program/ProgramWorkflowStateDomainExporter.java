/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.program;

import org.openmrs.Concept;
import org.openmrs.OpenmrsObject;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class ProgramWorkflowStateDomainExporter extends CsvDomainExporter<ProgramWorkflowState> {
	
	@Override
	public Domain getDomain() {
		return Domain.PROGRAM_WORKFLOW_STATES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof ProgramWorkflowState;
	}
	
	@Override
	public Collection<ProgramWorkflowState> getAllInstances() {
		List<ProgramWorkflowState> states = new ArrayList<>();
		for (Program program : Context.getProgramWorkflowService().getAllPrograms(true)) {
			for (ProgramWorkflow workflow : program.getAllWorkflows()) {
				states.addAll(workflow.getStates(true));
			}
		}
		return states;
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(ProgramWorkflowState instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		ProgramWorkflow workflow = instance.getProgramWorkflow();
		if (workflow != null) {
			dependencies.add(workflow);
		}
		Concept concept = instance.getConcept();
		if (concept != null) {
			dependencies.add(concept);
		}
		return dependencies;
	}
	
	@Override
	protected List<BaseLineExporter<ProgramWorkflowState>> chain() {
		return Collections.singletonList(new ProgramWorkflowStateLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "states.csv";
	}
}
