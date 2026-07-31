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

import lombok.AllArgsConstructor;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadataexport.api.ActiveBuildException;
import org.openmrs.module.metadataexport.api.ExportJobRunner;
import org.openmrs.module.metadataexport.api.MetadataExportService;
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportPackage;
import org.openmrs.module.metadataexport.api.model.ExportPackageEntry;
import org.openmrs.module.metadataexport.web.controller.dto.ExportBuildDto;
import org.openmrs.module.metadataexport.web.controller.dto.ExportPackageDto;
import org.openmrs.module.metadataexport.web.controller.dto.ExportPackageEntryDto;
import org.openmrs.module.metadataexport.web.controller.dto.ExportPackageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

@Controller("metadataexport.ExportPackageController")
@RequestMapping(MetadataExportRestConstants.BASE + "/packages")
@AllArgsConstructor
public class ExportPackageController {
	
	private final ExportJobRunner exportJobRunner;
	
	@GetMapping
	@ResponseBody
	public List<ExportPackageDto> listPackages(
	        @RequestParam(value = "includeRetired", defaultValue = "false") boolean includeRetired) {
		Context.requirePrivilege(MetadataExportRestConstants.GET_PRIVILEGE);
		List<ExportPackageDto> dtos = new ArrayList<>();
		for (ExportPackage exportPackage : service().getAllPackages(includeRetired)) {
			dtos.add(ExportPackageDto.from(exportPackage, latestBuild(exportPackage)));
		}
		return dtos;
	}
	
	@PostMapping
	@ResponseBody
	public ResponseEntity<ExportPackageDto> createPackage(@RequestBody ExportPackageRequest request) {
		Context.requirePrivilege(MetadataExportRestConstants.MANAGE_PRIVILEGE);
		ExportPackage exportPackage = new ExportPackage();
		apply(request, exportPackage);
		ExportPackage saved = service().saveExportPackage(exportPackage);
		return ResponseEntity.status(HttpStatus.CREATED).body(ExportPackageDto.from(saved));
	}
	
	@GetMapping("/{uuid}")
	@ResponseBody
	public ResponseEntity<ExportPackageDto> getPackage(@PathVariable String uuid) {
		Context.requirePrivilege(MetadataExportRestConstants.GET_PRIVILEGE);
		ExportPackage exportPackage = service().getPackageByUuid(uuid);
		if (exportPackage == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(ExportPackageDto.from(exportPackage, latestBuild(exportPackage)));
	}
	
	@PutMapping("/{uuid}")
	@ResponseBody
	public ResponseEntity<ExportPackageDto> updatePackage(@PathVariable String uuid,
	        @RequestBody ExportPackageRequest request) {
		Context.requirePrivilege(MetadataExportRestConstants.MANAGE_PRIVILEGE);
		ExportPackage exportPackage = service().getPackageByUuid(uuid);
		if (exportPackage == null) {
			return ResponseEntity.notFound().build();
		}
		apply(request, exportPackage);
		ExportPackage saved = service().saveExportPackage(exportPackage);
		return ResponseEntity.ok(ExportPackageDto.from(saved, latestBuild(saved)));
	}
	
	@DeleteMapping("/{uuid}")
	@ResponseBody
	public ResponseEntity<Void> retirePackage(@PathVariable String uuid,
	        @RequestParam(value = "reason", defaultValue = "web service call") String reason) {
		Context.requirePrivilege(MetadataExportRestConstants.MANAGE_PRIVILEGE);
		ExportPackage exportPackage = service().getPackageByUuid(uuid);
		if (exportPackage == null) {
			return ResponseEntity.notFound().build();
		}
		service().retireExportPackage(exportPackage, reason);
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/{uuid}/builds")
	@ResponseBody
	public ResponseEntity<?> triggerBuild(@PathVariable String uuid) {
		Context.requirePrivilege(MetadataExportRestConstants.MANAGE_PRIVILEGE);
		if (service().getPackageByUuid(uuid) == null) {
			return ResponseEntity.notFound().build();
		}
		try {
			ExportBuild queued = exportJobRunner.trigger(uuid);
			return ResponseEntity.accepted().body(ExportBuildDto.from(queued));
		}
		catch (ActiveBuildException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Collections.singletonMap("error", e.getMessage()));
		}
	}
	
	@GetMapping("/{uuid}/builds")
	@ResponseBody
	public ResponseEntity<List<ExportBuildDto>> listBuilds(@PathVariable String uuid) {
		Context.requirePrivilege(MetadataExportRestConstants.GET_PRIVILEGE);
		if (service().getPackageByUuid(uuid) == null) {
			return ResponseEntity.notFound().build();
		}
		List<ExportBuildDto> dtos = new ArrayList<>();
		for (ExportBuild build : service().getBuilds(uuid)) {
			dtos.add(ExportBuildDto.from(build));
		}
		return ResponseEntity.ok(dtos);
	}
	
	private static void apply(ExportPackageRequest request, ExportPackage exportPackage) {
		if (request.getEntries() == null) {
			throw new IllegalArgumentException("entries is required; send an empty list to export every registered domain");
		}
		exportPackage.setName(request.getName());
		exportPackage.setDescription(request.getDescription());
		exportPackage.getEntries().clear();
		for (ExportPackageEntryDto entryDto : request.getEntries()) {
			ExportPackageEntry entry = new ExportPackageEntry();
			entry.setDomain(entryDto.getDomain() == null ? null : entryDto.getDomain().trim().toUpperCase(Locale.ROOT));
			if (entryDto.getItemUuids() != null) {
				entry.getItemUuids().addAll(new LinkedHashSet<>(entryDto.getItemUuids()));
			}
			exportPackage.getEntries().add(entry);
		}
	}
	
	private ExportBuild latestBuild(ExportPackage exportPackage) {
		return service().getLatestBuild(exportPackage);
	}
	
	private static MetadataExportService service() {
		return Context.getService(MetadataExportService.class);
	}
}
