/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.select;

import org.openmrs.OpenmrsObject;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporterRegistry;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Turns a set of seed objects into a self-contained {@link ExportManifest} by walking dependencies
 * to a fixpoint. This is the single, uniform cross-domain traversal (the thing MDS gets for free
 * from being content-neutral): each object is routed to its owning domain via the registry, added
 * to the manifest once (visited by the owner's {@link DomainExporter#identityKey}, so cycles and
 * diamonds are safe), and its dependencies are enqueued — repeating until nothing new is
 * discovered.
 */
public class Selector {
	
	private final DomainExporterRegistry registry;
	
	public Selector(DomainExporterRegistry registry) {
		this.registry = registry;
	}
	
	public ExportManifest select(Collection<? extends OpenmrsObject> seeds) {
		ExportManifest manifest = new ExportManifest();
		Set<String> visited = new HashSet<>();
		Deque<OpenmrsObject> queue = new ArrayDeque<>(seeds);
		
		while (!queue.isEmpty()) {
			OpenmrsObject instance = HibernateUtil.getRealObjectFromProxy(queue.poll());
			if (instance == null) {
				continue;
			}
			
			DomainExporter<?> owner = registry.forObject(instance);
			if (owner == null) {
				// No registered domain owns this (e.g. a standard concept class); the target instance
				// is assumed to provide it. Skip without failing.
				continue;
			}
			
			// Identity (and thus de-duplication) is the owner's concern, not a bare uuid: uuids are
			// only unique per table, so the key must be domain-aware. A null key means "do not export".
			String identity = identityKeyOf(owner, instance);
			if (identity == null || !visited.add(identity)) {
				continue;
			}
			
			manifest.add(owner.getDomain(), identity, instance);
			queue.addAll(dependenciesOf(owner, instance));
		}
		return manifest;
	}
	
	/**
	 * Safe unchecked call: {@code owner.handles(instance)} was true, so it accepts this object as T.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends OpenmrsObject> String identityKeyOf(DomainExporter<T> owner, OpenmrsObject instance) {
		return owner.identityKey((T) instance);
	}
	
	/**
	 * Safe unchecked call: {@code owner.handles(instance)} was true, so it accepts this object as T.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends OpenmrsObject> Collection<? extends OpenmrsObject> dependenciesOf(DomainExporter<T> owner,
	        OpenmrsObject instance) {
		return owner.getDependencies((T) instance);
	}
}
