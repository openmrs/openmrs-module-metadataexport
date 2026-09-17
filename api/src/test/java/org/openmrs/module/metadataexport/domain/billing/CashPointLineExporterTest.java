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

import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.module.billing.api.model.CashPoint;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CashPointLineExporterTest {
	
	@Test
	void exportsAllColumns() {
		Location location = new Location();
		location.setName("Main Clinic");
		
		CashPoint cashPoint = new CashPoint();
		cashPoint.setUuid("550e8400-e29b-41d4-a716-446655440001");
		cashPoint.setName("Cash Desk 1");
		cashPoint.setDescription("Main entrance cash desk");
		cashPoint.setLocation(location);
		
		ExportLine line = new ExportLine();
		new CashPointLineExporter().writeLine(cashPoint, line);
		
		assertEquals("550e8400-e29b-41d4-a716-446655440001", line.get("uuid"));
		assertEquals("Cash Desk 1", line.get("name"));
		assertEquals("Main entrance cash desk", line.get("description"));
		assertEquals("Main Clinic", line.get("location"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void omitsOptionalColumnsWhenNull() {
		CashPoint cashPoint = new CashPoint();
		cashPoint.setUuid("550e8400-e29b-41d4-a716-446655440001");
		cashPoint.setName("Cash Desk 1");
		
		ExportLine line = new ExportLine();
		new CashPointLineExporter().writeLine(cashPoint, line);
		
		assertEquals("550e8400-e29b-41d4-a716-446655440001", line.get("uuid"));
		assertEquals("Cash Desk 1", line.get("name"));
		assertNull(line.get("description"));
		assertNull(line.get("location"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void exportsRetiredInstanceWithVoidRetireFlagOnly() {
		CashPoint cashPoint = new CashPoint();
		cashPoint.setUuid("550e8400-e29b-41d4-a716-446655440001");
		cashPoint.setName("Cash Desk 1");
		cashPoint.setDescription("Main entrance cash desk");
		cashPoint.setRetired(true);
		
		ExportLine line = new ExportLine();
		new CashPointLineExporter().writeLine(cashPoint, line);
		
		assertEquals("550e8400-e29b-41d4-a716-446655440001", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"));
		assertNull(line.get("description"));
		assertNull(line.get("location"));
	}
}
