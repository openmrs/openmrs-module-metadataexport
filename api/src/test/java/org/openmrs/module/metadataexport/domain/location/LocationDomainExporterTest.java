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

import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.OpenmrsObject;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocationDomainExporterTest {
	
	private final LocationDomainExporter exporter = new LocationDomainExporter();
	
	@Test
	void getDependencies_includesParentAndTags() {
		LocationTag tag = new LocationTag();
		tag.setName("Login Location");
		
		Location parent = new Location();
		parent.setUuid("parent-uuid");
		
		Location location = new Location();
		location.setParentLocation(parent);
		location.addTag(tag);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(location);
		
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(parent), "parent location must be pulled into the closure");
		assertTrue(dependencies.contains(tag), "referenced tag must be pulled into the closure");
	}
	
	@Test
	void getDependencies_tagsOnlyWhenNoParent() {
		LocationTag tag = new LocationTag();
		tag.setName("Login Location");
		
		Location location = new Location();
		location.addTag(tag);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(location);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(tag));
	}
	
	@Test
	void getDependencies_parentOnlyWhenNoTags() {
		Location parent = new Location();
		parent.setUuid("parent-uuid");
		
		Location location = new Location();
		location.setParentLocation(parent);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(location);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(parent));
	}
	
	@Test
	void getDependencies_excludesRetiredTags() {
		LocationTag live = new LocationTag();
		live.setName("Login Location");
		
		LocationTag retired = new LocationTag();
		retired.setName("Old Tag");
		retired.setRetired(true);
		
		Location location = new Location();
		location.addTag(live);
		location.addTag(retired);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(location);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(live));
	}
	
	@Test
	void getDependencies_emptyWhenNoParentOrTags() {
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(new Location());
		
		assertTrue(dependencies.isEmpty());
	}
}
