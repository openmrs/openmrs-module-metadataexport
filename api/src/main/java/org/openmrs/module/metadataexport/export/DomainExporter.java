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
import org.openmrs.api.APIException;
import org.openmrs.module.initializer.Domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A self-describing, format-neutral exporter for one Iniz {@link Domain}. The ExporterService holds
 * a registry of these and never contains any per-domain logic, so adding a domain is a new class
 * rather than a new service method.
 * <p>
 * Implementations own their own output: a CSV domain writes one file, the AMPATH forms domain
 * writes one JSON file per form. The selection seam ({@link #getAllInstances} /
 * {@link #getDependencies}) is kept separate from the writing ({@link #export}) so that
 * instance-level selection and cross-domain dependency closure can be layered on later without
 * touching the writers.
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
	
	/**
	 * The rows to search when a package names specific uuids. The default is every exportable row,
	 * which is the only correct answer for a domain whose export filter is decided over the whole
	 * collection. A domain that exports every row it has and offers a direct lookup narrows this to the
	 * rows the uuids name, so a build seeded with three concepts does not load the dictionary. The
	 * result must be a subset of {@link #getAllInstances()}.
	 */
	default Collection<T> candidatesFor(Collection<String> uuids) {
		return getAllInstances();
	}
	
	/**
	 * Rows this domain has on this server but never exports (retired, voided, unimportable), each
	 * mapped from its uuid to a sentence that names the row and says why. Empty for the many domains
	 * that export every row. For a domain exported in full, recorded in the build's manifest and logged
	 * once; for a uuid-scoped entry, consulted by {@link #getInstancesByUuids} so a package that names
	 * such a row fails with the reason instead of as an unknown uuid.
	 */
	default Map<String, String> exclusions() {
		return Collections.emptyMap();
	}
	
	/**
	 * {@link #exclusions()} for a domain with one reason: every row of {@code all} that
	 * {@code excluded} accepts, as {@code uuid → "<uuid> <reason>"}.
	 */
	static <R extends OpenmrsObject> Map<String, String> exclusions(Collection<R> all, Predicate<R> excluded,
	        String reason) {
		return exclusions(all, excluded, row -> reason);
	}
	
	/**
	 * {@link #exclusions()} for a domain whose reason depends on the row: every row of {@code all} that
	 * {@code excluded} accepts, as {@code uuid → "<uuid> <reason.apply(row)>"}. The reason should name
	 * the row (its name, the referenced row's uuid) and the one cause that applies, since this is what
	 * the manifest and the failed build show.
	 */
	static <R extends OpenmrsObject> Map<String, String> exclusions(Collection<R> all, Predicate<R> excluded,
	        Function<R, String> reason) {
		Map<String, String> result = new LinkedHashMap<>();
		for (R row : all) {
			if (excluded.test(row)) {
				result.put(row.getUuid(), row.getUuid() + " " + reason.apply(row));
			}
		}
		return result;
	}
	
	/**
	 * The requested rows, found among {@link #candidatesFor}. Fails when any uuid is left over, naming
	 * separately those that are {@link #exclusions() excluded} (with the reason) and those the domain
	 * does not know at all, so one failed build reports every problem with the package.
	 */
	default Collection<T> getInstancesByUuids(Collection<String> uuids) {
		Set<String> wanted = new LinkedHashSet<>(uuids);
		List<T> found = new ArrayList<>();
		for (T instance : candidatesFor(wanted)) {
			if (wanted.remove(instance.getUuid())) {
				found.add(instance);
			}
		}
		if (wanted.isEmpty()) {
			return found;
		}
		Map<String, String> excluded = new LinkedHashMap<>(exclusions());
		excluded.keySet().retainAll(wanted);
		wanted.removeAll(excluded.keySet());
		List<String> problems = new ArrayList<>();
		if (!excluded.isEmpty()) {
			problems.add("Not exported from domain " + getDomain()
			        + " (fix them on this server or remove them from the package): " + String.join("; ", excluded.values()));
		}
		if (!wanted.isEmpty()) {
			problems.add("Unknown uuids in domain " + getDomain() + ": " + wanted);
		}
		throw new APIException(String.join("; ", problems));
	}
}
