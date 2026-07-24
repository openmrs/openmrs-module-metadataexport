/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.metadata;

import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.openmrs.module.metadataexport.export.ExportContext;
import org.openmrs.module.metadatasharing.ExportedPackage;
import org.openmrs.module.metadatasharing.api.MetadataSharingService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Collections;

/**
 * Copies out packages already built and published through the metadatasharing module's own UI (each
 * an {@link ExportedPackage} holding a ready-made, self-contained zip). This is not a CSV/XML row
 * domain, so it implements {@link DomainExporter} directly instead of extending
 * {@link org.openmrs.module.metadataexport.export.CsvDomainExporter} or
 * {@link org.openmrs.module.metadataexport.export.XmlDomainExporter}.
 */
@Component
public class MetadataSharingDomainExporter implements DomainExporter<ExportedPackage> {
	
	@Override
	public Domain getDomain() {
		return Domain.METADATASHARING;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof ExportedPackage;
	}
	
	@Override
	public Collection<ExportedPackage> getAllInstances() {
		return Context.getService(MetadataSharingService.class).getAllExportedPackages();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(ExportedPackage instance) {
		return Collections.emptyList();
	}
	
	@Override
	public void export(Collection<ExportedPackage> instances, ExportContext context) throws IOException {
		File domainDir = new File(new File(context.getOutputDir(), "configuration"), getDomain().getName());
		domainDir.mkdirs();
		for (ExportedPackage pkg : instances) {
			File target = new File(domainDir, pkg.getUuid() + ".zip");
			try (InputStream in = pkg.getSerializedPackageStream()) {
				Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}
}
