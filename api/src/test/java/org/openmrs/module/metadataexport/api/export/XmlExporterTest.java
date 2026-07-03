/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.export;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.module.initializer.Domain;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlExporterTest {
	
	@TempDir
	File outDir;
	
	@Test
	void writeXml_writesUnderConfigurationDomainDirAndRoundTrips() throws Exception {
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element config = document.createElement("config");
		Element container = document.createElement("globalProperties");
		Element value = document.createElement("value");
		value.appendChild(document.createTextNode("en & km_KH"));
		container.appendChild(value);
		config.appendChild(container);
		document.appendChild(config);
		
		new XmlExporter().writeXml(document, Domain.GLOBAL_PROPERTIES, outDir, "globalProperties.xml");
		
		File written = new File(new File(outDir, "configuration"),
		        Domain.GLOBAL_PROPERTIES.getName() + "/globalProperties.xml");
		assertTrue(written.exists(), "expected " + written);
		
		Document reparsed = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(written);
		assertEquals("config", reparsed.getDocumentElement().getTagName());
		assertEquals(1, reparsed.getElementsByTagName("globalProperties").getLength());
		assertEquals("en & km_KH", reparsed.getElementsByTagName("value").item(0).getTextContent());
	}
}
