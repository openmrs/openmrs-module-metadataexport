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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.Location;
import org.openmrs.module.initializer.Domain;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmpathFormTranslationDomainExporterTest {
	
	private static final String TRANSLATIONS = "{\"uuid\":\"c5bf3efe\",\"form\":\"Old name\",\"form_name_translation\":\"Triage FR\","
	        + "\"description\":\"French\",\"language\":\"fr\",\"translations\":{\"Yes\":\"Oui\"}}";
	
	private final AmpathFormTranslationDomainExporter exporter = new AmpathFormTranslationDomainExporter();
	
	@Test
	void ownsTheAmpathFormsTranslationsDomain() {
		assertEquals(Domain.AMPATH_FORMS_TRANSLATIONS, exporter.getDomain());
	}
	
	@Test
	void handlesOnlyTranslationResources() {
		assertTrue(exporter.handles(resource("Triage_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE)));
		assertFalse(exporter.handles(resource("JSON schema", FormResources.AMPATH_JSON_SCHEMA_DATATYPE)));
		assertFalse(exporter.handles(new Form()));
		assertFalse(exporter.handles(new Location()));
	}
	
	@Test
	void toTranslations_refreshesTheFormNameAndKeepsTheRest() throws Exception {
		FormResource resource = resource("Triage_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE);
		resource.setForm(form("Triage"));
		
		ObjectNode translations = AmpathFormTranslationDomainExporter.toTranslations(resource,
		    FormResources.asJsonObject(TRANSLATIONS));
		
		assertEquals("Triage", translations.get("form").asText(), "the loader resolves the form by this name");
		assertEquals("fr", translations.get("language").asText());
		assertEquals("Triage FR", translations.get("form_name_translation").asText());
		assertEquals("c5bf3efe", translations.get("uuid").asText());
		assertEquals("Oui", translations.get("translations").get("Yes").asText());
	}
	
	@Test
	void toTranslations_fillsInAMissingLanguageFromTheResourceName() throws Exception {
		FormResource resource = resource("Triage_translations_es", FormResources.LONG_FREE_TEXT_DATATYPE);
		resource.setForm(form("Triage"));
		
		ObjectNode translations = AmpathFormTranslationDomainExporter.toTranslations(resource,
		    FormResources.asJsonObject("{\"translations\":{\"Yes\":\"Sí\"}}"));
		
		assertEquals("es", translations.get("language").asText());
		assertEquals("Triage", translations.get("form").asText());
	}
	
	@Test
	void toTranslations_treatsABlankLanguageAsMissing() {
		FormResource resource = resource("Triage_translations_es", FormResources.LONG_FREE_TEXT_DATATYPE);
		
		ObjectNode translations = AmpathFormTranslationDomainExporter.toTranslations(resource,
		    FormResources.asJsonObject("{\"language\":\"\",\"translations\":{}}"));
		
		assertEquals("es", translations.get("language").asText(), "Initializer rejects a blank language");
	}
	
	@Test
	void fileNameFor_usesTheOwningFormsStemWithTheLanguageSuffix() {
		Form form = form("Triage Form");
		form.setUuid("f1");
		FormResource resource = resource("Triage Form_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE);
		resource.setForm(form);
		
		assertEquals("triage_form_translations_fr", AmpathFormTranslationDomainExporter.fileNameFor(resource,
		    FormResources.uniqueStems(Collections.singletonList(form))));
		assertEquals("triage_form_translations_fr",
		    AmpathFormTranslationDomainExporter.fileNameFor(resource, Collections.emptyMap()),
		    "without a precomputed stem the plain sanitized name is used");
	}
	
	@Test
	void fileNameFor_keepsTranslationsOfLikeNamedFormsApart() {
		Form triage = form("Triage");
		triage.setUuid("f1");
		Form upper = form("TRIAGE");
		upper.setUuid("f2");
		FormResource fr1 = resource("Triage_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE);
		fr1.setForm(triage);
		FormResource fr2 = resource("TRIAGE_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE);
		fr2.setForm(upper);
		java.util.Map<String, String> stems = FormResources.uniqueStems(Arrays.asList(triage, upper));
		
		assertNotEquals(AmpathFormTranslationDomainExporter.fileNameFor(fr1, stems),
		    AmpathFormTranslationDomainExporter.fileNameFor(fr2, stems));
	}
	
	@Test
	void getDependencies_isTheOwningForm() {
		FormResource resource = resource("Triage_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE);
		Form form = form("Triage");
		resource.setForm(form);
		
		assertEquals(Collections.singletonList(form), exporter.getDependencies(resource));
		assertTrue(exporter.getDependencies(resource("x_translations_fr", FormResources.LONG_FREE_TEXT_DATATYPE)).isEmpty());
	}
	
	private static Form form(String name) {
		Form form = new Form();
		form.setName(name);
		form.setVersion("1.0");
		return form;
	}
	
	private static FormResource resource(String name, String datatype) {
		FormResource resource = new FormResource();
		resource.setName(name);
		resource.setDatatypeClassname(datatype);
		return resource;
	}
}
