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
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.module.billing.api.model.BillableService;
import org.openmrs.module.billing.api.model.BillableServiceStatus;
import org.openmrs.module.metadataexport.export.ExportLine;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BillableServiceLineExporterTest {
	
	@Test
	void exportsAllColumns() {
		Concept concept = new Concept();
		concept.setUuid("550e8400-e29b-41d4-a716-446655440001");
		
		Concept serviceType = new Concept();
		serviceType.setUuid("550e8400-e29b-41d4-a716-446655440002");
		
		ConceptName serviceTypeName = new ConceptName();
		serviceTypeName.setName("service-name");
		serviceTypeName.setLocale(Locale.ENGLISH);
		serviceType.setPreferredName(serviceTypeName);
		
		BillableService billableService = new BillableService();
		billableService.setUuid("550e8400-e29b-41d4-a716-446655440003");
		billableService.setName("test");
		billableService.setShortName("shortname");
		billableService.setConcept(concept);
		billableService.setServiceType(serviceType);
		billableService.setServiceStatus(BillableServiceStatus.ENABLED);
		
		ExportLine line = new ExportLine();
		new BillableServiceLineExporter().writeLine(billableService, line);
		assertEquals("550e8400-e29b-41d4-a716-446655440003", line.get("uuid"));
		assertEquals("test", line.get("Service Name"));
		assertEquals("shortname", line.get("Short Name"));
		assertEquals("550e8400-e29b-41d4-a716-446655440001", line.get("Concept"));
		assertEquals("service-name", line.get("Service Type"));
		assertEquals(BillableServiceStatus.ENABLED.name(), line.get("Service Status"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void omitsOptionalColumnsWhenNull() {
		BillableService billableService = new BillableService();
		billableService.setUuid("550e8400-e29b-41d4-a716-446655440003");
		billableService.setName("test");
		billableService.setShortName("shortname");
		billableService.setServiceStatus(null);
		
		ExportLine line = new ExportLine();
		new BillableServiceLineExporter().writeLine(billableService, line);
		assertEquals("550e8400-e29b-41d4-a716-446655440003", line.get("uuid"));
		assertEquals("test", line.get("Service Name"));
		assertEquals("shortname", line.get("Short Name"));
		assertNull(line.get("Concept"));
		assertNull(line.get("Service Type"));
		assertNull(line.get("Service Status"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void exportsRetiredInstanceWithVoidRetireFlagOnly() {
		BillableService billableService = new BillableService();
		billableService.setUuid("550e8400-e29b-41d4-a716-446655440003");
		billableService.setName("retired test");
		billableService.setShortName("ret short");
		billableService.setRetired(true);
		
		ExportLine line = new ExportLine();
		new BillableServiceLineExporter().writeLine(billableService, line);
		assertEquals("550e8400-e29b-41d4-a716-446655440003", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("Service Name"));
		assertNull(line.get("Short Name"));
		assertNull(line.get("Concept"));
		assertNull(line.get("Service Type"));
		assertNull(line.get("Service Status"));
	}
}
