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
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.MetadataExportService;
import org.openmrs.module.metadataexport.api.model.ExportPackage;
import org.openmrs.module.metadataexport.api.model.ExportPackageEntry;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Drives the real dispatcher chain (openmrs-servlet.xml + this module's web context) instead of a
 * standalone setup, so these assertions cover the resolver production actually uses. Without the
 * ExceptionHandlerExceptionResolver registered in webModuleApplicationContext.xml, the advice never
 * runs here: a logged-out request comes back 200 with a stack-trace body.
 */
class RealDispatcherErrorHandlingTest extends BaseModuleWebContextSensitiveTest {
	
	private static final String PACKAGES = MetadataExportRestConstants.BASE + "/packages";
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	private MockMvc mockMvc;
	
	@BeforeEach
	void setUpMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@Test
	void unauthenticatedRequestReturns401() throws Exception {
		Context.logout();
		try {
			assertEquals(401, mockMvc.perform(get(PACKAGES)).andReturn().getResponse().getStatus());
		}
		finally {
			authenticate();
		}
	}
	
	@Test
	void validationFailureReturns400WithFieldErrors() throws Exception {
		ExportPackage existing = new ExportPackage();
		existing.setName("Dup");
		existing.setDescription("test");
		ExportPackageEntry entry = new ExportPackageEntry();
		entry.setDomain(Domain.LOCATIONS.name());
		existing.getEntries().add(entry);
		Context.getService(MetadataExportService.class).saveExportPackage(existing);
		
		MockHttpServletResponse response = mockMvc.perform(post(PACKAGES).contentType(MediaType.APPLICATION_JSON)
		        .content("{\"name\":\"Dup\",\"entries\":[{\"domain\":\"locations\"}]}")).andReturn().getResponse();
		
		assertEquals(400, response.getStatus());
		JsonNode body = new ObjectMapper().readTree(response.getContentAsString());
		assertNotNull(body.get("fieldErrors").get("name"));
	}
	
	@Test
	void missingEntriesReturns400() throws Exception {
		MockHttpServletResponse response = mockMvc
		        .perform(post(PACKAGES).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Partial\"}"))
		        .andReturn().getResponse();
		
		assertEquals(400, response.getStatus());
	}
	
	@Test
	void malformedJsonReturns400() throws Exception {
		assertEquals(400,
		    mockMvc.perform(post(PACKAGES).contentType(MediaType.APPLICATION_JSON).content("{ this is not json")).andReturn()
		            .getResponse().getStatus());
	}
}
