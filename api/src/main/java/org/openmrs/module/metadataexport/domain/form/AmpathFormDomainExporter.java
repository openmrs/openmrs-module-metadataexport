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
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.JsonDomainExporter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Writes each AMPATH form as one JSON file, the inverse of Initializer's {@code AmpathFormsLoader}.
 * Which forms are written is decided by {@link AmpathFormScan}.
 * <p>
 * The loader stores the whole file it reads as the form's schema clob, so the stored schema is
 * already the file to write back; only the top-level keys the loader copies onto the {@link Form}
 * row (name, version, description, published, retired and the encounter type name) are refreshed
 * from that row, which is the authoritative copy of those on the exporting server. Everything else
 * in the schema is written out untouched: the {@code processor} the loader only inspects, the
 * {@code uuid} it ignores, and the form content itself.
 */
@Slf4j
@Component
public class AmpathFormDomainExporter extends JsonDomainExporter<Form> {
	
	public static final String NAME = "name";
	
	public static final String VERSION = "version";
	
	public static final String DESCRIPTION = "description";
	
	public static final String PUBLISHED = "published";
	
	public static final String RETIRED = "retired";
	
	public static final String ENCOUNTER = "encounter";
	
	public static final String PROCESSOR = "processor";
	
	public static final String ENCOUNTER_FORM_PROCESSOR = "EncounterFormProcessor";
	
	@Override
	public Domain getDomain() {
		return Domain.AMPATH_FORMS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof Form && FormResources.isAmpathForm(FormResources.resourcesOf((Form) instance));
	}
	
	/** The exportable forms; every AMPATH form left out is logged once here with the reason. */
	@Override
	public Collection<Form> getAllInstances() {
		AmpathFormScan scan = AmpathFormScan.run();
		for (String exclusion : scan.formExclusions().values()) {
			log.warn("AMPATH forms: skipping form {}", exclusion);
		}
		return scan.exportableForms();
	}
	
	/**
	 * Same as the default, but a uuid that exists only as a form {@link #getAllInstances()} hides
	 * (retired, superseded or without a readable schema) is reported with the reason rather than as
	 * unknown.
	 */
	@Override
	public Collection<Form> getInstancesByUuids(Collection<String> uuids) {
		AmpathFormScan scan = AmpathFormScan.run();
		Set<String> wanted = new HashSet<>(uuids);
		List<Form> found = new ArrayList<>();
		for (Form form : scan.exportableForms()) {
			if (wanted.remove(form.getUuid())) {
				found.add(form);
			}
		}
		if (!wanted.isEmpty()) {
			List<String> hidden = new ArrayList<>();
			for (String uuid : new ArrayList<>(wanted)) {
				String exclusion = scan.formExclusions().get(uuid);
				if (exclusion != null) {
					wanted.remove(uuid);
					hidden.add(exclusion);
				}
			}
			List<String> problems = new ArrayList<>();
			if (!hidden.isEmpty()) {
				problems.add("AMPATH forms exist but this exporter does not write them (fix them on this server or remove"
				        + " them from the package): " + hidden);
			}
			if (!wanted.isEmpty()) {
				problems.add("Unknown uuids in domain " + getDomain() + ": " + wanted);
			}
			throw new APIException(String.join("; ", problems));
		}
		return found;
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(Form instance) {
		return dependencies(instance, exportableTranslationsOf(FormResources.resourcesOf(instance)));
	}
	
	/**
	 * The encounter type the loader resolves by name, plus the form's translation resources so that
	 * exporting a form carries its translations along. Only translations the translations exporter
	 * would itself select belong here, or the manifest would list a resource that never gets a file.
	 */
	static List<OpenmrsObject> dependencies(Form form, Collection<FormResource> translations) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		if (form.getEncounterType() != null) {
			dependencies.add(form.getEncounterType());
		}
		dependencies.addAll(FormResources.translationsOf(translations));
		return dependencies;
	}
	
	/**
	 * The same content rule {@link AmpathFormScan} applies, for one form's resources without a full
	 * scan.
	 */
	private static List<FormResource> exportableTranslationsOf(Collection<FormResource> resources) {
		List<FormResource> exportable = new ArrayList<>();
		for (FormResource resource : FormResources.translationsOf(resources)) {
			if (FormResources.isTranslationDocument(FormResources.readJsonObject(resource))) {
				exportable.add(resource);
			}
		}
		return exportable;
	}
	
	/**
	 * One refreshed schema per form. Selection ({@link AmpathFormScan}) already dropped forms without a
	 * readable schema, so finding one here means the data changed under the export; that fails the
	 * build rather than silently leaving a listed form without a file.
	 */
	@Override
	protected Map<String, JsonNode> toDocuments(Collection<Form> instances) throws IOException {
		Map<String, JsonNode> documents = new LinkedHashMap<>();
		Map<String, String> stems = FormResources.uniqueStems(instances);
		for (Form form : instances) {
			FormResources.JsonRead read = FormResources
			        .readJson(FormResources.schemaResource(FormResources.resourcesOf(form)));
			if (!read.isObject()) {
				throw new IOException("AMPATH form " + FormResources.describe(form) + " no longer has a readable JSON object"
				        + " in its '" + FormResources.JSON_SCHEMA_RESOURCE + "' resource (" + read.problem
				        + "); it did when it was selected");
			}
			ObjectNode schema = toSchema(form, read.node);
			if (form.getEncounterType() == null && needsEncounter(schema)) {
				log.warn("AMPATH forms: form {} has no encounter type; Initializer rejects the file until an 'encounter'"
				        + " entry naming one is added",
				    FormResources.describe(form));
			}
			String fileName = stems.get(form.getUuid()) + JSON_EXTENSION;
			if (documents.put(fileName, schema) != null) {
				throw new IOException("AMPATH forms: two forms resolved to the same file " + fileName);
			}
		}
		return documents;
	}
	
	/** Refreshes, in place, the keys the loader copies onto the form row, and returns the same node. */
	static ObjectNode toSchema(Form form, ObjectNode schema) {
		schema.put(NAME, form.getName());
		schema.put(VERSION, form.getVersion());
		if (form.getDescription() == null) {
			schema.remove(DESCRIPTION);
		} else {
			schema.put(DESCRIPTION, form.getDescription());
		}
		schema.put(PUBLISHED, BooleanUtils.isTrue(form.getPublished()));
		schema.put(RETIRED, BooleanUtils.isTrue(form.getRetired()));
		if (form.getEncounterType() == null) {
			schema.remove(ENCOUNTER);
		} else {
			schema.put(ENCOUNTER, form.getEncounterType().getName());
		}
		return schema;
	}
	
	/**
	 * Mirrors the loader's {@code isEncounterForm}: an {@code encounter} entry is mandatory unless
	 * {@code processor} names a processor other than {@value #ENCOUNTER_FORM_PROCESSOR}.
	 */
	static boolean needsEncounter(ObjectNode schema) {
		JsonNode processor = schema.get(PROCESSOR);
		return processor == null || processor.isNull() || StringUtils.isBlank(processor.asText())
		        || ENCOUNTER_FORM_PROCESSOR.equalsIgnoreCase(processor.asText());
	}
}
