/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.export;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportLineTest {
	
	@Test
	void put_skipsNullAndEmptyValues() {
		ExportLine line = new ExportLine();
		line.put("a", "1");
		line.put("b", "");
		line.put("c", null);
		
		assertTrue(line.containsHeader("a"));
		assertFalse(line.containsHeader("b"), "empty value should not create a column");
		assertFalse(line.containsHeader("c"), "null value should not create a column");
	}
	
	@Test
	void getHeaders_preservesFirstSeenOrder() {
		ExportLine line = new ExportLine();
		line.put("z", "1");
		line.put("a", "2");
		line.put("m", "3");
		
		assertEquals(Arrays.asList("z", "a", "m"), new ArrayList<>(line.getHeaders()));
	}
	
	@Test
	void getValueOrBlank_returnsEmptyStringWhenAbsent() {
		ExportLine line = new ExportLine();
		assertEquals("", line.getValueOrBlank("missing"));
		
		line.put("x", "v");
		assertEquals("v", line.getValueOrBlank("x"));
	}
}
