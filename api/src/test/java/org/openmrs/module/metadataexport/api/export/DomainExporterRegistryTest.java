/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.export;

import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainExporterRegistryTest {
	
	private static class StubExporter<T extends OpenmrsObject> implements DomainExporter<T> {
		
		@Getter
		private final Domain domain;
		
		private final Class<T> type;
		
		StubExporter(Domain domain, Class<T> type) {
			this.domain = domain;
			this.type = type;
		}
		
		public boolean handles(OpenmrsObject instance) {
			return type.isInstance(instance);
		}
		
		public Collection<T> getAllInstances() {
			return Collections.emptyList();
		}
		
		public Collection<? extends OpenmrsObject> getDependencies(T instance) {
			return Collections.emptyList();
		}
		
		public void export(Collection<T> instances, ExportContext context) {
		}
	}
	
	@Test
	void forObject_returnsFirstHandlerInRegistrationOrder() {
		StubExporter<Concept> first = new StubExporter<>(Domain.CONCEPTS, Concept.class);
		StubExporter<Concept> second = new StubExporter<>(Domain.CONCEPTS, Concept.class);
		DomainExporterRegistry registry = new DomainExporterRegistry(Arrays.asList(first, second));
		
		assertSame(first, registry.forObject(new Concept()));
	}
	
	@Test
	void forObject_returnsNullWhenNoExporterHandlesTheObject() {
		DomainExporterRegistry registry = new DomainExporterRegistry(
		        Collections.singletonList(new StubExporter<>(Domain.CONCEPTS, Concept.class)));
		
		assertNull(registry.forObject(new EncounterType()));
	}
	
	@Test
	void forDomain_matchesByDomainAndIsNullWhenAbsent() {
		StubExporter<Concept> concepts = new StubExporter<>(Domain.CONCEPTS, Concept.class);
		StubExporter<EncounterType> encounters = new StubExporter<>(Domain.ENCOUNTER_TYPES, EncounterType.class);
		DomainExporterRegistry registry = new DomainExporterRegistry(Arrays.asList(concepts, encounters));
		
		assertSame(encounters, registry.forDomain(Domain.ENCOUNTER_TYPES));
		assertNull(registry.forDomain(Domain.ROLES));
	}
	
	@Test
	void all_isUnmodifiable() {
		DomainExporterRegistry registry = new DomainExporterRegistry(
		        Collections.singletonList(new StubExporter<>(Domain.CONCEPTS, Concept.class)));
		
		assertThrows(UnsupportedOperationException.class,
		    () -> registry.all().add(new StubExporter<>(Domain.ROLES, org.openmrs.Role.class)));
	}
}
