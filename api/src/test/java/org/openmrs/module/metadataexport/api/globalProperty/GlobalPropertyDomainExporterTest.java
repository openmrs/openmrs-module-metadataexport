/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.globalProperty;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.GlobalProperty;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.InitializerSerializer;
import org.openmrs.module.metadataexport.api.export.ExportContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalPropertyDomainExporterTest {
	
	private final GlobalPropertyDomainExporter exporter = new GlobalPropertyDomainExporter();
	
	@Test
	void toDocuments_wrapsPropertiesInConfigContainer() {
		Map<String, Document> documents = exporter
		        .toDocuments(Arrays.asList(new GlobalProperty("locale.allowed.list", "en, km_KH"),
		            new GlobalProperty("addresshierarchy.i18nSupport", "true")));
		
		assertEquals(Collections.singleton("globalProperties.xml"), documents.keySet());
		Document document = documents.get("globalProperties.xml");
		
		assertEquals("config", document.getDocumentElement().getTagName());
		assertEquals(1, document.getElementsByTagName("globalProperties").getLength());
		
		NodeList properties = document.getElementsByTagName("globalProperty");
		assertEquals(2, properties.getLength());
		
		Element first = (Element) properties.item(0);
		assertEquals("locale.allowed.list", childText(first, "property"));
		assertEquals("en, km_KH", childText(first, "value"));
	}
	
	@Test
	void toDocuments_nullValueBecomesEmptyElement() {
		Map<String, Document> documents = exporter
		        .toDocuments(Collections.singletonList(new GlobalProperty("some.property", null)));
		
		Element property = (Element) documents.get("globalProperties.xml").getElementsByTagName("globalProperty").item(0);
		assertEquals("some.property", childText(property, "property"));
		assertEquals("", childText(property, "value"));
	}
	
	@Test
	void export_roundTripsThroughInitializerDeserializer(@TempDir File outDir) throws Exception {
		exporter.export(Arrays.asList(new GlobalProperty("locale.allowed.list", "en & km_KH"),
		    new GlobalProperty("some.property", null)), new ExportContext(outDir));
		
		File written = new File(new File(outDir, "configuration"),
		        Domain.GLOBAL_PROPERTIES.getName() + "/globalProperties.xml");
		assertTrue(written.exists(), "expected " + written);
		
		List<GlobalProperty> loaded;
		try (InputStream in = Files.newInputStream(written.toPath())) {
			loaded = InitializerSerializer.getGlobalPropertiesConfig(in).getGlobalProperties();
		}
		
		Map<String, String> byName = new HashMap<>();
		for (GlobalProperty gp : loaded) {
			byName.put(gp.getProperty(), gp.getPropertyValue());
		}
		
		assertEquals(2, loaded.size());
		assertEquals("en & km_KH", byName.get("locale.allowed.list"));
		assertTrue(byName.containsKey("some.property"), "null-valued GP should round-trip");
		assertEquals("", StringUtils.defaultString(byName.get("some.property")));
	}
	
	private static String childText(Element parent, String tag) {
		Node node = parent.getElementsByTagName(tag).item(0);
		return node == null ? null : node.getTextContent();
	}
}
