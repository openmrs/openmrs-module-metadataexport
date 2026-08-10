/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.addresshierarchy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.layout.address.AddressSupport;
import org.openmrs.layout.address.AddressTemplate;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.config.AddressComponent;
import org.openmrs.module.addresshierarchy.config.AddressConfiguration;
import org.openmrs.module.addresshierarchy.config.AddressConfigurationLoader;
import org.openmrs.module.addresshierarchy.config.AddressHierarchyFile;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.openmrs.module.metadataexport.export.ExportContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exports the address hierarchy, which Initializer does not load through its own line-processor
 * framework: {@code AddressHierarchyLoader} hands the whole directory to the addresshierarchy
 * module's own {@code AddressConfigurationLoader}. So this is a directory-of-files domain, not a
 * row-per-object CSV, and it implements {@link DomainExporter} directly (like the metadatasharing
 * exporter) rather than extending the CSV/XML line framework.
 * <p>
 * It reproduces the two files a fresh import needs:
 * <ul>
 * <li>{@code addressConfiguration.xml} &mdash; rebuilt from the ordered hierarchy levels and the
 * live address template global property, then serialized with the module's own
 * {@link AddressConfigurationLoader#writeToString(AddressConfiguration)} so the XML matches the
 * import format. Each ordered level maps 1:1 to an {@code addressComponent}: the level's name is
 * the component's {@code nameMapping} and the level's {@code required} flag is
 * {@code requiredInHierarchy}; only {@code sizeMapping}/{@code elementDefault} and the
 * {@code lineByLineFormat} come from the address template.</li>
 * <li>{@code addresshierarchy.csv} &mdash; one headerless row per leaf entry, the root-to-leaf
 * entry names joined by the entry delimiter, each cell optionally carrying
 * {@code name^userGeneratedId}.</li>
 * </ul>
 * The entry/identifier delimiters are not persisted anywhere (they are used only at import time),
 * so canonical defaults are emitted and the CSV is written to match them.
 */
@Slf4j
@Component
@OpenmrsProfile(modules = { "addresshierarchy:2.17.0" })
public class AddressHierarchyDomainExporter implements DomainExporter<AddressHierarchyEntry> {
	
	public static final String CONFIG_FILE_NAME = "addressConfiguration.xml";
	
	public static final String ENTRIES_FILE_NAME = "addresshierarchy.csv";
	
	public static final String ENTRY_DELIMITER = ",";
	
	public static final String IDENTIFIER_DELIMITER = "^";
	
	private static final int DEFAULT_SIZE_MAPPING = 40;
	
	@Override
	public Domain getDomain() {
		return Domain.ADDRESS_HIERARCHY;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof AddressHierarchyEntry;
	}
	
	@Override
	public Collection<AddressHierarchyEntry> getAllInstances() {
		AddressHierarchyService service = Context.getService(AddressHierarchyService.class);
		List<AddressHierarchyEntry> all = new ArrayList<>();
		for (AddressHierarchyLevel level : service.getOrderedAddressHierarchyLevels()) {
			all.addAll(service.getAddressHierarchyEntriesByLevel(level));
		}
		return all;
	}

	@Override
	public Collection<? extends OpenmrsObject> getDependencies(AddressHierarchyEntry instance) {
		return Collections.emptyList();
	}
	
	@Override
	public void export(Collection<AddressHierarchyEntry> instances, ExportContext context) throws IOException {
		List<AddressHierarchyLevel> levels = Context.getService(AddressHierarchyService.class)
		        .getOrderedAddressHierarchyLevels();
		if (levels.isEmpty()) {
			log.warn("Address Hierarchy: no hierarchy levels are configured, nothing to export");
			return;
		}
		
		File domainDir = new File(new File(context.getOutputDir(), "configuration"), getDomain().getName());
		domainDir.mkdirs();
		
		AddressTemplate template = AddressSupport.getInstance().getDefaultLayoutTemplate();
		String configXml = buildAddressConfigurationXml(levels, template);
		Files.write(new File(domainDir, CONFIG_FILE_NAME).toPath(), configXml.getBytes(StandardCharsets.UTF_8));
		
		String entriesCsv = buildEntriesCsv(instances);
		Files.write(new File(domainDir, ENTRIES_FILE_NAME).toPath(), entriesCsv.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Rebuilds the {@code addressConfiguration.xml} content from the ordered hierarchy levels and the
	 * live address template, delegating the actual serialization to the addresshierarchy module so the
	 * output stays in lockstep with what it parses on import.
	 */
	String buildAddressConfigurationXml(List<AddressHierarchyLevel> levels, AddressTemplate template) {
		Map<String, String> sizeMappings = template == null ? Collections.emptyMap() : template.getSizeMappings();
		Map<String, String> elementDefaults = template == null ? Collections.emptyMap() : template.getElementDefaults();
		
		AddressConfiguration configuration = new AddressConfiguration();
		for (AddressHierarchyLevel level : levels) {
			AddressField field = level.getAddressField();
			String token = field == null ? null : field.getName();
			
			AddressComponent component = new AddressComponent();
			component.setField(field);
			component.setNameMapping(level.getName());
			component.setSizeMapping(parseSize(sizeMappings.get(token)));
			component.setElementDefault(elementDefaults.get(token));
			component.setRequiredInHierarchy(Boolean.TRUE.equals(level.getRequired()));
			configuration.addAddressComponent(component);
		}
		
		if (template != null && template.getLineByLineFormat() != null) {
			configuration.setLineByLineFormat(new ArrayList<>(template.getLineByLineFormat()));
		}
		
		AddressHierarchyFile file = new AddressHierarchyFile();
		file.setFilename(ENTRIES_FILE_NAME);
		file.setEntryDelimiter(ENTRY_DELIMITER);
		file.setIdentifierDelimiter(IDENTIFIER_DELIMITER);
		configuration.setAddressHierarchyFile(file);
		
		return AddressConfigurationLoader.writeToString(configuration);
	}
	
	/**
	 * Builds the headerless entries CSV from the entries alone (no extra queries): a leaf is any entry
	 * that is not some other entry's parent, and each leaf's row is its root-to-leaf path walked
	 * through {@link AddressHierarchyEntry#getParent()}. Rows are sorted for deterministic output.
	 */
	String buildEntriesCsv(Collection<AddressHierarchyEntry> instances) {
		Set<Integer> parentIds = new HashSet<>();
		for (AddressHierarchyEntry entry : instances) {
			AddressHierarchyEntry parent = entry.getParent();
			if (parent != null && parent.getId() != null) {
				parentIds.add(parent.getId());
			}
		}
		
		List<String> rows = new ArrayList<>();
		for (AddressHierarchyEntry entry : instances) {
			if (entry.getId() != null && parentIds.contains(entry.getId())) {
				continue; // not a leaf: it is covered by its descendants' rows
			}
			rows.add(buildRow(entry));
		}
		Collections.sort(rows);
		
		StringBuilder csv = new StringBuilder();
		for (String row : rows) {
			csv.append(row).append('\n');
		}
		return csv.toString();
	}
	
	private static String buildRow(AddressHierarchyEntry leaf) {
		Deque<String> cells = new ArrayDeque<>();
		for (AddressHierarchyEntry current = leaf; current != null; current = current.getParent()) {
			cells.addFirst(buildCell(current));
		}
		return String.join(ENTRY_DELIMITER, cells);
	}
	
	private static String buildCell(AddressHierarchyEntry entry) {
		String name = entry.getName() == null ? "" : entry.getName();
		if (StringUtils.isNotEmpty(entry.getUserGeneratedId())) {
			return name + IDENTIFIER_DELIMITER + entry.getUserGeneratedId();
		}
		return name;
	}
	
	private static int parseSize(String size) {
		if (StringUtils.isBlank(size)) {
			return DEFAULT_SIZE_MAPPING;
		}
		try {
			return Integer.parseInt(size.trim());
		}
		catch (NumberFormatException e) {
			return DEFAULT_SIZE_MAPPING;
		}
	}
}
