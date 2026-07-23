/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.role;

import org.openmrs.OpenmrsObject;
import org.openmrs.Role;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class RoleDomainExporter extends CsvDomainExporter<Role> {
	
	@Override
	public Domain getDomain() {
		return Domain.ROLES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof Role;
	}
	
	@Override
	public Collection<Role> getAllInstances() {
		return Context.getUserService().getAllRoles();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(Role instance) {
		List<OpenmrsObject> deps = new ArrayList<>();
		deps.addAll(instance.getInheritedRoles());
		deps.addAll(instance.getPrivileges());
		return deps;
	}
	
	@Override
	protected List<BaseLineExporter<Role>> chain() {
		return Collections.singletonList(new RoleLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "roles.csv";
	}
}
