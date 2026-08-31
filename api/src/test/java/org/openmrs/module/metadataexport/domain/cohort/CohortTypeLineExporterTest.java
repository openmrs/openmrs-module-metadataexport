/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.cohort;

import org.junit.jupiter.api.Test;
import org.openmrs.module.cohort.CohortType;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CohortTypeLineExporterTest {
	
	@Test
	void exportsUuidNameAndDescription() {
		CohortType cohortType = new CohortType();
		cohortType.setUuid("aaa1a367-3047-4833-af27-b30e2dac9028");
		cohortType.setName("Support Group");
		cohortType.setDescription("Patients enrolled in a support group");
		
		ExportLine line = new ExportLine();
		new CohortTypeLineExporter().writeLine(cohortType, line);
		
		assertEquals("aaa1a367-3047-4833-af27-b30e2dac9028", line.get("uuid"));
		assertEquals("Support Group", line.get("name"));
		assertEquals("Patients enrolled in a support group", line.get("description"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void liveTypeWithoutDescriptionOmitsDescriptionColumn() {
		CohortType cohortType = new CohortType();
		cohortType.setUuid("aaa1a367-3047-4833-af27-b30e2dac9028");
		cohortType.setName("Support Group");
		
		ExportLine line = new ExportLine();
		new CohortTypeLineExporter().writeLine(cohortType, line);
		
		assertEquals("Support Group", line.get("name"));
		assertNull(line.get("description"), "empty description is not written as a column");
	}
	
	@Test
	void neverWritesTheVoidRetireFlag() {
		CohortType cohortType = new CohortType();
		cohortType.setUuid("439559c2-a3a4-4a25-b4b2-1a0299e287ee");
		cohortType.setName("Old Group");
		cohortType.setVoided(true);
		
		ExportLine line = new ExportLine();
		new CohortTypeLineExporter().writeLine(cohortType, line);
		
		assertNull(line.get("void/retire"));
	}
}
