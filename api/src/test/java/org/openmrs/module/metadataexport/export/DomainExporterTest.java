/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.export;

import org.junit.jupiter.api.Test;
import org.openmrs.EncounterType;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.module.initializer.Domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainExporterTest {
	
	@Test
	void getInstancesByUuids_returnsOnlyTheRequestedInstances() {
		DomainExporter<EncounterType> exporter = exporterWith(type("et-1"), type("et-2"), type("et-3"));
		
		Collection<EncounterType> found = exporter.getInstancesByUuids(Arrays.asList("et-1", "et-3"));
		
		assertEquals(Arrays.asList("et-1", "et-3"), found.stream().map(OpenmrsObject::getUuid).collect(Collectors.toList()));
	}
	
	@Test
	void getInstancesByUuids_throwsNamingDomainAndEveryUnknownUuid() {
		DomainExporter<EncounterType> exporter = exporterWith(type("et-1"));
		
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Arrays.asList("et-1", "nope-1", "nope-2")));
		
		assertTrue(e.getMessage().contains(Domain.ENCOUNTER_TYPES.toString()), e.getMessage());
		assertTrue(e.getMessage().contains("nope-1"), e.getMessage());
		assertTrue(e.getMessage().contains("nope-2"), e.getMessage());
	}
	
	private static EncounterType type(String uuid) {
		EncounterType type = new EncounterType();
		type.setUuid(uuid);
		return type;
	}
	
	private static DomainExporter<EncounterType> exporterWith(EncounterType... instances) {
		return new DomainExporter<EncounterType>() {
			
			@Override
			public Domain getDomain() {
				return Domain.ENCOUNTER_TYPES;
			}
			
			@Override
			public boolean handles(OpenmrsObject instance) {
				return instance instanceof EncounterType;
			}
			
			@Override
			public Collection<EncounterType> getAllInstances() {
				return Arrays.asList(instances);
			}
			
			@Override
			public Collection<? extends OpenmrsObject> getDependencies(EncounterType instance) {
				return Collections.emptyList();
			}
			
			@Override
			public void export(Collection<EncounterType> toExport, ExportContext context) {
			}
		};
	}
}
