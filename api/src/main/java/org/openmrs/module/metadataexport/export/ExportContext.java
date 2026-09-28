/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.export;

import org.openmrs.module.initializer.Domain;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Carries the shared state threaded through every {@link DomainExporter#export}: the output root
 * and where beneath it the Initializer domain directories go.
 * <p>
 * Initializer itself reads {@code <app data dir>/configuration/<domain>}, which is what the startup
 * export mirrors ({@link #CONFIGURATION_DIR}). An OpenMRS content package nests the same tree one
 * level deeper, at {@code configuration/backend_configuration/<domain>}
 * ({@link #CONTENT_PACKAGE_CONFIGURATION_DIR}): that is the only folder the SDK's
 * {@code ContentHelper.installBackendConfig} copies out of a content package zip, and a zip without
 * it installs nothing, silently.
 */
public class ExportContext {
	
	/** Where Initializer reads domains from, relative to the application data directory. */
	public static final String CONFIGURATION_DIR = "configuration";
	
	/** Where the SDK and the content packager plugin expect domains inside a content package zip. */
	public static final String CONTENT_PACKAGE_CONFIGURATION_DIR = CONFIGURATION_DIR + "/backend_configuration";
	
	private final File configurationDir;
	
	/** Writes domains under {@code <outputDir>/configuration/<domain>}, as Initializer reads them. */
	public ExportContext(File outputDir) {
		this(outputDir, CONFIGURATION_DIR);
	}
	
	/**
	 * Writes domains under {@code <outputDir>/<configurationPath>/<domain>}.
	 *
	 * @param configurationPath the directory holding the domain directories, relative to
	 *            {@code outputDir}, with {@code /} separators; one of the constants on this class
	 */
	public ExportContext(File outputDir, String configurationPath) {
		this.configurationDir = new File(outputDir, configurationPath);
	}
	
	/** Same as {@link #ExportContext(File, String)} with {@link #CONTENT_PACKAGE_CONFIGURATION_DIR}. */
	public static ExportContext forContentPackage(File outputDir) {
		return new ExportContext(outputDir, CONTENT_PACKAGE_CONFIGURATION_DIR);
	}
	
	/** The directory this domain's files go in, created if missing. */
	public File domainDir(Domain domain) throws IOException {
		File dir = new File(configurationDir, domain.getName());
		Files.createDirectories(dir.toPath());
		return dir;
	}
}
