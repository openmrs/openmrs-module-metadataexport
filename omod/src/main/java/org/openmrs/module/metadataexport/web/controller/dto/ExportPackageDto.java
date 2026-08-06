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
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportPackage;
import org.openmrs.module.metadataexport.api.model.ExportPackageEntry;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ExportPackageDto {
	
	private String uuid;
	
	private String name;
	
	private String description;
	
	private Boolean retired;
	
	private Date dateCreated;
	
	private List<ExportPackageEntryDto> entries = new ArrayList<>();
	
	private ExportBuildDto latestBuild;
	
	public static ExportPackageDto from(ExportPackage exportPackage) {
		ExportPackageDto dto = new ExportPackageDto();
		dto.setUuid(exportPackage.getUuid());
		dto.setName(exportPackage.getName());
		dto.setDescription(exportPackage.getDescription());
		dto.setRetired(exportPackage.getRetired());
		dto.setDateCreated(exportPackage.getDateCreated());
		for (ExportPackageEntry entry : exportPackage.getEntries()) {
			dto.getEntries().add(ExportPackageEntryDto.from(entry));
		}
		return dto;
	}
	
	public static ExportPackageDto from(ExportPackage exportPackage, ExportBuild latestBuild) {
		ExportPackageDto dto = from(exportPackage);
		if (latestBuild != null) {
			dto.setLatestBuild(ExportBuildDto.from(latestBuild));
		}
		return dto;
	}
}
