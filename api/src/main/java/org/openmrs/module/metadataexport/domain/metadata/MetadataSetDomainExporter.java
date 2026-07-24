/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.metadata;

import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.metadatamapping.MetadataSet;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;
import org.openmrs.module.metadatamapping.api.MetadataSetSearchCriteria;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class MetadataSetDomainExporter extends CsvDomainExporter<MetadataSet> {
	
	@Override
	public Domain getDomain() {
		return Domain.METADATA_SETS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof MetadataSet;
	}
	
	@Override
	public Collection<MetadataSet> getAllInstances() {
		return Context.getService(MetadataMappingService.class)
		        .getMetadataSets(new MetadataSetSearchCriteria(true, null, null));
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(MetadataSet instance) {
		return Collections.emptyList();
	}
	
	@Override
	protected List<BaseLineExporter<MetadataSet>> chain() {
		return Collections.singletonList(new MetadataSetLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "metadatasets.csv";
	}
}
