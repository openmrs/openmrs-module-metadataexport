/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.personattributetype;

import org.openmrs.OpenmrsObject;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class PersonAttributeTypeDomainExporter extends CsvDomainExporter<PersonAttributeType> {
	
	@Override
	protected List<BaseLineExporter<PersonAttributeType>> chain() {
		return Collections.singletonList(new PersonAttributeTypeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "personAttributeTypes.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.PERSON_ATTRIBUTE_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof PersonAttributeType;
	}
	
	@Override
	public Collection<PersonAttributeType> getAllInstances() {
		return Context.getPersonService().getAllPersonAttributeTypes(true);
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(PersonAttributeType instance) {
		return Collections.emptyList();
	}
}
