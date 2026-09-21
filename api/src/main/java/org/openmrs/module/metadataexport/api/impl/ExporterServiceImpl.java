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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.ExporterService;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporterRegistry;
import org.openmrs.module.metadataexport.export.ExportContext;
import org.openmrs.module.metadataexport.select.ExportManifest;
import org.openmrs.module.metadataexport.select.Selector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
public class ExporterServiceImpl implements ExporterService {
	
	private final DomainExporterRegistry registry;
	
	@Override
	public void export(File outDir, Collection<Domain> domains) throws IOException {
		List<OpenmrsObject> seeds = new ArrayList<>();
		List<Domain> selected = new ArrayList<>();
		for (DomainExporter<?> exporter : registry.all()) {
			if (isSelected(domains, exporter.getDomain())) {
				seeds.addAll(exporter.getAllInstances());
				selected.add(exporter.getDomain());
			}
		}
		exclusions(selected);
		exportSeeds(outDir, seeds);
	}
	
	@Override
	public Map<Domain, Map<String, String>> exclusions(Collection<Domain> domains) {
		Map<Domain, Map<String, String>> excluded = new LinkedHashMap<>();
		for (Domain domain : domains) {
			DomainExporter<?> exporter = registry.forDomain(domain);
			if (exporter == null) {
				throw new APIException("No exporter registered for domain " + domain);
			}
			Map<String, String> exclusions = exporter.exclusions();
			if (!exclusions.isEmpty()) {
				excluded.put(domain, exclusions);
				log.warn("Metadata Export: {} {} row(s) exist but are not exported: {}", exclusions.size(), domain,
				    String.join("; ", exclusions.values()));
			}
		}
		return excluded;
	}
	
	@Override
	public ExportManifest exportSeeds(File outDir, Collection<? extends OpenmrsObject> seeds) throws IOException {
		ExportManifest manifest = new Selector(registry).select(seeds);
		
		ExportContext context = new ExportContext(outDir);
		for (Domain domain : manifest.getDomains()) {
			writeDomain(registry.forDomain(domain), manifest.get(domain), context);
		}
		return manifest;
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
