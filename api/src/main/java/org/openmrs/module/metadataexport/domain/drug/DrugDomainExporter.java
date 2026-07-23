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

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugIngredient;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class DrugDomainExporter extends CsvDomainExporter<Drug> {
	
	@Override
	protected List<BaseLineExporter<Drug>> chain() {
		return Arrays.asList(new DrugLineExporter(), new DrugIngredientsExporter(), new DrugMappingsExporter());
	}
	
	@Override
	protected String fileName() {
		return "drugs.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.DRUGS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof Drug;
	}
	
	@Override
	public Collection<Drug> getAllInstances() {
		return Context.getConceptService().getAllDrugs(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(Drug drug) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		Concept drugConcept = drug.getConcept();
		if (drugConcept != null) {
			dependencies.add(drugConcept);
		}
		Concept dosageForm = drug.getDosageForm();
		if (dosageForm != null) {
			dependencies.add(dosageForm);
		}
		
		for (DrugIngredient ingredient : drug.getIngredients()) {
			if (ingredient.getIngredient() != null) {
				dependencies.add(ingredient.getIngredient());
			}
			if (ingredient.getUnits() != null) {
				dependencies.add(ingredient.getUnits());
			}
		}
		return dependencies;
	}
}
