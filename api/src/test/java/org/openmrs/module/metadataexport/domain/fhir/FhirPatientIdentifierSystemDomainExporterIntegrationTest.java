/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.fhir;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.model.FhirPatientIdentifierSystem;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the two database-reaching paths against real seeded rows — in particular two rows sharing
 * one patient identifier type, the duplicate shape {@code exportable} exists to collapse. Also the
 * only place the build executes {@code from FhirPatientIdentifierSystem}.
 */
class FhirPatientIdentifierSystemDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private final FhirPatientIdentifierSystemDomainExporter exporter = new FhirPatientIdentifierSystemDomainExporter();
	
	@BeforeEach
	void seedTwoRowsSharingOneIdentifierType() {
		PatientIdentifierType type = Context.getPatientService().getAllPatientIdentifierTypes(false).get(0);
		
		FhirPatientIdentifierSystem live = row(LIVE_UUID, type, "http://openmrs.example.org/openmrs-id", false);
		FhirPatientIdentifierSystem retired = row(RETIRED_UUID, type, "http://openmrs.example.org/old-id", true);
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		sessionFactory.getCurrentSession().saveOrUpdate(retired);
		sessionFactory.getCurrentSession().saveOrUpdate(live);
	}
	
	private static FhirPatientIdentifierSystem row(String uuid, PatientIdentifierType type, String url, boolean retired) {
		FhirPatientIdentifierSystem row = new FhirPatientIdentifierSystem();
		row.setUuid(uuid);
		row.setName(type.getName());
		row.setPatientIdentifierType(type);
		row.setUrl(url);
		row.setRetired(retired);
		return row;
	}
	
	@Test
	void getAllInstances_keepsOneRowPerIdentifierTypePreferringTheUnretiredOne() {
		Collection<FhirPatientIdentifierSystem> instances = exporter.getAllInstances();
		
		assertEquals(1, instances.size());
		assertEquals(LIVE_UUID, instances.iterator().next().getUuid());
	}
	
	@Test
	void getInstancesByUuids_refusesADuplicateThatGetAllInstancesDrops() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Arrays.asList(LIVE_UUID, RETIRED_UUID)));
		
		assertTrue(e.getMessage().contains(RETIRED_UUID),
		    "the dropped duplicate must be named, or the package quietly collapses on import");
	}
	
	@Test
	void getInstancesByUuids_returnsAnExportableRow() {
		Collection<FhirPatientIdentifierSystem> found = exporter.getInstancesByUuids(Collections.singletonList(LIVE_UUID));
		
		assertEquals(1, found.size());
		assertEquals(LIVE_UUID, found.iterator().next().getUuid());
	}
	
	@Test
	void getInstancesByUuids_stillReportsUnknownUuids() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Collections.singletonList("7e3f4d5a-3f10-11e4-adec-0800271c1b75")));
		
		assertTrue(e.getMessage().contains("Unknown uuids"));
	}
}
