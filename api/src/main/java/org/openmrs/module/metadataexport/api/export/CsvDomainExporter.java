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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Base for domains whose Iniz format is CSV. Subclasses supply the line-exporter chain and the file
 * name(s); this class drives {@link CsvExporter}.
 * <p>
 * A CSV domain is not assumed to be a single file: {@link #partition} maps file name → the
 * instances that belong in it, and each entry becomes its own CSV. The default is one file
 * ({@link #fileName}), but a domain may split into several (e.g. to keep the union-header width
 * manageable, since one unusually verbose row otherwise widens the table for every row in the
 * file).
 */
public abstract class CsvDomainExporter<T extends OpenmrsObject> implements DomainExporter<T> {
	
	protected abstract List<BaseLineExporter<T>> chain();
	
	protected abstract String fileName();
	
	/** File name → the instances written to it. Default: everything in one file. */
	protected Map<String, Collection<T>> partition(Collection<T> instances) {
		return Collections.singletonMap(fileName(), instances);
	}
	
	@Override
	public void export(Collection<T> instances, ExportContext context) throws IOException {
		CsvExporter<T> exporter = new CsvExporter<>(chain(), getDomain());
		for (Map.Entry<String, Collection<T>> file : partition(instances).entrySet()) {
			exporter.writeCsv(file.getValue(), context.getOutputDir(), file.getKey());
		}
	}
}
