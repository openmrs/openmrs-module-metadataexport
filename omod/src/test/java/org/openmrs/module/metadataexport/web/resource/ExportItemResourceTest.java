/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.web.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmrs.Drug;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.MetadataExportConstants;
import org.openmrs.module.metadataexport.export.DomainExporterRegistry;
import org.openmrs.module.metadataexport.web.controller.MetadataExportRestController;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.IllegalRequestException;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.jupiter.MainResourceControllerTest;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Against the standard test dataset: locations Unknown Location, Xanadu and the retired Never Never
 * Land; concept CD4 COUNT. As in the other resource tests, {@code handle()} surfaces errors as the
 * thrown exception rather than the status the base controller would map it to.
 */
class ExportItemResourceTest extends MainResourceControllerTest {
	
	private static final String UNKNOWN_LOCATION = "8d6c993e-c2cc-11de-8d13-0010c6dffd0f";
	
	private static final String NEVER_NEVER_LAND = "167ce20c-4785-4285-9119-d197268f7f4a";
	
	private static final String CD4_COUNT = "a09ab2c5-878e-4905-b25d-5784167d0216";
	
	private static final String RETIRED_CONCEPT = "7e02d1a0-7869-11e3-981f-0800200c9a66";
	
	@Override
	public String getURI() {
		return "metadataexport/items";
	}
	
	@Override
	public String getUuid() {
		return UNKNOWN_LOCATION;
	}
	
	@Override
	public long getAllCount() {
		return 0; // unused: there is no unfiltered list, see shouldGetAll
	}
	
	/** Replaces the base test: listing needs {@code ?domain=}, an unfiltered GET is refused. */
	@Override
	@Test
	public void shouldGetAll() throws Exception {
		assertThrows(ResourceDoesNotSupportOperationException.class, () -> handle(newGetRequest(getURI())));
		
		SimpleObject live = deserialize(handle(items("LOCATIONS")));
		MockHttpServletRequest request = items("LOCATIONS");
		request.addParameter("includeAll", "true");
		SimpleObject all = deserialize(handle(request));
		
		assertEquals(2, ((List<?>) live.get("results")).size(), "retired rows are hidden unless includeAll");
		assertEquals(3, ((List<?>) all.get("results")).size());
	}
	
	/** Replaces the base test: resolving a uuid needs {@code ?domain=}. */
	@Override
	@Test
	public void shouldGetDefaultByUuid() throws Exception {
		MockHttpServletRequest request = item(getUuid(), "LOCATIONS");
		request.addParameter("v", "default");
		SimpleObject item = deserialize(handle(request));
		
		assertEquals(getUuid(), item.get("uuid"));
		assertEquals("Unknown Location", item.get("display"));
		assertEquals("LOCATIONS", item.get("domain"));
		assertEquals(false, item.get("retired"));
	}
	
	@Override
	@Test
	public void shouldGetRefByUuid() throws Exception {
		MockHttpServletRequest request = item(getUuid(), "LOCATIONS");
		request.addParameter("v", "ref");
		
		assertEquals(getUuid(), deserialize(handle(request)).get("uuid"));
	}
	
	@Override
	@Test
	public void shouldGetFullByUuid() throws Exception {
		MockHttpServletRequest request = item(getUuid(), "LOCATIONS");
		request.addParameter("v", "full");
		
		assertEquals(getUuid(), deserialize(handle(request)).get("uuid"));
	}
	
	@Test
	void retrieve_withoutADomainIsRefused() {
		assertThrows(ResourceDoesNotSupportOperationException.class,
		    () -> handle(newGetRequest(getURI() + "/" + getUuid())));
	}
	
	@Test
	void retrieve_unknownUuidIsNotFound() {
		ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class,
		    () -> handle(item("no-such-uuid", "LOCATIONS")));
		
		assertTrue(e.getMessage().contains("no-such-uuid"), e.getMessage());
	}
	
	/** The build would refuse the row with this reason; the picker must not reduce it to "deleted". */
	@Test
	void retrieve_excludedRowIsNotFoundWithTheBuildsReason() {
		ConceptService service = Context.getConceptService();
		Drug twin = new Drug();
		twin.setName("ASPIRIN");
		twin.setConcept(service.getConcept(88));
		service.saveDrug(twin);
		service.retireDrug(twin, "re-created under the live name");
		
		ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> handle(item(twin.getUuid(), "DRUGS")));
		
		assertTrue(e.getMessage().startsWith("Not exportable from domain DRUGS"), e.getMessage());
		assertTrue(e.getMessage().contains(twin.getUuid()), e.getMessage());
	}
	
	@Test
	void retrieve_findsARetiredRowAndFlagsIt() throws Exception {
		SimpleObject item = deserialize(handle(item(NEVER_NEVER_LAND, "LOCATIONS")));
		
		assertEquals("Never Never Land", item.get("display"));
		assertEquals(true, item.get("retired"));
	}
	
	@Test
	void search_filtersByDisplayCaseInsensitively() throws Exception {
		MockHttpServletRequest request = items("LOCATIONS");
		request.addParameter("q", "XAN");
		
		List<Map<String, Object>> results = deserialize(handle(request)).get("results");
		
		assertEquals(1, results.size());
		assertEquals("Xanadu", results.get(0).get("display"));
	}
	
	@Test
	void search_domainNameIsCaseInsensitive() throws Exception {
		assertEquals(2, ((List<?>) deserialize(handle(items("locations"))).get("results")).size());
	}
	
	@Test
	void search_isPagedWithTotalCountAndLinks() throws Exception {
		MockHttpServletRequest request = items("LOCATIONS");
		request.addParameter("limit", "1");
		request.addParameter("totalCount", "true");
		SimpleObject first = deserialize(handle(request));
		
		assertEquals(1, ((List<?>) first.get("results")).size());
		assertEquals(2, first.<Object> get("totalCount"));
		List<Map<String, Object>> links = first.get("links");
		assertEquals(1, links.size());
		assertEquals("next", links.get(0).get("rel"));
		assertTrue(links.get(0).get("uri").toString().contains("startIndex=1"), links.get(0).get("uri").toString());
		
		request = items("LOCATIONS");
		request.addParameter("limit", "1");
		request.addParameter("startIndex", "5");
		SimpleObject past = deserialize(handle(request));
		
		assertTrue(((List<?>) past.get("results")).isEmpty(), "a start past the end is an empty last page");
		List<Map<String, Object>> pastLinks = past.get("links");
		assertEquals(1, pastLinks.size(), "last page: prev only");
		assertEquals("prev", pastLinks.get(0).get("rel"));
	}
	
	@Test
	void search_rejectsANegativeStartIndex() {
		MockHttpServletRequest request = items("LOCATIONS");
		request.addParameter("startIndex", "-1");
		
		assertThrows(IllegalRequestException.class, () -> handle(request));
	}
	
	/** The domain is the only selector, so an unknown one is a 404, not an empty list. */
	@Test
	void search_unknownDomainIsNotFound() {
		ObjectNotFoundException e = assertThrows(ObjectNotFoundException.class, () -> handle(items("NO_SUCH")));
		
		assertTrue(e.getMessage().contains("NO_SUCH"), e.getMessage());
	}
	
	@Test
	void search_domainWithoutAnExporterIsNotFound() {
		DomainExporterRegistry registry = Context.getRegisteredComponents(DomainExporterRegistry.class).get(0);
		assumeTrue(registry.forDomain(Domain.LIQUIBASE) == null, "liquibase is documented as not exportable");
		
		assertThrows(ObjectNotFoundException.class, () -> handle(items("LIQUIBASE")));
	}
	
	@Test
	void search_conceptsListTheDictionaryOnePageAtATime() throws Exception {
		MockHttpServletRequest request = items("CONCEPTS");
		request.addParameter("limit", "2");
		request.addParameter("totalCount", "true");
		SimpleObject body = deserialize(handle(request));
		
		assertEquals(2, ((List<?>) body.get("results")).size());
		assertTrue((Integer) body.get("totalCount") > 2, body.get("totalCount").toString());
		List<Map<String, Object>> links = body.get("links");
		assertEquals("next", links.get(0).get("rel"));
	}
	
	@Test
	void search_conceptsHideRetiredHitsUnlessIncludeAll() throws Exception {
		MockHttpServletRequest request = items("CONCEPTS");
		request.addParameter("q", "week");
		List<Map<String, Object>> live = deserialize(handle(request)).get("results");
		request.addParameter("includeAll", "true");
		List<Map<String, Object>> all = deserialize(handle(request)).get("results");
		
		assertTrue(live.stream().noneMatch(item -> RETIRED_CONCEPT.equals(item.get("uuid"))));
		assertTrue(all.stream()
		        .anyMatch(item -> RETIRED_CONCEPT.equals(item.get("uuid")) && Boolean.TRUE.equals(item.get("retired"))));
	}
	
	@Test
	void search_conceptsUseTheNameIndex() throws Exception {
		MockHttpServletRequest request = items("CONCEPTS");
		request.addParameter("q", "cd4");
		List<Map<String, Object>> results = deserialize(handle(request)).get("results");
		
		assertEquals(1, results.size(), "three names match but it is one concept");
		assertEquals(CD4_COUNT, results.get(0).get("uuid"));
		assertEquals("CD4 COUNT", results.get(0).get("display"));
	}
	
	@Test
	void selfLinkCarriesTheDomain() throws Exception {
		SimpleObject item = deserialize(handle(item(getUuid(), "LOCATIONS")));
		List<Map<String, Object>> links = item.get("links");
		
		assertFalse(links.isEmpty());
		assertTrue(links.get(0).get("uri").toString().endsWith("/items/" + getUuid() + "?domain=LOCATIONS"),
		    links.get(0).get("uri").toString());
	}
	
	/**
	 * Pinned through the base controller's mapping because the resource checks the privilege itself: a
	 * {@code ContextAuthenticationException} from {@code Context.requirePrivilege} would be a 500.
	 */
	@Test
	void requestsAreRefusedWhenNotAuthenticated() throws Exception {
		Context.logout();
		try {
			APIAuthenticationException e = assertThrows(APIAuthenticationException.class, () -> handle(items("LOCATIONS")));
			MockHttpServletResponse response = new MockHttpServletResponse();
			new MetadataExportRestController().handleException(e, new MockHttpServletRequest(), response);
			assertEquals(401, response.getStatus());
		}
		finally {
			authenticate();
		}
	}
	
	/**
	 * The module's Get privilege alone opens no domain: one read through a core or module service is
	 * refused by {@code @Authorized}, and one the exporter reads from a table directly asks for the
	 * same privilege itself, so the picker never shows a caller rows the core API would refuse them.
	 * Butch (user 3-4) holds the Provider role, which has no privileges in the standard dataset. The
	 * two fhir2 exporters are not registered in this test context (their module is not started); their
	 * own api tests pin the same check.
	 */
	@ParameterizedTest
	@CsvSource({ "CONCEPTS, Get Concepts", "CONCEPT_SETS, Get Concepts", "CONCEPT_REFERENCE_RANGE, Get Concepts" })
	void searchWithOnlyThePackageGetPrivilegeIsRefusedWithTheDomainsOwnReadPrivilege(String domain, String privilege) {
		Context.becomeUser("3-4");
		Context.addProxyPrivilege(MetadataExportConstants.GET_PRIVILEGE);
		try {
			APIAuthenticationException e = assertThrows(APIAuthenticationException.class, () -> handle(items(domain)));
			assertEquals("Privilege required: " + privilege, e.getMessage());
		}
		finally {
			Context.removeProxyPrivilege(MetadataExportConstants.GET_PRIVILEGE);
			authenticate();
		}
	}
	
	/**
	 * Resolve loads the domain the same way, and the check comes before the lookup, so any uuid shows
	 * it.
	 */
	@Test
	void resolveWithOnlyThePackageGetPrivilegeIsRefusedToo() {
		Context.becomeUser("3-4");
		Context.addProxyPrivilege(MetadataExportConstants.GET_PRIVILEGE);
		try {
			APIAuthenticationException e = assertThrows(APIAuthenticationException.class,
			    () -> handle(item(CD4_COUNT, "CONCEPT_SETS")));
			assertEquals("Privilege required: " + PrivilegeConstants.GET_CONCEPTS, e.getMessage());
		}
		finally {
			Context.removeProxyPrivilege(MetadataExportConstants.GET_PRIVILEGE);
			authenticate();
		}
	}
	
	@Test
	void thePackageGetPrivilegeWithTheDomainsReadPrivilegeIsEnough() throws Exception {
		Context.becomeUser("3-4");
		Context.addProxyPrivilege(MetadataExportConstants.GET_PRIVILEGE);
		Context.addProxyPrivilege(PrivilegeConstants.GET_CONCEPTS);
		try {
			SimpleObject page = deserialize(handle(items("CONCEPT_SETS")));
			
			assertFalse(((List<?>) page.get("results")).isEmpty());
		}
		finally {
			Context.removeProxyPrivilege(PrivilegeConstants.GET_CONCEPTS);
			Context.removeProxyPrivilege(MetadataExportConstants.GET_PRIVILEGE);
			authenticate();
		}
	}
	
	private MockHttpServletRequest items(String domain) {
		MockHttpServletRequest request = request(RequestMethod.GET, getURI());
		request.addParameter("domain", domain);
		return request;
	}
	
	private MockHttpServletRequest item(String uuid, String domain) {
		MockHttpServletRequest request = request(RequestMethod.GET, getURI() + "/" + uuid);
		request.addParameter("domain", domain);
		return request;
	}
}
