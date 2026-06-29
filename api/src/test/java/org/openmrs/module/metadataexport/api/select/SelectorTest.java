/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.select;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.export.DomainExporter;
import org.openmrs.module.metadataexport.api.export.DomainExporterRegistry;
import org.openmrs.module.metadataexport.api.export.ExportContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectorTest {
	
	/** A test-double DomainExporter over concepts with a configurable dependency graph (by uuid). */
	private static class FakeConceptExporter implements DomainExporter<Concept> {
		
		final Map<String, List<Concept>> dependencies = new HashMap<>();
		
		public Domain getDomain() {
			return Domain.CONCEPTS;
		}
		
		public boolean handles(OpenmrsObject instance) {
			return instance instanceof Concept;
		}
		
		public Collection<Concept> getAllInstances() {
			return Collections.emptyList();
		}
		
		public Collection<? extends OpenmrsObject> getDependencies(Concept instance) {
			return dependencies.getOrDefault(instance.getUuid(), Collections.emptyList());
		}
		
		public void export(Collection<Concept> instances, ExportContext context) {
			// not exercised here
		}
	}
	
	private static Concept concept(String uuid) {
		Concept c = new Concept();
		c.setUuid(uuid);
		return c;
	}
	
	private Selector selectorWith(FakeConceptExporter exporter) {
		return new Selector(new DomainExporterRegistry(Collections.<DomainExporter<?>> singletonList(exporter)));
	}
	
	private List<String> conceptUuids(ExportManifest manifest) {
		return manifest.get(Domain.CONCEPTS).stream().map(OpenmrsObject::getUuid).collect(Collectors.toList());
	}
	
	@Test
	void select_collectsTransitiveDependencies() {
		Concept a = concept("a"), b = concept("b"), c = concept("c");
		FakeConceptExporter exporter = new FakeConceptExporter();
		exporter.dependencies.put("a", Arrays.asList(b));
		exporter.dependencies.put("b", Arrays.asList(c));
		
		ExportManifest manifest = selectorWith(exporter).select(Arrays.asList(a));
		
		assertEquals(new HashSet<>(Arrays.asList("a", "b", "c")), new HashSet<>(conceptUuids(manifest)));
	}
	
	@Test
	void select_isCycleSafe() {
		Concept a = concept("a"), b = concept("b");
		FakeConceptExporter exporter = new FakeConceptExporter();
		exporter.dependencies.put("a", Arrays.asList(b));
		exporter.dependencies.put("b", Arrays.asList(a)); // cycle
		
		ExportManifest manifest = selectorWith(exporter).select(Arrays.asList(a));
		
		assertEquals(2, conceptUuids(manifest).size());
	}
	
	@Test
	void select_visitsSharedDependencyOnce() {
		Concept a = concept("a"), b = concept("b"), c = concept("c"), d = concept("d");
		FakeConceptExporter exporter = new FakeConceptExporter();
		exporter.dependencies.put("a", Arrays.asList(b, c)); // diamond: b and c both depend on d
		exporter.dependencies.put("b", Arrays.asList(d));
		exporter.dependencies.put("c", Arrays.asList(d));
		
		ExportManifest manifest = selectorWith(exporter).select(Arrays.asList(a));
		
		List<String> uuids = conceptUuids(manifest);
		assertEquals(4, uuids.size());
		assertEquals(1, uuids.stream().filter("d"::equals).count(), "shared member should appear once");
	}
	
	@Test
	void select_skipsObjectsNoDomainOwns() {
		Concept a = concept("a");
		EncounterType orphan = new EncounterType();
		orphan.setUuid("et"); // no registered exporter handles EncounterType here
		
		FakeConceptExporter exporter = new FakeConceptExporter();
		
		ExportManifest manifest = selectorWith(exporter).select(Arrays.asList(a, orphan));
		
		assertTrue(conceptUuids(manifest).contains("a"));
		assertFalse(manifest.getDomains().contains(Domain.ENCOUNTER_TYPES), "unowned object must be skipped");
	}
}
