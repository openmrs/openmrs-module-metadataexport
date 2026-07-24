/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.metadata;

import org.openmrs.module.initializer.api.mdm.MetadataTermMappingsLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;
import org.openmrs.module.metadatamapping.MetadataSource;
import org.openmrs.module.metadatamapping.MetadataTermMapping;

public class MetadataTermMappingLineExporter extends MetadataLineExporter<MetadataTermMapping> {
	
	@Override
	public void export(MetadataTermMapping mapping, ExportLine line) {
		line.put(MetadataTermMappingsLineProcessor.MAPPING_CODE, mapping.getCode());
		
		MetadataSource source = mapping.getMetadataSource();
		if (source != null) {
			line.put(MetadataTermMappingsLineProcessor.MAPPING_SOURCE, source.getName());
		}
		
		line.put(MetadataTermMappingsLineProcessor.METADATA_CLASS_NAME, mapping.getMetadataClass());
		line.put(MetadataTermMappingsLineProcessor.METADATA_UUID, mapping.getMetadataUuid());
	}
}
