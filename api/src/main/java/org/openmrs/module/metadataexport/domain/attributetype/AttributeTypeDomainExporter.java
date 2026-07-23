/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.attributetype;

import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.attribute.BaseAttributeType;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class AttributeTypeDomainExporter extends CsvDomainExporter<BaseAttributeType<?>> {
	
	@Override
	protected String fileName() {
		return "attribute_types.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.ATTRIBUTE_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof BaseAttributeType;
	}
	
	@Override
	public Collection<BaseAttributeType<?>> getAllInstances() {
		List<BaseAttributeType<?>> all = new ArrayList<>();
		all.addAll(Context.getLocationService().getAllLocationAttributeTypes());
		all.addAll(Context.getVisitService().getAllVisitAttributeTypes());
		all.addAll(Context.getProviderService().getAllProviderAttributeTypes());
		all.addAll(Context.getConceptService().getAllConceptAttributeTypes());
		all.addAll(Context.getProgramWorkflowService().getAllProgramAttributeTypes());
		return all;
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(BaseAttributeType<?> instance) {
		return Collections.emptyList();
	}
	
	@Override
	protected List<BaseLineExporter<BaseAttributeType<?>>> chain() {
		return Collections.singletonList(new AttributeTypeLineExporter());
	}
}
