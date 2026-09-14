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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ClobDatatypeStorage;
import org.openmrs.customdatatype.NotYetPersistedException;
import org.openmrs.customdatatype.datatype.LongFreeTextDatatype;
import org.openmrs.module.ModuleUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What the AMPATH form domains share: how an AMPATH form and one of its translation resources are
 * recognised, how their clob-backed content is read and parsed, how versions compare and how files
 * are named. Which forms and translations are exported is decided by {@link AmpathFormScan}.
 * <p>
 * Both are modelled on what Initializer's {@code AmpathFormsLoader} and
 * {@code AmpathFormsTranslationsLoader} write: the schema is a {@link FormResource} named
 * {@value #JSON_SCHEMA_RESOURCE} whose datatype is the {@value #AMPATH_JSON_SCHEMA_DATATYPE} alias,
 * and each translation is a resource named {@code <form name>_translations_<language>}; the content
 * of both lives in a {@link ClobDatatypeStorage} row whose uuid is the resource's value reference.
 * The O3 Form Builder saves the same resource name and datatype alias for the schema through the
 * REST API, and translation resources with only a name and a value reference (no datatype at all),
 * see {@code src/forms.resource.ts} in openmrs-esm-form-builder.
 */
final class FormResources {
	
	static final String JSON_SCHEMA_RESOURCE = "JSON schema";
	
	/**
	 * Not a class name: Initializer and the Form Builder store this alias as the datatype classname, so
	 * {@link FormResource#getValue()} cannot resolve a datatype for it. Read the clob directly.
	 */
	static final String AMPATH_JSON_SCHEMA_DATATYPE = "AmpathJsonSchema";
	
	static final String LONG_FREE_TEXT_DATATYPE = LongFreeTextDatatype.class.getName();
	
	/**
	 * The entry Initializer's translations loader uses to recognise an existing resource as a
	 * translation when it re-imports a file.
	 */
	static final String TRANSLATIONS_KEY = "translations";
	
	private static final Pattern TRANSLATION_NAME = Pattern.compile("^(.+)_translations_([A-Za-z_]+)$");
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private FormResources() {
	}
	
	/** The schema resource of an AMPATH form, or null when these resources are not an AMPATH form's. */
	static FormResource schemaResource(Collection<FormResource> resources) {
		if (resources == null) {
			return null;
		}
		for (FormResource resource : resources) {
			if (JSON_SCHEMA_RESOURCE.equals(resource.getName()) && isClobBacked(resource.getDatatypeClassname())) {
				return resource;
			}
		}
		return null;
	}
	
	static boolean isAmpathForm(Collection<FormResource> resources) {
		return schemaResource(resources) != null;
	}
	
	/**
	 * A resource named the way Initializer names translations, whose value reference can be read as a
	 * clob: Initializer's own {@code LongFreeTextDatatype}, the {@value #AMPATH_JSON_SCHEMA_DATATYPE}
	 * alias, or no datatype at all, which is how the Form Builder saves translations (its REST call
	 * carries only the name and the clob's uuid as value reference). Initializer only re-matches
	 * {@code LongFreeTextDatatype} resources on re-import, which on a fresh target is the datatype it
	 * creates anyway.
	 */
	static boolean isTranslation(FormResource resource) {
		return resource != null && resource.getName() != null && TRANSLATION_NAME.matcher(resource.getName()).matches()
		        && (StringUtils.isBlank(resource.getDatatypeClassname()) || isClobBacked(resource.getDatatypeClassname()));
	}
	
	/** The language suffix of a translation resource's name, or null when the name is not one. */
	static String languageOf(FormResource resource) {
		if (resource == null || resource.getName() == null) {
			return null;
		}
		Matcher matcher = TRANSLATION_NAME.matcher(resource.getName());
		return matcher.matches() ? matcher.group(2) : null;
	}
	
	static List<FormResource> translationsOf(Collection<FormResource> resources) {
		List<FormResource> translations = new ArrayList<>();
		if (resources != null) {
			for (FormResource resource : resources) {
				if (isTranslation(resource)) {
					translations.add(resource);
				}
			}
		}
		return translations;
	}
	
	static boolean isClobBacked(String datatypeClassname) {
		return AMPATH_JSON_SCHEMA_DATATYPE.equals(datatypeClassname) || LONG_FREE_TEXT_DATATYPE.equals(datatypeClassname);
	}
	
	static Collection<FormResource> resourcesOf(Form form) {
		return Context.getFormService().getFormResourcesForForm(form);
	}
	
	/**
	 * The clob content a resource points at, or null when it cannot be read; {@link #readJson} says
	 * why. Deliberately bypasses {@link FormResource#getValue()}, see
	 * {@link #AMPATH_JSON_SCHEMA_DATATYPE}.
	 */
	static String readClob(FormResource resource) {
		return readJson(resource).text;
	}
	
	/**
	 * The clob content parsed as a JSON object, or null when it cannot be; {@link #readJson} says why.
	 */
	static ObjectNode readJsonObject(FormResource resource) {
		return readJson(resource).node;
	}
	
	/**
	 * Reads and parses a resource's clob, keeping the diagnosis when that fails so that the skip
	 * warning and the error a REST user sees can say what is actually wrong with the resource.
	 */
	static JsonRead readJson(FormResource resource) {
		if (resource == null) {
			return JsonRead.problem("there is no such resource");
		}
		String reference;
		try {
			reference = resource.getValueReference();
		}
		catch (NotYetPersistedException e) {
			reference = null;
		}
		if (StringUtils.isBlank(reference)) {
			return JsonRead.problem("no value has ever been stored for it");
		}
		ClobDatatypeStorage clob = Context.getDatatypeService().getClobDatatypeStorageByUuid(reference);
		if (clob == null) {
			return JsonRead.problem("it references clob " + reference + ", which does not exist");
		}
		return parseJsonObject(clob.getValue());
	}
	
	/** Parses JSON text into an object node, keeping the diagnosis when it is not one. */
	static JsonRead parseJsonObject(String json) {
		if (json == null) {
			return JsonRead.problem("its content is empty");
		}
		JsonNode parsed;
		try {
			parsed = MAPPER.readTree(json);
		}
		catch (JsonProcessingException e) {
			return JsonRead.problem("it is not valid JSON (" + e.getOriginalMessage() + ")");
		}
		catch (IOException e) {
			return JsonRead.problem("it could not be read (" + e.getMessage() + ")");
		}
		if (parsed == null || parsed.isMissingNode()) {
			return JsonRead.problem("its content is empty");
		}
		if (!parsed.isObject()) {
			return JsonRead
			        .problem("it is a JSON " + parsed.getNodeType().name().toLowerCase(Locale.ROOT) + ", not an object");
		}
		return JsonRead.of(json, (ObjectNode) parsed);
	}
	
	/** Parses JSON text into an object node, or null when it is not valid JSON or not an object. */
	static ObjectNode asJsonObject(String json) {
		return parseJsonObject(json).node;
	}
	
	/** Whether parsed content is a JSON object with a {@value #TRANSLATIONS_KEY} entry. */
	static boolean isTranslationDocument(JsonNode document) {
		return document != null && document.isObject() && document.has(TRANSLATIONS_KEY);
	}
	
	/**
	 * Result of reading a clob-backed resource as JSON: either the parsed object (and its raw text), or
	 * a clause saying why not, phrased to follow "its resource ...".
	 */
	static final class JsonRead {
		
		final String text;
		
		final ObjectNode node;
		
		final String problem;
		
		private JsonRead(String text, ObjectNode node, String problem) {
			this.text = text;
			this.node = node;
			this.problem = problem;
		}
		
		static JsonRead of(String text, ObjectNode node) {
			return new JsonRead(text, node, null);
		}
		
		static JsonRead problem(String problem) {
			return new JsonRead(null, null, problem);
		}
		
		boolean isObject() {
			return node != null;
		}
	}
	
	/** A file-system safe stem for a form name: lower case, anything unusual becomes an underscore. */
	static String fileName(String name) {
		if (StringUtils.isBlank(name)) {
			return "_";
		}
		return name.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
	}
	
	/**
	 * A distinct file stem per form, keyed by form uuid. Names that sanitize to the same stem (e.g.
	 * {@code Triage} and {@code TRIAGE}) get their version appended; any stems still shared after that
	 * (same version, or a name such as {@code Triage 1.0} that already reads like a suffixed one) get
	 * the form uuid appended, which makes them unique because form uuids are.
	 */
	static Map<String, String> uniqueStems(Collection<Form> forms) {
		Map<String, List<Form>> byStem = new LinkedHashMap<>();
		for (Form form : forms) {
			byStem.computeIfAbsent(fileName(form.getName()), s -> new ArrayList<>()).add(form);
		}
		Map<String, String> candidates = new LinkedHashMap<>();
		for (Map.Entry<String, List<Form>> entry : byStem.entrySet()) {
			for (Form form : entry.getValue()) {
				candidates.put(form.getUuid(),
				    entry.getValue().size() == 1 ? entry.getKey() : entry.getKey() + "_" + fileName(form.getVersion()));
			}
		}
		Map<String, Integer> occurrences = new HashMap<>();
		for (String stem : candidates.values()) {
			occurrences.merge(stem, 1, Integer::sum);
		}
		Map<String, String> stems = new HashMap<>();
		for (Map.Entry<String, String> entry : candidates.entrySet()) {
			String stem = entry.getValue();
			stems.put(entry.getKey(), occurrences.get(stem) == 1 ? stem : stem + "_" + entry.getKey());
		}
		return stems;
	}
	
	static String describe(Form form) {
		return form.getUuid() + " (" + form.getName() + " v" + form.getVersion() + ")";
	}
	
	static String describe(FormResource resource) {
		return resource.getUuid() + " (" + resource.getName() + ")";
	}
	
	/**
	 * Keeps one form per name (the exact name: Initializer's {@code getForm(name)} is exact too, so
	 * {@code "Triage"} and {@code "Triage "} are separately importable), the one with the highest
	 * version, ties going to the most recently created. Each form dropped is explained in
	 * {@code exclusions} (uuid to sentence) when that map is given.
	 */
	static List<Form> latestVersionPerName(Collection<Form> forms, Map<String, String> exclusions) {
		Map<String, Form> latest = new LinkedHashMap<>();
		for (Form form : forms) {
			String key = form.getName() == null ? "" : form.getName();
			Form current = latest.get(key);
			if (current == null) {
				latest.put(key, form);
				continue;
			}
			int order = VERSION_ORDER.compare(form, current);
			Form older = order > 0 ? current : form;
			Form newer = older == current ? form : current;
			latest.put(key, newer);
			if (exclusions != null) {
				exclusions.put(older.getUuid(),
				    describe(older) + " is superseded by " + describe(newer)
				            + (order == 0
				                    ? ", an arbitrary pick between two forms with the same name, version and creation date"
				                    : ", the newer version of the same name"));
			}
		}
		return new ArrayList<>(latest.values());
	}
	
	private static final Comparator<Form> VERSION_ORDER = (a, b) -> {
		int byVersion = compareVersions(a.getVersion(), b.getVersion());
		if (byVersion != 0) {
			return byVersion;
		}
		if (a.getDateCreated() != null && b.getDateCreated() != null) {
			return a.getDateCreated().compareTo(b.getDateCreated());
		}
		return 0;
	};
	
	/**
	 * Core's dotted-number comparison, which never throws but ranks anything it cannot parse as equal
	 * (e.g. {@code alpha} vs {@code beta}, or {@code 1.0} vs {@code 1}); such non-equal strings fall
	 * back to plain string order so the choice is at least deterministic.
	 */
	static int compareVersions(String a, String b) {
		String left = StringUtils.trimToEmpty(a);
		String right = StringUtils.trimToEmpty(b);
		int byNumbers = ModuleUtil.compareVersion(left, right);
		if (byNumbers != 0 || left.equals(right)) {
			return byNumbers;
		}
		return left.compareTo(right);
	}
}
