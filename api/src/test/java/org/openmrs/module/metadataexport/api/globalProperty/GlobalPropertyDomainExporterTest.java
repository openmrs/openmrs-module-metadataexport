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

import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
	
	private static String childText(Element parent, String tag) {
		Node node = parent.getElementsByTagName(tag).item(0);
		return node == null ? null : node.getTextContent();
	}
}
