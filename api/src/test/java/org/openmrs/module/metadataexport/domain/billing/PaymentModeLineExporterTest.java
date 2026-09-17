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
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.billing.api.model.PaymentModeAttributeType;
import org.openmrs.module.metadataexport.export.ExportLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PaymentModeLineExporterTest {
	
	@Test
	void exportsAllColumns() {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("550e8400-e29b-41d4-a716-446655440001");
		paymentMode.setName("Insurance");
		
		PaymentModeAttributeType attr1 = new PaymentModeAttributeType();
		attr1.setName("PolicyNumber");
		attr1.setFormat("java.lang.String");
		attr1.setRegExp("^[A-Z0-9]+$");
		attr1.setRequired(true);
		
		PaymentModeAttributeType attr2 = new PaymentModeAttributeType();
		attr2.setName("CardNumber");
		attr2.setFormat("java.lang.String");
		attr2.setRegExp("^[0-9]+$");
		attr2.setRequired(false);
		
		List<PaymentModeAttributeType> attributeTypes = new ArrayList<>();
		attributeTypes.add(attr1);
		attributeTypes.add(attr2);
		paymentMode.setAttributeTypes(attributeTypes);
		
		ExportLine line = new ExportLine();
		new PaymentModeLineExporter().writeLine(paymentMode, line);
		
		assertEquals("550e8400-e29b-41d4-a716-446655440001", line.get("uuid"));
		assertEquals("Insurance", line.get("name"));
		assertEquals(
		    "PolicyNumber :: java.lang.String :: ^[A-Z0-9]+$ :: True;CardNumber :: java.lang.String :: ^[0-9]+$ :: False",
		    line.get("attributes"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void omitsOptionalColumnsWhenNull() {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("550e8400-e29b-41d4-a716-446655440001");
		paymentMode.setName("Cash");
		
		ExportLine line = new ExportLine();
		new PaymentModeLineExporter().writeLine(paymentMode, line);
		
		assertEquals("550e8400-e29b-41d4-a716-446655440001", line.get("uuid"));
		assertEquals("Cash", line.get("name"));
		assertNull(line.get("attributes"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void omitsAttributesWhenAttributeTypesEmpty() {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("550e8400-e29b-41d4-a716-446655440001");
		paymentMode.setName("Cash");
		paymentMode.setAttributeTypes(Collections.emptyList());
		
		ExportLine line = new ExportLine();
		new PaymentModeLineExporter().writeLine(paymentMode, line);
		
		assertEquals("550e8400-e29b-41d4-a716-446655440001", line.get("uuid"));
		assertEquals("Cash", line.get("name"));
		assertNull(line.get("attributes"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void filtersOutRetiredAttributeTypes() {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("550e8400-e29b-41d4-a716-446655440001");
		paymentMode.setName("Insurance");
		
		PaymentModeAttributeType activeAttr = new PaymentModeAttributeType();
		activeAttr.setName("PolicyNumber");
		activeAttr.setFormat("java.lang.String");
		activeAttr.setRegExp("^[A-Z0-9]+$");
		activeAttr.setRequired(true);
		
		PaymentModeAttributeType retiredAttr = new PaymentModeAttributeType();
		retiredAttr.setName("OldField");
		retiredAttr.setFormat("java.lang.String");
		retiredAttr.setRetired(true);
		
		List<PaymentModeAttributeType> attributeTypes = new ArrayList<>();
		attributeTypes.add(activeAttr);
		attributeTypes.add(retiredAttr);
		paymentMode.setAttributeTypes(attributeTypes);
		
		ExportLine line = new ExportLine();
		new PaymentModeLineExporter().writeLine(paymentMode, line);
		
		assertEquals("550e8400-e29b-41d4-a716-446655440001", line.get("uuid"));
		assertEquals("Insurance", line.get("name"));
		assertEquals("PolicyNumber :: java.lang.String :: ^[A-Z0-9]+$ :: True", line.get("attributes"));
	}
	
	@Test
	void handlesNullFieldsInAttributeType() {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("550e8400-e29b-41d4-a716-446655440001");
		paymentMode.setName("Check");
		
		PaymentModeAttributeType attr = new PaymentModeAttributeType();
		List<PaymentModeAttributeType> attributeTypes = new ArrayList<>();
		attributeTypes.add(attr);
		paymentMode.setAttributeTypes(attributeTypes);
		
		ExportLine line = new ExportLine();
		new PaymentModeLineExporter().writeLine(paymentMode, line);
		
		assertEquals("550e8400-e29b-41d4-a716-446655440001", line.get("uuid"));
		assertEquals("Check", line.get("name"));
		assertEquals(" ::  ::  :: False", line.get("attributes"));
	}
	
	@Test
	void exportsRetiredInstanceWithVoidRetireFlagOnly() {
		PaymentMode paymentMode = new PaymentMode();
		paymentMode.setUuid("550e8400-e29b-41d4-a716-446655440001");
		paymentMode.setName("Retired Mode");
		paymentMode.setRetired(true);
		
		ExportLine line = new ExportLine();
		new PaymentModeLineExporter().writeLine(paymentMode, line);
		
		assertEquals("550e8400-e29b-41d4-a716-446655440001", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"));
		assertNull(line.get("attributes"));
	}
}
