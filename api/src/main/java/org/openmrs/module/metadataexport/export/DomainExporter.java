/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.export;

import org.hibernate.Hibernate;
import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;

import java.io.IOException;
import java.util.Collection;

/**
 * A self-describing, format-neutral exporter for one Iniz {@link Domain}. The ExporterService holds
 * a registry of these and never contains any per-domain logic, so adding a domain is a new class
 * rather than a new service method.
 * <p>
 * Implementations own their own output: a CSV domain writes one file, a future forms domain could
 * write many JSON files. The selection seam ({@link #getAllInstances} / {@link #getDependencies})
 * is kept separate from the writing ({@link #export}) so that instance-level selection and
 * cross-domain dependency closure can be layered on later without touching the writers.
 */
public interface DomainExporter<T extends OpenmrsObject> {
	
	Domain getDomain();
	
	boolean handles(OpenmrsObject instance);
	
	Collection<T> getAllInstances();
	
	// dependencies may belong to other domains, hence the wider element type
	Collection<? extends OpenmrsObject> getDependencies(T instance);
	
	void export(Collection<T> instances, ExportContext context) throws IOException;
	
	/**
	 * Stable identity of an instance for de-duplication during selection: two objects with the same key
	 * are treated as the same export row and are visited (and written) once.
	 * <p>
	 * The default is (real entity class, uuid). This matches OpenMRS's actual uniqueness guarantee,
	 * which is per-table, not global: UUIDs are not globally unique and have historically been reused
	 * across tables, so a bare-uuid key would silently drop one of two same-uuid objects that live in
	 * different tables (e.g. a location and a visit attribute type, both under
	 * {@code ATTRIBUTE_TYPES}). {@link Hibernate#getClass} is used so a lazy proxy keys the same as its
	 * initialized twin.
	 * <p>
	 * Returning {@code null} excludes the instance from the export; the default does so for a null
	 * uuid, which cannot be represented as an Initializer row. Override when a domain's identity is not
	 * (class, uuid) — e.g. content we do not care to preserve as a stable row across versions.
	 */
	default String identityKey(T instance) {
		if (instance.getUuid() == null) {
			return null;
		}
		return Hibernate.getClass(instance).getName() + ' ' + instance.getUuid();
	}
}
