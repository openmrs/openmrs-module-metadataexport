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

import org.openmrs.EncounterType;
import org.openmrs.OpenmrsObject;
import org.openmrs.Privilege;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class EncounterTypeDomainExporter extends CsvDomainExporter<EncounterType> {
	
	@Override
	public Domain getDomain() {
		return Domain.ENCOUNTER_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof EncounterType;
	}
	
	@Override
	public Collection<EncounterType> getAllInstances() {
		Collection<EncounterType> all = allRows();
		return DomainExporter.without(all, DomainExporter.retiredNameClashes(all, "encounter type"));
	}
	
	/**
	 * Retired rows a same-named row would absorb on import; see
	 * {@link DomainExporter#retiredNameClashes}.
	 */
	@Override
	public Map<String, String> exclusions() {
		return DomainExporter.retiredNameClashes(allRows(), "encounter type");
	}
	
	private static Collection<EncounterType> allRows() {
		return Context.getEncounterService().getAllEncounterTypes();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(EncounterType instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		
		Privilege editPrivilege = instance.getEditPrivilege();
		if (editPrivilege != null) {
			dependencies.add(editPrivilege);
		}
		Privilege viewPrivilege = instance.getViewPrivilege();
		if (viewPrivilege != null) {
			dependencies.add(viewPrivilege);
		}
		return dependencies;
	}
	
	@Override
	protected List<BaseLineExporter<EncounterType>> chain() {
		return Collections.singletonList(new EncounterTypeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "encounterTypes.csv";
	}
}
