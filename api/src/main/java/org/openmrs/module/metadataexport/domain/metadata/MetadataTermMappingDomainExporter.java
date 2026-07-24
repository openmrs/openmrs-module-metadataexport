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

import org.openmrs.OpenmrsMetadata;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.metadatamapping.MetadataSource;
import org.openmrs.module.metadatamapping.MetadataTermMapping;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;
import org.openmrs.module.metadatamapping.api.MetadataTermMappingSearchCriteriaBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class MetadataTermMappingDomainExporter extends CsvDomainExporter<MetadataTermMapping> {
	
	@Override
	public Domain getDomain() {
		return Domain.METADATA_TERM_MAPPINGS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof MetadataTermMapping;
	}
	
	@Override
	public Collection<MetadataTermMapping> getAllInstances() {
		return Context.getService(MetadataMappingService.class)
		        .getMetadataTermMappings(new MetadataTermMappingSearchCriteriaBuilder().setIncludeAll(true).build());
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(MetadataTermMapping instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		
		MetadataSource source = instance.getMetadataSource();
		Class<? extends OpenmrsMetadata> type = MetadataMappingUtils.resolveMetadataClass(instance.getMetadataClass());
		if (source != null && type != null) {
			OpenmrsMetadata mappedObject = Context.getService(MetadataMappingService.class).getMetadataItem(type,
			    source.getName(), instance.getCode());
			if (mappedObject != null) {
				dependencies.add(mappedObject);
			}
		}
		
		return dependencies;
	}
	
	@Override
	protected List<BaseLineExporter<MetadataTermMapping>> chain() {
		return Collections.singletonList(new MetadataTermMappingLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "metadatatermmappings.csv";
	}
}
