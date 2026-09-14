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

public final class MetadataExportConstants {
	
	public static final String MODULE_ID = "metadataexport";
	
	/** Read packages and builds. Declared in config.xml. */
	public static final String GET_PRIVILEGE = "Get Metadata Export Packages";
	
	/** Create, update and retire packages; trigger builds. Declared in config.xml. */
	public static final String MANAGE_PRIVILEGE = "Manage Metadata Export Packages";
	
	/**
	 * Download a completed build's zip. Separate from Manage so a role can fetch exports without
	 * editing them. Declared in config.xml.
	 */
	public static final String DOWNLOAD_PRIVILEGE = "Download Metadata Export Packages";
	
	private MetadataExportConstants() {
	}
}
