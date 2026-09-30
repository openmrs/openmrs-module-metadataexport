/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.visittype;

import org.openmrs.OpenmrsObject;
import org.openmrs.VisitType;
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
public class VisitTypeDomainExporter extends CsvDomainExporter<VisitType> {
	
	@Override
	public Domain getDomain() {
		return Domain.VISIT_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof VisitType;
	}
	
	@Override
	public Collection<VisitType> getAllInstances() {
		Collection<VisitType> all = allRows();
		return DomainExporter.without(all, DomainExporter.retiredNameClashes(all, "visit type"));
	}
	
	/**
	 * Retired rows a same-named row would absorb on import; see
	 * {@link DomainExporter#retiredNameClashes}.
	 */
	@Override
	public Map<String, String> exclusions() {
		return DomainExporter.retiredNameClashes(allRows(), "visit type");
	}
	
	private static Collection<VisitType> allRows() {
		return Context.getVisitService().getAllVisitTypes();
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(VisitType instance) {
		return Collections.emptyList();
	}
	
	@Override
	protected List<BaseLineExporter<VisitType>> chain() {
		return Collections.singletonList(new VisitTypeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "visitTypes.csv";
	}
}
