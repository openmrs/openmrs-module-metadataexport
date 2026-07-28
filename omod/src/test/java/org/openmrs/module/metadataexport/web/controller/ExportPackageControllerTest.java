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
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.ActiveBuildException;
import org.openmrs.module.metadataexport.api.ExportJobRunner;
import org.openmrs.module.metadataexport.api.MetadataExportService;
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportPackage;
import org.openmrs.module.metadataexport.api.model.ExportPackageEntry;
import org.openmrs.module.metadataexport.api.model.ExportStatus;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

class ExportPackageControllerTest extends BaseModuleWebContextSensitiveTest {
	
	private static final String PACKAGES = MetadataExportRestConstants.BASE + "/packages";
	
	private final ObjectMapper mapper = new ObjectMapper();
	
	private MockMvc mockMvc;
	
	private ExportBuild cannedBuild;
	
	private APIException triggerFailure;
	
	@BeforeEach
	void setUpMockMvc() {
		cannedBuild = null;
		triggerFailure = null;
		ExportJobRunner stubRunner = new ExportJobRunner() {
			
			@Override
			public ExportBuild trigger(String packageUuid) {
				if (triggerFailure != null) {
					throw triggerFailure;
				}
				return cannedBuild;
			}
		};
		mockMvc = MockMvcBuilders.standaloneSetup(new ExportPackageController(stubRunner))
		        .setControllerAdvice(new MetadataExportControllerAdvice()).build();
	}
	
	@Test
	void createPackage_persistsAndReturns201() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(post(PACKAGES).contentType(MediaType.APPLICATION_JSON)
		        .content(packageJson("Site A locations", "locations", "loc-1"))).andReturn().getResponse();
		
		assertEquals(201, response.getStatus());
		JsonNode body = mapper.readTree(response.getContentAsString());
		assertEquals("Site A locations", body.get("name").asText());
		assertEquals("LOCATIONS", body.get("entries").get(0).get("domain").asText());
		assertEquals("loc-1", body.get("entries").get(0).get("itemUuids").get(0).asText());
		
		ExportPackage persisted = service().getPackageByUuid(body.get("uuid").asText());
		assertNotNull(persisted);
		assertEquals("Site A locations", persisted.getName());
	}
	
	@Test
	void createPackage_rejectsDuplicateNameWith400AndFieldError() throws Exception {
		saveExportPackage("Dup");
		
		MockHttpServletResponse response = mockMvc
		        .perform(post(PACKAGES).contentType(MediaType.APPLICATION_JSON).content(packageJson("Dup", "locations")))
		        .andReturn().getResponse();
		
		assertEquals(400, response.getStatus());
		JsonNode body = mapper.readTree(response.getContentAsString());
		assertNotNull(body.get("fieldErrors").get("name"));
	}
	
	@Test
	void createPackage_rejectsUnknownDomainWith400() throws Exception {
		MockHttpServletResponse response = mockMvc
		        .perform(post(PACKAGES).contentType(MediaType.APPLICATION_JSON).content(packageJson("Bad", "not_a_domain")))
		        .andReturn().getResponse();
		
		assertEquals(400, response.getStatus());
	}
	
	@Test
	void getPackage_returns404ForUnknownUuid() throws Exception {
		assertEquals(404, mockMvc.perform(get(PACKAGES + "/no-such-uuid")).andReturn().getResponse().getStatus());
	}
	
	@Test
	void listPackages_returnsSavedPackages() throws Exception {
		saveExportPackage("Listed");
		
		MockHttpServletResponse response = mockMvc.perform(get(PACKAGES)).andReturn().getResponse();
		
		assertEquals(200, response.getStatus());
		JsonNode body = mapper.readTree(response.getContentAsString());
		assertTrue(body.isArray());
		assertEquals("Listed", body.get(0).get("name").asText());
	}
	
	@Test
	void updatePackage_replacesNameAndEntries() throws Exception {
		ExportPackage saved = saveExportPackage("Before");
		
		MockHttpServletResponse response = mockMvc.perform(put(PACKAGES + "/" + saved.getUuid())
		        .contentType(MediaType.APPLICATION_JSON).content(packageJson("After", "encounter_types", "et-1")))
		        .andReturn().getResponse();
		
		assertEquals(200, response.getStatus());
		ExportPackage reloaded = service().getPackageByUuid(saved.getUuid());
		assertEquals("After", reloaded.getName());
		assertEquals(1, reloaded.getEntries().size());
		assertEquals("ENCOUNTER_TYPES", reloaded.getEntries().get(0).getDomain());
	}
	
	@Test
	void updatePackage_rejectsABodyWithoutEntriesInsteadOfWideningTheExport() throws Exception {
		ExportPackage saved = saveExportPackage("Scoped");
		
		MockHttpServletResponse response = mockMvc.perform(
		    put(PACKAGES + "/" + saved.getUuid()).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Renamed\"}"))
		        .andReturn().getResponse();
		
		assertEquals(400, response.getStatus());
		ExportPackage reloaded = service().getPackageByUuid(saved.getUuid());
		assertEquals("Scoped", reloaded.getName());
		assertEquals(1, reloaded.getEntries().size());
	}
	
	@Test
	void createPackage_acceptsAnExplicitlyEmptyEntriesList() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(
		    post(PACKAGES).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Everything\",\"entries\":[]}"))
		        .andReturn().getResponse();
		
		assertEquals(201, response.getStatus());
		JsonNode body = mapper.readTree(response.getContentAsString());
		assertEquals(0, body.get("entries").size());
	}
	
	@Test
	void createPackage_dedupesItemUuids() throws Exception {
		MockHttpServletResponse response = mockMvc
		        .perform(post(PACKAGES).contentType(MediaType.APPLICATION_JSON).content(
		            "{\"name\":\"Dedup uuids\",\"entries\":[{\"domain\":\"locations\",\"itemUuids\":[\"x\",\"x\"]}]}"))
		        .andReturn().getResponse();
		
		assertEquals(201, response.getStatus());
		JsonNode body = mapper.readTree(response.getContentAsString());
		assertEquals(1, body.get("entries").get(0).get("itemUuids").size());
		ExportPackage persisted = service().getPackageByUuid(body.get("uuid").asText());
		assertEquals(Collections.singletonList("x"), persisted.getEntries().get(0).getItemUuids());
	}
	
	@Test
	void createPackage_returns400ForMalformedJson() throws Exception {
		assertEquals(400,
		    mockMvc.perform(post(PACKAGES).contentType(MediaType.APPLICATION_JSON).content("{ this is not json")).andReturn()
		            .getResponse().getStatus());
	}
	
	@Test
	void createPackage_returns400ForAnEmptyBody() throws Exception {
		assertEquals(400,
		    mockMvc.perform(post(PACKAGES).contentType(MediaType.APPLICATION_JSON)).andReturn().getResponse().getStatus());
	}
	
	@Test
	void createPackage_returns415ForAnUnsupportedContentType() throws Exception {
		assertEquals(415, mockMvc.perform(post(PACKAGES).contentType(MediaType.TEXT_PLAIN).content("name=x")).andReturn()
		        .getResponse().getStatus());
	}
	
	@Test
	void retirePackage_returns204AndRetires() throws Exception {
		ExportPackage saved = saveExportPackage("Old");
		
		MockHttpServletResponse response = mockMvc
		        .perform(delete(PACKAGES + "/" + saved.getUuid()).param("reason", "obsolete")).andReturn().getResponse();
		
		assertEquals(204, response.getStatus());
		assertTrue(service().getPackageByUuid(saved.getUuid()).getRetired());
	}
	
	@Test
	void triggerBuild_returns202WithTheQueuedBuild() throws Exception {
		ExportPackage saved = saveExportPackage("Triggerable");
		cannedBuild = new ExportBuild();
		cannedBuild.setExportPackage(saved);
		cannedBuild.setVersion(1);
		cannedBuild.setExportStatus(ExportStatus.QUEUED);
		
		MockHttpServletResponse response = mockMvc.perform(post(PACKAGES + "/" + saved.getUuid() + "/builds")).andReturn()
		        .getResponse();
		
		assertEquals(202, response.getStatus());
		JsonNode body = mapper.readTree(response.getContentAsString());
		assertEquals("QUEUED", body.get("status").asText());
		assertEquals(saved.getUuid(), body.get("packageUuid").asText());
	}
	
	@Test
	void triggerBuild_returns409WhenABuildIsAlreadyActive() throws Exception {
		ExportPackage saved = saveExportPackage("Busy");
		triggerFailure = new ActiveBuildException("Build v1 of 'Busy' is already RUNNING");
		
		MockHttpServletResponse response = mockMvc.perform(post(PACKAGES + "/" + saved.getUuid() + "/builds")).andReturn()
		        .getResponse();
		
		assertEquals(409, response.getStatus());
	}
	
	@Test
	void triggerBuild_returns404ForUnknownPackage() throws Exception {
		assertEquals(404, mockMvc.perform(post(PACKAGES + "/no-such-uuid/builds")).andReturn().getResponse().getStatus());
	}
	
	@Test
	void listBuilds_returnsHistory() throws Exception {
		ExportPackage saved = saveExportPackage("With builds");
		ExportBuild build = new ExportBuild();
		build.setExportPackage(saved);
		build.setVersion(1);
		build.setExportStatus(ExportStatus.FAILED);
		service().saveExportBuild(build);
		
		MockHttpServletResponse response = mockMvc.perform(get(PACKAGES + "/" + saved.getUuid() + "/builds")).andReturn()
		        .getResponse();
		
		assertEquals(200, response.getStatus());
		JsonNode body = mapper.readTree(response.getContentAsString());
		assertEquals(1, body.size());
		assertEquals("FAILED", body.get(0).get("status").asText());
	}
	
	@Test
	void endpointsReturn401WhenNotAuthenticated() throws Exception {
		Context.logout();
		try {
			assertEquals(401, mockMvc.perform(get(PACKAGES)).andReturn().getResponse().getStatus());
		}
		finally {
			authenticate();
		}
	}
	
	private String packageJson(String name, String domain, String... itemUuids) throws Exception {
		Map<String, Object> entry = new LinkedHashMap<>();
		entry.put("domain", domain);
		entry.put("itemUuids", Arrays.asList(itemUuids));
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("description", "test");
		body.put("entries", Collections.singletonList(entry));
		return mapper.writeValueAsString(body);
	}
	
	private ExportPackage saveExportPackage(String name) {
		ExportPackage exportPackage = new ExportPackage();
		exportPackage.setName(name);
		exportPackage.setDescription("test");
		ExportPackageEntry entry = new ExportPackageEntry();
		entry.setDomain(Domain.LOCATIONS.name());
		exportPackage.getEntries().add(entry);
		return service().saveExportPackage(exportPackage);
	}
	
	private static MetadataExportService service() {
		return Context.getService(MetadataExportService.class);
	}
}
