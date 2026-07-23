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

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.ExportLine;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Emits the {@code ingredient N} / {@code ingredient N strength} / {@code ingredient N units}
 * columns. Inverse of Initializer's {@code IngredientsDrugLineProcessor}. Ingredient and units
 * concepts are emitted as UUIDs (pulled into the export via
 * {@code DrugDomainExporter#getDependencies}).
 * <p>
 * A {@link Drug#getIngredients()} is an unordered set, so ingredients are sorted by ingredient
 * concept UUID to keep the numbered columns stable across exports. Initializer treats the index as
 * a per-row grouping key only, not as identity, so the ordering is not significant on load.
 * <p>
 * Initializer's ingredient header constants are not {@code public}, so they are re-declared here.
 * Keep in sync with {@code org.openmrs.module.initializer.api.drugs.IngredientsDrugLineProcessor}.
 */
public class DrugIngredientsExporter extends BaseLineExporter<Drug> {
	
	private static final String HEADER_INGREDIENT = "ingredient";
	
	private static final String HEADER_STRENGTH = "strength";
	
	private static final String HEADER_UNITS = "units";
	
	@Override
	public void export(Drug drug, ExportLine line) {
		if (BooleanUtils.isTrue(drug.getRetired())) {
			return;
		}
		
		List<DrugIngredient> ingredients = drug.getIngredients().stream().filter(i -> i.getIngredient() != null)
		        .sorted(
		            Comparator.comparing(i -> i.getIngredient().getUuid(), Comparator.nullsLast(Comparator.naturalOrder())))
		        .collect(Collectors.toList());
		
		for (int i = 0; i < ingredients.size(); i++) {
			DrugIngredient drugIngredient = ingredients.get(i);
			String base = HEADER_INGREDIENT + " " + (i + 1);
			line.put(base, drugIngredient.getIngredient().getUuid());
			if (drugIngredient.getStrength() != null) {
				line.put(base + " " + HEADER_STRENGTH, String.valueOf(drugIngredient.getStrength()));
			}
			Concept units = drugIngredient.getUnits();
			if (units != null) {
				line.put(base + " " + HEADER_UNITS, units.getUuid());
			}
		}
	}
}
