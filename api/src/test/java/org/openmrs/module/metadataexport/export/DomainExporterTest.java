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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainExporterTest {
	
	@Test
	void getInstancesByUuids_returnsOnlyTheRequestedInstances() {
		DomainExporter<EncounterType> exporter = new Stub(type("et-1"), type("et-2"), type("et-3"));
		
		Collection<EncounterType> found = exporter.getInstancesByUuids(Arrays.asList("et-1", "et-3"));
		
		assertEquals(Arrays.asList("et-1", "et-3"), uuids(found));
	}
	
	@Test
	void getInstancesByUuids_throwsNamingDomainAndEveryUnknownUuid() {
		DomainExporter<EncounterType> exporter = new Stub(type("et-1"));
		
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Arrays.asList("et-1", "nope-1", "nope-2")));
		
		assertTrue(e.getMessage().contains(Domain.ENCOUNTER_TYPES.toString()), e.getMessage());
		assertTrue(e.getMessage().contains("nope-1"), e.getMessage());
		assertTrue(e.getMessage().contains("nope-2"), e.getMessage());
	}
	
	@Test
	void getInstancesByUuids_reportsExcludedAndUnknownUuidsSeparatelyInOneMessage() {
		Stub exporter = new Stub(type("et-1"));
		exporter.exclusions.put("old-1", "old-1 is retired, and cannot be imported");
		exporter.exclusions.put("old-2", "old-2 is retired, and cannot be imported");
		
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Arrays.asList("et-1", "old-1", "nope-1")));
		
		assertTrue(e.getMessage().contains("old-1 is retired, and cannot be imported"), e.getMessage());
		assertTrue(e.getMessage().contains("Unknown uuids in domain ENCOUNTER_TYPES: [nope-1]"), e.getMessage());
		assertFalse(e.getMessage().contains("old-2"), "exclusions nobody asked for are not reported: " + e.getMessage());
		assertFalse(e.getMessage().contains("et-1"), "a resolvable uuid is not a problem: " + e.getMessage());
	}
	
	@Test
	void getInstancesByUuids_searchesTheCandidatesADomainNarrowsTo() {
		Stub exporter = new Stub(type("et-1"), type("et-2")) {
			
			@Override
			public Collection<EncounterType> candidatesFor(Collection<String> uuids) {
				return Collections.singletonList(type("et-2"));
			}
		};
		
		assertEquals(Collections.singletonList("et-2"), uuids(exporter.getInstancesByUuids(Arrays.asList("et-2"))));
		assertEquals(0, exporter.getAllInstancesCalls, "a narrowed lookup must not fall back to loading every row");
	}
	
	@Test
	void exclusions_helperNamesEachExcludedRowWithTheReason() {
		EncounterType live = type("live");
		EncounterType retired = type("old");
		retired.setRetired(true);
		
		Map<String, String> exclusions = DomainExporter.exclusions(Arrays.asList(live, retired), EncounterType::getRetired,
		    "is retired");
		
		assertEquals(Collections.singletonMap("old", "old is retired"), exclusions);
	}
	
	private static List<String> uuids(Collection<? extends OpenmrsObject> instances) {
		return instances.stream().map(OpenmrsObject::getUuid).collect(Collectors.toList());
	}
	
	private static EncounterType type(String uuid) {
		EncounterType type = new EncounterType();
		type.setUuid(uuid);
		return type;
	}
	
	private static class Stub implements DomainExporter<EncounterType> {
		
		private final List<EncounterType> instances;
		
		final Map<String, String> exclusions = new LinkedHashMap<>();
		
		int getAllInstancesCalls;
		
		Stub(EncounterType... instances) {
			this.instances = Arrays.asList(instances);
		}
		
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
			getAllInstancesCalls++;
			return instances;
		}
		
		@Override
		public Map<String, String> exclusions() {
			return exclusions;
		}
		
		@Override
		public Collection<? extends OpenmrsObject> getDependencies(EncounterType instance) {
			return Collections.emptyList();
		}
		
		@Override
		public void export(Collection<EncounterType> toExport, ExportContext context) {
		}
	}
}
