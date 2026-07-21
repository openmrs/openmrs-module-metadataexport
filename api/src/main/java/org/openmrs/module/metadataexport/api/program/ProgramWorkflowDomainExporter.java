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
public class ProgramWorkflowDomainExporter extends CsvDomainExporter<ProgramWorkflow> {
	
	@Override
	public Domain getDomain() {
		return Domain.PROGRAM_WORKFLOWS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof ProgramWorkflow;
	}
	
	@Override
	public Collection<ProgramWorkflow> getAllInstances() {
		List<ProgramWorkflow> workflows = new ArrayList<>();
		for (Program program : Context.getProgramWorkflowService().getAllPrograms(true)) {
			workflows.addAll(program.getAllWorkflows());
		}
		return workflows;
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(ProgramWorkflow instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		Program program = instance.getProgram();
		if (program != null) {
			dependencies.add(program);
		}
		Concept concept = instance.getConcept();
		if (concept != null) {
			dependencies.add(concept);
		}
		return dependencies;
	}
	
	@Override
	protected List<BaseLineExporter<ProgramWorkflow>> chain() {
		return Collections.singletonList(new ProgramWorkflowLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "programworkflows.csv";
	}
}
