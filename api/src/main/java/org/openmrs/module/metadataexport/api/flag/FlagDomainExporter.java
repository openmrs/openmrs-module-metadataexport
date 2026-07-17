/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.flag;

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.CsvDomainExporter;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.module.patientflags.Tag;
import org.openmrs.module.patientflags.api.FlagService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class FlagDomainExporter extends CsvDomainExporter<Flag> {
	
	@Override
	public Domain getDomain() {
		return Domain.FLAGS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof Flag;
	}
	
	@Override
	public Collection<Flag> getAllInstances() {
		return Context.getService(FlagService.class).getAllFlags();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(Flag instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		if (instance.getPriority() != null) {
			dependencies.add(instance.getPriority());
		}
		if (instance.getTags() != null) {
			for (Tag tag : instance.getTags()) {
				if (BooleanUtils.isNotTrue(tag.getRetired())) {
					dependencies.add(tag);
				}
			}
		}
		return dependencies;
	}
	
	@Override
	protected List<BaseLineExporter<Flag>> chain() {
		return Collections.singletonList(new FlagLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "flags.csv";
	}
}
