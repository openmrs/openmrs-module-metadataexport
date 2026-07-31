/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.Location;
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportPackage;
import org.openmrs.module.metadataexport.api.model.ExportPackageEntry;
import org.openmrs.module.metadataexport.api.model.ExportStatus;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.util.Arrays;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataExportServiceTest extends BaseModuleContextSensitiveTest {
	
	private MetadataExportService service;
	
	@BeforeEach
	void setUp(@TempDir File appDataDir) {
		service = Context.getService(MetadataExportService.class);
		OpenmrsUtil.setApplicationDataDirectory(appDataDir.getAbsolutePath());
	}
	
	@Test
	void savePackage_roundTripsTheDefinitionWithEntries() {
		ExportPackage saved = service
		        .saveExportPackage(packageWith("Site A locations", Domain.LOCATIONS.name(), "u-1", "u-2"));
		
		ExportPackage loaded = service.getPackageByUuid(saved.getUuid());
		
		assertEquals("Site A locations", loaded.getName());
		assertEquals(1, loaded.getEntries().size());
		ExportPackageEntry entry = loaded.getEntries().get(0);
		assertEquals(Domain.LOCATIONS.name(), entry.getDomain());
		assertEquals(Arrays.asList("u-1", "u-2"), entry.getItemUuids());
	}
	
	@Test
	void savePackage_rejectsADuplicateName() {
		service.saveExportPackage(packageWith("Dup", Domain.LOCATIONS.name()));
		
		assertThrows(ValidationException.class,
		    () -> service.saveExportPackage(packageWith("Dup", Domain.LOCATIONS.name())));
	}
	
	@Test
	void savePackage_rejectsAnUnknownDomain() {
		assertThrows(ValidationException.class, () -> service.saveExportPackage(packageWith("Bad", "NOT_A_DOMAIN")));
	}
	
	@Test
	void savePackage_rejectsADomainWithoutARegisteredExporter() {
		assertThrows(ValidationException.class, () -> service.saveExportPackage(packageWith("Forms pkg", "HTML_FORMS")));
	}
	
	@Test
	void savePackage_allowsResavingUnderItsOwnName() {
		ExportPackage saved = service.saveExportPackage(packageWith("Same", Domain.LOCATIONS.name()));
		saved.setDescription("updated");
		
		service.saveExportPackage(saved);
		
		assertEquals("updated", service.getPackageByUuid(saved.getUuid()).getDescription());
	}
	
	@Test
	void failStrandedBuilds_marksActiveBuildsFailedAndLeavesTerminalOnesAlone() {
		ExportPackage saved = service.saveExportPackage(packageWith("Stranded", Domain.LOCATIONS.name()));
		ExportBuild running = new ExportBuild();
		running.setExportPackage(saved);
		running.setVersion(1);
		running.setExportStatus(ExportStatus.RUNNING);
		service.saveExportBuild(running);
		ExportBuild completed = new ExportBuild();
		completed.setExportPackage(saved);
		completed.setVersion(2);
		completed.setExportStatus(ExportStatus.COMPLETED);
		service.saveExportBuild(completed);
		
		int recovered = service.failStrandedBuilds("Interrupted by a server restart");
		
		assertEquals(1, recovered);
		ExportBuild reloaded = service.getBuildByUuid(running.getUuid());
		assertEquals(ExportStatus.FAILED, reloaded.getExportStatus());
		assertEquals("Interrupted by a server restart", reloaded.getErrorMessage());
		assertEquals(ExportStatus.COMPLETED, service.getBuildByUuid(completed.getUuid()).getExportStatus());
	}
	
	@Test
	void retirePackage_fillsTheRetireFieldsViaAop() {
		ExportPackage saved = service.saveExportPackage(packageWith("Old", Domain.LOCATIONS.name()));
		
		service.retireExportPackage(saved, "obsolete");
		
		ExportPackage reloaded = service.getPackageByUuid(saved.getUuid());
		assertTrue(reloaded.getRetired());
		assertEquals("obsolete", reloaded.getRetireReason());
		assertNotNull(reloaded.getRetiredBy());
		assertNotNull(reloaded.getDateRetired());
	}
	
	@Test
	void runBuild_exportsTheScopedLocationsAndZipsThem() throws Exception {
		Location target = Context.getLocationService().getAllLocations().get(0);
		ExportPackage saved = service
		        .saveExportPackage(packageWith("Site A locations", Domain.LOCATIONS.name(), target.getUuid()));
		
		ExportBuild build = new ExportBuild();
		build.setExportPackage(saved);
		build.setVersion(1);
		build.setExportStatus(ExportStatus.QUEUED);
		build = service.saveExportBuild(build);
		
		ExportBuild completed = service.runBuild(build.getUuid());
		
		assertEquals(ExportStatus.COMPLETED, completed.getExportStatus());
		assertNotNull(completed.getDateCompleted());
		assertNotNull(completed.getManifestJson());
		assertTrue(completed.getManifestJson().contains(target.getUuid()));
		
		File zip = service.getBuildZip(completed);
		assertNotNull(zip);
		assertTrue(zip.exists(), "expected " + zip);
		try (ZipFile zipFile = new ZipFile(zip)) {
			assertNotNull(zipFile.getEntry("package.json"), "package.json should sit at the zip root");
			assertNotNull(zipFile.getEntry("configuration/locations/locations.csv"),
			    "the Initializer tree should sit beside it");
		}
	}
	
	@Test
	void runBuild_withNoEntriesExportsEveryRegisteredDomain() throws Exception {
		ExportPackage everything = new ExportPackage();
		everything.setName("Everything");
		everything.setDescription("test");
		ExportPackage saved = service.saveExportPackage(everything);
		
		ExportBuild build = new ExportBuild();
		build.setExportPackage(saved);
		build.setVersion(1);
		build.setExportStatus(ExportStatus.QUEUED);
		build = service.saveExportBuild(build);
		
		ExportBuild completed = service.runBuild(build.getUuid());
		
		assertEquals(ExportStatus.COMPLETED, completed.getExportStatus());
		try (ZipFile zipFile = new ZipFile(service.getBuildZip(completed))) {
			assertNotNull(zipFile.getEntry("configuration/locations/locations.csv"));
			assertNotNull(zipFile.getEntry("configuration/encountertypes/encounterTypes.csv"));
			assertNotNull(zipFile.getEntry("package.json"));
		}
	}
	
	private static ExportPackage packageWith(String name, String domain, String... itemUuids) {
		ExportPackage exportPackage = new ExportPackage();
		exportPackage.setName(name);
		exportPackage.setDescription("test package");
		ExportPackageEntry entry = new ExportPackageEntry();
		entry.setDomain(domain);
		entry.getItemUuids().addAll(Arrays.asList(itemUuids));
		exportPackage.getEntries().add(entry);
		return exportPackage;
	}
}
