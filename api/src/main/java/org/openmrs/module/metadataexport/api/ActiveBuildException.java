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

import org.openmrs.api.APIException;

/**
 * A build was requested for a package that already has a QUEUED or RUNNING build. The REST layer
 * maps exactly this type to 409; other {@link APIException}s keep their generic handling.
 */
public class ActiveBuildException extends APIException {
	
	public ActiveBuildException(String message) {
		super(message);
	}
	
	public ActiveBuildException(String message, Throwable cause) {
		super(message, cause);
	}
}
