/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.impl;

import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.ExporterService;
import org.openmrs.module.metadataexport.api.export.DomainExporter;
import org.openmrs.module.metadataexport.api.export.DomainExporterRegistry;
import org.openmrs.module.metadataexport.api.export.ExportContext;
import org.openmrs.module.metadataexport.api.select.ExportManifest;
import org.openmrs.module.metadataexport.api.select.Selector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExporterServiceImpl implements ExporterService {
	
	private final DomainExporterRegistry registry;
	
	public ExporterServiceImpl(DomainExporterRegistry registry) {
		this.registry = registry;
	}
	
	@Override
	public void export(File outDir, Collection<Domain> domains) throws IOException {
		List<OpenmrsObject> seeds = new ArrayList<>();
		for (DomainExporter<?> exporter : registry.all()) {
			if (isSelected(domains, exporter.getDomain())) {
				seeds.addAll(exporter.getAllInstances());
			}
		}
		
		ExportManifest manifest = new Selector(registry).select(seeds);
		
		ExportContext context = new ExportContext(outDir);
		for (Domain domain : manifest.getDomains()) {
			writeDomain(registry.forDomain(domain), manifest.get(domain), context);
		}
	}
	
	private static boolean isSelected(Collection<Domain> domains, Domain domain) {
		return domains == null || domains.isEmpty() || domains.contains(domain);
	}
	
	@SuppressWarnings("unchecked")
	private <T extends OpenmrsObject> void writeDomain(DomainExporter<T> exporter, Collection<OpenmrsObject> bucket,
	        ExportContext context) throws IOException {
		List<T> typed = new ArrayList<>();
		for (OpenmrsObject instance : bucket) {
			typed.add((T) instance);
		}
		exporter.export(typed, context);
	}
}
