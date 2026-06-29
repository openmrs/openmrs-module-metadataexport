/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.export;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Set;

public class ExportLine {
	
	private final LinkedHashMap<String, String> values = new LinkedHashMap<>();
	
	public void put(String header, String value) {
		if (StringUtils.isNotEmpty(value)) {
			values.put(header, value);
		}
	}
	
	public String get(String header) {
		return values.get(header);
	}
	
	public Set<String> getHeaders() {
		return values.keySet();
	}
	
	public boolean containsHeader(String header) {
		return values.containsKey(header);
	}
	
	public String getValueOrBlank(String header) {
		return StringUtils.defaultString(values.get(header));
	}
}
