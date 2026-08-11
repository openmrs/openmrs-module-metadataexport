/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.idgen;

import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.idgen.AutoGenerationOption;
import org.openmrs.module.idgen.BaseIdentifierSource;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoGenerationOptionDomainExporterTest {
	
	private final AutoGenerationOptionDomainExporter exporter = new AutoGenerationOptionDomainExporter();
	
	@Test
	void dependenciesIncludeIdentifierTypeSourceAndLocation() {
		PatientIdentifierType type = new PatientIdentifierType();
		SequentialIdentifierGenerator source = new SequentialIdentifierGenerator();
		Location location = new Location();
		
		AutoGenerationOption option = new AutoGenerationOption();
		option.setIdentifierType(type);
		option.setSource(source);
		option.setLocation(location);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(option);
		
		assertEquals(3, dependencies.size());
		assertTrue(dependencies.contains(type));
		assertTrue(dependencies.contains(source));
		assertTrue(dependencies.contains(location));
	}
	
	@Test
	void dependenciesOmitUnsetLocation() {
		PatientIdentifierType type = new PatientIdentifierType();
		SequentialIdentifierGenerator source = new SequentialIdentifierGenerator();
		
		AutoGenerationOption option = new AutoGenerationOption();
		option.setIdentifierType(type);
		option.setSource(source);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(option);
		
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(type));
		assertTrue(dependencies.contains(source));
	}
	
	@Test
	void dependenciesAreNullSafe() {
		assertTrue(exporter.getDependencies(new AutoGenerationOption()).isEmpty());
	}
	
	@Test
	void handlesAutoGenerationOptionsOnly() {
		assertTrue(exporter.handles(new AutoGenerationOption()));
		assertFalse(exporter.handles(new PatientIdentifierType()));
	}
	
	@Test
	void exportableFiltersRetiredOptions() {
		AutoGenerationOption live = new AutoGenerationOption();
		AutoGenerationOption retired = new AutoGenerationOption();
		retired.setRetired(true);
		
		List<AutoGenerationOption> result = AutoGenerationOptionDomainExporter.exportable(Arrays.asList(live, retired));
		
		assertEquals(Collections.singletonList(live), result);
	}
	
	@Test
	void exportableSkipsOptionsWithUnsupportedSourceType() {
		AutoGenerationOption dangling = new AutoGenerationOption();
		dangling.setSource(new BaseIdentifierSource() {});
		AutoGenerationOption kept = new AutoGenerationOption();
		kept.setSource(new SequentialIdentifierGenerator());
		
		List<AutoGenerationOption> result = AutoGenerationOptionDomainExporter.exportable(Arrays.asList(dangling, kept));
		
		assertEquals(Collections.singletonList(kept), result,
		    "an option pointing at a source the idgen exporter skips would be a dangling reference");
	}
	
	@Test
	void exportableSortsByTypeNameThenLocationNullSafe() {
		AutoGenerationOption unnamedType = new AutoGenerationOption();
		unnamedType.setUuid("a");
		AutoGenerationOption noLocation = option("ID Type", null, "b");
		AutoGenerationOption withLocation = option("ID Type", "Ward", "c");
		
		List<AutoGenerationOption> result = AutoGenerationOptionDomainExporter
		        .exportable(Arrays.asList(withLocation, noLocation, unnamedType));
		
		assertEquals(Arrays.asList(unnamedType, noLocation, withLocation), result);
	}
	
	private static AutoGenerationOption option(String typeName, String locationName, String uuid) {
		AutoGenerationOption option = new AutoGenerationOption();
		option.setUuid(uuid);
		PatientIdentifierType type = new PatientIdentifierType();
		type.setName(typeName);
		option.setIdentifierType(type);
		if (locationName != null) {
			Location location = new Location();
			location.setName(locationName);
			option.setLocation(location);
		}
		return option;
	}
}
