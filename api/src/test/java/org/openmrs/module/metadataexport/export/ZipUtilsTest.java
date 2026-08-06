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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZipUtilsTest {
	
	@Test
	void zipDirectory_zipsTreeRelativeToSourceWithForwardSlashes(@TempDir File dir) throws IOException {
		File source = new File(dir, "content");
		write(new File(source, "package.json"), "{\"version\":1}");
		write(new File(source, "configuration/locations/locations.csv"), "uuid,name");
		write(new File(source, "configuration/encountertypes/encounterTypes.csv"), "uuid");
		File zip = new File(dir, "out.zip");
		
		ZipUtils.zipDirectory(source, zip);
		
		try (ZipFile zipFile = new ZipFile(zip)) {
			assertEquals(Arrays.asList("configuration/encountertypes/encounterTypes.csv",
			    "configuration/locations/locations.csv", "package.json"), entryNames(zipFile));
			assertEquals("{\"version\":1}", content(zipFile, "package.json"));
		}
	}
	
	@Test
	void zipDirectory_overwritesAnExistingZip(@TempDir File dir) throws IOException {
		File source = new File(dir, "content");
		write(new File(source, "a.txt"), "first");
		File zip = new File(dir, "out.zip");
		ZipUtils.zipDirectory(source, zip);
		
		write(new File(source, "b.txt"), "second");
		ZipUtils.zipDirectory(source, zip);
		
		try (ZipFile zipFile = new ZipFile(zip)) {
			assertEquals(Arrays.asList("a.txt", "b.txt"), entryNames(zipFile));
		}
	}
	
	private static void write(File file, String content) throws IOException {
		Files.createDirectories(file.getParentFile().toPath());
		Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
	}
	
	private static List<String> entryNames(ZipFile zipFile) {
		List<String> names = new ArrayList<>();
		for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
			names.add(entries.nextElement().getName());
		}
		return names;
	}
	
	private static String content(ZipFile zipFile, String entryName) throws IOException {
		try (InputStream in = zipFile.getInputStream(zipFile.getEntry(entryName));
		        Scanner scanner = new Scanner(in, StandardCharsets.UTF_8.name()).useDelimiter("\\A")) {
			return scanner.hasNext() ? scanner.next() : "";
		}
	}
}
