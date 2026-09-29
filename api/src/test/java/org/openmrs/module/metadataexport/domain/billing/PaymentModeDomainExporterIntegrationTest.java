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
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.PaymentModeService;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.initializer.api.CsvFailingLines;
import org.openmrs.module.initializer.api.billing.PaymentModesCsvParser;
import org.openmrs.module.initializer.api.billing.PaymentModesLineProcessor;
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

public class PaymentModeDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String LIVE_UUID = "c1d8a345-3f10-11e4-adec-0800271c1b75";
	
	private static final String RETIRED_UUID = "439559c2-a3a4-4a25-b4b2-1a0299e287ee";
	
	private final PaymentModeDomainExporter exporter = new PaymentModeDomainExporter();
	
	@BeforeEach
	void seedPaymentModes() {
		seedOneRetiredAndOneNonRetiredPaymentMode();
	}
	
	@Test
	void shouldGetPaymentModeAllInstances() {
		Collection<PaymentMode> paymentModes = exporter.getAllInstances();
		
		assertNotNull(paymentModes);
		assertEquals(2, paymentModes.size());
		
		List<String> uuids = paymentModes.stream().map(PaymentMode::getUuid).collect(Collectors.toList());
		assertTrue(uuids.contains(LIVE_UUID));
		assertTrue(uuids.contains(RETIRED_UUID));
	}
	
	@Test
	void shouldGetEmptyPaymentModesIfAllInstancesEmpty() {
		purgeAllPaymentModes();
		
		Collection<PaymentMode> paymentModes = exporter.getAllInstances();
		assertEquals(0, paymentModes.size());
	}
	
	@Test
	void export_thenReimportOntoAFreshTarget(@TempDir File outDir) throws Exception {
		exporter.export(exporter.getAllInstances(), new ExportContext(outDir));
		purgeAllPaymentModes();
		assertTrue(paymentModeService().getPaymentModes(true).isEmpty(), "the target must start without the modes");
		
		CsvFailingLines failed = replayThroughInitializer(outDir);
		
		assertTrue(failed.getFailingLines().isEmpty(), describe(failed));
		PaymentMode live = paymentModeService().getPaymentModeByUuid(LIVE_UUID);
		assertEquals("Cash", live.getName());
		assertFalse(live.getRetired());
		PaymentMode retired = paymentModeService().getPaymentModeByUuid(RETIRED_UUID);
		assertEquals("Cheque", retired.getName());
		assertTrue(retired.getRetired(), "the retired mode must be reimported with its retirement flag");
	}
	
	@Test
	void export_thenReimportOntoATargetThatAlreadyHasTheModes(@TempDir File outDir) throws Exception {
		exporter.export(exporter.getAllInstances(), new ExportContext(outDir));
		
		CsvFailingLines failed = replayThroughInitializer(outDir);
		
		assertTrue(failed.getFailingLines().isEmpty(), describe(failed));
		assertEquals(2, paymentModeService().getPaymentModes(true).size(),
		    "existing rows are matched by uuid, not duplicated");
		assertFalse(paymentModeService().getPaymentModeByUuid(LIVE_UUID).getRetired());
		assertTrue(paymentModeService().getPaymentModeByUuid(RETIRED_UUID).getRetired());
	}
	
	private void seedOneRetiredAndOneNonRetiredPaymentMode() {
		PaymentModeService service = Context.getService(PaymentModeService.class);
		
		PaymentMode live = createPaymentMode(LIVE_UUID, "Cash");
		service.savePaymentMode(live);
		
		PaymentMode retired = createPaymentMode(RETIRED_UUID, "Cheque");
		service.savePaymentMode(retired);
		service.retirePaymentMode(retired, "Discontinued");
		
		Context.flushSession();
	}
	
	private void purgeAllPaymentModes() {
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		for (PaymentMode mode : paymentModeService().getPaymentModes(true)) {
			sessionFactory.getCurrentSession().delete(mode);
		}
		sessionFactory.getCurrentSession().flush();
	}
	
	private static CsvFailingLines replayThroughInitializer(File outDir) throws Exception {
		File csv = outDir.toPath().resolve(Paths.get("configuration", Domain.PAYMENT_MODES.getName(), "paymentModes.csv"))
		        .toFile();
		assertTrue(csv.exists(), "expected " + csv);
		PaymentModesCsvParser parser = new PaymentModesCsvParser(paymentModeService(), new PaymentModesLineProcessor());
		try (InputStream in = new FileInputStream(csv)) {
			parser.setInputStream(in);
			List<String[]> lines = parser.getLines();
			assertEquals(2, lines.size(), "both modes (live and retired) must be in the file");
			return parser.process(lines);
		}
	}
	
	private static String describe(CsvFailingLines failed) {
		return failed.getErrorDetails().stream()
		        .map(d -> d.getCsvLine().prettyPrint() + " -> " + ExceptionUtils.getRootCauseMessage(d.getException()))
		        .collect(Collectors.joining("\n", "Iniz rejected exported lines:\n", ""));
	}
	
	private static PaymentModeService paymentModeService() {
		return Context.getService(PaymentModeService.class);
	}
	
	private PaymentMode createPaymentMode(String uuid, String name) {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid(uuid);
		paymentMode.setName(name);
		return paymentMode;
	}
}
