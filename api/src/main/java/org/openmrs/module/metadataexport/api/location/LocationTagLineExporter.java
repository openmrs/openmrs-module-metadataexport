/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.location;

import org.openmrs.LocationTag;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.metadataexport.api.export.MetadataLineExporter;

public class LocationTagLineExporter extends MetadataLineExporter<LocationTag> {
	
	@Override
	public void export(LocationTag locationTag, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, locationTag.getName());
		line.put(BaseLineProcessor.HEADER_DESC, locationTag.getDescription());
	}
}
