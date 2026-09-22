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
import org.openmrs.module.cohort.CohortAttributeType;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CohortAttributeTypeDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private final CohortAttributeTypeDomainExporter exporter = new CohortAttributeTypeDomainExporter();
	
	@BeforeEach
	void seedOneLiveAndOneRetiredAttributeType() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		sessionFactory.getCurrentSession().saveOrUpdate(row(LIVE_UUID, "Sponsor", false));
		sessionFactory.getCurrentSession().saveOrUpdate(row(RETIRED_UUID, "Old sponsor", true));
	}
	
	private static CohortAttributeType row(String uuid, String name, boolean retired) {
		CohortAttributeType type = new CohortAttributeType();
		type.setUuid(uuid);
		type.setName(name);
		type.setDescription(name);
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		type.setMinOccurs(0);
		type.setRetired(retired);
		type.setCreator(new User(1));
		type.setDateCreated(new Date());
		return type;
	}
	
	@Test
	void getAllInstances_andExclusions_partitionTheRows() {
		Collection<CohortAttributeType> exported = exporter.getAllInstances();
		Map<String, String> exclusions = exporter.exclusions();
		
		assertEquals(1, exported.size());
		assertEquals(LIVE_UUID, exported.iterator().next().getUuid());
		assertEquals(Collections.singleton(RETIRED_UUID), exclusions.keySet());
		assertTrue(exclusions.get(RETIRED_UUID).startsWith(RETIRED_UUID + " ('Old sponsor') is retired"),
		    exclusions.toString());
	}
	
	@Test
	void getInstancesByUuids_reportsARetiredRowWithTheReasonRatherThanAsUnknown() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Collections.singletonList(RETIRED_UUID)));
		
		assertTrue(e.getMessage().contains(RETIRED_UUID + " ('Old sponsor') is retired"), e.getMessage());
		assertFalse(e.getMessage().contains("Unknown uuids"), e.getMessage());
	}
}
