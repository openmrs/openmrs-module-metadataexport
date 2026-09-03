/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.systemtask;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.OpenmrsObject;
import org.openmrs.ProviderRole;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.tasks.Priority;
import org.openmrs.module.tasks.SystemTask;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemTaskDomainExporterIntegrationTest extends BaseModuleContextSensitiveTest {
	
	private static final String ROLE_UUID = "6a9d7e9a-2f3b-4c1d-9e8f-1a2b3c4d5e6f";
	
	private final SystemTaskDomainExporter exporter = new SystemTaskDomainExporter();
	
	private ProviderRole nurse;
	
	@BeforeEach
	void seedProviderRole() {
		nurse = new ProviderRole();
		nurse.setUuid(ROLE_UUID);
		nurse.setName("Nurse");
		nurse.setDescription("Ward nursing staff");
		SessionFactory sessionFactory = Context.getRegisteredComponent("sessionFactory", SessionFactory.class);
		sessionFactory.getCurrentSession().saveOrUpdate(nurse);
		sessionFactory.getCurrentSession().flush();
	}
	
	@Test
	void getDependencies_pullsInTheAssigneeProviderRole() {
		SystemTask task = taskAssignedTo(nurse.getProviderRoleId());
		
		Collection<? extends OpenmrsObject> dependencies = exporter.getDependencies(task);
		
		assertEquals(1, dependencies.size());
		assertEquals(ROLE_UUID, dependencies.iterator().next().getUuid());
	}
	
	@Test
	void getDependencies_ignoresAnUnknownAssignee() {
		SystemTask task = taskAssignedTo(Integer.MAX_VALUE);
		
		assertTrue(exporter.getDependencies(task).isEmpty());
	}
	
	@Test
	void lineExporter_writesTheAssigneeAsAUuid() {
		SystemTask task = taskAssignedTo(nurse.getProviderRoleId());
		
		ExportLine line = new ExportLine();
		new SystemTaskLineExporter().writeLine(task, line);
		
		assertEquals(ROLE_UUID, line.get("default assignee role"));
	}
	
	@Test
	void lineExporter_omitsAnUnknownAssignee() {
		SystemTask task = taskAssignedTo(Integer.MAX_VALUE);
		
		ExportLine line = new ExportLine();
		new SystemTaskLineExporter().writeLine(task, line);
		
		assertEquals("vital-check", line.get("name"));
		assertNull(line.get("default assignee role"));
	}
	
	private static SystemTask taskAssignedTo(Integer providerRoleId) {
		SystemTask task = new SystemTask();
		task.setUuid("550e8400-e29b-41d4-a716-446655440001");
		task.setName("vital-check");
		task.setTitle("Daily Vital Check");
		task.setPriority(Priority.HIGH);
		task.setDefaultAssigneeProviderRoleId(providerRoleId);
		return task;
	}
}
