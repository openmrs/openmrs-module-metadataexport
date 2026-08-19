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
import org.openmrs.module.idgen.IdentifierPool;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.ExportLine;

/**
 * Columns specific to {@link IdentifierPool} sources. The pooled identifiers themselves are runtime
 * data and are not exported. The boolean columns are always emitted — an absent cell becomes a null
 * that NPEs when Iniz assigns it into idgen's primitive-backed fields.
 */
public class IdentifierPoolLineExporter extends BaseLineExporter<IdentifierSource> {
	
	@Override
	public void export(IdentifierSource source, ExportLine line) {
		source = HibernateUtil.getRealObjectFromProxy(source);
		if (!(source instanceof IdentifierPool)) {
			return;
		}
		
		IdentifierPool pool = (IdentifierPool) source;
		if (pool.getSource() != null) {
			line.put(IdentifierSourceLineExporter.HEADER_POOL_IDENTIFIER_SOURCE, pool.getSource().getUuid());
		}
		line.put(IdentifierSourceLineExporter.HEADER_POOL_BATCH_SIZE, String.valueOf(pool.getBatchSize()));
		line.put(IdentifierSourceLineExporter.HEADER_POOL_MINIMUM_SIZE, String.valueOf(pool.getMinPoolSize()));
		line.put(IdentifierSourceLineExporter.HEADER_POOL_REFILL_WITH_TASK,
		    Boolean.toString(pool.isRefillWithScheduledTask()));
		line.put(IdentifierSourceLineExporter.HEADER_POOL_SEQUENTIAL_ALLOCATION, Boolean.toString(pool.isSequential()));
	}
}
