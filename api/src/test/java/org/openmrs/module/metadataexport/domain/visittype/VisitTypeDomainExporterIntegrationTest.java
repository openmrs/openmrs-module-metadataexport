/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.visittype;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Visit types stand in for every domain whose Initializer parser falls back to a lookup by name and
 * whose table allows two rows of one name: a retired row that shares its name with another row must
 * stay out of the export and be reported as an exclusion, while a retired row with a unique name is
 * exported like any other.
 */
class VisitTypeDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "5b1a0c2e-3d4f-4a6b-8c9d-0e1f2a3b4c5d";
	
	private static final String SUPERSEDED_UUID = "6c2b1d3f-4e5a-4b7c-9d0e-1f2a3b4c5d6e";
	
	private static final String UNIQUE_RETIRED_UUID = "7d3c2e4a-5f6b-4c8d-a0e1-2f3a4b5c6d7f";
	
	private final VisitTypeDomainExporter exporter = new VisitTypeDomainExporter();
	
	/** A retired "Outreach" replaced by a live "Outreach", plus a uniquely named retired type. */
	@BeforeEach
	void seedVisitTypes() {
		VisitService service = Context.getVisitService();
		VisitType superseded = new VisitType("Outreach", "First attempt");
		superseded.setUuid(SUPERSEDED_UUID);
		service.saveVisitType(superseded);
		service.retireVisitType(superseded, "Recreated");
		
		VisitType live = new VisitType("Outreach", "Current definition");
		live.setUuid(LIVE_UUID);
		service.saveVisitType(live);
		
		VisitType unique = new VisitType("Old intake", "No longer used");
		unique.setUuid(UNIQUE_RETIRED_UUID);
		service.saveVisitType(unique);
		service.retireVisitType(unique, "Discontinued");
		Context.flushSession();
	}
	
	@Test
	void getAllInstances_leavesOutTheRetiredRowWhoseNameALiveRowHolds() {
		Set<String> exported = exporter.getAllInstances().stream().map(VisitType::getUuid).collect(Collectors.toSet());
		
		assertTrue(exported.contains(LIVE_UUID));
		assertTrue(exported.contains(UNIQUE_RETIRED_UUID), "a uniquely named retired row is exported in full");
		assertFalse(exported.contains(SUPERSEDED_UUID),
		    "Initializer would bind this row to the live 'Outreach' by name and retire it, so it must not be exported");
	}
	
	@Test
	void exclusions_nameTheLiveRowTheImportWouldBindTo() {
		Map<String, String> exclusions = exporter.exclusions();
		
		assertEquals(Collections.singleton(SUPERSEDED_UUID), exclusions.keySet());
		String reason = exclusions.get(SUPERSEDED_UUID);
		assertTrue(reason.contains("('Outreach') is retired and shares its name with the live visit type " + LIVE_UUID),
		    reason);
	}
	
	@Test
	void getInstancesByUuids_failsForTheExcludedRowWithTheReason() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Arrays.asList(LIVE_UUID, SUPERSEDED_UUID)));
		
		assertTrue(e.getMessage().contains(SUPERSEDED_UUID), e.getMessage());
		assertTrue(e.getMessage().contains("shares its name"), "the reason, not 'unknown uuid': " + e.getMessage());
		assertFalse(e.getMessage().contains("Unknown uuids"), e.getMessage());
	}
	
	@Test
	void getInstancesByUuids_returnsAUniquelyNamedRetiredRow() {
		assertEquals(1, exporter.getInstancesByUuids(Collections.singletonList(UNIQUE_RETIRED_UUID)).size());
	}
}
