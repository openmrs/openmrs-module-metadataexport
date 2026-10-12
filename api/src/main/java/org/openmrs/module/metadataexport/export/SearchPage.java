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

import lombok.Value;
import org.openmrs.OpenmrsObject;

import java.util.List;

/**
 * One page of a picker search with the size of the whole result, from
 * {@link DomainExporter#search(String, boolean, int, int)}. The two come together because the
 * picker asks for both on every request: a domain that scans its rows in memory would otherwise
 * scan, label and sort them twice per keystroke (and the AMPATH form domains would run their file
 * scan twice), and a domain that pages in the database can run its page and count queries side by
 * side.
 */
@Value
public class SearchPage<T extends OpenmrsObject> {
	
	/** The rows from {@code startIndex}, at most {@code limit} of them; empty past the end. */
	List<T> rows;
	
	/** How many rows the search has in all, for the picker's paging links. */
	long total;
}
