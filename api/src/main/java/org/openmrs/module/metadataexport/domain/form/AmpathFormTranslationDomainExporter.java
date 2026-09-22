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
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.JsonDomainExporter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Writes each AMPATH form translation resource as one JSON file, the inverse of Initializer's
 * {@code AmpathFormsTranslationsLoader}. Which resources are written is decided by
 * {@link AmpathFormScan}.
 * <p>
 * The loader stores the whole file as the resource's clob, so the stored content is written back
 * with only the {@code form} entry (the owning form's name, which the loader resolves) refreshed
 * and a missing {@code language} filled in from the resource name.
 */
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
	
	@Override
	public Collection<FormResource> getAllInstances() {
		return AmpathFormScan.run().exportableTranslations();
	}
	
	/**
	 * The translation-named resources left out, including those of forms that are themselves not
	 * exported, with the reason.
	 */
	@Override
	public Map<String, String> exclusions() {
		return new LinkedHashMap<>(AmpathFormScan.run().translationExclusions());
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
