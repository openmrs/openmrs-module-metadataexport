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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.Form;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonDomainExporterTest {
	
	/** One document per form, named by uuid; a form named "skip" is left out of the map. */
	private static class TestDomainExporter extends JsonDomainExporter<Form> {
		
		@Override
		public Domain getDomain() {
			return Domain.AMPATH_FORMS;
		}
		
		@Override
		public boolean handles(OpenmrsObject instance) {
			return instance instanceof Form;
		}
		
		@Override
		public Collection<Form> getAllInstances() {
			return Collections.emptyList();
		}
		
		@Override
		public Collection<? extends OpenmrsObject> getDependencies(Form instance) {
			return Collections.emptyList();
		}
		
		@Override
		protected Map<String, JsonNode> toDocuments(Collection<Form> instances) {
			Map<String, JsonNode> documents = new LinkedHashMap<>();
			for (Form form : instances) {
				if ("skip".equals(form.getName())) {
					continue;
				}
				ObjectNode document = newObject();
				document.put("name", form.getName());
				documents.put(form.getUuid() + JSON_EXTENSION, document);
			}
			return documents;
		}
	}
	
	@Test
	void export_writesOneFilePerDocumentUnderTheDomainDir(@TempDir File outDir) throws Exception {
		new TestDomainExporter().export(Arrays.asList(form("f1", "First"), form("f2", "Second")), new ExportContext(outDir));
		
		File domainDir = outDir.toPath().resolve(Paths.get("configuration", Domain.AMPATH_FORMS.getName())).toFile();
		assertEquals(2, domainDir.list().length);
		assertEquals("First", new ObjectMapper().readTree(new File(domainDir, "f1.json")).get("name").asText());
		assertEquals("Second", new ObjectMapper().readTree(new File(domainDir, "f2.json")).get("name").asText());
	}
	
	@Test
	void export_writesNothingForAnInstanceLeftOutOfTheMap(@TempDir File outDir) throws Exception {
		new TestDomainExporter().export(Arrays.asList(form("f1", "First"), form("f2", "skip")), new ExportContext(outDir));
		
		File domainDir = outDir.toPath().resolve(Paths.get("configuration", Domain.AMPATH_FORMS.getName())).toFile();
		assertTrue(new File(domainDir, "f1.json").exists());
		assertFalse(new File(domainDir, "f2.json").exists(),
		    "leaving an instance out of the map is how a subclass skips it");
	}
	
	@Test
	void export_withNoDocumentsWritesNoFiles(@TempDir File outDir) throws Exception {
		new TestDomainExporter().export(Collections.emptyList(), new ExportContext(outDir));
		
		File domainDir = outDir.toPath().resolve(Paths.get("configuration", Domain.AMPATH_FORMS.getName())).toFile();
		assertTrue(domainDir.list() == null || domainDir.list().length == 0);
	}
	
	@Test
	void readTree_parsesStoredContentForEditing() throws Exception {
		JsonNode tree = JsonDomainExporter.readTree("{\"name\":\"x\",\"pages\":[]}");
		
		assertEquals("x", tree.get("name").asText());
		assertTrue(tree.get("pages").isArray());
	}
	
	private static Form form(String uuid, String name) {
		Form form = new Form();
		form.setUuid(uuid);
		form.setName(name);
		return form;
	}
}
