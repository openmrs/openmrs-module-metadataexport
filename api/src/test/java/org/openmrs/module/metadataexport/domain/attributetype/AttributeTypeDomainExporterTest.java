/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.attributetype;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptAttributeType;
import org.openmrs.LocationAttributeType;
import org.openmrs.ProgramAttributeType;
import org.openmrs.ProviderAttributeType;
import org.openmrs.VisitAttributeType;
import org.openmrs.module.cohort.CohortAttributeType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeTypeDomainExporterTest {
	
	private final AttributeTypeDomainExporter exporter = new AttributeTypeDomainExporter();
	
	@Test
	void handlesEveryPlatformAttributeType() {
		assertTrue(exporter.handles(new LocationAttributeType()));
		assertTrue(exporter.handles(new VisitAttributeType()));
		assertTrue(exporter.handles(new ProviderAttributeType()));
		assertTrue(exporter.handles(new ConceptAttributeType()));
		assertTrue(exporter.handles(new ProgramAttributeType()));
	}
	
	@Test
	void doesNotClaimAttributeTypesOwnedByOtherModules() {
		// CohortAttributeType extends BaseAttributeType but belongs to the cohort domain; claiming
		// it here would route it to a line exporter whose entityName(...) throws.
		assertFalse(exporter.handles(new CohortAttributeType()));
	}
}
