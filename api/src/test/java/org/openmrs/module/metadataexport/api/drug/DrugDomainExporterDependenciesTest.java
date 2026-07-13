/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.drug;

import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.OpenmrsObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrugDomainExporterDependenciesTest {
	
	private final DrugDomainExporter exporter = new DrugDomainExporter();
	
	private static Concept concept(String uuid) {
		Concept c = new Concept();
		c.setUuid(uuid);
		return c;
	}
	
	private static DrugIngredient ingredient(String conceptUuid, String unitsUuid) {
		DrugIngredient ingredient = new DrugIngredient();
		ingredient.setIngredient(concept(conceptUuid));
		if (unitsUuid != null) {
			ingredient.setUnits(concept(unitsUuid));
		}
		return ingredient;
	}
	
	private HashSet<String> dependencyUuids(Drug drug) {
		return exporter.getDependencies(drug).stream().map(OpenmrsObject::getUuid)
		        .collect(Collectors.toCollection(HashSet::new));
	}
	
	@Test
	void getDependencies_collectsConceptDosageFormAndIngredientConcepts() {
		Drug drug = new Drug();
		drug.setConcept(concept("drug-concept"));
		drug.setDosageForm(concept("dosage-form"));
		drug.getIngredients().add(ingredient("ingredient-concept", "units-concept"));
		
		assertEquals(new HashSet<>(Arrays.asList("drug-concept", "dosage-form", "ingredient-concept", "units-concept")),
		    dependencyUuids(drug));
	}
	
	@Test
	void getDependencies_skipsNullDosageFormAndNullUnits() {
		Drug drug = new Drug();
		drug.setConcept(concept("drug-concept"));
		drug.getIngredients().add(ingredient("ingredient-concept", null));
		
		assertEquals(new HashSet<>(Arrays.asList("drug-concept", "ingredient-concept")), dependencyUuids(drug));
	}
	
	@Test
	void getDependencies_isEmptyForDrugWithNoConceptRefs() {
		assertTrue(exporter.getDependencies(new Drug()).isEmpty());
	}
}
