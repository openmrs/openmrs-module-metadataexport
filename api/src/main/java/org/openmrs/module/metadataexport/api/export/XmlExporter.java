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

import org.openmrs.module.initializer.Domain;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 * Low-level writer for XML domains: serializes an already-built DOM {@link Document} to
 * {@code configuration/<domain>/<fileName>} beneath the export root, indented and UTF-8 encoded.
 * <p>
 * The XML analogue of {@link CsvExporter}, but the division of labour differs: {@code CsvExporter}
 * builds the document itself from a line-exporter chain, whereas here the caller hands in a
 * finished {@link Document} and this class only places and serializes it (see
 * {@link XmlDomainExporter} for why the seam sits higher for XML).
 */
public class XmlExporter {
	
	public void writeXml(Document document, Domain domain, File outDir, String fileName) throws IOException {
		File domainDir = new File(new File(outDir, "configuration"), domain.getName());
		domainDir.mkdirs();
		File target = new File(domainDir, fileName);
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(target);
			transformer.transform(source, result);
		}
		catch (TransformerException e) {
			throw new IOException("Failed to write XML for domain " + domain.getName(), e);
		}
	}
}
