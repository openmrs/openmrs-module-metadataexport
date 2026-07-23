/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.location;

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.OpenmrsObject;
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
public class LocationDomainExporter extends CsvDomainExporter<Location> {
	
	@Override
	protected List<BaseLineExporter<Location>> chain() {
		return Collections.singletonList(new LocationLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "locations.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.LOCATIONS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof Location;
	}
	
	@Override
	public Collection<Location> getAllInstances() {
		return Context.getLocationService().getAllLocations(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(Location instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		if (instance.getTags() != null) {
			for (LocationTag tag : instance.getTags()) {
				if (BooleanUtils.isNotTrue(tag.getRetired())) {
					dependencies.add(tag);
				}
			}
		}
		if (instance.getParentLocation() != null) {
			dependencies.add(instance.getParentLocation());
		}
		return dependencies;
	}
}
