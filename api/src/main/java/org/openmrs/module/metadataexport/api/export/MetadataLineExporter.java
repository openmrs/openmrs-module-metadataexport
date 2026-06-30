/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.export;

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.Retireable;
import org.openmrs.module.initializer.api.BaseLineProcessor;

/**
 * Base for the primary line exporter of a metadata domain. Writes the columns every Initializer row
 * carries — the uuid, and for a retired object the {@code void/retire} flag — then delegates the
 * domain-specific columns to {@link #export}. A retired object is emitted as uuid + flag only, so
 * {@link #export} only ever sees a live instance.
 */
public abstract class MetadataLineExporter<T extends OpenmrsObject & Retireable> extends BaseLineExporter<T> {
	
	@Override
	public final void writeLine(T instance, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_UUID, instance.getUuid());
		
		if (BooleanUtils.isTrue(instance.getRetired())) {
			line.put(BaseLineProcessor.HEADER_VOID_RETIRE, "true");
			return;
		}
		
		export(instance, line);
	}
}
