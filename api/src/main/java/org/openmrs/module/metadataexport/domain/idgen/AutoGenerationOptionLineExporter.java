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

import org.openmrs.module.idgen.AutoGenerationOption;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.idgen.autogen.AutoGenerationOptionLineProcessor;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.ExportLine;

/**
 * Iniz ignores {@code void/retire} for this domain (idgen never persists option retirement), so a
 * full row is always written. The boolean columns are always emitted — an absent cell becomes a
 * null that NPEs unboxing into idgen's primitive-boolean setters.
 */
public class AutoGenerationOptionLineExporter extends BaseLineExporter<AutoGenerationOption> {
	
	@Override
	public void export(AutoGenerationOption option, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_UUID, option.getUuid());
		if (option.getIdentifierType() != null) {
			line.put(AutoGenerationOptionLineProcessor.IDENTIFIER_TYPE, option.getIdentifierType().getUuid());
		}
		if (option.getLocation() != null) {
			line.put(AutoGenerationOptionLineProcessor.LOCATION, option.getLocation().getUuid());
		}
		if (option.getSource() != null) {
			line.put(AutoGenerationOptionLineProcessor.IDENTIFIER_SOURCE, option.getSource().getUuid());
		}
		line.put(AutoGenerationOptionLineProcessor.MANUAL_ENTRY_ENABLED, Boolean.toString(option.isManualEntryEnabled()));
		line.put(AutoGenerationOptionLineProcessor.AUTO_GEN_ENABLED,
		    Boolean.toString(option.isAutomaticGenerationEnabled()));
	}
}
