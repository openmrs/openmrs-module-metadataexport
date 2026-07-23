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

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.ExportLine;

public class ConceptNumericExporter extends BaseLineExporter<Concept> {
	
	private static final String HEADER_AH = "absolute high";
	
	private static final String HEADER_CH = "critical high";
	
	private static final String HEADER_NH = "normal high";
	
	private static final String HEADER_AL = "absolute low";
	
	private static final String HEADER_CL = "critical low";
	
	private static final String HEADER_NL = "normal low";
	
	private static final String HEADER_ALLOWDECIMALS = "allow decimals";
	
	private static final String HEADER_UNITS = "units";
	
	private static final String HEADER_PRECISION = "display precision";
	
	@Override
	public void export(Concept concept, ExportLine line) {
		// A numeric concept reached as a lazy association (e.g. an answer or set member) is a
		// Concept-typed proxy, so instanceof ConceptNumeric would be false; unwrap to the real class.
		concept = HibernateUtil.getRealObjectFromProxy(concept);
		if (BooleanUtils.isTrue(concept.getRetired()) || !(concept instanceof ConceptNumeric)) {
			return;
		}
		
		ConceptNumeric cn = (ConceptNumeric) concept;
		put(line, HEADER_AH, cn.getHiAbsolute());
		put(line, HEADER_CH, cn.getHiCritical());
		put(line, HEADER_NH, cn.getHiNormal());
		put(line, HEADER_AL, cn.getLowAbsolute());
		put(line, HEADER_CL, cn.getLowCritical());
		put(line, HEADER_NL, cn.getLowNormal());
		put(line, HEADER_ALLOWDECIMALS, cn.getAllowDecimal());
		put(line, HEADER_UNITS, cn.getUnits());
		put(line, HEADER_PRECISION, cn.getDisplayPrecision());
	}
	
	private void put(ExportLine line, String header, Object value) {
		if (value != null) {
			line.put(header, value.toString());
		}
	}
}
