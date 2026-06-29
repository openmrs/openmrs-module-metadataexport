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

import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Holds the available {@link DomainExporter}s and routes a domain or an arbitrary object to the one
 * that owns it. Shared by selection (object → owning domain via {@link DomainExporter#handles}) and
 * export (domain → its exporter).
 * <p>
 * The exporters are discovered by Spring component scanning: every {@link DomainExporter} annotated
 * with {@code @Component} is injected here automatically, so adding a domain never requires editing
 * a registration list.
 */
@Component
public class DomainExporterRegistry {
	
	private final List<DomainExporter<?>> exporters;
	
	@Autowired
	public DomainExporterRegistry(List<DomainExporter<?>> exporters) {
		this.exporters = exporters;
	}
	
	public List<DomainExporter<?>> all() {
		return Collections.unmodifiableList(exporters);
	}
	
	public DomainExporter<?> forObject(OpenmrsObject instance) {
		for (DomainExporter<?> exporter : exporters) {
			if (exporter.handles(instance)) {
				return exporter;
			}
		}
		return null;
	}
	
	public DomainExporter<?> forDomain(Domain domain) {
		for (DomainExporter<?> exporter : exporters) {
			if (exporter.getDomain() == domain) {
				return exporter;
			}
		}
		return null;
	}
}
