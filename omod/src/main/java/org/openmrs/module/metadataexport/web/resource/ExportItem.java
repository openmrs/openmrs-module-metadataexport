/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.web.resource;

import lombok.Value;

/**
 * One row of an exportable domain as a picker sees it: what a package entry would store
 * ({@code domain}, {@code uuid}) plus the label to show for it. Transient, never persisted: it is
 * built from the domain's exporter on every request, so there is nothing to create or delete.
 */
@Value
public class ExportItem {
	
	String domain;
	
	String uuid;
	
	String display;
	
	boolean retired;
}
