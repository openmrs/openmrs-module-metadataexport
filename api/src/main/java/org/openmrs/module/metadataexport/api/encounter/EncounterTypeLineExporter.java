/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.encounter;

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.EncounterType;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.api.export.BaseLineExporter;
import org.openmrs.module.metadataexport.api.export.ExportLine;

/**
 * Inverse of Initializer's EncounterTypeLineProcessor: uuid, name, description, and the view/edit
 * privileges (by name). A retired type is emitted as uuid + flag only.
 */
public class EncounterTypeLineExporter extends BaseLineExporter<EncounterType> {
	
	private static final String HEADER_VIEW_PRIV = "view privilege";
	
	private static final String HEADER_EDIT_PRIV = "edit privilege";
	
	@Override
	public void export(EncounterType type, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_UUID, type.getUuid());
		
		if (BooleanUtils.isTrue(type.getRetired())) {
			line.put(BaseLineProcessor.HEADER_VOID_RETIRE, "true");
			return;
		}
		
		line.put(BaseLineProcessor.HEADER_NAME, type.getName());
		line.put(BaseLineProcessor.HEADER_DESC, type.getDescription());
		if (type.getViewPrivilege() != null) {
			line.put(HEADER_VIEW_PRIV, type.getViewPrivilege().getName());
		}
		if (type.getEditPrivilege() != null) {
			line.put(HEADER_EDIT_PRIV, type.getEditPrivilege().getName());
		}
	}
}
