/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportPackage;

import java.io.File;
import java.util.List;

public interface MetadataExportService extends OpenmrsService {
	
	ExportPackage saveExportPackage(ExportPackage exportPackage);
	
	ExportPackage getPackageByUuid(String uuid);
	
	List<ExportPackage> getAllPackages(boolean includeRetired);
	
	ExportPackage retireExportPackage(ExportPackage exportPackage, String reason);
	
	ExportBuild saveExportBuild(ExportBuild build);
	
	ExportBuild getBuildByUuid(String uuid);
	
	List<ExportBuild> getBuilds(String packageUuid);
	
	ExportBuild getLatestBuild(ExportPackage exportPackage);
	
	ExportBuild runBuild(String buildUuid);
	
	int failStrandedBuilds(String reason);
	
	File getBuildZip(ExportBuild build);
}
