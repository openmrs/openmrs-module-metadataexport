/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.drug;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.DrugReferenceMap;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrugExportersTest {
	
	private static Concept concept(String uuid) {
		Concept c = new Concept();
		c.setUuid(uuid);
		return c;
	}
	
	private static DrugIngredient ingredient(String conceptUuid, Double strength, String unitsUuid) {
		DrugIngredient ingredient = new DrugIngredient();
		ingredient.setIngredient(concept(conceptUuid));
		ingredient.setStrength(strength);
		if (unitsUuid != null) {
			ingredient.setUnits(concept(unitsUuid));
		}
		return ingredient;
	}
	
	@Test
	void drugLine_exportsUuidNameStrengthAndConceptRefsByUuid() {
		Drug drug = new Drug();
		drug.setUuid("drug-uuid");
		drug.setName("Aspirin 81mg");
		drug.setDescription("Low-dose aspirin");
		drug.setStrength("81mg");
		drug.setConcept(concept("aspirin-concept"));
		drug.setDosageForm(concept("tablet-concept"));
		
		ExportLine line = new ExportLine();
		new DrugLineExporter().writeLine(drug, line);
		
		assertEquals("drug-uuid", line.get("uuid"));
		assertEquals("Aspirin 81mg", line.get("name"));
		assertEquals("Low-dose aspirin", line.get("description"));
		assertEquals("81mg", line.get("strength"));
		assertEquals("aspirin-concept", line.get("concept drug"));
		assertEquals("tablet-concept", line.get("concept dosage form"));
	}
	
	@Test
	void drugLine_omitsNullDosageForm() {
		Drug drug = new Drug();
		drug.setUuid("drug-uuid");
		drug.setName("Aspirin");
		drug.setConcept(concept("aspirin-concept"));
		
		ExportLine line = new ExportLine();
		new DrugLineExporter().writeLine(drug, line);
		
		assertEquals("aspirin-concept", line.get("concept drug"));
		assertNull(line.get("concept dosage form"));
	}
	
	@Test
	void drugLine_retiredDrugEmitsUuidAndFlagOnly() {
		Drug drug = new Drug();
		drug.setUuid("old");
		drug.setName("Discontinued");
		drug.setConcept(concept("aspirin-concept"));
		drug.setRetired(true);
		
		ExportLine line = new ExportLine();
		new DrugLineExporter().writeLine(drug, line);
		
		assertEquals("old", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void ingredients_exportNumberedColumnsSortedByConceptUuid() {
		Drug drug = new Drug();
		drug.setUuid("drug-uuid");
		// Added out of order; export sorts by ingredient concept uuid for stable numbering
		drug.getIngredients().add(ingredient("ingredient-b", 200.0, null));
		drug.getIngredients().add(ingredient("ingredient-a", 100.0, "mg-concept"));
		
		ExportLine line = new ExportLine();
		new DrugIngredientsExporter().export(drug, line);
		
		assertEquals("ingredient-a", line.get("ingredient 1"));
		assertEquals("100.0", line.get("ingredient 1 strength"));
		assertEquals("mg-concept", line.get("ingredient 1 units"));
		assertEquals("ingredient-b", line.get("ingredient 2"));
		assertEquals("200.0", line.get("ingredient 2 strength"));
		assertNull(line.get("ingredient 2 units"), "null units column is omitted");
	}
	
	@Test
	void ingredients_emptyWhenDrugHasNoIngredients() {
		Drug drug = new Drug();
		drug.setUuid("drug-uuid");
		
		ExportLine line = new ExportLine();
		new DrugIngredientsExporter().export(drug, line);
		
		assertTrue(line.getHeaders().isEmpty());
	}
	
	@Test
	void ingredients_skippedForRetiredDrug() {
		Drug drug = new Drug();
		drug.setUuid("old");
		drug.setRetired(true);
		drug.getIngredients().add(ingredient("ingredient-a", 100.0, "mg-concept"));
		
		ExportLine line = new ExportLine();
		new DrugIngredientsExporter().export(drug, line);
		
		assertTrue(line.getHeaders().isEmpty());
	}
	
	@Test
	void mappings_exportByTypeAndSource() {
		ConceptSource source = new ConceptSource();
		source.setName("SNOMED CT");
		ConceptReferenceTerm term = new ConceptReferenceTerm();
		term.setCode("387458008");
		term.setConceptSource(source);
		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("SAME-AS");
		DrugReferenceMap map = new DrugReferenceMap();
		map.setConceptReferenceTerm(term);
		map.setConceptMapType(mapType);
		
		Drug drug = new Drug();
		drug.setUuid("drug-uuid");
		drug.addDrugReferenceMap(map);
		
		ExportLine line = new ExportLine();
		new DrugMappingsExporter().export(drug, line);
		
		assertEquals("387458008", line.get("mappings|SAME-AS|SNOMED CT"));
	}
}
