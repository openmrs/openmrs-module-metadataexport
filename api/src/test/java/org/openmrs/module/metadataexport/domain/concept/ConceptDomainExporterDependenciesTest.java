/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.concept;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptAttribute;
import org.openmrs.ConceptAttributeType;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSource;
import org.openmrs.OpenmrsObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConceptDomainExporterDependenciesTest {
	
	private final ConceptDomainExporter exporter = new ConceptDomainExporter();
	
	private static Concept concept(String uuid) {
		Concept c = new Concept();
		c.setUuid(uuid);
		return c;
	}
	
	private HashSet<String> answerConceptUuids(Concept concept) {
		return exporter.getDependencies(concept).stream().filter(d -> d instanceof Concept).map(OpenmrsObject::getUuid)
		        .collect(Collectors.toCollection(HashSet::new));
	}
	
	private HashSet<String> membershipMemberUuids(Concept concept) {
		return exporter.getDependencies(concept).stream().filter(d -> d instanceof ConceptSet)
		        .map(d -> ((ConceptSet) d).getConcept().getUuid()).collect(Collectors.toCollection(HashSet::new));
	}
	
	@Test
	void getDependencies_collectsAnswerConceptsAndSetMembershipRows() {
		Concept c = concept("c");
		c.addAnswer(new ConceptAnswer(concept("answer1")));
		c.addAnswer(new ConceptAnswer(concept("answer2")));
		c.addSetMember(concept("member1"));
		c.addSetMember(concept("member2"));
		
		assertEquals(new HashSet<>(Arrays.asList("answer1", "answer2")), answerConceptUuids(c));
		assertEquals(new HashSet<>(Arrays.asList("member1", "member2")), membershipMemberUuids(c));
	}
	
	@Test
	void getDependencies_includesConceptClass() {
		Concept c = concept("c");
		ConceptClass conceptClass = new ConceptClass();
		c.setConceptClass(conceptClass);
		
		assertTrue(exporter.getDependencies(c).contains(conceptClass));
	}
	
	@Test
	void getDependencies_includesMappingSources() {
		Concept c = concept("c");
		ConceptSource ciel = new ConceptSource();
		ConceptSource snomed = new ConceptSource();
		ConceptMapType sameAs = new ConceptMapType();
		c.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(ciel, "1234", null), sameAs));
		c.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(snomed, "5678", null), sameAs));
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(c);
		
		assertTrue(dependencies.contains(ciel));
		assertTrue(dependencies.contains(snomed));
	}
	
	@Test
	void getDependencies_includesActiveAttributeTypesOnly() {
		Concept c = concept("c");
		ConceptAttributeType activeType = new ConceptAttributeType();
		ConceptAttributeType voidedType = new ConceptAttributeType();
		ConceptAttribute active = new ConceptAttribute();
		active.setAttributeType(activeType);
		ConceptAttribute voided = new ConceptAttribute();
		voided.setAttributeType(voidedType);
		voided.setVoided(true);
		c.addAttribute(active);
		c.addAttribute(voided);
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(c);
		
		assertTrue(dependencies.contains(activeType));
		assertFalse(dependencies.contains(voidedType));
	}
}
