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

/**
 * Base for domains whose Iniz format is XML. Subclasses build the DOM for their domain in
 * {@link #toDocuments}; this class drives {@link XmlExporter} to place and serialize each document.
 * <p>
 * The abstraction boundary sits deliberately higher than {@link CsvDomainExporter}: a CSV subclass
 * returns <em>data</em> (its line-exporter chain) and the framework owns the format, whereas an XML
 * subclass returns an already-built {@link Document} and owns its full structure. XML shape is too
 * domain-specific to factor into a shared per-field seam, so this class owns only file placement
 * and serialization, not document construction.
 * <p>
 * Like {@link CsvDomainExporter}, an XML domain is not assumed to be a single file:
 * {@link #toDocuments} maps file name -> the document for that file, so a domain may emit one file
 * (global properties) or many (e.g. one document per form).
 */
public abstract class XmlDomainExporter<T extends OpenmrsObject> implements DomainExporter<T> {
	
	protected abstract Map<String, Document> toDocuments(Collection<T> instances);
	
	protected abstract String fileName();
	
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
