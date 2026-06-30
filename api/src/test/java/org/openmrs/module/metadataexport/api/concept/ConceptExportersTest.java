/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.concept;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptAttribute;
import org.openmrs.ConceptAttributeType;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptNameType;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConceptExportersTest {
	
	private static Concept concept(String uuid) {
		Concept c = new Concept();
		c.setUuid(uuid);
		return c;
	}
	
	@Test
	void conceptLine_exportsNamesDescriptionAndClassification() {
		Concept c = concept("cu");
		
		ConceptName fsn = new ConceptName("Malaria", Locale.ENGLISH);
		fsn.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		fsn.setLocalePreferred(true);
		fsn.setUuid("fsn-uuid");
		c.addName(fsn);
		
		ConceptName synonym = new ConceptName("Bad blood", Locale.ENGLISH);
		synonym.setConceptNameType(null);
		synonym.setUuid("syn-uuid");
		c.addName(synonym);
		
		c.addDescription(new ConceptDescription("A disease", Locale.ENGLISH));
		
		ConceptClass conceptClass = new ConceptClass();
		conceptClass.setName("Diagnosis");
		c.setConceptClass(conceptClass);
		
		ConceptDatatype datatype = new ConceptDatatype();
		datatype.setName("N/A");
		c.setDatatype(datatype);
		
		c.setVersion("1.2");
		
		ExportLine line = new ExportLine();
		new ConceptLineExporter().writeLine(c, line);
		
		assertEquals("cu", line.get("uuid"));
		assertEquals("Malaria", line.get("fully specified name:en"));
		assertEquals("fsn-uuid", line.get("fully specified name:en:uuid"));
		assertEquals("true", line.get("fully specified name:en:preferred"));
		assertEquals("Bad blood", line.get("synonym 1:en"));
		assertEquals("syn-uuid", line.get("synonym 1:en:uuid"));
		assertEquals("A disease", line.get("description:en"));
		assertEquals("Diagnosis", line.get("data class"));
		assertEquals("N/A", line.get("data type"));
		assertEquals("1.2", line.get("version"));
	}
	
	@Test
	void conceptLine_retiredConceptEmitsUuidAndFlagOnly() {
		Concept c = concept("retired");
		c.setRetired(true);
		ConceptName fsn = new ConceptName("Hidden", Locale.ENGLISH);
		fsn.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		c.addName(fsn);
		
		ExportLine line = new ExportLine();
		new ConceptLineExporter().writeLine(c, line);
		
		assertEquals("retired", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("fully specified name:en"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void numeric_exportsRangesUnitsAndPrecision() {
		ConceptNumeric cn = new ConceptNumeric();
		cn.setUuid("n");
		cn.setHiAbsolute(100.0);
		cn.setLowAbsolute(0.0);
		cn.setUnits("mg");
		cn.setAllowDecimal(true);
		cn.setDisplayPrecision(2);
		
		ExportLine line = new ExportLine();
		new ConceptNumericExporter().export(cn, line);
		
		assertEquals("100.0", line.get("absolute high"));
		assertEquals("0.0", line.get("absolute low"));
		assertEquals("mg", line.get("units"));
		assertEquals("true", line.get("allow decimals"));
		assertEquals("2", line.get("display precision"));
	}
	
	@Test
	void numeric_ignoresNonNumericConcept() {
		ExportLine line = new ExportLine();
		new ConceptNumericExporter().export(concept("plain"), line);
		assertTrue(line.getHeaders().isEmpty());
	}
	
	@Test
	void complex_exportsHandler() {
		ConceptComplex cc = new ConceptComplex();
		cc.setUuid("x");
		cc.setHandler("ImageHandler");
		
		ExportLine line = new ExportLine();
		new ConceptComplexExporter().export(cc, line);
		
		assertEquals("ImageHandler", line.get("complex data handler"));
	}
	
	@Test
	void nested_exportsAnswersAndMembersByUuidInOrder() {
		Concept coded = concept("coded");
		ConceptAnswer a1 = new ConceptAnswer(concept("a1"));
		a1.setSortWeight(1.0);
		ConceptAnswer a2 = new ConceptAnswer(concept("a2"));
		a2.setSortWeight(2.0);
		coded.addAnswer(a1);
		coded.addAnswer(a2);
		
		Concept set = concept("set");
		set.addSetMember(concept("m1"));
		set.addSetMember(concept("m2"));
		
		ExportLine answersLine = new ExportLine();
		new NestedConceptExporter().export(coded, answersLine);
		assertEquals("a1; a2", answersLine.get("answers"));
		
		ExportLine membersLine = new ExportLine();
		new NestedConceptExporter().export(set, membersLine);
		assertEquals("m1; m2", membersLine.get("members"));
	}
	
	@Test
	void mappings_exportsByTypeAndSource() {
		Concept c = concept("c");
		ConceptSource source = new ConceptSource();
		source.setName("SNOMED CT");
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setCode("12345");
		term.setConceptSource(source);
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("SAME-AS");
		ConceptMap map = new ConceptMap();
		map.setConceptReferenceTerm(term);
		map.setConceptMapType(mapType);
		c.addConceptMapping(map);
		
		ExportLine line = new ExportLine();
		new MappingsConceptExporter().export(c, line);
		
		assertEquals("12345", line.get("mappings|SAME-AS|SNOMED CT"));
	}
	
	@Test
	void attributes_exportByTypeNameUsingReferenceString() {
		ConceptAttributeType type = new ConceptAttributeType();
		type.setName("audit note");
		type.setDatatypeClassname("org.openmrs.customdatatype.datatype.FreeTextDatatype");
		
		ConceptAttribute attribute = new ConceptAttribute();
		attribute.setAttributeType(type);
		attribute.setValueReferenceInternal("reviewed-by-x");
		
		Concept c = concept("c");
		c.addAttribute(attribute);
		
		ExportLine line = new ExportLine();
		new ConceptAttributeExporter().export(c, line);
		
		assertEquals("reviewed-by-x", line.get("attribute|audit note"));
	}
}
