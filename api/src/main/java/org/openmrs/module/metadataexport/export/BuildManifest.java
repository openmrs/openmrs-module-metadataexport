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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportPackage;
import org.openmrs.module.metadataexport.api.model.ExportPackageEntry;
import org.openmrs.module.metadataexport.select.ExportManifest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * The human- and machine-readable record of one build: the package identity, the entries as the
 * user defined them, and every item that actually got exported (including dependency-pulled ones).
 * Written as {@code package.json} at the zip root and stored on the build row as
 * {@code manifest_json} — the metadatasharing header.xml analogue. Also records, per domain
 * exported in full, the rows that were deliberately left out and why.
 */
@Getter
@Setter
public class BuildManifest {
	
	private String name;
	
	private String description;
	
	private String packageUuid;
	
	private String buildUuid;
	
	private Integer version;
	
	private Date dateCreated;
	
	private List<Entry> entries = new ArrayList<>();
	
	private Map<String, List<Item>> resolvedItems = new LinkedHashMap<>();
	
	/**
	 * For each domain exported in full, the rows this server has that were left out, uuid to reason.
	 * The one place a consumer of the zip can learn that an export was not complete.
	 */
	private Map<String, Map<String, String>> excluded = new LinkedHashMap<>();
	
	@Getter
	@Setter
	public static class Entry {
		
		private String domain;
		
		private List<String> itemUuids = new ArrayList<>();
	}
	
	@Getter
	@Setter
	public static class Item {
		
		private String type;
		
		private String uuid;
		
		private String display;
	}
	
	public static BuildManifest of(ExportPackage exportPackage, ExportBuild build, ExportManifest exported,
	        Map<Domain, Map<String, String>> excluded) {
		BuildManifest manifest = new BuildManifest();
		manifest.setName(exportPackage.getName());
		manifest.setDescription(exportPackage.getDescription());
		manifest.setPackageUuid(exportPackage.getUuid());
		manifest.setBuildUuid(build.getUuid());
		manifest.setVersion(build.getVersion());
		manifest.setDateCreated(build.getDateCreated());
		
		for (ExportPackageEntry packageEntry : exportPackage.getEntries()) {
			Entry entry = new Entry();
			entry.setDomain(packageEntry.getDomain());
			entry.setItemUuids(new ArrayList<>(packageEntry.getItemUuids()));
			manifest.getEntries().add(entry);
		}
		
		for (Domain domain : exported.getDomains()) {
			List<Item> items = new ArrayList<>();
			for (OpenmrsObject object : exported.get(domain)) {
				Item item = new Item();
				// same class resolution as DomainExporter.identityKey, so proxies report the real type
				item.setType(Hibernate.getClass(object).getName());
				item.setUuid(object.getUuid());
				if (object instanceof OpenmrsMetadata) {
					item.setDisplay(((OpenmrsMetadata) object).getName());
				}
				items.add(item);
			}
			manifest.getResolvedItems().put(domain.name(), items);
		}
		for (Map.Entry<Domain, Map<String, String>> entry : excluded.entrySet()) {
			manifest.getExcluded().put(entry.getKey().name(), new LinkedHashMap<>(entry.getValue()));
		}
		return manifest;
	}
	
	public String toJson() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		iso.setTimeZone(TimeZone.getTimeZone("UTC"));
		mapper.setDateFormat(iso);
		return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
	}
}
