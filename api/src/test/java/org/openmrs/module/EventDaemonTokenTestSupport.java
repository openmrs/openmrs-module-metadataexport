/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module;

import org.openmrs.event.EventActivator;

/**
 * Hands the event module the daemon token it needs to run its after-completion listeners. Platform
 * 3.0's {@code Daemon} rejects the null token those listeners hold when the event module is not
 * started, which fails every test transaction that touched an entity. Lives in
 * {@code org.openmrs.module} because {@link ModuleFactory#passDaemonToken(Module)} is
 * package-private; registered as a bean in TestingApplicationContext.xml.
 */
public class EventDaemonTokenTestSupport {
	
	public void passEventDaemonToken() {
		Module module = new Module("event");
		module.setModuleId("event");
		module.setModuleActivator(new EventActivator());
		ModuleFactory.passDaemonToken(module);
	}
}
