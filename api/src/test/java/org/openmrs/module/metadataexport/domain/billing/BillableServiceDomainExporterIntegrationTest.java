/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.billing;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillableServiceService;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.billing.api.search.BillableServiceSearch;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.CsvFailingLines;
import org.openmrs.module.initializer.api.billing.BillableServicesCsvParser;
import org.openmrs.module.initializer.api.billing.BillableServicesLineProcessor;
import org.openmrs.module.metadataexport.export.ExportContext;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class BillableServiceDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private static final String UNKNOWN_UUID = "7e3f4d5a-3f10-11e4-adec-0800271c1b75";
	
	private final BillableServiceDomainExporter exporter = new BillableServiceDomainExporter();
	
	@BeforeEach
	void seedBillableServices() {
		BillableServiceService service = Context.getService(BillableServiceService.class);
		
		service.saveBillableService(createBillableService(LIVE_UUID, "General Consultation", "Gen Con"));
		
		BillableService retired = createBillableService(RETIRED_UUID, "Discontinued Service", "Disc Serv");
		service.saveBillableService(retired);
		service.retireBillableService(retired, "Discontinued");
		
		Context.flushSession();
	}
	
	@Test
	void getAllInstances_excludesRetiredServices() {
		Collection<BillableService> instances = exporter.getAllInstances();
		
		assertEquals(1, instances.size());
		assertEquals(LIVE_UUID, instances.iterator().next().getUuid());
	}
	
	@Test
	void getInstancesByUuids_returnsALiveRow() {
		Collection<BillableService> found = exporter.getInstancesByUuids(Collections.singletonList(LIVE_UUID));
		
		assertEquals(1, found.size());
		assertEquals(LIVE_UUID, found.iterator().next().getUuid());
	}
	
	@Test
	void getInstancesByUuids_reportsARetiredRowAsNotImportableRatherThanUnknown() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Collections.singletonList(RETIRED_UUID)));
		
		assertTrue(e.getMessage().contains("retired"), "a retired row must not be reported as unknown");
		assertTrue(e.getMessage().contains(RETIRED_UUID));
		assertFalse(e.getMessage().contains("Unknown uuids"));
	}
	
	@Test
	void getInstancesByUuids_stillReportsUnknownUuids() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Collections.singletonList(UNKNOWN_UUID)));
		
		assertTrue(e.getMessage().contains("Unknown uuids"));
		assertTrue(e.getMessage().contains(UNKNOWN_UUID));
	}
	
	@Test
	void getInstancesByUuids_reportsRetiredAndUnknownUuidsInOneMessage() {
		APIException e = assertThrows(APIException.class,
		    () -> exporter.getInstancesByUuids(Arrays.asList(LIVE_UUID, RETIRED_UUID, UNKNOWN_UUID)));
		
		assertTrue(e.getMessage().contains(RETIRED_UUID), "the retired uuid must be reported");
		assertTrue(e.getMessage().contains(UNKNOWN_UUID), "the unknown uuid must be reported in the same round");
		assertFalse(e.getMessage().contains(LIVE_UUID), "a resolvable uuid is not a problem");
	}
	
	@Test
	void getAllInstances_andExclusions_partitionTheRows() {
		Collection<BillableService> exported = exporter.getAllInstances();
		Map<String, String> exclusions = exporter.exclusions();
		
		assertEquals(1, exported.size());
		assertEquals(LIVE_UUID, exported.iterator().next().getUuid());
		assertEquals(Collections.singleton(RETIRED_UUID), exclusions.keySet());
		assertTrue(exclusions.get(RETIRED_UUID).startsWith(RETIRED_UUID + " ('Discontinued Service') is retired"),
		    exclusions.toString());
	}
	
	@Test
	void export_thenReimportOntoAFreshTarget(@TempDir File outDir) throws Exception {
		exporter.export(exporter.getAllInstances(), new ExportContext(outDir));
		purgeAllBillableServices();
		assertTrue(getAllBillableServices(true).isEmpty(), "the target must start without the services");
		
		CsvFailingLines failed = replayThroughInitializer(outDir);
		
		assertTrue(failed.getFailingLines().isEmpty(), describe(failed));
		BillableService live = billableServiceService().getBillableServiceByUuid(LIVE_UUID);
		assertNotNull(live, "the live service must come back");
		assertEquals("General Consultation", live.getName());
		assertEquals("Gen Con", live.getShortName());
		assertEquals(BillableServiceStatus.ENABLED, live.getServiceStatus());
		assertFalse(live.getRetired());
		assertEquals(1, getLiveBillableServices().size(), "only the live service is exported, so only it must reappear");
	}
	
	@Test
	void export_thenReimportOntoATargetThatAlreadyHasTheRows(@TempDir File outDir) throws Exception {
		exporter.export(exporter.getAllInstances(), new ExportContext(outDir));
		
		CsvFailingLines failed = replayThroughInitializer(outDir);
		
		assertTrue(failed.getFailingLines().isEmpty(), describe(failed));
		assertEquals(1, getLiveBillableServices().size(), "existing rows are matched by uuid, not duplicated");
		assertFalse(billableServiceService().getBillableServiceByUuid(LIVE_UUID).getRetired());
		assertTrue(billableServiceService().getBillableServiceByUuid(RETIRED_UUID).getRetired(),
		    "the retired service is not in the file, so the import must leave it alone");
	}
	
	private void purgeAllBillableServices() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		for (BillableService svc : getAllBillableServices(true)) {
			sessionFactory.getCurrentSession().delete(svc);
		}
		sessionFactory.getCurrentSession().flush();
	}
	
	private static CsvFailingLines replayThroughInitializer(File outDir) throws Exception {
		File csv = outDir.toPath()
		        .resolve(Paths.get("configuration", Domain.BILLABLE_SERVICES.getName(), "billableServices.csv")).toFile();
		assertTrue(csv.exists(), "expected " + csv);
		BillableServicesCsvParser parser = new BillableServicesCsvParser(billableServiceService(),
		        new BillableServicesLineProcessor(Context.getConceptService()));
		try (InputStream in = new FileInputStream(csv)) {
			parser.setInputStream(in);
			List<String[]> lines = parser.getLines();
			assertEquals(1, lines.size(), "only the live service must be in the file");
			return parser.process(lines);
		}
	}
	
	private static String describe(CsvFailingLines failed) {
		return failed.getErrorDetails().stream()
		        .map(d -> d.getCsvLine().prettyPrint() + " -> " + ExceptionUtils.getRootCauseMessage(d.getException()))
		        .collect(Collectors.joining("\n", "Iniz rejected exported lines:\n", ""));
	}
	
	private static List<BillableService> getAllBillableServices(boolean includeRetired) {
		return billableServiceService()
		        .getBillableServices(new BillableServiceSearch(null, null, null, null, null, includeRetired), null);
	}
	
	private static List<BillableService> getLiveBillableServices() {
		return getAllBillableServices(false);
	}
	
	private static BillableServiceService billableServiceService() {
		return Context.getService(BillableServiceService.class);
	}
	
	private BillableService createBillableService(String uuid, String name, String shortName) {
		BillableService billableService = new BillableService();
		billableService.setUuid(uuid);
		billableService.setName(name);
		billableService.setShortName(shortName);
		Concept concept = Context.getConceptService().getConcept(7);
		billableService.setConcept(concept);
		billableService.setServiceType(concept);
		billableService.setServiceStatus(BillableServiceStatus.ENABLED);
		return billableService;
	}
}
