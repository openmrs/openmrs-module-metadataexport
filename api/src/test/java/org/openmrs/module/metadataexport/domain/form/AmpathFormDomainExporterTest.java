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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmpathFormDomainExporterTest {
	
	private static final String SCHEMA = "{\"uuid\":\"xxxx\",\"name\":\"Old name\",\"version\":\"1\",\"description\":\"old\","
	        + "\"published\":false,\"retired\":true,\"encounter\":\"Emergency\",\"processor\":\"EncounterFormProcessor\","
	        + "\"pages\":[{\"label\":\"Page 1\"}]}";
	
	private final AmpathFormDomainExporter exporter = new AmpathFormDomainExporter();
	
	@Test
	void ownsTheAmpathFormsDomain() {
		assertEquals(Domain.AMPATH_FORMS, exporter.getDomain());
	}
	
	@Test
	void toSchema_refreshesTheKeysInitializerReadsFromTheFormRow() throws Exception {
		Form form = form("Triage", "2.0");
		form.setDescription("Triage at the door");
		form.setPublished(true);
		form.setRetired(false);
		form.setEncounterType(encounterType("Scheduled"));
		
		ObjectNode schema = AmpathFormDomainExporter.toSchema(form, schema());
		
		assertEquals("Triage", schema.get("name").asText());
		assertEquals("2.0", schema.get("version").asText());
		assertEquals("Triage at the door", schema.get("description").asText());
		assertTrue(schema.get("published").asBoolean());
		assertFalse(schema.get("retired").asBoolean());
		assertEquals("Scheduled", schema.get("encounter").asText());
	}
	
	@Test
	void toSchema_writesTheRetiredFlagTruthfully() {
		Form form = form("Triage", "2.0");
		form.setRetired(true);
		
		assertTrue(AmpathFormDomainExporter.toSchema(form, schema()).get("retired").asBoolean(),
		    "selection never passes a retired form, but should one arrive it must not be resurrected live");
	}
	
	@Test
	void toSchema_leavesTheRestOfTheStoredSchemaUntouched() throws Exception {
		ObjectNode schema = AmpathFormDomainExporter.toSchema(form("Triage", "2.0"), schema());
		
		assertEquals("xxxx", schema.get("uuid").asText(), "Initializer ignores the uuid, so it is passed through");
		assertEquals("EncounterFormProcessor", schema.get("processor").asText());
		assertEquals("Page 1", schema.get("pages").get(0).get("label").asText());
	}
	
	@Test
	void toSchema_treatsAnUnsetPublishedFlagAsFalse() throws Exception {
		Form form = form("Triage", "2.0");
		form.setPublished(null);
		
		ObjectNode schema = AmpathFormDomainExporter.toSchema(form, schema());
		
		assertFalse(schema.get("published").asBoolean(), "the loader unboxes the flag, so it must always be present");
		assertFalse(schema.get("retired").asBoolean());
	}
	
	@Test
	void toSchema_dropsDescriptionAndEncounterWhenTheFormHasNone() throws Exception {
		Form form = form("Triage", "2.0");
		form.setDescription(null);
		form.setEncounterType(null);
		
		ObjectNode schema = AmpathFormDomainExporter.toSchema(form, schema());
		
		assertFalse(schema.has("description"));
		assertFalse(schema.has("encounter"), "a stale encounter name from the stored schema must not survive");
	}
	
	@Test
	void needsEncounter_mirrorsTheLoader() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		assertTrue(AmpathFormDomainExporter.needsEncounter((ObjectNode) mapper.readTree("{}")));
		assertTrue(AmpathFormDomainExporter.needsEncounter((ObjectNode) mapper.readTree("{\"processor\":null}")));
		assertTrue(AmpathFormDomainExporter.needsEncounter((ObjectNode) mapper.readTree("{\"processor\":\" \"}")));
		assertTrue(AmpathFormDomainExporter
		        .needsEncounter((ObjectNode) mapper.readTree("{\"processor\":\"encounterformprocessor\"}")));
		assertFalse(AmpathFormDomainExporter
		        .needsEncounter((ObjectNode) mapper.readTree("{\"processor\":\"ProgramEnrollmentProcessor\"}")));
	}
	
	@Test
	void dependencies_areTheEncounterTypeAndTheTranslationResources() {
		Form form = form("Triage", "2.0");
		EncounterType encounterType = encounterType("Scheduled");
		form.setEncounterType(encounterType);
		FormResource schema = resource("JSON schema", FormResources.AMPATH_JSON_SCHEMA_DATATYPE);
		FormResource fr = resource("Triage_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE);
		
		List<OpenmrsObject> dependencies = AmpathFormDomainExporter.dependencies(form, Arrays.asList(schema, fr));
		
		assertEquals(Arrays.<OpenmrsObject> asList(encounterType, fr), dependencies);
	}
	
	@Test
	void dependencies_areJustTheTranslationsWhenThereIsNoEncounterType() {
		Form form = form("Triage", "2.0");
		
		assertTrue(AmpathFormDomainExporter.dependencies(form, Collections.emptyList()).isEmpty());
	}
	
	private static ObjectNode schema() {
		return FormResources.asJsonObject(SCHEMA);
	}
	
	private static Form form(String name, String version) {
		Form form = new Form();
		form.setName(name);
		form.setVersion(version);
		return form;
	}
	
	private static EncounterType encounterType(String name) {
		EncounterType type = new EncounterType();
		type.setName(name);
		return type;
	}
	
	private static FormResource resource(String name, String datatype) {
		FormResource resource = new FormResource();
		resource.setName(name);
		resource.setDatatypeClassname(datatype);
		return resource;
	}
}
