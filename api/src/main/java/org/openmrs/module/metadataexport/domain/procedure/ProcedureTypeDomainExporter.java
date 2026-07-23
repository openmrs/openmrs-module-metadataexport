/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.procedure;

import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.procedure.ProcedureService;
import org.openmrs.module.emrapi.procedure.ProcedureType;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
@OpenmrsProfile(modules = { "emrapi:3.4.* - 9.*" })
public class ProcedureTypeDomainExporter extends CsvDomainExporter<ProcedureType> {
	
	@Override
	protected List<BaseLineExporter<ProcedureType>> chain() {
		return Collections.singletonList(new ProcedureTypeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "procedureTypes.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.PROCEDURE_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof ProcedureType;
	}
	
	@Override
	public Collection<ProcedureType> getAllInstances() {
		return Context.getService(ProcedureService.class).getAllProcedureTypes(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(ProcedureType instance) {
		return Collections.emptyList();
	}
}
