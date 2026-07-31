/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.validator;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.api.db.MetadataExportDao;
import org.openmrs.module.metadataexport.api.model.ExportPackage;
import org.openmrs.module.metadataexport.api.model.ExportPackageEntry;
import org.openmrs.module.metadataexport.export.DomainExporterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@AllArgsConstructor
@Handler(supports = { ExportPackage.class }, order = 50)
public class ExportPackageValidator implements Validator {
	
	private final MetadataExportDao metadataExportDao;
	
	private final DomainExporterRegistry domainExporterRegistry;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return ExportPackage.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		ExportPackage exportPackage = (ExportPackage) target;
		
		if (StringUtils.isBlank(exportPackage.getName())) {
			errors.rejectValue("name", "metadataexport.package.name.required", "An export package requires a name");
		} else {
			ExportPackage sameName = metadataExportDao.getPackageByName(exportPackage.getName());
			if (sameName != null && !sameName.getUuid().equals(exportPackage.getUuid())) {
				errors.rejectValue("name", "metadataexport.package.name.duplicate",
				    "An export package with this name already exists");
			}
		}
		
		for (int i = 0; i < exportPackage.getEntries().size(); i++) {
			ExportPackageEntry entry = exportPackage.getEntries().get(i);
			try {
				Domain domain = entry.getDomainEnum();
				if (domainExporterRegistry.forDomain(domain) == null) {
					errors.rejectValue("entries[" + i + "].domain", "metadataexport.package.entry.domain.unsupported",
					    "No exporter supports domain '" + entry.getDomain() + "'");
				}
			}
			catch (IllegalArgumentException | NullPointerException e) {
				errors.rejectValue("entries[" + i + "].domain", "metadataexport.package.entry.domain.unknown",
				    "Unknown domain '" + entry.getDomain() + "'");
			}
		}
	}
}
