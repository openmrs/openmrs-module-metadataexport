/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.relationshipType;

import org.openmrs.RelationshipType;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.relationship.types.RelationshipTypeLineProcessor;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.metadataexport.api.export.MetadataLineExporter;

public class RelationshipTypeLineExporter extends MetadataLineExporter<RelationshipType> {
	
	@Override
	public void export(RelationshipType relationshipType, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, relationshipType.getName());
		line.put(BaseLineProcessor.HEADER_DESC, relationshipType.getDescription());
		line.put(RelationshipTypeLineProcessor.A_IS_TO_B, relationshipType.getaIsToB());
		line.put(RelationshipTypeLineProcessor.B_IS_TO_A, relationshipType.getbIsToA());
		line.put(RelationshipTypeLineProcessor.PREFERRED, String.valueOf(relationshipType.getPreferred()));
		line.put(RelationshipTypeLineProcessor.WEIGHT, String.valueOf(relationshipType.getWeight()));
	}
}
