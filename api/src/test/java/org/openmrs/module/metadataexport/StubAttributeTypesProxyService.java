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

import org.openmrs.attribute.BaseAttributeType;
import org.openmrs.module.initializer.api.attributes.types.AttributeTypeEntity;
import org.openmrs.module.initializer.api.attributes.types.AttributeTypesProxyService;

import java.util.Collections;
import java.util.Set;

/** Unused stub; see {@code TestingApplicationContext.xml}. */
public class StubAttributeTypesProxyService implements AttributeTypesProxyService {
	
	@Override
	public BaseAttributeType<?> saveAttributeType(BaseAttributeType<?> attributeType) {
		throw new UnsupportedOperationException("stub");
	}
	
	@Override
	public BaseAttributeType<?> getAttributeTypeByUuid(String uuid, AttributeTypeEntity entity) {
		throw new UnsupportedOperationException("stub");
	}
	
	@Override
	public BaseAttributeType<?> getAttributeTypeByName(String name, AttributeTypeEntity entity) {
		throw new UnsupportedOperationException("stub");
	}
	
	@Override
	public Set<AttributeTypeEntity> getSupportedTypes() {
		return Collections.emptySet();
	}
	
	@Override
	public void onStartup() {
	}
	
	@Override
	public void onShutdown() {
	}
}
