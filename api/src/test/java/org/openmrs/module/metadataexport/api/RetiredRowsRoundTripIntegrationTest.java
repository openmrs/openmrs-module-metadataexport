/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptName;
import org.openmrs.EncounterType;
import org.openmrs.RelationshipType;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.InitializerConstants;
import org.openmrs.module.initializer.api.CsvFailingLines;
import org.openmrs.module.initializer.api.CsvParser;
import org.openmrs.module.metadataexport.domain.concept.ConceptDomainExporter;
import org.openmrs.module.metadataexport.domain.encounter.EncounterTypeDomainExporter;
import org.openmrs.module.metadataexport.domain.relationshiptype.RelationshipTypeDomainExporter;
import org.openmrs.module.metadataexport.export.ExportContext;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Retired metadata must be exported as complete rows, not uuid-only stubs: Initializer only skips
 * the columns of a retired row when the target already has the object, and otherwise creates the
 * object from the row before retiring it. A stub therefore fails validation on a fresh target and
 * takes every live row that references it down with it. This replays the exported files through
 * Initializer's own parsers, first onto a target that lacks the objects and then onto one that
 * already has them.
 */
class RetiredRowsRoundTripIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_CONCEPT_UUID = "984ad3a7-0b5c-4e2b-9b6f-1d2c3e4f5a60";
	
	private static final String RETIRED_CONCEPT_UUID = "162339ab-7c8d-4e9f-a0b1-c2d3e4f5a6b7";
	
	private static final String RETIRED_RELATIONSHIP_TYPE_UUID = "3982f469-cedc-4b2d-91ea-fe38f881e1a0";
	
	private static final String RETIRED_ENCOUNTER_TYPE_UUID = "8e4d3f5b-6a7c-4d9e-b1f2-3a4b5c6d7e8a";
	
	@BeforeEach
	void seedALiveConceptAnsweredByARetiredOne() {
		ConceptService conceptService = Context.getConceptService();
		
		Concept retired = new Concept();
		retired.setUuid(RETIRED_CONCEPT_UUID);
		retired.addName(fullySpecifiedName("Withdrawn vaccine"));
		retired.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
		retired.setConceptClass(conceptService.getConceptClassByName("Misc"));
		conceptService.saveConcept(retired);
		conceptService.retireConcept(retired, "Superseded");
		
		Concept live = new Concept();
		live.setUuid(LIVE_CONCEPT_UUID);
		live.addName(fullySpecifiedName("Immunizations"));
		live.setDatatype(conceptService.getConceptDatatypeByName("Coded"));
		live.setConceptClass(conceptService.getConceptClassByName("Question"));
		live.addAnswer(new ConceptAnswer(retired));
		conceptService.saveConcept(live);
		
		RelationshipType relationshipType = new RelationshipType();
		relationshipType.setUuid(RETIRED_RELATIONSHIP_TYPE_UUID);
		relationshipType.setaIsToB("Uncle");
		relationshipType.setbIsToA("Nephew");
		relationshipType.setDescription("A relationship of an uncle and his nephew");
		personService().saveRelationshipType(relationshipType);
		personService().retireRelationshipType(relationshipType, "No longer used");
		
		// A domain whose Initializer parser also looks rows up by name; this name is unique, so it round-trips.
		EncounterType encounterType = new EncounterType("Withdrawn intake", "Replaced by the triage form");
		encounterType.setUuid(RETIRED_ENCOUNTER_TYPE_UUID);
		encounterService().saveEncounterType(encounterType);
		encounterService().retireEncounterType(encounterType, "Discontinued");
		
		Context.flushSession();
		Context.clearSession();
	}
	
	@Test
	void export_thenReimportOntoATargetThatLacksTheRetiredObjects(@TempDir File outDir) throws Exception {
		exportSeededRows(outDir);
		purgeSeededRows();
		
		CsvFailingLines concepts = replay(conceptsCsv(outDir), Domain.CONCEPTS);
		CsvFailingLines relationshipTypes = replay(relationshipTypesCsv(outDir), Domain.RELATIONSHIP_TYPES);
		CsvFailingLines encounterTypes = replay(encounterTypesCsv(outDir), Domain.ENCOUNTER_TYPES);
		
		assertTrue(concepts.getFailingLines().isEmpty(), describe(concepts));
		assertTrue(relationshipTypes.getFailingLines().isEmpty(), describe(relationshipTypes));
		assertTrue(encounterTypes.getFailingLines().isEmpty(), describe(encounterTypes));
		
		Concept retired = Context.getConceptService().getConceptByUuid(RETIRED_CONCEPT_UUID);
		assertNotNull(retired, "the retired concept must be created on the fresh target");
		assertTrue(retired.getRetired(), "and retired once created");
		assertEquals("Withdrawn vaccine", retired.getName(Locale.ENGLISH).getName());
		
		Concept live = Context.getConceptService().getConceptByUuid(LIVE_CONCEPT_UUID);
		assertNotNull(live, "the live concept must load, which needs its retired answer to exist");
		assertEquals(Collections.singletonList(RETIRED_CONCEPT_UUID),
		    live.getAnswers().stream().map(a -> a.getAnswerConcept().getUuid()).collect(Collectors.toList()));
		
		RelationshipType relationshipType = personService().getRelationshipTypeByUuid(RETIRED_RELATIONSHIP_TYPE_UUID);
		assertNotNull(relationshipType, "the retired relationship type must be created on the fresh target");
		assertTrue(relationshipType.getRetired());
		assertEquals("Uncle", relationshipType.getaIsToB());
		assertEquals("Nephew", relationshipType.getbIsToA());
		
		EncounterType encounterType = encounterService().getEncounterTypeByUuid(RETIRED_ENCOUNTER_TYPE_UUID);
		assertNotNull(encounterType, "the retired encounter type must be created on the fresh target under its own uuid");
		assertTrue(encounterType.getRetired());
		assertEquals("Withdrawn intake", encounterType.getName());
	}
	
	@Test
	void export_thenReimportOntoATargetThatAlreadyHasTheRetiredObjects(@TempDir File outDir) throws Exception {
		exportSeededRows(outDir);
		
		CsvFailingLines concepts = replay(conceptsCsv(outDir), Domain.CONCEPTS);
		CsvFailingLines relationshipTypes = replay(relationshipTypesCsv(outDir), Domain.RELATIONSHIP_TYPES);
		
		assertTrue(concepts.getFailingLines().isEmpty(), describe(concepts));
		assertTrue(relationshipTypes.getFailingLines().isEmpty(), describe(relationshipTypes));
		// Initializer does not re-fill an existing retired row; it re-applies the flag and stamps its own
		// retire reason over the seeded one, which is what shows the rows were processed rather than skipped.
		Concept retired = Context.getConceptService().getConceptByUuid(RETIRED_CONCEPT_UUID);
		assertTrue(retired.getRetired());
		assertEquals(InitializerConstants.DEFAULT_RETIRE_REASON, retired.getRetireReason());
		RelationshipType relationshipType = personService().getRelationshipTypeByUuid(RETIRED_RELATIONSHIP_TYPE_UUID);
		assertTrue(relationshipType.getRetired());
		assertEquals(InitializerConstants.DEFAULT_RETIRE_REASON, relationshipType.getRetireReason());
		assertEquals(1, Context.getConceptService().getConceptByUuid(LIVE_CONCEPT_UUID).getAnswers().size());
	}
	
	private void exportSeededRows(File outDir) throws Exception {
		ConceptService conceptService = Context.getConceptService();
		Concept live = conceptService.getConceptByUuid(LIVE_CONCEPT_UUID);
		Concept retired = conceptService.getConceptByUuid(RETIRED_CONCEPT_UUID);
		// The live row first, so the replay has to cope with a forward reference like a real package.
		new ConceptDomainExporter().export(Arrays.asList(live, retired), new ExportContext(outDir));
		new RelationshipTypeDomainExporter().export(
		    Collections.singletonList(personService().getRelationshipTypeByUuid(RETIRED_RELATIONSHIP_TYPE_UUID)),
		    new ExportContext(outDir));
		new EncounterTypeDomainExporter().export(
		    Collections.singletonList(encounterService().getEncounterTypeByUuid(RETIRED_ENCOUNTER_TYPE_UUID)),
		    new ExportContext(outDir));
		assertTrue(conceptsCsv(outDir).exists(), "expected " + conceptsCsv(outDir));
		assertTrue(relationshipTypesCsv(outDir).exists(), "expected " + relationshipTypesCsv(outDir));
		assertTrue(encounterTypesCsv(outDir).exists(), "expected " + encounterTypesCsv(outDir));
	}
	
	private void purgeSeededRows() {
		ConceptService conceptService = Context.getConceptService();
		conceptService.purgeConcept(conceptService.getConceptByUuid(LIVE_CONCEPT_UUID));
		conceptService.purgeConcept(conceptService.getConceptByUuid(RETIRED_CONCEPT_UUID));
		personService().purgeRelationshipType(personService().getRelationshipTypeByUuid(RETIRED_RELATIONSHIP_TYPE_UUID));
		encounterService().purgeEncounterType(encounterService().getEncounterTypeByUuid(RETIRED_ENCOUNTER_TYPE_UUID));
		Context.flushSession();
		Context.clearSession();
		assertNull(conceptService.getConceptByUuid(LIVE_CONCEPT_UUID), "the target must start without the concepts");
		assertNull(conceptService.getConceptByUuid(RETIRED_CONCEPT_UUID));
		assertNull(personService().getRelationshipTypeByUuid(RETIRED_RELATIONSHIP_TYPE_UUID));
		assertNull(encounterService().getEncounterTypeByUuid(RETIRED_ENCOUNTER_TYPE_UUID));
	}
	
	private static File conceptsCsv(File outDir) {
		return outDir.toPath().resolve(Paths.get("configuration", Domain.CONCEPTS.getName(), "concepts.csv")).toFile();
	}
	
	private static File encounterTypesCsv(File outDir) {
		return outDir.toPath().resolve(Paths.get("configuration", Domain.ENCOUNTER_TYPES.getName(), "encounterTypes.csv"))
		        .toFile();
	}
	
	private static File relationshipTypesCsv(File outDir) {
		return outDir.toPath()
		        .resolve(Paths.get("configuration", Domain.RELATIONSHIP_TYPES.getName(), "relationshipTypes.csv")).toFile();
	}
	
	/**
	 * Feeds the exported file back through the Initializer parser registered for the domain, the only
	 * thing that shows the file is loadable rather than merely well-shaped. Like Initializer's loader,
	 * lines that fail are retried while each pass resolves at least one of them, which is how a row
	 * that references a later row in the same file gets through.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static CsvFailingLines replay(File csv, Domain domain) throws Exception {
		CsvParser parser = Context.getRegisteredComponents(CsvParser.class).stream().filter(p -> p.getDomain() == domain)
		        .findFirst().orElseThrow(() -> new AssertionError("no Initializer parser registered for " + domain));
		try (InputStream in = new FileInputStream(csv)) {
			parser.setInputStream(in);
			List<String[]> lines = parser.getLines();
			CsvFailingLines failed = parser.process(lines);
			while (!failed.getFailingLines().isEmpty() && failed.getFailingLines().size() < lines.size()) {
				lines = failed.getFailingLines();
				failed = parser.process(lines);
			}
			return failed;
		}
	}
	
	private static String describe(CsvFailingLines failed) {
		return failed.getErrorDetails().stream()
		        .map(d -> d.getCsvLine().prettyPrint() + " -> " + ExceptionUtils.getRootCauseMessage(d.getException()))
		        .collect(Collectors.joining("\n", "Iniz rejected exported lines:\n", ""));
	}
	
	private static ConceptName fullySpecifiedName(String name) {
		ConceptName conceptName = new ConceptName(name, Locale.ENGLISH);
		conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		return conceptName;
	}
	
	private static PersonService personService() {
		return Context.getPersonService();
	}
	
	private static EncounterService encounterService() {
		return Context.getEncounterService();
	}
}
