/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.drug;

import org.junit.jupiter.api.Test;
import org.openmrs.Drug;
import org.openmrs.api.APIException;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Against the standard test dataset. */
class DrugDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String KNOWN_UUID = "3cfcf118-931c-46f7-8ff6-7b876f0d4202";
	
	private static final String UNKNOWN_UUID = "7e3f4d5a-3f10-11e4-adec-0800271c1b75";
	
	private final DrugDomainExporter exporter = new DrugDomainExporter();
	
	@Test
	void candidatesFor_looksTheRowsUpDirectlyInsteadOfLoadingTheDomain() {
		Collection<Drug> candidates = exporter.candidatesFor(Arrays.asList(KNOWN_UUID, UNKNOWN_UUID));
		
		assertEquals(1, candidates.size(), "only the rows the uuids name, not the whole table");
		assertEquals(KNOWN_UUID, candidates.iterator().next().getUuid());
		assertTrue(exporter.getAllInstances().size() > 1, "the dataset has more rows than the lookup returned");
	}
	
	@Test
	void getInstancesByUuids_returnsTheRequestedRowAndStillReportsUnknownUuids() {
		Collection<Drug> found = exporter.getInstancesByUuids(Collections.singletonList(KNOWN_UUID));
		assertEquals(KNOWN_UUID, found.iterator().next().getUuid());
		
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Arrays.asList(KNOWN_UUID, UNKNOWN_UUID)));
		assertTrue(e.getMessage().contains("Unknown uuids"), e.getMessage());
		assertTrue(e.getMessage().contains(UNKNOWN_UUID), e.getMessage());
	}
}
