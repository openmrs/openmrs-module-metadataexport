/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.idgen;

import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class IdentifierSourceLineExporter extends MetadataLineExporter<IdentifierSource> {
	
	public static final String HEADER_IDTYPE = "Identifier type";
	
	public static final String HEADER_POOL_IDENTIFIER_SOURCE = "pool identifier source";
	
	public static final String HEADER_POOL_BATCH_SIZE = "pool refill batch size";
	
	public static final String HEADER_POOL_MINIMUM_SIZE = "pool minimum size";
	
	public static final String HEADER_POOL_REFILL_WITH_TASK = "pool refill with task";
	
	public static final String HEADER_POOL_SEQUENTIAL_ALLOCATION = "pool sequential allocation";
	
	public static final String HEADER_URL = "url";
	
	public static final String HEADER_USER = "user";
	
	public static final String HEADER_PASS = "password";
	
	public static final String HEADER_PREFIX = "prefix";
	
	public static final String HEADER_SUFFIX = "suffix";
	
	public static final String HEADER_FIRST_ID_BASE = "first identifier base";
	
	public static final String HEADER_MIN_LENGTH = "min length";
	
	public static final String HEADER_MAX_LENGTH = "max length";
	
	public static final String HEADER_BASE_CHAR_SET = "base character set";
	
	@Override
	public void export(IdentifierSource source, ExportLine line) {
		if (source.getIdentifierType() != null) {
			line.put(HEADER_IDTYPE, source.getIdentifierType().getUuid());
		}
		line.put(BaseLineProcessor.HEADER_NAME, source.getName());
		line.put(BaseLineProcessor.HEADER_DESC, source.getDescription());
	}
	
	@Override
	protected void writeRetiredDiscriminators(IdentifierSource source, ExportLine line) {
		export(source, line);
	}
}
