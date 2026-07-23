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

import com.opencsv.CSVWriter;
import lombok.AllArgsConstructor;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

@AllArgsConstructor
public class CsvExporter<T extends OpenmrsObject> {
	
	private List<BaseLineExporter<T>> chain;
	
	private Domain domain;
	
	public List<ExportLine> toLines(Collection<T> instances) {
		List<ExportLine> lines = new ArrayList<>();
		for (T instance : instances) {
			ExportLine line = new ExportLine();
			for (BaseLineExporter<T> exporter : chain) {
				exporter.writeLine(instance, line);
			}
			lines.add(line);
		}
		return lines;
	}
	
	public void writeCsv(Collection<T> instances, File outDir, String fileName) throws IOException {
		List<ExportLine> lines = toLines(instances);
		LinkedHashSet<String> headers = new LinkedHashSet<>();
		for (ExportLine line : lines) {
			headers.addAll(line.getHeaders());
		}
		
		headers.add(BaseLineExporter.VERSION_LHS + "1");
		String[] headerRow = headers.toArray(new String[0]);
		
		File domainDir = new File(new File(outDir, "configuration"), domain.getName());
		domainDir.mkdirs();
		File target = new File(domainDir, fileName);
		
		try (CSVWriter writer = new CSVWriter(
		        new OutputStreamWriter(Files.newOutputStream(target.toPath()), StandardCharsets.UTF_8))) {
			writer.writeNext(headerRow);
			for (ExportLine line : lines) {
				String[] row = new String[headerRow.length];
				for (int i = 0; i < headerRow.length; i++) {
					row[i] = line.getValueOrBlank(headerRow[i]);
				}
				writer.writeNext(row);
			}
			
		}
	}
	
}
