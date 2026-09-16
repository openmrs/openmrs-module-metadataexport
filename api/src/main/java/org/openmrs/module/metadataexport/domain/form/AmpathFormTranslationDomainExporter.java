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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Writes each AMPATH form translation resource as one JSON file, the inverse of Initializer's
 * {@code AmpathFormsTranslationsLoader}. Which resources are written is decided by
 * {@link AmpathFormScan}.
 * <p>
 * The loader stores the whole file as the resource's clob, so the stored content is written back
 * with only the {@code form} entry (the owning form's name, which the loader resolves) refreshed
 * and a missing {@code language} filled in from the resource name.
 */
@Slf4j
@Component
public class AmpathFormTranslationDomainExporter extends JsonDomainExporter<FormResource> {
	
	public static final String FORM = "form";
	
	public static final String LANGUAGE = "language";
	
	public static final String FILE_INFIX = "_translations_";
	
	@Override
	public Domain getDomain() {
		return Domain.AMPATH_FORMS_TRANSLATIONS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof FormResource && FormResources.isTranslation((FormResource) instance);
	}
	
	/**
	 * The exportable translation resources; every translation-named resource left out, including those
	 * of forms that are themselves not exported, is logged once here with the reason.
	 */
	@Override
	public Collection<FormResource> getAllInstances() {
		AmpathFormScan scan = AmpathFormScan.run();
		for (String exclusion : scan.translationExclusions().values()) {
			log.warn("AMPATH form translations: skipping resource {}", exclusion);
		}
		return scan.exportableTranslations();
	}
	
	/**
	 * Same as the default, but a uuid that is a translation resource {@link #getAllInstances()} hides
	 * is reported with the reason rather than as unknown.
	 */
	@Override
	public Collection<FormResource> getInstancesByUuids(Collection<String> uuids) {
		AmpathFormScan scan = AmpathFormScan.run();
		Set<String> wanted = new HashSet<>(uuids);
		List<FormResource> found = new ArrayList<>();
		for (FormResource resource : scan.exportableTranslations()) {
			if (wanted.remove(resource.getUuid())) {
				found.add(resource);
			}
		}
		if (!wanted.isEmpty()) {
			List<String> hidden = new ArrayList<>();
			for (String uuid : new ArrayList<>(wanted)) {
				String exclusion = scan.translationExclusions().get(uuid);
				if (exclusion != null) {
					wanted.remove(uuid);
					hidden.add(exclusion);
				}
			}
			List<String> problems = new ArrayList<>();
			if (!hidden.isEmpty()) {
				problems.add("AMPATH form translations exist but this exporter does not write them (fix them on this"
				        + " server or remove them from the package): " + hidden);
			}
			if (!wanted.isEmpty()) {
				problems.add("Unknown uuids in domain " + getDomain() + ": " + wanted);
			}
			throw new APIException(String.join("; ", problems));
		}
		return found;
	}
	
	/** The owning form: the loader looks it up by name before it can attach the translation. */
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(FormResource instance) {
		return instance.getForm() == null ? Collections.emptyList() : Collections.singletonList(instance.getForm());
	}
	
	/**
	 * One file per resource. Selection already dropped resources without matchable content, so finding
	 * one here means the data changed under the export; that fails the build rather than silently
	 * leaving a listed resource without a file.
	 */
	@Override
	protected Map<String, JsonNode> toDocuments(Collection<FormResource> instances) throws IOException {
		Map<String, JsonNode> documents = new LinkedHashMap<>();
		Map<String, String> stems = FormResources.uniqueStems(owningForms(instances));
		for (FormResource resource : instances) {
			FormResources.JsonRead read = FormResources.readJson(resource);
			if (!read.isObject()) {
				throw new IOException("AMPATH form translation resource " + FormResources.describe(resource)
				        + " no longer holds a readable JSON object (" + read.problem + "); it did when it was selected");
			}
			String fileName = fileNameFor(resource, stems) + JSON_EXTENSION;
			if (documents.put(fileName, toTranslations(resource, read.node)) != null) {
				throw new IOException("AMPATH form translations: two resources resolved to the same file " + fileName);
			}
		}
		return documents;
	}
	
	/**
	 * Refreshes, in place, the {@code form} name the loader resolves and fills in a blank or missing
	 * {@code language} from the resource name; returns the same node.
	 */
	static ObjectNode toTranslations(FormResource resource, ObjectNode translations) {
		if (resource.getForm() != null) {
			translations.put(FORM, resource.getForm().getName());
		}
		if (StringUtils.isBlank(translations.path(LANGUAGE).asText(null)) && FormResources.languageOf(resource) != null) {
			translations.put(LANGUAGE, FormResources.languageOf(resource));
		}
		return translations;
	}
	
	/**
	 * {@code <form stem>_translations_<language>}, modelled on how Initializer names the resource, with
	 * the owning form's stem taken from {@link FormResources#uniqueStems} so that forms whose names
	 * sanitize alike cannot overwrite each other's translations.
	 */
	static String fileNameFor(FormResource resource, Map<String, String> stems) {
		Form form = resource.getForm();
		String stem = form == null ? null : stems.get(form.getUuid());
		if (stem == null) {
			stem = FormResources.fileName(form == null ? null : form.getName());
		}
		return stem + FILE_INFIX + FormResources.languageOf(resource);
	}
	
	private static Collection<Form> owningForms(Collection<FormResource> resources) {
		Map<String, Form> forms = new LinkedHashMap<>();
		for (FormResource resource : resources) {
			if (resource.getForm() != null) {
				forms.putIfAbsent(resource.getForm().getUuid(), resource.getForm());
			}
		}
		return forms.values();
	}
}
