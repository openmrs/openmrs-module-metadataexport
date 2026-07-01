/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.visitType;

import org.openmrs.OpenmrsObject;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class VisitTypeDomainExporter extends CsvDomainExporter<VisitType> {
	
	@Override
	public Domain getDomain() {
		return Domain.VISIT_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof VisitType;
	}
	
	@Override
	public Collection<VisitType> getAllInstances() {
		return Context.getVisitService().getAllVisitTypes();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(VisitType instance) {
		return Collections.emptyList();
	}
	
	@Override
	protected List<BaseLineExporter<VisitType>> chain() {
		return Collections.singletonList(new VisitTypeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "visitTypes.csv";
	}
}
