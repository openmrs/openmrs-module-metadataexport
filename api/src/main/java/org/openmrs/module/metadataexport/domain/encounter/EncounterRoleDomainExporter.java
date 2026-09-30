/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.encounter;

import org.openmrs.EncounterRole;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class EncounterRoleDomainExporter extends CsvDomainExporter<EncounterRole> {
	
	@Override
	protected List<BaseLineExporter<EncounterRole>> chain() {
		return Collections.singletonList(new EncounterRoleLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "encounterRoles.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.ENCOUNTER_ROLES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof EncounterRole;
	}
	
	@Override
	public Collection<EncounterRole> getAllInstances() {
		Collection<EncounterRole> all = allRows();
		return DomainExporter.without(all, DomainExporter.retiredNameClashes(all, "encounter role"));
	}
	
	/**
	 * Retired rows a same-named row would absorb on import; see
	 * {@link DomainExporter#retiredNameClashes}.
	 */
	@Override
	public Map<String, String> exclusions() {
		return DomainExporter.retiredNameClashes(allRows(), "encounter role");
	}
	
	private static Collection<EncounterRole> allRows() {
		return Context.getEncounterService().getAllEncounterRoles(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(EncounterRole instance) {
		return Collections.emptyList();
	}
}
