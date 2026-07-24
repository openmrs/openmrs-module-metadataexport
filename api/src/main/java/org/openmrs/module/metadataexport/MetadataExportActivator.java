/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.metadataexport.api.ExporterService;
import org.openmrs.module.metadataexport.api.MetadataExportService;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;

@Slf4j
public class MetadataExportActivator extends BaseModuleActivator implements DaemonTokenAware {
	
	private DaemonToken daemonToken;
	
	@Override
	public void setDaemonToken(DaemonToken token) {
		this.daemonToken = token;
		MetadataExportDaemonToken.set(token);
	}
	
	@Override
	public void started() {
		Daemon.runInDaemonThreadWithoutResult(() -> {
			recoverStrandedBuilds();
			exportAllMetadata();
		}, daemonToken);
	}
	
	private void recoverStrandedBuilds() {
		try {
			int recovered = Context.getService(MetadataExportService.class)
			        .failStrandedBuilds("Interrupted by a server restart");
			if (recovered > 0) {
				log.warn("Metadata Export: marked {} stranded QUEUED/RUNNING build(s) as FAILED", recovered);
			}
		}
		catch (Exception e) {
			log.error("Metadata Export: failed to recover stranded builds on startup", e);
		}
	}
	
	private void exportAllMetadata() {
		File outDir = new File(OpenmrsUtil.getApplicationDataDirectory(), "metadata_export");
		try {
			ExporterService exporterService = Context.getService(ExporterService.class);
			exporterService.export(outDir, null);
			log.info("Metadata Export: exported metadata to {}", outDir.getAbsolutePath());
		}
		catch (Exception e) {
			log.error("Metadata Export: failed to export metadata on startup", e);
		}
	}
}
