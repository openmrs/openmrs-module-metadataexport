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
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.CashPointService;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.CsvFailingLines;
import org.openmrs.module.initializer.api.billing.CashPointsCsvParser;
import org.openmrs.module.initializer.api.billing.CashPointsLineProcessor;
import org.openmrs.module.metadataexport.export.ExportContext;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CashPointDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private final CashPointDomainExporter exporter = new CashPointDomainExporter();
	
	@BeforeEach
	void seedCashPoints() {
		seedOneRetiredAndOneNonRetiredCashPoint();
	}
	
	@Test
	void shouldGetCashPointAllInstances() {
		Collection<CashPoint> cashPoints = exporter.getAllInstances();
		
		assertNotNull(cashPoints);
		assertEquals(2, cashPoints.size());
		
		List<String> uuids = cashPoints.stream().map(CashPoint::getUuid).collect(Collectors.toList());
		assertTrue(uuids.contains(LIVE_UUID));
		assertTrue(uuids.contains(RETIRED_UUID));
	}
	
	@Test
	void shouldGetEmptyCashPointsIfAllInstancesEmpty() {
		purgeAllCashPoints();
		
		Collection<CashPoint> cashPoints = exporter.getAllInstances();
		assertEquals(0, cashPoints.size());
	}
	
	@Test
	void export_thenReimportOntoAFreshTarget(@TempDir File outDir) throws Exception {
		exporter.export(exporter.getAllInstances(), new ExportContext(outDir));
		purgeAllCashPoints();
		assertTrue(cashPointService().getAllCashPoints(true).isEmpty(), "the target must start without the cash points");
		
		CsvFailingLines failed = replayThroughInitializer(outDir);
		
		assertTrue(failed.getFailingLines().isEmpty(), describe(failed));
		CashPoint live = cashPointService().getCashPointByUuid(LIVE_UUID);
		assertEquals("Main Desk", live.getName());
		assertFalse(live.getRetired());
		CashPoint retired = cashPointService().getCashPointByUuid(RETIRED_UUID);
		assertEquals("Old Desk", retired.getName());
		assertTrue(retired.getRetired(), "the retired cash point must be reimported with its retirement flag");
	}
	
	@Test
	void export_thenReimportOntoATargetThatAlreadyHasTheRows(@TempDir File outDir) throws Exception {
		exporter.export(exporter.getAllInstances(), new ExportContext(outDir));
		
		CsvFailingLines failed = replayThroughInitializer(outDir);
		
		assertTrue(failed.getFailingLines().isEmpty(), describe(failed));
		assertEquals(2, cashPointService().getAllCashPoints(true).size(),
		    "existing rows are matched by uuid, not duplicated");
		assertFalse(cashPointService().getCashPointByUuid(LIVE_UUID).getRetired());
		assertTrue(cashPointService().getCashPointByUuid(RETIRED_UUID).getRetired());
	}
	
	private void seedOneRetiredAndOneNonRetiredCashPoint() {
		CashPointService service = Context.getService(CashPointService.class);
		
		CashPoint live = createCashPoint(LIVE_UUID, "Main Desk");
		service.saveCashPoint(live);
		
		CashPoint retired = createCashPoint(RETIRED_UUID, "Old Desk");
		service.saveCashPoint(retired);
		service.retireCashPoint(retired, "Discontinued");
		
		Context.flushSession();
	}
	
	private void purgeAllCashPoints() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		for (CashPoint cp : cashPointService().getAllCashPoints(true)) {
			sessionFactory.getCurrentSession().remove(cp);
		}
		sessionFactory.getCurrentSession().flush();
	}
	
	private static CsvFailingLines replayThroughInitializer(File outDir) throws Exception {
		File csv = outDir.toPath().resolve(Paths.get("configuration", Domain.CASH_POINTS.getName(), "cashPoints.csv"))
		        .toFile();
		assertTrue(csv.exists(), "expected " + csv);
		CashPointsCsvParser parser = new CashPointsCsvParser(cashPointService(),
		        new CashPointsLineProcessor(Context.getLocationService()));
		try (InputStream in = new FileInputStream(csv)) {
			parser.setInputStream(in);
			List<String[]> lines = parser.getLines();
			assertEquals(2, lines.size(), "both cash points (live and retired) must be in the file");
			return parser.process(lines);
		}
	}
	
	private static String describe(CsvFailingLines failed) {
		return failed.getErrorDetails().stream()
		        .map(d -> d.getCsvLine().prettyPrint() + " -> " + ExceptionUtils.getRootCauseMessage(d.getException()))
		        .collect(Collectors.joining("\n", "Iniz rejected exported lines:\n", ""));
	}
	
	private static CashPointService cashPointService() {
		return Context.getService(CashPointService.class);
	}
	
	private CashPoint createCashPoint(String uuid, String name) {
		CashPoint cashPoint = new CashPoint();
		cashPoint.setUuid(uuid);
		cashPoint.setName(name);
		Location location = Context.getLocationService().getLocation(1);
		cashPoint.setLocation(location);
		return cashPoint;
	}
}
