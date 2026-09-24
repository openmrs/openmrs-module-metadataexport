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

import org.openmrs.module.initializer.InitializerMessageSource;

/**
 * Overriding the {@code InitializerMessageSource#initialize} call to suppress PostConstruct
 * classpath scanning to avoid Windows URI syntax errors during build.
 */
public class StubInitializerMessageSource extends InitializerMessageSource {
	
	@Override
	public void initialize() {
	}
}
