/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.concept;

import org.openmrs.ConceptMapType;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ConceptMapTypeDomainExporter extends CsvDomainExporter<ConceptMapType> {
	
	@Override
	protected List<BaseLineExporter<ConceptMapType>> chain() {
		return Collections.singletonList(new ConceptMapTypeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "conceptMapTypes.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.CONCEPT_MAP_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof ConceptMapType;
	}
	
	@Override
	public Collection<ConceptMapType> getAllInstances() {
		Collection<ConceptMapType> all = allRows();
		return DomainExporter.without(all, DomainExporter.retiredNameClashes(all, "concept map type"));
	}
	
	/**
	 * Retired rows a same-named row would absorb on import; see
	 * {@link DomainExporter#retiredNameClashes}.
	 */
	@Override
	public Map<String, String> exclusions() {
		return DomainExporter.retiredNameClashes(allRows(), "concept map type");
	}
	
	private static Collection<ConceptMapType> allRows() {
		return Context.getConceptService().getConceptMapTypes(true, true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(ConceptMapType instance) {
		return Collections.emptyList();
	}
}
