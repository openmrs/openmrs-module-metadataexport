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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.module.initializer.Domain;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonExporterTest {
	
	@TempDir
	File outDir;
	
	@Test
	void writeJson_writesUnderConfigurationDomainDirPrettyPrintedInUtf8WithATrailingNewline() throws Exception {
		ObjectNode document = new ObjectMapper().createObjectNode();
		document.put("name", "Formulaire d'essai — ខ្មែរ");
		document.putObject("translations").put("Yes", "Oui");
		
		new JsonExporter().writeJson(document, Domain.AMPATH_FORMS, outDir, "test_form.json");
		
		File written = new File(new File(outDir, "configuration"), Domain.AMPATH_FORMS.getName() + "/test_form.json");
		assertTrue(written.exists(), "expected " + written);
		String content = new String(Files.readAllBytes(written.toPath()), StandardCharsets.UTF_8);
		assertTrue(content.endsWith("}\n"), "newline-terminated, as Initializer's own sample files are");
		assertTrue(content.contains("\n  \"translations\" : {"), "pretty-printed, one entry per line");
		assertEquals(document, new ObjectMapper().readTree(written), "round-trips, including non-ASCII text");
	}
}
