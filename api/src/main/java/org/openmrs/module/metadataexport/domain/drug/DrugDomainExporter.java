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
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
	
	/** Every drug is exportable, so a package naming a few need not load the formulary. */
	@Override
	public Collection<Drug> candidatesFor(Collection<String> uuids) {
		ConceptService service = Context.getConceptService();
		return uuids.stream().map(service::getDrugByUuid).filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(Drug drug) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		dependencies.add(drug.getConcept());
		Concept dosageForm = drug.getDosageForm();
		if (dosageForm != null) {
			dependencies.add(dosageForm);
		}
		
		drug.getDrugReferenceMaps().forEach(
		    drugReferenceMap -> dependencies.add(drugReferenceMap.getConceptReferenceTerm().getConceptSource()));
		
		for (DrugIngredient ingredient : drug.getIngredients()) {
			dependencies.add(ingredient.getIngredient());
			if (ingredient.getUnits() != null) {
				dependencies.add(ingredient.getUnits());
			}
		}
		return dependencies;
	}
}
