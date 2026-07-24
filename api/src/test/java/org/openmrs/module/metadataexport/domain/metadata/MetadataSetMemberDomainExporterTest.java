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

import org.junit.jupiter.api.Test;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.metadatamapping.MetadataSet;
import org.openmrs.module.metadatamapping.MetadataSetMember;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Only covers paths that don't require resolving the mapped object, since that reaches
 * {@code Context.getService(...)} and needs a running OpenMRS context — not available to a plain
 * unit test. The resolvable-class path is exercised at integration/manual-verification level.
 */
class MetadataSetMemberDomainExporterTest {
	
	private final MetadataSetMemberDomainExporter exporter = new MetadataSetMemberDomainExporter();
	
	@Test
	void getDependencies_includesMetadataSetOnly_whenMetadataClassUnresolvable() {
		MetadataSet metadataSet = new MetadataSet();
		metadataSet.setUuid("f0ebcb99-7618-41b7-b0bf-8ff93de67b9e");
		
		MetadataSetMember member = new MetadataSetMember();
		member.setMetadataSet(metadataSet);
		member.setMetadataClass("org.openmrs.NoSuchClass");
		member.setMetadataUuid("some-uuid");
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(member);
		
		assertEquals(1, dependencies.size());
		assertTrue(dependencies.contains(metadataSet), "owning metadata set must be pulled into the closure");
	}
	
	@Test
	void getDependencies_emptyWhenNoMetadataSetOrResolvableClass() {
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(new MetadataSetMember());
		
		assertTrue(dependencies.isEmpty());
	}
}
