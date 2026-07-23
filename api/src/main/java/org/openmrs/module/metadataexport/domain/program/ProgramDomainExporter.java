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
import org.openmrs.OpenmrsObject;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class ProgramDomainExporter extends CsvDomainExporter<Program> {
	
	@Override
	public Domain getDomain() {
		return Domain.PROGRAMS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof Program;
	}
	
	@Override
	public Collection<Program> getAllInstances() {
		return Context.getProgramWorkflowService().getAllPrograms(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(Program instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		Concept concept = instance.getConcept();
		if (concept != null) {
			dependencies.add(concept);
		}
		Concept outcomesConcept = instance.getOutcomesConcept();
		if (outcomesConcept != null) {
			dependencies.add(outcomesConcept);
		}
		return dependencies;
	}
	
	@Override
	protected List<BaseLineExporter<Program>> chain() {
		return Collections.singletonList(new ProgramLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "programs.csv";
	}
}
