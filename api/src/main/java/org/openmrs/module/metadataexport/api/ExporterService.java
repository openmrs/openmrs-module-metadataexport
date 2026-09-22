/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api;

import org.openmrs.OpenmrsObject;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.select.ExportManifest;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public interface ExporterService {
	
	/**
	 * Export the given domains under {@code outDir} (writing a {@code configuration/} tree). A null or
	 * empty {@code domains} exports every registered domain. The service holds a registry of
	 * {@link org.openmrs.module.metadataexport.export.DomainExporter}s and contains no per-domain logic
	 * itself.
	 */
	void export(File outDir, Collection<Domain> domains) throws IOException;
	
	ExportManifest exportSeeds(File outDir, Collection<? extends OpenmrsObject> seeds) throws IOException;
	
	/**
	 * Rows of the given domains that exist on this server but are never exported, with the reason, as
	 * each {@link org.openmrs.module.metadataexport.export.DomainExporter#exclusions()} reports them;
	 * domains without any are left out. Logged once per domain, so a full-domain export that leaves
	 * rows behind says so somewhere other than a build that names them. Every domain passed must have a
	 * registered exporter.
	 */
	Map<Domain, Map<String, String>> exclusions(Collection<Domain> domains);
	
}
