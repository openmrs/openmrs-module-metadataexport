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
import org.openmrs.module.metadatamapping.MetadataSet;
import org.openmrs.module.metadatamapping.MetadataSetMember;
import org.openmrs.module.metadatamapping.RetiredHandlingMode;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;
import org.openmrs.module.metadatamapping.api.MetadataSetSearchCriteria;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class MetadataSetMemberDomainExporter extends CsvDomainExporter<MetadataSetMember> {
	
	@Override
	public Domain getDomain() {
		return Domain.METADATA_SET_MEMBERS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof MetadataSetMember;
	}
	
	@Override
	public Collection<MetadataSetMember> getAllInstances() {
		MetadataMappingService service = Context.getService(MetadataMappingService.class);
		List<MetadataSetMember> members = new ArrayList<>();
		for (MetadataSet metadataSet : service.getMetadataSets(new MetadataSetSearchCriteria(true, null, null))) {
			members.addAll(service.getMetadataSetMembers(metadataSet, RetiredHandlingMode.INCLUDE_RETIRED));
		}
		return members;
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(MetadataSetMember instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		
		MetadataSet metadataSet = instance.getMetadataSet();
		if (metadataSet != null) {
			dependencies.add(metadataSet);
		}
		
		Class<? extends OpenmrsMetadata> type = MetadataMappingUtils.resolveMetadataClass(instance.getMetadataClass());
		if (type != null) {
			OpenmrsMetadata mappedObject = Context.getService(MetadataMappingService.class).getMetadataItem(type, instance);
			if (mappedObject != null) {
				dependencies.add(mappedObject);
			}
		}
		
		return dependencies;
	}
	
	@Override
	protected List<BaseLineExporter<MetadataSetMember>> chain() {
		return Collections.singletonList(new MetadataSetMemberLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "metadatasetmembers.csv";
	}
}
