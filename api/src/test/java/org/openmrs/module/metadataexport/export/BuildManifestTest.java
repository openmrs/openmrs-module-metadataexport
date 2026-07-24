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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportPackage;
import org.openmrs.module.metadataexport.api.model.ExportPackageEntry;
import org.openmrs.module.metadataexport.select.ExportManifest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BuildManifestTest {
	
	@Test
	void toJson_capturesPackageEntriesAndResolvedItems() throws Exception {
		ExportPackage exportPackage = new ExportPackage();
		exportPackage.setName("Site A locations");
		exportPackage.setDescription("Locations for Site A");
		ExportPackageEntry entry = new ExportPackageEntry();
		entry.setDomain(Domain.LOCATIONS.name());
		entry.getItemUuids().addAll(Arrays.asList("loc-1", "loc-2"));
		exportPackage.getEntries().add(entry);
		
		ExportBuild build = new ExportBuild();
		build.setExportPackage(exportPackage);
		build.setVersion(3);
		
		Location siteA = new Location();
		siteA.setName("Site A");
		siteA.setUuid("loc-1");
		ExportManifest exported = new ExportManifest();
		exported.add(Domain.LOCATIONS, Location.class.getName() + " loc-1", siteA);
		
		String json = BuildManifest.of(exportPackage, build, exported).toJson();
		
		JsonNode root = new ObjectMapper().readTree(json);
		assertEquals("Site A locations", root.get("name").asText());
		assertEquals(exportPackage.getUuid(), root.get("packageUuid").asText());
		assertEquals(build.getUuid(), root.get("buildUuid").asText());
		assertEquals(3, root.get("version").asInt());
		
		JsonNode entryNode = root.get("entries").get(0);
		assertEquals("LOCATIONS", entryNode.get("domain").asText());
		assertEquals("loc-2", entryNode.get("itemUuids").get(1).asText());
		
		JsonNode item = root.get("resolvedItems").get("LOCATIONS").get(0);
		assertEquals(Location.class.getName(), item.get("type").asText());
		assertEquals("loc-1", item.get("uuid").asText());
		assertEquals("Site A", item.get("display").asText());
	}
}
