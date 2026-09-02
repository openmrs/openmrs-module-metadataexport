/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.cohort;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CohortTypeDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String VOIDED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private final CohortTypeDomainExporter exporter = new CohortTypeDomainExporter();
	
	@BeforeEach
	void seedOneLiveAndOneVoidedCohortType() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		sessionFactory.getCurrentSession().saveOrUpdate(row(LIVE_UUID, "Support Group", false));
		sessionFactory.getCurrentSession().saveOrUpdate(row(VOIDED_UUID, "Old Group", true));
	}
	
	private static CohortType row(String uuid, String name, boolean voided) {
		CohortType type = new CohortType();
		type.setUuid(uuid);
		type.setName(name);
		type.setDescription(name);
		type.setVoided(voided);
		type.setCreator(new User(1));
		type.setDateCreated(new Date());
		return type;
	}
	
	@Test
	void getAllInstances_excludesVoidedRows() {
		Collection<CohortType> instances = exporter.getAllInstances();
		
		assertEquals(1, instances.size());
		assertEquals(LIVE_UUID, instances.iterator().next().getUuid());
	}
	
	@Test
	void getInstancesByUuids_returnsALiveRow() {
		Collection<CohortType> found = exporter.getInstancesByUuids(Collections.singletonList(LIVE_UUID));
		
		assertEquals(1, found.size());
		assertEquals(LIVE_UUID, found.iterator().next().getUuid());
	}
	
	@Test
	void getInstancesByUuids_reportsAVoidedRowAsNotImportableRatherThanUnknown() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Collections.singletonList(VOIDED_UUID)));
		
		assertTrue(e.getMessage().contains("voided"), "a voided row must not be reported as unknown");
		assertTrue(e.getMessage().contains(VOIDED_UUID));
	}
	
	@Test
	void getInstancesByUuids_stillReportsUnknownUuids() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Collections.singletonList("7e3f4d5a-3f10-11e4-adec-0800271c1b75")));
		
		assertTrue(e.getMessage().contains("Unknown uuids"));
	}
}
