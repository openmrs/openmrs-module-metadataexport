/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.web.controller.dto;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.module.metadataexport.api.model.ExportPackageEntry;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ExportPackageEntryDto {
	
	private String domain;
	
	private List<String> itemUuids = new ArrayList<>();
	
	public static ExportPackageEntryDto from(ExportPackageEntry entry) {
		ExportPackageEntryDto dto = new ExportPackageEntryDto();
		dto.setDomain(entry.getDomain());
		dto.setItemUuids(new ArrayList<>(entry.getItemUuids()));
		return dto;
	}
}
