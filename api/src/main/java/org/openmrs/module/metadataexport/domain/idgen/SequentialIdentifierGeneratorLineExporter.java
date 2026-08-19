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

import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.ExportLine;

/**
 * Columns specific to {@link SequentialIdentifierGenerator} sources. {@code nextSequenceValue} is
 * runtime state and is not exported.
 */
public class SequentialIdentifierGeneratorLineExporter extends BaseLineExporter<IdentifierSource> {
	
	@Override
	public void export(IdentifierSource source, ExportLine line) {
		source = HibernateUtil.getRealObjectFromProxy(source);
		if (!(source instanceof SequentialIdentifierGenerator)) {
			return;
		}
		
		SequentialIdentifierGenerator generator = (SequentialIdentifierGenerator) source;
		line.put(IdentifierSourceLineExporter.HEADER_PREFIX, generator.getPrefix());
		line.put(IdentifierSourceLineExporter.HEADER_SUFFIX, generator.getSuffix());
		line.put(IdentifierSourceLineExporter.HEADER_FIRST_ID_BASE, generator.getFirstIdentifierBase());
		line.put(IdentifierSourceLineExporter.HEADER_MIN_LENGTH, generator.getMinLength());
		line.put(IdentifierSourceLineExporter.HEADER_MAX_LENGTH, generator.getMaxLength());
		line.put(IdentifierSourceLineExporter.HEADER_BASE_CHAR_SET, generator.getBaseCharacterSet());
	}
}
