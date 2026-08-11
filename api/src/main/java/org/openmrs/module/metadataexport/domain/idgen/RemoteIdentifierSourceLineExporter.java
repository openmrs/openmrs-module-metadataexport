/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.idgen;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.RemoteIdentifierSource;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.ExportLine;

/**
 * Columns specific to {@link RemoteIdentifierSource} sources.
 */
@Slf4j
public class RemoteIdentifierSourceLineExporter extends BaseLineExporter<IdentifierSource> {
	
	@Override
	public void export(IdentifierSource source, ExportLine line) {
		source = HibernateUtil.getRealObjectFromProxy(source);
		if (!(source instanceof RemoteIdentifierSource)) {
			return;
		}
		
		RemoteIdentifierSource remote = (RemoteIdentifierSource) source;
		if (StringUtils.isBlank(remote.getUser())) {
			log.warn("Idgen: remote identifier source {} has no user; Iniz requires one on import", remote.getUuid());
		}
		line.put(IdentifierSourceLineExporter.HEADER_URL, remote.getUrl());
		line.put(IdentifierSourceLineExporter.HEADER_USER, remote.getUser());
		line.put(IdentifierSourceLineExporter.HEADER_PASS, exportedPassword(remote));
	}
	
	/**
	 * The stored password is a live credential and never leaves the system: Iniz requires the column,
	 * so a per-source {@code property:} indirection is exported instead, resolved from the matching
	 * system/runtime property on the importing server.
	 */
	private String exportedPassword(RemoteIdentifierSource remote) {
		return "property:idgen.remote.password." + remote.getUuid();
	}
}
