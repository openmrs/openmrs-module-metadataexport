/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.location;

import org.openmrs.LocationTag;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class LocationTagDomainExporter extends CsvDomainExporter<LocationTag> {
	
	@Override
	protected List<BaseLineExporter<LocationTag>> chain() {
		return Collections.singletonList(new LocationTagLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "locationTags.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.LOCATION_TAGS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof LocationTag;
	}
	
	@Override
	public Collection<LocationTag> getAllInstances() {
		return Context.getLocationService().getAllLocationTags(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(LocationTag instance) {
		return Collections.emptyList();
	}
}
