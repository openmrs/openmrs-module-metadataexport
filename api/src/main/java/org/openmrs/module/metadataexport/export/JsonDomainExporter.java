/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openmrs.OpenmrsObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Base for domains whose Iniz format is JSON, one document per file. Subclasses build the documents
 * in {@link #toDocuments}; this class drives {@link JsonExporter} to place and serialize each one.
 * <p>
 * Same division of labour as {@link XmlDomainExporter}: the subclass owns the full structure of
 * each document (JSON shape is too domain-specific for a shared per-field seam) and this class owns
 * only file placement and serialization. {@link #toDocuments} maps file name &rarr; document, so a
 * domain may emit one file or many (e.g. one schema per form), and leaving an instance out of the
 * map is how a subclass skips it.
 */
public abstract class JsonDomainExporter<T extends OpenmrsObject> implements DomainExporter<T> {
	
	public static final String JSON_EXTENSION = ".json";
	
	protected abstract Map<String, JsonNode> toDocuments(Collection<T> instances) throws IOException;
	
	@Override
	public void export(Collection<T> instances, ExportContext context) throws IOException {
		JsonExporter jsonExporter = new JsonExporter();
		for (Map.Entry<String, JsonNode> file : toDocuments(instances).entrySet()) {
			jsonExporter.writeJson(file.getValue(), getDomain(), context.getOutputDir(), file.getKey());
		}
	}
	
	protected static ObjectNode newObject() {
		return JsonExporter.MAPPER.createObjectNode();
	}
}
