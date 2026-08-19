/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.conceptreferencerange;

import org.openmrs.ConceptReferenceRange;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.ExportLine;

public class ConceptReferenceRangeLineExporter extends BaseLineExporter<ConceptReferenceRange> {
	
	public final static String HEADER_CONCEPT_NUMERIC_UUID = "Concept Numeric uuid";
	
	public final static String HEADER_ABSOLUTE_LOW = "Absolute Low";
	
	public final static String HEADER_ABSOLUTE_HIGH = "Absolute High";
	
	public final static String HEADER_CRITICAL_LOW = "Critical Low";
	
	public final static String HEADER_CRITICAL_HIGH = "Critical High";
	
	public final static String HEADER_NORMAL_LOW = "Normal Low";
	
	public final static String HEADER_NORMAL_HIGH = "Normal High";
	
	public final static String HEADER_CRITERIA = "Criteria";
	
	@Override
	public void export(ConceptReferenceRange conceptReferenceRange, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_UUID, conceptReferenceRange.getUuid());
		line.put(HEADER_CONCEPT_NUMERIC_UUID, conceptReferenceRange.getConceptNumeric().getUuid());
		line.put(HEADER_ABSOLUTE_LOW, conceptReferenceRange.getLowAbsolute());
		line.put(HEADER_ABSOLUTE_HIGH, conceptReferenceRange.getHiAbsolute());
		line.put(HEADER_CRITICAL_LOW, conceptReferenceRange.getLowCritical());
		line.put(HEADER_CRITICAL_HIGH, conceptReferenceRange.getHiCritical());
		line.put(HEADER_NORMAL_LOW, conceptReferenceRange.getLowNormal());
		line.put(HEADER_NORMAL_HIGH, conceptReferenceRange.getHiNormal());
		line.put(HEADER_CRITERIA, conceptReferenceRange.getCriteria());
	}
}
