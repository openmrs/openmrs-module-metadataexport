/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.select;

import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The passive, content-free result of {@link Selector}: the complete set of objects to export,
 * bucketed by {@link Domain} and de-duplicated. This is the only thing that crosses from selection
 * to export, so the export side never sees how the set was chosen.
 */
public class ExportManifest {
	
	private final Map<Domain, LinkedHashMap<String, OpenmrsObject>> byDomain = new LinkedHashMap<>();
	
	public boolean add(Domain domain, String identityKey, OpenmrsObject instance) {
		return byDomain.computeIfAbsent(domain, d -> new LinkedHashMap<>()).putIfAbsent(identityKey, instance) == null;
	}
	
	public Set<Domain> getDomains() {
		return byDomain.keySet();
	}
	
	public Collection<OpenmrsObject> get(Domain domain) {
		LinkedHashMap<String, OpenmrsObject> bucket = byDomain.get(domain);
		return bucket == null ? Collections.<OpenmrsObject> emptyList() : bucket.values();
	}
	
	public boolean isEmpty() {
		return byDomain.isEmpty();
	}
	
	public Map<Domain, Collection<OpenmrsObject>> asMap() {
		Map<Domain, Collection<OpenmrsObject>> out = new LinkedHashMap<>();
		for (Map.Entry<Domain, LinkedHashMap<String, OpenmrsObject>> entry : byDomain.entrySet()) {
			out.put(entry.getKey(), Collections.unmodifiableCollection(entry.getValue().values()));
		}
		return Collections.unmodifiableMap(out);
	}
}
