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
import org.openmrs.module.metadataexport.export.DomainExporterRegistry;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class ExportDomainControllerTest extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	private DomainExporterRegistry domainExporterRegistry;
	
	private MockMvc mockMvc;
	
	@BeforeEach
	void setUpMockMvc() {
		mockMvc = MockMvcBuilders.standaloneSetup(new ExportDomainController(domainExporterRegistry))
		        .setControllerAdvice(new MetadataExportControllerAdvice()).build();
	}
	
	@Test
	void listDomains_returnsTheRegisteredDomainsSorted() throws Exception {
		MockHttpServletResponse response = mockMvc.perform(get(MetadataExportRestConstants.BASE + "/domains")).andReturn()
		        .getResponse();
		
		assertEquals(200, response.getStatus());
		JsonNode body = new ObjectMapper().readTree(response.getContentAsString());
		List<String> domains = new ArrayList<>();
		body.forEach(node -> domains.add(node.asText()));
		assertTrue(domains.contains("LOCATIONS"));
		assertTrue(domains.contains("ENCOUNTER_TYPES"));
		List<String> sorted = new ArrayList<>(domains);
		sorted.sort(String::compareTo);
		assertEquals(sorted, domains);
	}
}
