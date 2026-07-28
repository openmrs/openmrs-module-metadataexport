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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportStatus;
import org.openmrs.module.metadataexport.web.controller.MetadataExportRestConstants;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;

@Slf4j
@Getter
@Setter
public class ExportBuildDto {
	
	private String uuid;
	
	private String packageUuid;
	
	private Integer version;
	
	private String status;
	
	private Date dateCreated;
	
	private Date dateStarted;
	
	private Date dateCompleted;
	
	private String errorMessage;
	
	private String downloadUrl;
	
	private JsonNode manifest;
	
	public static ExportBuildDto from(ExportBuild build) {
		ExportBuildDto dto = new ExportBuildDto();
		dto.setUuid(build.getUuid());
		dto.setPackageUuid(build.getExportPackage().getUuid());
		dto.setVersion(build.getVersion());
		dto.setStatus(build.getExportStatus().name());
		dto.setDateCreated(build.getDateCreated());
		dto.setDateStarted(build.getDateStarted());
		dto.setDateCompleted(build.getDateCompleted());
		dto.setErrorMessage(build.getErrorMessage());
		if (build.getExportStatus() == ExportStatus.COMPLETED) {
			dto.setDownloadUrl(ServletUriComponentsBuilder.fromCurrentContextPath().path("/ws")
			        .path(MetadataExportRestConstants.BASE).path("/builds/" + build.getUuid() + "/download").toUriString());
		}
		return dto;
	}
	
	public static ExportBuildDto detailFrom(ExportBuild build) {
		ExportBuildDto dto = from(build);
		if (build.getManifestJson() != null) {
			try {
				dto.setManifest(new ObjectMapper().readTree(build.getManifestJson()));
			}
			catch (Exception e) {
				log.warn("Metadata Export: could not parse manifest of build {}", build.getUuid(), e);
			}
		}
		return dto;
	}
}
