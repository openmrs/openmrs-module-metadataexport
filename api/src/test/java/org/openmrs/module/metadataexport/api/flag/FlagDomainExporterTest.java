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

import org.junit.jupiter.api.Test;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.module.patientflags.Priority;
import org.openmrs.module.patientflags.Tag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlagDomainExporterTest {
	
	private final FlagDomainExporter exporter = new FlagDomainExporter();
	
	@Test
	void getDependencies_includesPriorityAndTags() {
		Priority priority = new Priority();
		priority.setName("High Priority");
		
		Tag tag = new Tag();
		tag.setName("Clinical");
		
		Flag flag = new Flag();
		flag.setPriority(priority);
		flag.setTags(Collections.singleton(tag));
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(flag);
		
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(priority), "referenced priority must be pulled into the closure");
		assertTrue(dependencies.contains(tag), "referenced tag must be pulled into the closure");
	}
	
	@Test
	void getDependencies_tagsOnlyWhenNoPriority() {
		Tag tag = new Tag();
		tag.setName("Clinical");
		
		Flag flag = new Flag();
		flag.setTags(Collections.singleton(tag));
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(flag);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(tag));
	}
	
	@Test
	void getDependencies_priorityOnlyWhenNoTags() {
		Priority priority = new Priority();
		priority.setName("High Priority");
		
		Flag flag = new Flag();
		flag.setPriority(priority);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(flag);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(priority));
	}
	
	@Test
	void getDependencies_excludesRetiredTags() {
		Tag live = new Tag();
		live.setName("Clinical");
		
		Tag retired = new Tag();
		retired.setName("Old Tag");
		retired.setRetired(true);
		
		Flag flag = new Flag();
		Set<Tag> tags = new HashSet<>();
		tags.add(live);
		tags.add(retired);
		flag.setTags(tags);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(flag);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(live));
	}
	
	@Test
	void getDependencies_emptyWhenNoPriorityOrTags() {
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(new Flag());
		
		assertTrue(dependencies.isEmpty());
	}
}
