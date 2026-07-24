/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.web.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.metadataexport.api.MetadataExportService;
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportStatus;
import org.openmrs.module.metadataexport.web.controller.dto.ExportBuildDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

@Controller("metadataexport.ExportBuildController")
@RequestMapping(MetadataExportRestConstants.BASE + "/builds")
public class ExportBuildController {
	
	@GetMapping("/{uuid}")
	@ResponseBody
	public ResponseEntity<ExportBuildDto> getBuild(@PathVariable String uuid) {
		Context.requirePrivilege(MetadataExportRestConstants.GET_PRIVILEGE);
		ExportBuild build = service().getBuildByUuid(uuid);
		if (build == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(ExportBuildDto.detailFrom(build));
	}
	
	@GetMapping("/{uuid}/download")
	@ResponseBody
	public ResponseEntity<?> download(@PathVariable String uuid, HttpServletResponse response) throws IOException {
		Context.requirePrivilege(MetadataExportRestConstants.MANAGE_PRIVILEGE);
		ExportBuild build = service().getBuildByUuid(uuid);
		if (build == null) {
			return ResponseEntity.notFound().build();
		}
		if (build.getExportStatus() != ExportStatus.COMPLETED) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
			        .body(Collections.singletonMap("error", "Build is " + build.getExportStatus() + ", not COMPLETED"));
		}
		File zip = service().getBuildZip(build);
		if (zip == null || !zip.exists()) {
			return ResponseEntity.status(HttpStatus.GONE)
			        .body(Collections.singletonMap("error", "The zip of this build no longer exists on the server"));
		}
		response.setContentType("application/zip");
		response.setHeader("Content-Length", String.valueOf(zip.length()));
		response.setHeader("Content-Disposition", "attachment; filename=\"" + zip.getName() + "\"");
		Files.copy(zip.toPath(), response.getOutputStream());
		response.flushBuffer();
		return null;
	}
	
	private static MetadataExportService service() {
		return Context.getService(MetadataExportService.class);
	}
}
