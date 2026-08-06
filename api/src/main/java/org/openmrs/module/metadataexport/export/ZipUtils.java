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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {
	
	private ZipUtils() {
	}
	
	public static void zipDirectory(File sourceDir, File zipFile) throws IOException {
		Path source = sourceDir.toPath();
		Files.createDirectories(zipFile.toPath().getParent());
		try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
			try (Stream<Path> paths = Files.walk(source)) {
				List<Path> files = paths.filter(Files::isRegularFile).filter(p -> !p.equals(zipFile.toPath())).sorted()
				        .collect(Collectors.toList());
				
				for (Path file : files) {
					String entryName = source.relativize(file).toString().replace(File.separatorChar, '/');
					zip.putNextEntry(new ZipEntry(entryName));
					Files.copy(file, zip);
					zip.closeEntry();
				}
			}
		}
	}
	
}
