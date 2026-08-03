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
import org.openmrs.module.metadatamapping.MetadataSource;
import org.openmrs.module.metadatamapping.MetadataTermMapping;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The mapped-object resolution always goes through {@code Context.getService(...)} (there is no
 * dependency here that can be resolved without it, unlike a set member which also has a bare
 * metadata-set reference) so only the short-circuit paths that never reach it are unit-testable.
 */
class MetadataTermMappingDomainExporterTest {
	
	private final MetadataTermMappingDomainExporter exporter = new MetadataTermMappingDomainExporter();
	
	@Test
	void getDependencies_emptyWhenMetadataSourceIsNull() {
		MetadataTermMapping mapping = new MetadataTermMapping();
		mapping.setCode("emr.primaryIdentifierType");
		mapping.setMetadataClass("org.openmrs.PatientIdentifierType");
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(mapping);
		
		assertTrue(dependencies.isEmpty());
	}
	
	@Test
	void getDependencies_emptyWhenMetadataClassUnresolvable() {
		MetadataSource source = new MetadataSource();
		source.setName("org.openmrs.module.emrapi");
		
		MetadataTermMapping mapping = new MetadataTermMapping();
		mapping.setMetadataSource(source);
		mapping.setCode("emr.primaryIdentifierType");
		mapping.setMetadataClass("org.openmrs.NoSuchClass");
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(mapping);
		
		assertTrue(dependencies.isEmpty());
	}
	
	@Test
	void getDependencies_emptyByDefault() {
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(new MetadataTermMapping());
		
		assertTrue(dependencies.isEmpty());
	}
}
