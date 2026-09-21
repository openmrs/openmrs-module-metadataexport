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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.Location;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.loaders.AmpathFormsLoader;
import org.openmrs.module.initializer.api.loaders.AmpathFormsTranslationsLoader;
import org.openmrs.module.initializer.api.utils.Utils;
import org.openmrs.module.metadataexport.export.ExportContext;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives both AMPATH exporters against real forms, resources and clobs, and replays the export
 * through Initializer's own loaders, which are in the test context because the initializer-api jar
 * carries its Spring context.
 */
class AmpathFormDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "1f5a0c3e-6f5e-4a5c-9c1d-2b7e8f9a0b11";
	
	private static final String OLDER_UUID = "2a6b1d4f-7a6f-4b6d-8d2e-3c8f9a0b1c22";
	
	private static final String RETIRED_UUID = "3b7c2e5a-8b7a-4c7e-9e3f-4d9a0b1c2d33";
	
	private static final String PLAIN_UUID = "4c8d3f6b-9c8b-4d8f-8f4a-5e0b1c2d3e44";
	
	private static final String NO_ENCOUNTER_UUID = "5d9e4a7c-0d9c-4e9a-9a5b-6f1c2d3e4f55";
	
	private static final String UNKNOWN_UUID = "6e0f5b8d-1e0d-4f0b-8b6c-7a2d3e4f5a66";
	
	private static final String BROKEN_UUID = "7f1a6c9e-2f1e-4a1c-9c7d-8b3e4f5a6b77";
	
	private static final String UNPARSEABLE_UUID = "8a2b7d0f-3a2f-4b2d-8d8e-9c4f5a6b7c88";
	
	private static final String STORED_SCHEMA = "{\"uuid\":\"xxxx\",\"name\":\"Old Triage\",\"version\":\"1.0\","
	        + "\"description\":\"stale\",\"published\":false,\"retired\":true,\"encounter\":\"Emergency\","
	        + "\"processor\":\"EncounterFormProcessor\",\"pages\":[{\"label\":\"Vitals\"}]}";
	
	/** A form Initializer accepts without an encounter type: its processor is not the encounter one. */
	private static final String STANDALONE_SCHEMA = "{\"uuid\":\"yyyy\",\"name\":\"Standalone\",\"version\":\"1.0\","
	        + "\"published\":false,\"retired\":false,\"encounter\":\"Emergency\",\"processor\":\"ProgramEnrollmentProcessor\"}";
	
	private static final String STORED_TRANSLATIONS = "{\"uuid\":\"c5bf3efe-3798-4052-8dcb-09aacfcbabdc\","
	        + "\"form\":\"Old Triage\",\"form_name_translation\":\"Triage FR\",\"description\":\"French\","
	        + "\"language\":\"fr\",\"translations\":{\"Vitals\":\"Signes vitaux\"}}";
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private final AmpathFormDomainExporter formExporter = new AmpathFormDomainExporter();
	
	private final AmpathFormTranslationDomainExporter translationExporter = new AmpathFormTranslationDomainExporter();
	
	private EncounterType scheduled;
	
	private FormResource liveTranslation;
	
	private FormResource nameOnlyTranslation;
	
	private FormResource supersededTranslation;
	
	private FormResource formBuilderTranslation;
	
	private FormResource unreadableTranslation;
	
	@BeforeEach
	void seedForms() {
		scheduled = Context.getEncounterService().getEncounterType(1);
		
		Form live = ampathForm(LIVE_UUID, "Triage", "2.0", scheduled, STORED_SCHEMA);
		live.setDescription("Triage at the door");
		live.setPublished(true);
		liveTranslation = translationResource(live, "fr", STORED_TRANSLATIONS);
		// how the O3 Form Builder saves a translation (uploadBackendTranslations.ts): name and clob reference only,
		// no datatype, and a clob holding just the translations, so "form" and "language" must be filled in on export
		formBuilderTranslation = formBuilderTranslationResource(live, "de",
		    "{\"translations\":{\"Vitals\":\"Vitalwerte\"}}");
		// a documented Initializer use: only the localized form name, no "translations" entry
		nameOnlyTranslation = translationResource(live, "es", "{\"language\":\"es\",\"form_name_translation\":\"Triaje\"}");
		// a translation whose clob is gone: nothing to write
		unreadableTranslation = translationResource(live, "it", STORED_TRANSLATIONS);
		Context.getDatatypeService().deleteClobDatatypeStorage(
		    Context.getDatatypeService().getClobDatatypeStorageByUuid(unreadableTranslation.getValueReference()));
		
		Form older = ampathForm(OLDER_UUID, "Triage", "1.0", scheduled, STORED_SCHEMA);
		supersededTranslation = translationResource(older, "fr", STORED_TRANSLATIONS);
		
		Form retired = ampathForm(RETIRED_UUID, "Legacy", "1.0", scheduled, STORED_SCHEMA);
		formService().retireForm(retired, "Replaced");
		
		Form plain = new Form();
		plain.setUuid(PLAIN_UUID);
		plain.setName("Plain");
		plain.setVersion("1.0");
		formService().saveForm(plain);
		
		ampathForm(NO_ENCOUNTER_UUID, "Standalone", "1.0", null, STANDALONE_SCHEMA);
		
		// a schema resource whose clob row is gone: nothing to write
		Form broken = ampathForm(BROKEN_UUID, "Broken", "1.0", scheduled, STORED_SCHEMA);
		Context.getDatatypeService().deleteClobDatatypeStorage(Context.getDatatypeService()
		        .getClobDatatypeStorageByUuid(formService().getFormResource(broken, "JSON schema").getValueReference()));
		
		// a half-saved schema: the clob exists but is not JSON
		ampathForm(UNPARSEABLE_UUID, "Half saved", "1.0", scheduled, "{\"name\": ");
		Context.flushSession();
	}
	
	@AfterEach
	void cleanAppDataDir() throws Exception {
		File appData = new File(OpenmrsUtil.getApplicationDataDirectory());
		assertTrue(appData.getName().startsWith("appdir-for-unit-tests-"), "refusing to clean " + appData);
		FileUtils.deleteDirectory(new File(appData, "configuration"));
		FileUtils.deleteDirectory(new File(appData, "configuration_checksums"));
	}
	
	@Test
	void scan_runsOncePerSessionAndAgainForANewOne() {
		AmpathFormScan first = AmpathFormScan.run();
		assertSame(first, AmpathFormScan.run(),
		    "a full export asks for the scan four times; all four must share one pass over the forms");
		
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		try (Session other = sessionFactory.openSession()) {
			assertNotSame(first, AmpathFormScan.runIn(other), "a new session must not be handed a previous export's forms");
		}
	}
	
	@Test
	void getAllInstances_returnsOnlyTheLiveLatestAmpathForms() {
		assertEquals(new HashSet<>(Arrays.asList(LIVE_UUID, NO_ENCOUNTER_UUID)), uuidsOf(formExporter.getAllInstances()));
	}
	
	@Test
	void handles_onlyFormsWithAJsonSchemaResource() {
		assertTrue(formExporter.handles(formService().getFormByUuid(LIVE_UUID)));
		assertTrue(formExporter.handles(formService().getFormByUuid(RETIRED_UUID)), "ownership is not about liveness");
		assertFalse(formExporter.handles(formService().getFormByUuid(PLAIN_UUID)));
		assertFalse(formExporter.handles(new Location()));
	}
	
	@Test
	void getInstancesByUuids_reportsHiddenFormsWithTheReasonTheyCannotBeImported() {
		APIException e = assertThrows(APIException.class, () -> formExporter
		        .getInstancesByUuids(Arrays.asList(LIVE_UUID, OLDER_UUID, RETIRED_UUID, BROKEN_UUID, UNKNOWN_UUID)));
		
		assertTrue(e.getMessage().contains(OLDER_UUID + " (Triage v1.0) is superseded"), e.getMessage());
		assertTrue(e.getMessage().contains(RETIRED_UUID + " (Legacy v1.0) is retired"), e.getMessage());
		assertTrue(e.getMessage().contains(BROKEN_UUID + " (Broken v1.0) has no readable JSON object in its"
		        + " 'JSON schema' resource: it references clob "),
		    e.getMessage());
		assertTrue(e.getMessage().contains("Unknown uuids"));
		assertTrue(e.getMessage().contains(UNKNOWN_UUID));
		assertFalse(e.getMessage().contains(LIVE_UUID + " (Triage v2.0) is"),
		    "the live form is named only as the superseding version, never as a problem itself");
		assertEquals(1, formExporter.getInstancesByUuids(Collections.singletonList(LIVE_UUID)).size());
		assertFalse(e.getMessage().contains(UNPARSEABLE_UUID), "not asked for, so not reported");
	}
	
	@Test
	void getAllInstances_andGetInstancesByUuids_treatAnUnparseableSchemaLikeAMissingOne() {
		assertFalse(uuidsOf(formExporter.getAllInstances()).contains(UNPARSEABLE_UUID));
		
		APIException e = assertThrows(APIException.class,
		    () -> formExporter.getInstancesByUuids(Collections.singletonList(UNPARSEABLE_UUID)));
		
		assertTrue(e.getMessage().contains(UNPARSEABLE_UUID + " (Half saved v1.0) has no readable JSON object in its"
		        + " 'JSON schema' resource: it is not valid JSON ("),
		    e.getMessage());
	}
	
	@Test
	void getDependencies_areTheEncounterTypeAndTheTranslations() {
		Context.flushSession();
		Context.clearSession();
		Form live = formService().getFormByUuid(LIVE_UUID);
		
		Collection<? extends OpenmrsObject> dependencies = formExporter.getDependencies(live);
		
		assertEquals(
		    new HashSet<>(Arrays.asList(scheduled.getUuid(), liveTranslation.getUuid(), formBuilderTranslation.getUuid(),
		        nameOnlyTranslation.getUuid())),
		    uuidsOf(dependencies), "the unreadable translation must not enter the manifest through closure either");
		assertTrue(translationExporter.handles(dependencies.stream().filter(d -> d instanceof FormResource).findFirst()
		        .orElseThrow(() -> new AssertionError("translation resource missing"))));
	}
	
	@Test
	void translationExporter_seesTheReadableTranslationsOfExportableFormsOnly() {
		Collection<FormResource> instances = translationExporter.getAllInstances();
		
		assertEquals(
		    new HashSet<>(Arrays.asList(liveTranslation.getUuid(), formBuilderTranslation.getUuid(),
		        nameOnlyTranslation.getUuid())),
		    uuidsOf(instances),
		    "the datatype-less Form Builder resource and the name-only resource are in; the unreadable resource and"
		            + " the superseded form's translation are left out of selection");
		assertEquals(LIVE_UUID,
		    translationExporter.getDependencies(instances.iterator().next()).iterator().next().getUuid());
	}
	
	@Test
	void translationExporter_reportsAHiddenTranslationWithTheReason() {
		APIException e = assertThrows(APIException.class, () -> translationExporter.getInstancesByUuids(
		    Arrays.asList(liveTranslation.getUuid(), unreadableTranslation.getUuid(), UNKNOWN_UUID)));
		
		assertTrue(e.getMessage().contains(
		    unreadableTranslation.getUuid() + " (Triage_translations_it) has no readable JSON object: it references clob "),
		    e.getMessage());
		assertTrue(e.getMessage().contains("Unknown uuids"));
		assertTrue(e.getMessage().contains(UNKNOWN_UUID));
		assertFalse(e.getMessage().contains(liveTranslation.getUuid()));
	}
	
	@Test
	void translationExporter_explainsATranslationOfANonExportableForm() {
		APIException e = assertThrows(APIException.class,
		    () -> translationExporter.getInstancesByUuids(Collections.singletonList(supersededTranslation.getUuid())));
		
		assertTrue(e.getMessage().contains("belongs to a form that is not exported: " + OLDER_UUID
		        + " (Triage v1.0) is superseded by " + LIVE_UUID + " (Triage v2.0)"),
		    e.getMessage());
	}
	
	@Test
	void export_writesOneRefreshedSchemaPerLiveForm(@TempDir File outDir) throws Exception {
		formExporter.export(formExporter.getAllInstances(), new ExportContext(outDir));
		
		File domainDir = domainDir(outDir, Domain.AMPATH_FORMS);
		assertEquals(new HashSet<>(Arrays.asList("triage.json", "standalone.json")),
		    new HashSet<>(Arrays.asList(domainDir.list())),
		    "retired, superseded, plain, clob-less and unparseable forms must not be written");
		
		JsonNode triage = MAPPER.readTree(new File(domainDir, "triage.json"));
		assertEquals("Triage", triage.get("name").asText());
		assertEquals("2.0", triage.get("version").asText());
		assertEquals("Triage at the door", triage.get("description").asText());
		assertTrue(triage.get("published").asBoolean());
		assertFalse(triage.get("retired").asBoolean());
		assertEquals(scheduled.getName(), triage.get("encounter").asText());
		assertEquals("xxxx", triage.get("uuid").asText());
		assertEquals("Vitals", triage.get("pages").get(0).get("label").asText());
		
		JsonNode standalone = MAPPER.readTree(new File(domainDir, "standalone.json"));
		assertFalse(standalone.has("encounter"), "the stale encounter from the stored schema must not survive");
		assertFalse(standalone.has("description"));
		assertEquals("ProgramEnrollmentProcessor", standalone.get("processor").asText());
	}
	
	@Test
	void export_writesOneTranslationFilePerLanguage(@TempDir File outDir) throws Exception {
		translationExporter.export(translationExporter.getAllInstances(), new ExportContext(outDir));
		
		File domainDir = domainDir(outDir, Domain.AMPATH_FORMS_TRANSLATIONS);
		assertEquals(
		    new HashSet<>(Arrays.asList("triage_translations_fr.json", "triage_translations_de.json",
		        "triage_translations_es.json")),
		    new HashSet<>(Arrays.asList(domainDir.list())),
		    "the superseded form's translation and the unreadable resource must not be written");
		assertEquals("Triaje",
		    MAPPER.readTree(new File(domainDir, "triage_translations_es.json")).get("form_name_translation").asText());
		JsonNode formBuilder = MAPPER.readTree(new File(domainDir, "triage_translations_de.json"));
		assertEquals("Vitalwerte", formBuilder.get("translations").get("Vitals").asText());
		assertEquals("Triage", formBuilder.get("form").asText(), "filled in: the Form Builder stores no form entry");
		assertEquals("de", formBuilder.get("language").asText(), "filled in: the Form Builder stores no language entry");
		File written = new File(domainDir, "triage_translations_fr.json");
		JsonNode translations = MAPPER.readTree(written);
		assertEquals("Triage", translations.get("form").asText());
		assertEquals("fr", translations.get("language").asText());
		assertEquals("Triage FR", translations.get("form_name_translation").asText());
		assertEquals("Signes vitaux", translations.get("translations").get("Vitals").asText());
	}
	
	@Test
	void export_thenReimportOntoAFreshTargetThroughInitializer(@TempDir File outDir) throws Exception {
		formExporter.export(formExporter.getAllInstances(), new ExportContext(outDir));
		translationExporter.export(translationExporter.getAllInstances(), new ExportContext(outDir));
		// the scan is cached per session: nothing below may call the exporters again in this test
		// the scan is cached per session: nothing below may call the exporters again in this test
		// the scan is cached per session: nothing below may call the exporters again in this test
		purgeSeededForms();
		assertTrue(allAmpathForms().isEmpty(), "the target must start without AMPATH forms");
		
		File appData = new File(OpenmrsUtil.getApplicationDataDirectory());
		FileUtils.copyDirectory(new File(outDir, ExportContext.CONFIGURATION_DIR),
		    new File(appData, ExportContext.CONFIGURATION_DIR));
		loader(AmpathFormsLoader.class).loadUnsafe(Collections.emptyList(), true);
		loader(AmpathFormsTranslationsLoader.class).loadUnsafe(Collections.emptyList(), true);
		Context.flushSession();
		
		String derivedUuid = Utils.generateUuidFromObjects(AmpathFormsLoader.AMPATH_FORMS_UUID, "Triage", "2.0");
		Form triage = formService().getFormByUuid(derivedUuid);
		assertNotNull(triage, "Initializer derives the uuid from name and version");
		assertEquals("Triage at the door", triage.getDescription());
		assertTrue(triage.getPublished());
		assertFalse(triage.getRetired());
		assertEquals(scheduled.getUuid(), triage.getEncounterType().getUuid());
		String schemaFile = new String(
		        Files.readAllBytes(new File(domainDir(outDir, Domain.AMPATH_FORMS), "triage.json").toPath()),
		        StandardCharsets.UTF_8);
		assertEquals(MAPPER.readTree(schemaFile),
		    MAPPER.readTree(FormResources.readClob(formService().getFormResource(triage, "JSON schema"))),
		    "the file itself becomes the stored schema");
		
		FormResource translation = formService().getFormResource(triage, "Triage_translations_fr");
		assertNotNull(formService().getFormResource(triage, "Triage_translations_es"),
		    "a name-only translation file imports like any other");
		assertNotNull(formService().getFormResource(triage, "Triage_translations_de"),
		    "the Form Builder translation imports only because export filled in 'form' and 'language'");
		assertNotNull(translation, "the translation must re-attach to the re-created form by name");
		assertEquals("Signes vitaux",
		    MAPPER.readTree(FormResources.readClob(translation)).get("translations").get("Vitals").asText());
		
		Form standalone = formService()
		        .getFormByUuid(Utils.generateUuidFromObjects(AmpathFormsLoader.AMPATH_FORMS_UUID, "Standalone", "1.0"));
		assertNotNull(standalone);
		assertEquals(new HashSet<>(Arrays.asList(triage.getUuid(), standalone.getUuid())), uuidsOf(allAmpathForms()),
		    "nothing else may come back");
	}
	
	private Form ampathForm(String uuid, String name, String version, EncounterType encounterType, String schemaJson) {
		Form form = new Form();
		form.setUuid(uuid);
		form.setName(name);
		form.setVersion(version);
		form.setEncounterType(encounterType);
		form = formService().saveForm(form);
		
		// exactly how Initializer and the O3 Form Builder store a schema: an alias datatype and a hand-made clob
		String clobUuid = UUID.randomUUID().toString();
		ClobDatatypeStorage clob = new ClobDatatypeStorage();
		clob.setUuid(clobUuid);
		clob.setValue(schemaJson);
		Context.getDatatypeService().saveClobDatatypeStorage(clob);
		FormResource schema = new FormResource();
		schema.setForm(form);
		schema.setName(FormResources.JSON_SCHEMA_RESOURCE);
		schema.setDatatypeClassname(FormResources.AMPATH_JSON_SCHEMA_DATATYPE);
		schema.setValueReferenceInternal(clobUuid);
		formService().saveFormResource(schema);
		return form;
	}
	
	/** Name and clob reference only, no datatype: what the Form Builder's REST call persists. */
	private FormResource formBuilderTranslationResource(Form form, String language, String json) {
		String clobUuid = UUID.randomUUID().toString();
		ClobDatatypeStorage clob = new ClobDatatypeStorage();
		clob.setUuid(clobUuid);
		clob.setValue(json);
		Context.getDatatypeService().saveClobDatatypeStorage(clob);
		FormResource resource = new FormResource();
		resource.setForm(form);
		resource.setName(form.getName() + "_translations_" + language);
		resource.setValueReferenceInternal(clobUuid);
		return formService().saveFormResource(resource);
	}
	
	private FormResource translationResource(Form form, String language, String json) {
		FormResource resource = new FormResource();
		resource.setForm(form);
		resource.setName(form.getName() + "_translations_" + language);
		resource.setDatatypeClassname(FormResources.LONG_FREE_TEXT_DATATYPE);
		resource.setValue(json);
		return formService().saveFormResource(resource);
	}
	
	/**
	 * Removes the forms seeded by this test (the standard dataset's own form is referenced by
	 * encounters).
	 */
	private static void purgeSeededForms() {
		for (String uuid : Arrays.asList(LIVE_UUID, OLDER_UUID, RETIRED_UUID, PLAIN_UUID, NO_ENCOUNTER_UUID, BROKEN_UUID,
		    UNPARSEABLE_UUID)) {
			Form form = formService().getFormByUuid(uuid);
			for (FormResource resource : new ArrayList<>(formService().getFormResourcesForForm(form))) {
				formService().purgeFormResource(resource);
			}
			formService().purgeForm(form);
		}
		Context.flushSession();
	}
	
	private static <T> T loader(Class<T> type) {
		List<T> loaders = Context.getRegisteredComponents(type);
		assertFalse(loaders.isEmpty(), "Initializer's " + type.getSimpleName() + " is not in the test context");
		return loaders.get(0);
	}
	
	private static List<Form> allAmpathForms() {
		List<Form> ampath = new ArrayList<>();
		for (Form form : formService().getAllForms(true)) {
			if (FormResources.isAmpathForm(FormResources.resourcesOf(form))) {
				ampath.add(form);
			}
		}
		return ampath;
	}
	
	private static File domainDir(File outDir, Domain domain) {
		return outDir.toPath().resolve(Paths.get(ExportContext.CONFIGURATION_DIR, domain.getName())).toFile();
	}
	
	private static Set<String> uuidsOf(Collection<? extends OpenmrsObject> objects) {
		return objects.stream().map(OpenmrsObject::getUuid).collect(Collectors.toSet());
	}
	
	private static FormService formService() {
		return Context.getFormService();
	}
}
