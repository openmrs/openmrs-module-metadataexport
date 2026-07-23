/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.flag;

import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.patientflags.Tag;
import org.openmrs.module.patientflags.api.FlagService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class FlagTagDomainExporter extends CsvDomainExporter<Tag> {
	
	@Override
	public Domain getDomain() {
		return Domain.FLAG_TAGS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof Tag;
	}
	
	@Override
	public Collection<Tag> getAllInstances() {
		return Context.getService(FlagService.class).getAllTags();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(Tag instance) {
		return Collections.emptyList();
	}
	
	@Override
	protected List<BaseLineExporter<Tag>> chain() {
		return Collections.singletonList(new FlagTagLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "tags.csv";
	}
}
