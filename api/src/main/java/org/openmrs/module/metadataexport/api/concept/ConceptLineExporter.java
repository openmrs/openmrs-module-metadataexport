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
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.c.ConceptLineProcessor;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.ExportLine;

import java.util.HashMap;
import java.util.Map;

public class ConceptLineExporter extends BaseLineExporter<Concept> {
	
	@Override
	public void export(Concept concept, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_UUID, concept.getUuid());
		
		if (BooleanUtils.isTrue(concept.getRetired())) {
			line.put(BaseLineProcessor.HEADER_VOID_RETIRE, "true");
			return;
		}
		
		exportNames(concept, line);
		exportDescriptions(concept, line);
		exportClassification(concept, line);
	}
	
	private void exportNames(Concept concept, ExportLine line) {
		Map<String, Integer> counters = new HashMap<>();
		
		for (ConceptName cn : concept.getNames(false)) {
			ConceptNameType type = cn.getConceptNameType();
			String prefix = prefixFor(type);
			String locale = cn.getLocale().toString();
			
			String base;
			if (isNumbered(type)) {
				String counterKey = prefix + BaseLineProcessor.LOCALE_SEPARATOR + locale;
				int n = counters.merge(counterKey, 1, Integer::sum);
				base = prefix + " " + n;
			} else {
				base = prefix;
			}
			
			String header = base + BaseLineProcessor.LOCALE_SEPARATOR + locale;
			line.put(header, cn.getName());
			line.put(header + BaseLineProcessor.LOCALE_SEPARATOR + BaseLineProcessor.HEADER_UUID, cn.getUuid());
			if (BooleanUtils.isTrue(cn.getLocalePreferred())) {
				line.put(header + BaseLineProcessor.LOCALE_SEPARATOR + ConceptLineProcessor.HEADER_PREFERRED, "true");
			}
		}
	}
	
	private void exportDescriptions(Concept concept, ExportLine line) {
		for (ConceptDescription description : concept.getDescriptions()) {
			String locale = description.getLocale().toString();
			line.put(BaseLineProcessor.HEADER_DESC + BaseLineProcessor.LOCALE_SEPARATOR + locale,
			    description.getDescription());
		}
	}
	
	private void exportClassification(Concept concept, ExportLine line) {
		if (concept.getConceptClass() != null) {
			line.put(ConceptLineProcessor.HEADER_CLASS, concept.getConceptClass().getName());
		}
		if (concept.getDatatype() != null) {
			line.put(ConceptLineProcessor.HEADER_DATATYPE, concept.getDatatype().getName());
		}
		line.put(ConceptLineProcessor.HEADER_VERSION, concept.getVersion());
	}
	
	private String prefixFor(ConceptNameType type) {
		if (type == ConceptNameType.FULLY_SPECIFIED) {
			return ConceptLineProcessor.HEADER_FSNAME;
		}
		if (type == ConceptNameType.SHORT) {
			return ConceptLineProcessor.HEADER_SHORTNAME;
		}
		if (type == ConceptNameType.INDEX_TERM) {
			return ConceptLineProcessor.HEADER_INDEX_TERM;
		}
		return ConceptLineProcessor.HEADER_SYNONYM;
	}
	
	private boolean isNumbered(ConceptNameType type) {
		return type == null || type == ConceptNameType.INDEX_TERM;
	}
}
