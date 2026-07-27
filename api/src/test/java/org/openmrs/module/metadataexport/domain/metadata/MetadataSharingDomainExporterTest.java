/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.module.metadataexport.export.ExportContext;
import org.openmrs.module.metadatasharing.ExportedPackage;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A plain unit test can't construct an {@link ExportedPackage}: its constructor (via
 * {@code Package}) reaches {@code OpenmrsConstants}/{@code ModuleFactory}, which need a running
 * OpenMRS context, and Mockito's inline mock maker can't instrument the 1.2.2 classes either. This
 * runs as a {@link BaseModuleContextSensitiveTest} instead, with a stub subclass that overrides
 * {@link ExportedPackage#getSerializedPackageStream()} to sidestep the
 * Hibernate-{@code Blob}-backed default implementation.
 */
class MetadataSharingDomainExporterTest extends BaseModuleContextSensitiveTest {
	
	private final MetadataSharingDomainExporter exporter = new MetadataSharingDomainExporter();
	
	private static class StubPackage extends ExportedPackage {
		
		private final byte[] payload;
		
		StubPackage(String uuid, byte[] payload) {
			setUuid(uuid);
			this.payload = payload;
		}
		
		@Override
		public InputStream getSerializedPackageStream() {
			return payload == null ? null : new ByteArrayInputStream(payload);
		}
	}
	
	@Test
	void export_writesPackageContentToAZipNamedByUuid(@TempDir File outDir) throws Exception {
		byte[] content = "fake zip bytes".getBytes(StandardCharsets.UTF_8);
		StubPackage pkg = new StubPackage("3c90b543-fb04-4738-872b-7d58c4a0cd6e", content);
		
		exporter.export(Collections.singletonList(pkg), new ExportContext(outDir));
		
		File written = outDir.toPath()
		        .resolve(Paths.get("configuration", "metadatasharing", "3c90b543-fb04-4738-872b-7d58c4a0cd6e.zip")).toFile();
		assertTrue(written.exists(), "expected " + written);
		assertArrayEquals(content, Files.readAllBytes(written.toPath()));
	}
	
	@Test
	void export_skipsPackageWithNoSerializedContent(@TempDir File outDir) throws Exception {
		StubPackage pkg = new StubPackage("28f3da50-3f56-4e4e-93cd-66f334970480", null);
		
		exporter.export(Collections.singletonList(pkg), new ExportContext(outDir));
		
		File notWritten = outDir.toPath()
		        .resolve(Paths.get("configuration", "metadatasharing", "28f3da50-3f56-4e4e-93cd-66f334970480.zip")).toFile();
		assertFalse(notWritten.exists(), "package with no content must be skipped, not fail the whole export");
	}
}
