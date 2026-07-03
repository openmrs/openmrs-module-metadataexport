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

import org.openmrs.OpenmrsObject;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public abstract class XmlDomainExporter<T extends OpenmrsObject> implements DomainExporter<T> {
	
	protected abstract Map<String, Document> toDocuments(Collection<T> instances);
	
	@Override
	public void export(Collection<T> instances, ExportContext context) throws IOException {
		XmlExporter xmlExporter = new XmlExporter();
		for (Map.Entry<String, Document> file : toDocuments(instances).entrySet()) {
			xmlExporter.writeXml(file.getValue(), getDomain(), context.getOutputDir(), file.getKey());
		}
	}
	
	protected Document newDocument() {
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException e) {
			throw new IllegalStateException("Could not create XML document builder", e);
		}
	}
}
