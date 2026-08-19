/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.idgen;

import org.junit.jupiter.api.Test;
import org.openmrs.module.idgen.RemoteIdentifierSource;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RemoteIdentifierSourceLineExporterTest {
	
	@Test
	void exportsUrlUserAndPasswordPlaceholder() {
		RemoteIdentifierSource remote = new RemoteIdentifierSource();
		remote.setUuid("9e1a2b3c-3f10-11e4-adec-0800271c1b75");
		remote.setUrl("https://idgen.example.org/generate");
		remote.setUser("idgen-user");
		remote.setPassword("s3cret-live-password");
		
		ExportLine line = new ExportLine();
		new RemoteIdentifierSourceLineExporter().writeLine(remote, line);
		
		assertEquals("https://idgen.example.org/generate", line.get("url"));
		assertEquals("idgen-user", line.get("user"));
		assertEquals("property:idgen.remote.password.9e1a2b3c-3f10-11e4-adec-0800271c1b75", line.get("password"));
	}
	
	@Test
	void skipsNonRemoteSources() {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();
		generator.setBaseCharacterSet("0123456789");
		
		ExportLine line = new ExportLine();
		new RemoteIdentifierSourceLineExporter().writeLine(generator, line);
		
		assertTrue(line.getHeaders().isEmpty(), "non-remote sources contribute no columns");
	}
	
	@Test
	void retiredRemoteStillExportsItsColumns() {
		RemoteIdentifierSource remote = new RemoteIdentifierSource();
		remote.setUuid("9e1a2b3c-3f10-11e4-adec-0800271c1b75");
		remote.setUrl("https://idgen.example.org/generate");
		remote.setUser("idgen-user");
		remote.setPassword("s3cret-live-password");
		remote.setRetired(true);
		
		ExportLine line = new ExportLine();
		new RemoteIdentifierSourceLineExporter().writeLine(remote, line);
		
		assertEquals("https://idgen.example.org/generate", line.get("url"),
		    "Iniz requires url/user/password even when it bootstraps a retired row");
		assertEquals("idgen-user", line.get("user"));
		assertEquals("property:idgen.remote.password.9e1a2b3c-3f10-11e4-adec-0800271c1b75", line.get("password"),
		    "retired rows get the placeholder too, never the live credential");
	}
}
