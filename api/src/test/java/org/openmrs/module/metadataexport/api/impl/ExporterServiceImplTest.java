/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.impl;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.export.DomainExporter;
import org.openmrs.module.metadataexport.api.export.DomainExporterRegistry;
import org.openmrs.module.metadataexport.api.export.ExportContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExporterServiceImplTest {
	
	private static class RecordingExporter<T extends OpenmrsObject> implements DomainExporter<T> {
		
		@Getter
		private final Domain domain;
		
		private final Class<T> type;
		
		final List<T> allInstances = new ArrayList<>();
		
		final Map<String, Collection<? extends OpenmrsObject>> dependencies = new HashMap<>();
		
		final List<T> exported = new ArrayList<>();
		
		int exportCalls = 0;
		
		RecordingExporter(Domain domain, Class<T> type) {
			this.domain = domain;
			this.type = type;
		}
		
		public boolean handles(OpenmrsObject instance) {
			return type.isInstance(instance);
		}
		
		public Collection<T> getAllInstances() {
			return allInstances;
		}
		
		public Collection<? extends OpenmrsObject> getDependencies(T instance) {
			return dependencies.getOrDefault(instance.getUuid(), Collections.emptyList());
		}
		
		public void export(Collection<T> instances, ExportContext context) {
			exportCalls++;
			exported.addAll(instances);
		}
	}
	
	private static Concept concept(String uuid) {
		Concept c = new Concept();
		c.setUuid(uuid);
		return c;
	}
	
	private static EncounterType encounterType(String uuid) {
		EncounterType et = new EncounterType();
		et.setUuid(uuid);
		return et;
	}
	
	private static List<String> uuids(Collection<? extends OpenmrsObject> instances) {
		return instances.stream().map(OpenmrsObject::getUuid).sorted().collect(Collectors.toList());
	}
	
	private ExporterServiceImpl serviceWith(DomainExporter<?>... exporters) {
		return new ExporterServiceImpl(new DomainExporterRegistry(Arrays.asList(exporters)));
	}
	
	@Test
	void export_withNullDomains_exportsEveryRegisteredDomain(@TempDir File outDir) throws Exception {
		RecordingExporter<Concept> concepts = new RecordingExporter<>(Domain.CONCEPTS, Concept.class);
		concepts.allInstances.add(concept("c1"));
		RecordingExporter<EncounterType> encounters = new RecordingExporter<>(Domain.ENCOUNTER_TYPES, EncounterType.class);
		encounters.allInstances.add(encounterType("e1"));
		
		serviceWith(concepts, encounters).export(outDir, null);
		
		assertEquals(Collections.singletonList("c1"), uuids(concepts.exported));
		assertEquals(Collections.singletonList("e1"), uuids(encounters.exported));
	}
	
	@Test
	void export_withEmptyDomains_exportsEveryRegisteredDomain(@TempDir File outDir) throws Exception {
		RecordingExporter<Concept> concepts = new RecordingExporter<>(Domain.CONCEPTS, Concept.class);
		concepts.allInstances.add(concept("c1"));
		RecordingExporter<EncounterType> encounters = new RecordingExporter<>(Domain.ENCOUNTER_TYPES, EncounterType.class);
		encounters.allInstances.add(encounterType("e1"));
		
		serviceWith(concepts, encounters).export(outDir, Collections.emptyList());
		
		assertEquals(Collections.singletonList("c1"), uuids(concepts.exported));
		assertEquals(Collections.singletonList("e1"), uuids(encounters.exported));
	}
	
	@Test
	void export_withSubset_seedsOnlySelectedDomains(@TempDir File outDir) throws Exception {
		RecordingExporter<Concept> concepts = new RecordingExporter<>(Domain.CONCEPTS, Concept.class);
		concepts.allInstances.add(concept("c1"));
		RecordingExporter<EncounterType> encounters = new RecordingExporter<>(Domain.ENCOUNTER_TYPES, EncounterType.class);
		encounters.allInstances.add(encounterType("e1"));
		
		serviceWith(concepts, encounters).export(outDir, Collections.singletonList(Domain.CONCEPTS));
		
		assertEquals(Collections.singletonList("c1"), uuids(concepts.exported));
		assertEquals(0, encounters.exportCalls);
		assertTrue(encounters.exported.isEmpty());
	}
	
	@Test
	void export_followsCrossDomainDependencyClosure(@TempDir File outDir) throws Exception {
		Concept c1 = concept("c1");
		EncounterType pulled = encounterType("pulled");
		RecordingExporter<Concept> concepts = new RecordingExporter<>(Domain.CONCEPTS, Concept.class);
		concepts.allInstances.add(c1);
		concepts.dependencies.put("c1", Collections.singletonList(pulled));
		RecordingExporter<EncounterType> encounters = new RecordingExporter<>(Domain.ENCOUNTER_TYPES, EncounterType.class);
		
		serviceWith(concepts, encounters).export(outDir, Collections.singletonList(Domain.CONCEPTS));
		
		assertEquals(Collections.singletonList("c1"), uuids(concepts.exported));
		assertEquals(Collections.singletonList("pulled"), uuids(encounters.exported));
	}
	
	@Test
	void export_dispatchesEachDomainBucketToItsOwningExporter(@TempDir File outDir) throws Exception {
		RecordingExporter<Concept> concepts = new RecordingExporter<>(Domain.CONCEPTS, Concept.class);
		concepts.allInstances.addAll(Arrays.asList(concept("c1"), concept("c2")));
		RecordingExporter<EncounterType> encounters = new RecordingExporter<>(Domain.ENCOUNTER_TYPES, EncounterType.class);
		encounters.allInstances.add(encounterType("e1"));
		
		serviceWith(concepts, encounters).export(outDir, null);
		
		assertEquals(Arrays.asList("c1", "c2"), uuids(concepts.exported));
		assertEquals(Collections.singletonList("e1"), uuids(encounters.exported));
	}
}
