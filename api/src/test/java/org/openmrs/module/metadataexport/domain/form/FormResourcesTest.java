/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.form;

import org.junit.jupiter.api.Test;
import org.openmrs.Form;
import org.openmrs.FormResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormResourcesTest {
	
	@Test
	void schemaResource_isTheJsonSchemaResourceWithAClobDatatype() {
		FormResource schema = resource("JSON schema", FormResources.AMPATH_JSON_SCHEMA_DATATYPE);
		FormResource other = resource("something else", FormResources.AMPATH_JSON_SCHEMA_DATATYPE);
		
		assertSame(schema, FormResources.schemaResource(Arrays.asList(other, schema)));
		assertTrue(FormResources.isAmpathForm(Collections.singletonList(schema)));
	}
	
	@Test
	void schemaResource_acceptsTheLongFreeTextDatatypeToo() {
		FormResource schema = resource("JSON schema", FormResources.LONG_FREE_TEXT_DATATYPE);
		
		assertSame(schema, FormResources.schemaResource(Collections.singletonList(schema)));
	}
	
	@Test
	void schemaResource_rejectsOtherDatatypesAndNames() {
		assertNull(FormResources.schemaResource(
		    Collections.singletonList(resource("JSON schema", "org.openmrs.customdatatype.datatype.FreeTextDatatype"))));
		assertNull(FormResources.schemaResource(
		    Collections.singletonList(resource("json schema", FormResources.AMPATH_JSON_SCHEMA_DATATYPE))));
		assertFalse(FormResources.isAmpathForm(Collections.emptyList()));
	}
	
	@Test
	void isTranslation_matchesInitializersResourceNamingWithAClobDatatype() {
		FormResource fr = resource("Triage Form_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE);
		FormResource ampath = resource("Triage Form_translations_en_GB", FormResources.AMPATH_JSON_SCHEMA_DATATYPE);
		
		assertTrue(FormResources.isTranslation(fr));
		assertTrue(FormResources.isTranslation(ampath));
		assertEquals("fr", FormResources.languageOf(fr));
		assertEquals("en_GB", FormResources.languageOf(ampath));
	}
	
	@Test
	void isTranslation_acceptsTheFormBuildersDatatypeLessResources() {
		FormResource formBuilder = resource("Triage Form_translations_de", null);
		
		assertTrue(FormResources.isTranslation(formBuilder),
		    "the Form Builder saves translations with only a name and a value reference");
		assertEquals("de", FormResources.languageOf(formBuilder));
	}
	
	@Test
	void isTranslation_rejectsOtherResources() {
		assertFalse(FormResources.isTranslation(resource("JSON schema", FormResources.AMPATH_JSON_SCHEMA_DATATYPE)));
		assertFalse(FormResources.isTranslation(resource("Triage_translations_fr", "some.other.Datatype")));
		assertFalse(FormResources.isTranslation(resource("_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE)));
		assertFalse(FormResources.isTranslation(resource(null, FormResources.LONG_FREE_TEXT_DATATYPE)));
		assertFalse(FormResources.isTranslation(null));
		assertNull(FormResources.languageOf(resource("JSON schema", FormResources.LONG_FREE_TEXT_DATATYPE)));
	}
	
	@Test
	void translationsOf_keepsOnlyTheTranslationResources() {
		FormResource schema = resource("JSON schema", FormResources.AMPATH_JSON_SCHEMA_DATATYPE);
		FormResource fr = resource("Triage_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE);
		FormResource es = resource("Triage_translations_es", FormResources.LONG_FREE_TEXT_DATATYPE);
		
		assertEquals(Arrays.asList(fr, es), FormResources.translationsOf(Arrays.asList(schema, fr, es)));
	}
	
	@Test
	void parseJsonObject_keepsTheDiagnosis() {
		assertTrue(FormResources.parseJsonObject("{\"name\": ").problem.startsWith("it is not valid JSON ("));
		assertEquals("it is a JSON array, not an object", FormResources.parseJsonObject("[1, 2]").problem);
		assertEquals("its content is empty", FormResources.parseJsonObject(null).problem);
		assertNull(FormResources.parseJsonObject("{}").problem);
	}
	
	@Test
	void latestVersionPerName_explainsEachDroppedVersion() {
		Form v1 = form("Triage", "1.0");
		v1.setUuid("old");
		Form v2 = form("Triage", "2.0");
		v2.setUuid("new");
		Map<String, String> exclusions = new HashMap<>();
		
		FormResources.latestVersionPerName(Arrays.asList(v1, v2), exclusions);
		
		assertEquals(Collections.singleton("old"), exclusions.keySet());
		assertTrue(exclusions.get("old").startsWith("old (Triage v1.0) is superseded by new (Triage v2.0)"),
		    exclusions.get("old"));
	}
	
	@Test
	void asJsonObject_acceptsOnlyAJsonObject() {
		assertEquals("x", FormResources.asJsonObject("{\"name\":\"x\"}").get("name").asText());
		assertNull(FormResources.asJsonObject("[1, 2]"));
		assertNull(FormResources.asJsonObject("\"just a string\""));
		assertNull(FormResources.asJsonObject("not json at all"));
		assertNull(FormResources.asJsonObject(null));
	}
	
	@Test
	void fileName_isLowerCaseAndFileSystemSafe() {
		assertEquals("triage_form__v2_", FormResources.fileName("Triage Form (v2)"));
		assertEquals("a.b-c_d", FormResources.fileName("  A.b-C/d "));
		assertEquals("_", FormResources.fileName(null));
		assertEquals("_", FormResources.fileName("   "));
	}
	
	@Test
	void latestVersionPerName_keepsTheHighestVersionOfEachName() {
		Form v1 = form("Triage", "1.0");
		Form v2 = form("Triage", "2.0");
		Form v10 = form("Triage", "10.0");
		Form other = form("Other", "1.0");
		
		List<Form> latest = FormResources.latestVersionPerName(Arrays.asList(v1, v10, other, v2), null);
		
		assertEquals(Arrays.asList(v10, other), latest, "10.0 must sort above 2.0, i.e. numerically not lexically");
	}
	
	@Test
	void latestVersionPerName_breaksAVersionTieByCreationDate() {
		Form older = form("Triage", "1.0");
		older.setDateCreated(new Date(1000));
		Form newer = form("Triage", "1.0");
		newer.setDateCreated(new Date(2000));
		
		assertEquals(Collections.singletonList(newer),
		    FormResources.latestVersionPerName(Arrays.asList(newer, older), null));
		assertEquals(Collections.singletonList(newer),
		    FormResources.latestVersionPerName(Arrays.asList(older, newer), null));
	}
	
	@Test
	void latestVersionPerName_keysOnTheExactNameLikeInitializerDoes() {
		Form triage = form("Triage", "1.0");
		Form triageWithSpace = form("Triage ", "1.0");
		
		assertEquals(2, FormResources.latestVersionPerName(Arrays.asList(triage, triageWithSpace), null).size(),
		    "Initializer's getForm(name) is exact, so both are separately importable");
	}
	
	@Test
	void latestVersionPerName_stillKeepsExactlyOneOnAFullTie() {
		Form a = form("Triage", "1.0");
		Form b = form("Triage", "1.0");
		
		List<Form> kept = FormResources.latestVersionPerName(Arrays.asList(a, b), null);
		
		assertEquals(1, kept.size());
		assertTrue(kept.contains(a) || kept.contains(b));
	}
	
	@Test
	void compareVersions_fallsBackToStringOrderWhereCoreRanksVersionsEqual() {
		assertTrue(FormResources.compareVersions("2.0", "1.9") > 0);
		assertTrue(FormResources.compareVersions("alpha", "beta") < 0, "core ranks non-numeric versions equal");
		assertTrue(FormResources.compareVersions("1.0", "1") != 0, "deterministic even where core says equal");
		assertEquals(0, FormResources.compareVersions(null, ""));
		assertEquals(0, FormResources.compareVersions("2.0", " 2.0 "));
	}
	
	@Test
	void uniqueStems_disambiguatesNamesThatSanitizeAlike() {
		Form triage = form("Triage", "1.0");
		triage.setUuid("a");
		Form upper = form("TRIAGE", "2.0");
		upper.setUuid("b");
		Form lower = form("triage", "2.0");
		lower.setUuid("c");
		Form other = form("Other", "1.0");
		other.setUuid("d");
		
		Map<String, String> stems = FormResources.uniqueStems(Arrays.asList(triage, upper, lower, other));
		
		assertEquals("other", stems.get("d"), "an unambiguous name keeps its plain stem");
		assertEquals("triage_1.0", stems.get("a"), "a collision gets the version appended");
		assertEquals("triage_2.0_b", stems.get("b"), "a collision on version too gets the uuid appended");
		assertEquals("triage_2.0_c", stems.get("c"));
		assertEquals(4, new HashSet<>(stems.values()).size());
	}
	
	@Test
	void uniqueStems_survivesANameThatAlreadyLooksLikeASuffixedStem() {
		Form triage = form("Triage", "1.0");
		triage.setUuid("a");
		Form lower = form("triage", "2.0");
		lower.setUuid("b");
		Form lookalike = form("Triage 1.0", "3.0");
		lookalike.setUuid("c");
		
		Map<String, String> stems = FormResources.uniqueStems(Arrays.asList(triage, lower, lookalike));
		
		assertEquals("triage_1.0_a", stems.get("a"), "collides with the look-alike after suffixing, so gets its uuid too");
		assertEquals("triage_2.0", stems.get("b"));
		assertEquals("triage_1.0_c", stems.get("c"));
		assertEquals(3, new HashSet<>(stems.values()).size());
	}
	
	private static Form form(String name, String version) {
		Form form = new Form();
		form.setName(name);
		form.setVersion(version);
		return form;
	}
	
	private static FormResource resource(String name, String datatype) {
		FormResource resource = new FormResource();
		resource.setName(name);
		resource.setDatatypeClassname(datatype);
		return resource;
	}
}
