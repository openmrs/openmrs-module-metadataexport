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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.module.initializer.Domain;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Low-level writer for JSON domains: serializes an already-built Jackson {@link JsonNode} to
 * {@code configuration/<domain>/<fileName>} beneath the export root, pretty-printed, UTF-8 encoded
 * and newline-terminated.
 * <p>
 * The JSON analogue of {@link XmlExporter}: the caller hands in a finished document and this class
 * only places and serializes it (see {@link JsonDomainExporter} for why the seam sits there).
 */
public class JsonExporter {
	
	/** Also backs {@link JsonDomainExporter#newObject()}. */
	static final ObjectMapper MAPPER = new ObjectMapper();
	
	public void writeJson(JsonNode document, Domain domain, File outDir, String fileName) throws IOException {
		File target = new File(ExportContext.domainDir(outDir, domain), fileName);
		String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(document) + "\n";
		Files.write(target.toPath(), json.getBytes(StandardCharsets.UTF_8));
	}
}
