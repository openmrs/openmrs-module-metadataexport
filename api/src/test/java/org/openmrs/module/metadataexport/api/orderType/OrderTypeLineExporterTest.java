/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.orderType;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptClass;
import org.openmrs.OrderType;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrderTypeLineExporterTest {
	
	private static ExportLine export(OrderType orderType) {
		ExportLine line = new ExportLine();
		new OrderTypeLineExporter().writeLine(orderType, line);
		return line;
	}
	
	@Test
	void exportsUuidNameDescriptionAndJavaClassName() {
		OrderType orderType = new OrderType();
		orderType.setUuid("ot");
		orderType.setName("Drug Order");
		orderType.setDescription("An order for medication");
		orderType.setJavaClassName("org.openmrs.DrugOrder");
		
		ExportLine line = export(orderType);
		
		assertEquals("ot", line.get("uuid"));
		assertEquals("Drug Order", line.get("name"));
		assertEquals("An order for medication", line.get("description"));
		assertEquals("org.openmrs.DrugOrder", line.get("java class name"));
	}
	
	@Test
	void referencesParentByUuidNotName() {
		OrderType parent = new OrderType();
		parent.setUuid("parent-uuid");
		parent.setName("Order");
		
		OrderType child = new OrderType();
		child.setUuid("child");
		child.setName("Drug Order");
		child.setParent(parent);
		
		ExportLine line = export(child);
		
		assertEquals("parent-uuid", line.get("parent"),
		    "parent must be referenced by uuid so a retired parent (emitted as uuid-only) still resolves on import");
	}
	
	@Test
	void omitsParentWhenAbsent() {
		OrderType orderType = new OrderType();
		orderType.setUuid("ot");
		orderType.setName("Drug Order");
		
		ExportLine line = export(orderType);
		
		assertNull(line.get("parent"));
	}
	
	@Test
	void exportsConceptClassesAsSemicolonSeparatedUuids() {
		OrderType orderType = new OrderType();
		orderType.setUuid("ot");
		orderType.setName("Drug Order");
		orderType.setConceptClasses(new LinkedHashSet<>(Arrays.asList(conceptClass("cc-drug"), conceptClass("cc-test"))));
		
		ExportLine line = export(orderType);
		
		Set<String> uuids = Arrays.stream(line.get("concept classes").split(";")).map(String::trim).filter(s -> !s.isEmpty())
		        .collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList("cc-drug", "cc-test")), uuids);
	}
	
	@Test
	void omitsConceptClassesWhenNone() {
		OrderType orderType = new OrderType();
		orderType.setUuid("ot");
		orderType.setName("Drug Order");
		
		ExportLine line = export(orderType);
		
		assertNull(line.get("concept classes"), "an empty value is dropped rather than written as a blank column");
	}
	
	@Test
	void retiredOrderTypeEmitsUuidAndFlagOnly() {
		OrderType orderType = new OrderType();
		orderType.setUuid("old");
		orderType.setName("Retired Order");
		orderType.setRetired(true);
		
		ExportLine line = export(orderType);
		
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
	}
	
	private static ConceptClass conceptClass(String uuid) {
		ConceptClass conceptClass = new ConceptClass();
		conceptClass.setUuid(uuid);
		return conceptClass;
	}
}
