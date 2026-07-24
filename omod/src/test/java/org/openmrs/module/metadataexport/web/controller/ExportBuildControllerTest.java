/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.MetadataExportService;
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportPackage;
import org.openmrs.module.metadataexport.api.model.ExportPackageEntry;
import org.openmrs.module.metadataexport.api.model.ExportStatus;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class ExportBuildControllerTest extends BaseModuleWebContextSensitiveTest {
	
	private static final String BUILDS = MetadataExportRestConstants.BASE + "/builds";
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	private MockMvc mockMvc;
	
	private File appDataDir;
	
	@BeforeEach
	void setUpMockMvc(@TempDir File appDataDir) {
		this.appDataDir = appDataDir;
		OpenmrsUtil.setApplicationDataDirectory(appDataDir.getAbsolutePath());
		mockMvc = MockMvcBuilders.standaloneSetup(new ExportBuildController())
		        .setControllerAdvice(new MetadataExportControllerAdvice()).build();
	}
	
	@Test
	void getBuild_returnsStatusWithoutDownloadUrlWhileQueued() throws Exception {
		ExportBuild build = saveBuild(ExportStatus.QUEUED);
		
		MockHttpServletResponse response = mockMvc.perform(get(BUILDS + "/" + build.getUuid())).andReturn().getResponse();
		
		assertEquals(200, response.getStatus());
		JsonNode body = mapper.readTree(response.getContentAsString());
		assertEquals("QUEUED", body.get("status").asText());
		assertEquals(build.getExportPackage().getUuid(), body.get("packageUuid").asText());
		assertTrue(body.get("downloadUrl").isNull());
	}
	
	@Test
	void getBuild_includesManifestAndDownloadUrlWhenCompleted() throws Exception {
		ExportBuild build = saveBuild(ExportStatus.COMPLETED);
		build.setManifestJson("{\"version\":1}");
		service().saveExportBuild(build);
		
		MockHttpServletResponse response = mockMvc.perform(get(BUILDS + "/" + build.getUuid())).andReturn().getResponse();
		
		assertEquals(200, response.getStatus());
		JsonNode body = mapper.readTree(response.getContentAsString());
		assertEquals(1, body.get("manifest").get("version").asInt());
		assertTrue(body.get("downloadUrl").asText().endsWith("/builds/" + build.getUuid() + "/download"));
	}
	
	@Test
	void getBuild_returns404ForUnknownUuid() throws Exception {
		assertEquals(404, mockMvc.perform(get(BUILDS + "/no-such-uuid")).andReturn().getResponse().getStatus());
	}
	
	@Test
	void download_returns409WhileTheBuildIsNotCompleted() throws Exception {
		ExportBuild build = saveBuild(ExportStatus.RUNNING);
		
		MockHttpServletResponse response = mockMvc.perform(get(BUILDS + "/" + build.getUuid() + "/download")).andReturn()
		        .getResponse();
		
		assertEquals(409, response.getStatus());
	}
	
	@Test
	void download_streamsTheZipOfACompletedBuild() throws Exception {
		byte[] zipBytes = "not really a zip".getBytes(StandardCharsets.UTF_8);
		File zip = new File(appDataDir, "metadataexport/packages/p-1/1/metadataexport-test-v1.zip");
		Files.createDirectories(zip.getParentFile().toPath());
		Files.write(zip.toPath(), zipBytes);
		ExportBuild build = saveBuild(ExportStatus.COMPLETED);
		build.setZipPath("metadataexport/packages/p-1/1/metadataexport-test-v1.zip");
		service().saveExportBuild(build);
		
		MockHttpServletResponse response = mockMvc.perform(get(BUILDS + "/" + build.getUuid() + "/download")).andReturn()
		        .getResponse();
		
		assertEquals(200, response.getStatus());
		assertEquals("application/zip", response.getContentType());
		assertNotNull(response.getHeader("Content-Disposition"));
		assertTrue(response.getHeader("Content-Disposition").contains("metadataexport-test-v1.zip"));
		assertArrayEquals(zipBytes, response.getContentAsByteArray());
	}
	
	@Test
	void download_returns410WhenTheZipFileIsGone() throws Exception {
		ExportBuild build = saveBuild(ExportStatus.COMPLETED);
		build.setZipPath("metadataexport/packages/p-1/1/deleted.zip");
		service().saveExportBuild(build);
		
		MockHttpServletResponse response = mockMvc.perform(get(BUILDS + "/" + build.getUuid() + "/download")).andReturn()
		        .getResponse();
		
		assertEquals(410, response.getStatus());
	}
	
	private ExportBuild saveBuild(ExportStatus status) {
		ExportPackage exportPackage = new ExportPackage();
		exportPackage.setName("Build test " + System.nanoTime());
		exportPackage.setDescription("test");
		ExportPackageEntry entry = new ExportPackageEntry();
		entry.setDomain(Domain.LOCATIONS.name());
		exportPackage.getEntries().add(entry);
		exportPackage = service().saveExportPackage(exportPackage);
		
		ExportBuild build = new ExportBuild();
		build.setExportPackage(exportPackage);
		build.setVersion(1);
		build.setExportStatus(status);
		return service().saveExportBuild(build);
	}
	
	private static MetadataExportService service() {
		return Context.getService(MetadataExportService.class);
	}
}
