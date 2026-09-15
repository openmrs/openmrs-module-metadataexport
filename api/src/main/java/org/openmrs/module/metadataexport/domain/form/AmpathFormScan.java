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

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.api.context.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One pass over the server's AMPATH forms that decides, once, which forms and which translation
 * resources the two AMPATH exporters write, and records a sentence for every one they leave out.
 * Both exporters' {@code getAllInstances} and {@code getInstancesByUuids} run one scan each, so the
 * export policy lives in exactly one place and an excluded uuid is explained from the decision that
 * excluded it rather than re-derived by elimination.
 * <p>
 * The policy: a form is exported when it carries a {@value FormResources#JSON_SCHEMA_RESOURCE}
 * resource, is not retired, is the newest version of its name, and that resource holds a JSON
 * object. A retired form is left out because {@code AmpathFormsLoader} cannot create one: its
 * {@code createNewForm} copies the retired flag from the file and saves without a retire reason,
 * which core's {@code FormValidator} rejects, so the file fails on every target that does not
 * already carry that name and version (only the update path, taken when the derived uuid exists,
 * sets a reason). Initializer would import an older version, but it is left out because
 * {@code AmpathFormsLoader} derives the form uuid from name and version, and when that uuid is new
 * it retires the live form of that name (core's {@code getForm(name)}, the highest live version)
 * before creating the new one, so with two versions in one package the survivor depends on file
 * order. A translation resource is exported when its form is and its content is a JSON object,
 * whatever its entries: {@code AmpathFormsTranslationsLoader} reads only {@code form} and
 * {@code language}, a file carrying just {@code form_name_translation} is a documented use of the
 * domain, and core's {@code saveFormResource} replaces an existing resource of the same name, so
 * nothing is duplicated on re-import. Only a resource whose clob is missing or does not parse to an
 * object is left out, because there is nothing to write. Deciding all of this before selection
 * keeps the build manifest honest: everything it lists gets a file.
 */
final class AmpathFormScan {
	
	private final List<Form> exportableForms = new ArrayList<>();
	
	/** Form uuid to sentence, for every AMPATH form that is not exported. */
	private final Map<String, String> formExclusions = new LinkedHashMap<>();
	
	private final Map<String, List<FormResource>> translationsByForm = new LinkedHashMap<>();
	
	/**
	 * Resource uuid to sentence, for every translation-named resource of an AMPATH form that is not
	 * exported.
	 */
	private final Map<String, String> translationExclusions = new LinkedHashMap<>();
	
	private AmpathFormScan() {
	}
	
	static AmpathFormScan run() {
		AmpathFormScan scan = new AmpathFormScan();
		Map<Form, Collection<FormResource>> resourcesByForm = new LinkedHashMap<>();
		List<Form> live = new ArrayList<>();
		for (Form form : Context.getFormService().getAllForms(true)) {
			Collection<FormResource> resources = FormResources.resourcesOf(form);
			if (!FormResources.isAmpathForm(resources)) {
				continue;
			}
			resourcesByForm.put(form, resources);
			if (BooleanUtils.isTrue(form.getRetired())) {
				scan.formExclusions.put(form.getUuid(), FormResources.describe(form)
				        + " is retired; Initializer cannot create a retired form (its loader saves the retired flag without"
				        + " a retire reason, which core's FormValidator rejects), so the file fails on any target that does"
				        + " not already carry this name and version");
			} else {
				live.add(form);
			}
		}
		for (Form form : FormResources.latestVersionPerName(live, scan.formExclusions)) {
			FormResources.JsonRead schema = FormResources.readJson(FormResources.schemaResource(resourcesByForm.get(form)));
			if (schema.isObject()) {
				scan.exportableForms.add(form);
			} else {
				scan.formExclusions.put(form.getUuid(),
				    FormResources.describe(form) + " has no readable JSON object in its '"
				            + FormResources.JSON_SCHEMA_RESOURCE + "' resource: " + schema.problem);
			}
		}
		for (Map.Entry<Form, Collection<FormResource>> entry : resourcesByForm.entrySet()) {
			Form form = entry.getKey();
			String formExclusion = scan.formExclusions.get(form.getUuid());
			for (FormResource resource : FormResources.translationsOf(entry.getValue())) {
				if (formExclusion != null) {
					scan.translationExclusions.put(resource.getUuid(),
					    FormResources.describe(resource) + " belongs to a form that is not exported: " + formExclusion);
					continue;
				}
				FormResources.JsonRead content = FormResources.readJson(resource);
				if (content.isObject()) {
					scan.translationsByForm.computeIfAbsent(form.getUuid(), f -> new ArrayList<>()).add(resource);
				} else {
					scan.translationExclusions.put(resource.getUuid(),
					    FormResources.describe(resource) + " has no readable JSON object: " + content.problem);
				}
			}
		}
		return scan;
	}
	
	List<Form> exportableForms() {
		return exportableForms;
	}
	
	/** Sentences for the AMPATH forms left out, keyed by form uuid. */
	Map<String, String> formExclusions() {
		return formExclusions;
	}
	
	List<FormResource> exportableTranslations() {
		List<FormResource> all = new ArrayList<>();
		for (List<FormResource> translations : translationsByForm.values()) {
			all.addAll(translations);
		}
		return all;
	}
	
	/** Sentences for the translation resources left out, keyed by resource uuid. */
	Map<String, String> translationExclusions() {
		return translationExclusions;
	}
}
