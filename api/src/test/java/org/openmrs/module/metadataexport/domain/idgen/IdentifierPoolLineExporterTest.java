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
import org.openmrs.module.idgen.IdentifierPool;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierPoolLineExporterTest {
	
	private static SequentialIdentifierGenerator backingSource() {
		SequentialIdentifierGenerator backing = new SequentialIdentifierGenerator();
		backing.setUuid("c1d8a345-3f10-11e4-adec-0800271c1b75");
		return backing;
	}
	
	@Test
	void exportsAllPoolColumns() {
		IdentifierPool pool = new IdentifierPool();
		pool.setSource(backingSource());
		pool.setBatchSize(250);
		pool.setMinPoolSize(50);
		pool.setRefillWithScheduledTask(false);
		pool.setSequential(true);
		
		ExportLine line = new ExportLine();
		new IdentifierPoolLineExporter().writeLine(pool, line);
		
		assertEquals("c1d8a345-3f10-11e4-adec-0800271c1b75", line.get("pool identifier source"));
		assertEquals("250", line.get("pool refill batch size"));
		assertEquals("50", line.get("pool minimum size"));
		assertEquals("false", line.get("pool refill with task"));
		assertEquals("true", line.get("pool sequential allocation"));
	}
	
	@Test
	void defaultConstructedPoolExportsIdgenDefaults() {
		IdentifierPool pool = new IdentifierPool();
		pool.setSource(backingSource());
		
		ExportLine line = new ExportLine();
		new IdentifierPoolLineExporter().writeLine(pool, line);
		
		assertEquals("1000", line.get("pool refill batch size"));
		assertEquals("500", line.get("pool minimum size"));
		assertEquals("true", line.get("pool refill with task"));
		assertEquals("false", line.get("pool sequential allocation"));
	}
	
	@Test
	void skipsNonPoolSources() {
		SequentialIdentifierGenerator generator = new SequentialIdentifierGenerator();
		generator.setBaseCharacterSet("0123456789");
		
		ExportLine line = new ExportLine();
		new IdentifierPoolLineExporter().writeLine(generator, line);
		
		assertTrue(line.getHeaders().isEmpty(), "non-pool sources contribute no columns");
	}
	
	@Test
	void retiredPoolStillExportsItsColumns() {
		IdentifierPool pool = new IdentifierPool();
		pool.setSource(backingSource());
		pool.setRetired(true);
		
		ExportLine line = new ExportLine();
		new IdentifierPoolLineExporter().writeLine(pool, line);
		
		assertEquals("c1d8a345-3f10-11e4-adec-0800271c1b75", line.get("pool identifier source"));
		assertEquals("true", line.get("pool refill with task"),
		    "missing boolean cells NPE when Iniz fills a bootstrapped retired row");
		assertEquals("false", line.get("pool sequential allocation"));
	}
}
