/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.role;

import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.metadataexport.api.export.MetadataLineExporter;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

public class RoleLineExporter extends MetadataLineExporter<Role> {
	
	static final String HEADER_ROLE_NAME = "Role name";
	
	static final String HEADER_INHERITED_ROLES = "Inherited roles";
	
	static final String HEADER_PRIVILEGES = "Privileges";
	
	@Override
	public void export(Role role, ExportLine line) {
		line.put(HEADER_ROLE_NAME, role.getRole());
		line.put(BaseLineProcessor.HEADER_DESC, role.getDescription());
		
		String inheritedRoles = emptyIfNull(role.getInheritedRoles()).stream().map(Role::getRole).sorted()
		        .collect(Collectors.joining("; "));
		line.put(HEADER_INHERITED_ROLES, inheritedRoles);
		
		String privileges = emptyIfNull(role.getPrivileges()).stream().map(Privilege::getPrivilege).sorted()
		        .collect(Collectors.joining("; "));
		line.put(HEADER_PRIVILEGES, privileges);
	}
}
