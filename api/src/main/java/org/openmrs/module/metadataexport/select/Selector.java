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

import lombok.extern.slf4j.Slf4j;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporterRegistry;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Turns a set of seed objects into a self-contained {@link ExportManifest} by walking dependencies
 * to a fixpoint. This is the single, uniform cross-domain traversal (the thing MDS gets for free
 * from being content-neutral): each object is routed to its owning domain via the registry, added
 * to the manifest once (visited by the owner's {@link DomainExporter#identityKey}, so cycles and
 * diamonds are safe), and its dependencies are enqueued — repeating until nothing new is
 * discovered. A dependency its owning domain {@link DomainExporter#exclusions() excludes} is not
 * pulled in: the row is unimportable, so exporting it would only move the failure to the target,
 * and the build manifest would contradict the zip. The referencing row is exported as it is, with a
 * warning naming both.
 */
@Slf4j
public class Selector {
	
	private final DomainExporterRegistry registry;
	
	public Selector(DomainExporterRegistry registry) {
		this.registry = registry;
	}
	
	public ExportManifest select(Collection<? extends OpenmrsObject> seeds) {
		ExportManifest manifest = new ExportManifest();
		Set<String> visited = new HashSet<>();
		Map<DomainExporter<?>, Map<String, String>> exclusionsByOwner = new HashMap<>();
		Deque<Pending> queue = new ArrayDeque<>();
		seeds.forEach(seed -> queue.add(new Pending(seed, null)));
		
		while (!queue.isEmpty()) {
			Pending pending = queue.poll();
			OpenmrsObject instance = HibernateUtil.getRealObjectFromProxy(pending.instance);
			
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
			
			// Seeds were vetted by getAllInstances/getInstancesByUuids; only a dependency can be an excluded row.
			// (Checking seeds too would also make a scoped build load whole tables the seeds never touch.)
			if (pending.referrer != null) {
				String exclusion = exclusionsByOwner.computeIfAbsent(owner, DomainExporter::exclusions)
				        .get(instance.getUuid());
				if (exclusion != null) {
					log.warn(
					    "Metadata Export: {} {} references a {} row that is not exported ({}); the reference is written"
					            + " as it is and cannot resolve on a target that lacks the row",
					    pending.referrerDomain(), pending.referrer.getUuid(), owner.getDomain(), exclusion);
					continue;
				}
			}
			
			manifest.add(owner.getDomain(), identity, instance);
			for (OpenmrsObject dependency : dependenciesOf(owner, instance)) {
				queue.add(new Pending(dependency, instance));
			}
		}
		return manifest;
	}
	
	/** A queued object and the exported row whose dependencies it came from (null for a seed). */
	private final class Pending {
		
		final OpenmrsObject instance;
		
		final OpenmrsObject referrer;
		
		Pending(OpenmrsObject instance, OpenmrsObject referrer) {
			this.instance = instance;
			this.referrer = referrer;
		}
		
		Object referrerDomain() {
			DomainExporter<?> owner = registry.forObject(referrer);
			return owner == null ? referrer.getClass().getSimpleName() : owner.getDomain();
		}
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
