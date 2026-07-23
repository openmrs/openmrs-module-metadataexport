/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.globalproperty;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.GlobalProperty;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.XmlDomainExporter;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GlobalPropertyDomainExporter extends XmlDomainExporter<GlobalProperty> {
	
	private static final String[] MODULE_STATE_SUFFIXES = { ".started", ".mandatory" };
	
	@Override
	protected Map<String, Document> toDocuments(Collection<GlobalProperty> instances) {
		Document document = newDocument();
		Element config = document.createElement("config");
		Element container = document.createElement("globalProperties");
		for (GlobalProperty globalProperty : instances) {
			Element gpElement = document.createElement("globalProperty");
			
			Element property = document.createElement("property");
			property.appendChild(document.createTextNode(globalProperty.getProperty()));
			
			Element value = document.createElement("value");
			value.appendChild(document.createTextNode(StringUtils.defaultString(globalProperty.getPropertyValue())));
			
			gpElement.appendChild(property);
			gpElement.appendChild(value);
			container.appendChild(gpElement);
		}
		config.appendChild(container);
		document.appendChild(config);
		return Collections.singletonMap(fileName(), document);
	}
	
	@Override
	public Domain getDomain() {
		return Domain.GLOBAL_PROPERTIES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof GlobalProperty;
	}
	
	@Override
	public Collection<GlobalProperty> getAllInstances() {
		return Context.getAdministrationService().getAllGlobalProperties().stream()
		        .filter(globalProperty -> !isModuleStateProperty(globalProperty.getProperty())).collect(Collectors.toList());
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(GlobalProperty instance) {
		return Collections.emptyList();
	}
	
	private String fileName() {
		return "globalProperties.xml";
	}
	
	static boolean isModuleStateProperty(String property) {
		return property != null && Arrays.stream(MODULE_STATE_SUFFIXES).anyMatch(property::endsWith);
	}
}
