/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.addresshierarchy;

import org.junit.jupiter.api.Test;
import org.openmrs.layout.address.AddressTemplate;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressHierarchyDomainExporterTest {
	
	private final AddressHierarchyDomainExporter exporter = new AddressHierarchyDomainExporter();
	
	private static AddressHierarchyEntry entry(int id, String name, AddressHierarchyEntry parent) {
		AddressHierarchyEntry entry = new AddressHierarchyEntry();
		entry.setId(id);
		entry.setName(name);
		entry.setParent(parent);
		return entry;
	}
	
	private static AddressHierarchyLevel level(AddressField field, String name, boolean required) {
		AddressHierarchyLevel level = new AddressHierarchyLevel();
		level.setAddressField(field);
		level.setName(name);
		level.setRequired(required);
		return level;
	}
	
	@Test
	void buildEntriesCsv_emitsOneSortedRowPerLeafPath() {
		AddressHierarchyEntry country = entry(1, "Cambodia", null);
		AddressHierarchyEntry province = entry(2, "Banteay Meanchey", country);
		AddressHierarchyEntry district = entry(3, "Mongkol Borei", province);
		AddressHierarchyEntry otherProvince = entry(4, "Kampong Cham", country);
		
		String csv = exporter.buildEntriesCsv(Arrays.asList(country, province, district, otherProvince));
		
		// country and the first province are interior nodes (parents), so only the two leaves are rows,
		// each a full root-to-leaf path, sorted for deterministic output.
		assertEquals("Cambodia,Banteay Meanchey,Mongkol Borei\n" + "Cambodia,Kampong Cham\n", csv);
	}
	
	@Test
	void buildEntriesCsv_appendsUserGeneratedIdWithIdentifierDelimiter() {
		AddressHierarchyEntry country = entry(1, "Cambodia", null);
		AddressHierarchyEntry province = entry(2, "Banteay Meanchey", country);
		province.setUserGeneratedId("BM");
		
		String csv = exporter.buildEntriesCsv(Arrays.asList(country, province));
		
		assertEquals("Cambodia,Banteay Meanchey^BM\n", csv);
	}
	
	@Test
	void buildAddressConfigurationXml_reproducesComponentsFileAndFormat() {
		Map<String, String> sizeMappings = new HashMap<>();
		sizeMappings.put(AddressField.COUNTRY.getName(), "40");
		sizeMappings.put(AddressField.STATE_PROVINCE.getName(), "40");
		Map<String, String> elementDefaults = new HashMap<>();
		elementDefaults.put(AddressField.COUNTRY.getName(), "addresshierarchy.cambodia");
		
		AddressTemplate template = new AddressTemplate("addressTemplate");
		template.setSizeMappings(sizeMappings);
		template.setElementDefaults(elementDefaults);
		template.setLineByLineFormat(Arrays.asList("stateProvince", "country"));
		
		String xml = exporter
		        .buildAddressConfigurationXml(Arrays.asList(level(AddressField.COUNTRY, "Location.country", true),
		            level(AddressField.STATE_PROVINCE, "Location.province", true)), template);
		
		assertTrue(xml.contains("<field>COUNTRY</field>"), xml);
		assertTrue(xml.contains("<nameMapping>Location.country</nameMapping>"), xml);
		assertTrue(xml.contains("<sizeMapping>40</sizeMapping>"), xml);
		assertTrue(xml.contains("<elementDefault>addresshierarchy.cambodia</elementDefault>"), xml);
		assertTrue(xml.contains("<requiredInHierarchy>true</requiredInHierarchy>"), xml);
		assertTrue(xml.contains("<field>STATE_PROVINCE</field>"), xml);
		assertTrue(xml.contains("<string>country</string>"), xml);
		assertTrue(xml.contains("<filename>addresshierarchy.csv</filename>"), xml);
		assertTrue(xml.contains("<entryDelimiter>,</entryDelimiter>"), xml);
		assertTrue(xml.contains("<identifierDelimiter>^</identifierDelimiter>"), xml);
	}
	
	@Test
	void buildAddressConfigurationXml_defaultsSizeWhenTemplateMissing() {
		String xml = exporter
		        .buildAddressConfigurationXml(Arrays.asList(level(AddressField.COUNTRY, "Location.country", false)), null);
		
		assertTrue(xml.contains("<sizeMapping>40</sizeMapping>"), xml);
		assertTrue(xml.contains("<requiredInHierarchy>false</requiredInHierarchy>"), xml);
	}
}
