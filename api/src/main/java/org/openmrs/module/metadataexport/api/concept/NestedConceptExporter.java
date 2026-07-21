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

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Exports a concept's q-and-a answers as the {@code answers} column. Set members are not handled
 * here; they are exported by {@link ConceptSetDomainExporter}, which preserves their sort weight.
 */
public class NestedConceptExporter extends BaseLineExporter<Concept> {
	
	private static final String HEADER_ANSWERS = "answers";
	
	private static final String LIST_SEPARATOR = "; ";
	
	@Override
	public void export(Concept concept, ExportLine line) {
		if (BooleanUtils.isTrue(concept.getRetired())) {
			return;
		}
		
		String answers = concept.getAnswers().stream()
		        .sorted(Comparator.comparing(ConceptAnswer::getSortWeight, Comparator.nullsLast(Comparator.naturalOrder())))
		        .map(ConceptAnswer::getAnswerConcept).filter(Objects::nonNull).map(Concept::getUuid)
		        .collect(Collectors.joining(LIST_SEPARATOR));
		line.put(HEADER_ANSWERS, answers);
	}
}
