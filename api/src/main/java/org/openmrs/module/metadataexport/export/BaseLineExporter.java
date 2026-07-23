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

import org.openmrs.OpenmrsObject;

public abstract class BaseLineExporter<T extends OpenmrsObject> {
	
	public static final String VERSION_LHS = "_version:";
	
	/** Domain-specific columns for one instance. Subclasses implement this. */
	public abstract void export(T instance, ExportLine line);
	
	/**
	 * Framework entry point. Defaults to {@link #export}; wrappers like {@link MetadataLineExporter}
	 * override.
	 */
	public void writeLine(T instance, ExportLine line) {
		export(instance, line);
	}
	
}
