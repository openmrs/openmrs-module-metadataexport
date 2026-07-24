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

import org.openmrs.module.DaemonToken;

/**
 * Holds the module's {@link DaemonToken} (handed to the activator by the module framework) so that
 * Spring components can start daemon threads without depending on the activator instance.
 */
public final class MetadataExportDaemonToken {
	
	private static volatile DaemonToken token;
	
	private MetadataExportDaemonToken() {
	}
	
	public static void set(DaemonToken daemonToken) {
		token = daemonToken;
	}
	
	public static DaemonToken get() {
		return token;
	}
}
