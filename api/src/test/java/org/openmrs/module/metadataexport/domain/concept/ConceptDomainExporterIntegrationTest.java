/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.concept;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadataexport.export.SearchPage;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Against the standard test dataset. */
class ConceptDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String KNOWN_UUID = "0cbe2ed3-cd5f-4f46-9459-26127c9265ab";
	
	private static final String UNKNOWN_UUID = "7e3f4d5a-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "7e02d1a0-7869-11e3-981f-0800200c9a66";
	
	private final ConceptDomainExporter exporter = new ConceptDomainExporter();
	
	@Test
	void candidatesFor_looksTheRowsUpDirectlyInsteadOfLoadingTheDomain() {
		Collection<Concept> candidates = exporter.candidatesFor(Arrays.asList(KNOWN_UUID, UNKNOWN_UUID));
		
		assertEquals(1, candidates.size(), "only the rows the uuids name, not the whole table");
		assertEquals(KNOWN_UUID, candidates.iterator().next().getUuid());
		assertTrue(exporter.getAllInstances().size() > 1, "the dataset has more rows than the lookup returned");
	}
	
	@Test
	void getInstancesByUuids_returnsTheRequestedRowAndStillReportsUnknownUuids() {
		Collection<Concept> found = exporter.getInstancesByUuids(Collections.singletonList(KNOWN_UUID));
		assertEquals(KNOWN_UUID, found.iterator().next().getUuid());
		
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Arrays.asList(KNOWN_UUID, UNKNOWN_UUID)));
		assertTrue(e.getMessage().contains("Unknown uuids"), e.getMessage());
		assertTrue(e.getMessage().contains(UNKNOWN_UUID), e.getMessage());
	}
	
	@Test
	void display_isTheConceptName() {
		assertEquals("COUGH SYRUP", exporter.display(Context.getConceptService().getConceptByUuid(KNOWN_UUID)));
	}
	
	@Test
	void search_usesTheConceptIndexAndReturnsEachConceptOnce() {
		SearchPage<Concept> hits = exporter.search("cd4", true, 0, 50);
		
		assertEquals(1, hits.getRows().size(), "CD4 COUNT has three names matching 'cd4' but is one concept");
		assertEquals("a09ab2c5-878e-4905-b25d-5784167d0216", hits.getRows().get(0).getUuid());
		assertEquals(1, hits.getTotal());
		assertTrue(exporter.search("xyzzy", true, 0, 50).getRows().isEmpty());
	}
	
	@Test
	void search_blankPagesTheDictionaryInIdOrder() {
		long live = exporter.getAllInstances().stream().filter(concept -> !concept.getRetired()).count();
		
		List<Concept> first = exporter.search("  ", false, 0, 2).getRows();
		SearchPage<Concept> second = exporter.search(null, false, 2, 2);
		
		assertEquals(2, first.size());
		assertEquals(2, second.getRows().size());
		assertTrue(first.get(1).getConceptId() < second.getRows().get(0).getConceptId(), "pages continue in id order");
		assertEquals(live, second.getTotal());
		assertEquals(exporter.getAllInstances().size(), exporter.search("", true, 0, 2).getTotal());
	}
	
	@Test
	void search_findsRetiredConceptsOnlyWhenAskedFor() {
		assertTrue(
		    exporter.search("week", true, 0, 50).getRows().stream()
		            .anyMatch(concept -> RETIRED_UUID.equals(concept.getUuid())),
		    "retired '1/day x 7 days/week' must be a hit when retired rows are asked for");
		assertTrue(exporter.search("week", false, 0, 50).getRows().stream()
		        .noneMatch(concept -> RETIRED_UUID.equals(concept.getUuid())));
	}
	
	/**
	 * The indexed branches go through the service and its {@code @Authorized}; the blank branches read
	 * Hibernate and must refuse the same caller the same way. Butch (3-4) has no privileges.
	 */
	@Test
	void search_refusesACallerWithoutGetConceptsOnEveryBranch() {
		Context.becomeUser("3-4");
		try {
			assertThrows(APIAuthenticationException.class, () -> exporter.search("", true, 0, 5));
			assertThrows(APIAuthenticationException.class, () -> exporter.search("cd4", true, 0, 5));
		}
		finally {
			authenticate();
		}
	}
}
