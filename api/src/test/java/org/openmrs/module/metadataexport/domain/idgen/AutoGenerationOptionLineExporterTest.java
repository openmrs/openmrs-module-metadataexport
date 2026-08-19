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

import org.junit.jupiter.api.Test;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.idgen.AutoGenerationOption;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.metadataexport.export.ExportLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AutoGenerationOptionLineExporterTest {
	
	private static AutoGenerationOption option() {
		PatientIdentifierType type = new PatientIdentifierType();
		type.setUuid("a5d38e09-efcb-4d91-a526-50ce1ba5011a");
		
		SequentialIdentifierGenerator source = new SequentialIdentifierGenerator();
		source.setUuid("c1d8a345-3f10-11e4-adec-0800271c1b75");
		
		AutoGenerationOption option = new AutoGenerationOption();
		option.setUuid("f5c1f1b2-3f10-11e4-adec-0800271c1b75");
		option.setIdentifierType(type);
		option.setSource(source);
		return option;
	}
	
	@Test
	void exportsAllColumnsWithUuidReferences() {
		AutoGenerationOption option = option();
		Location location = new Location();
		location.setUuid("8d6c993e-c2cc-11de-8d13-0010c6dffd0f");
		option.setLocation(location);
		option.setManualEntryEnabled(false);
		option.setAutomaticGenerationEnabled(true);
		
		ExportLine line = new ExportLine();
		new AutoGenerationOptionLineExporter().writeLine(option, line);
		
		assertEquals("f5c1f1b2-3f10-11e4-adec-0800271c1b75", line.get("uuid"));
		assertEquals("a5d38e09-efcb-4d91-a526-50ce1ba5011a", line.get("identifier type"));
		assertEquals("8d6c993e-c2cc-11de-8d13-0010c6dffd0f", line.get("location"));
		assertEquals("c1d8a345-3f10-11e4-adec-0800271c1b75", line.get("identifier source"));
		assertEquals("false", line.get("manual entry enabled"));
		assertEquals("true", line.get("auto generation enabled"));
	}
	
	@Test
	void omitsLocationWhenUnsetButAlwaysEmitsBothBooleans() {
		AutoGenerationOption option = option();
		
		ExportLine line = new ExportLine();
		new AutoGenerationOptionLineExporter().writeLine(option, line);
		
		assertNull(line.get("location"), "unset location is not written as a column");
		assertEquals("true", line.get("manual entry enabled"));
		assertEquals("false", line.get("auto generation enabled"));
	}
	
	@Test
	void neverEmitsVoidRetireColumn() {
		AutoGenerationOption option = option();
		option.setRetired(true);
		
		ExportLine line = new ExportLine();
		new AutoGenerationOptionLineExporter().writeLine(option, line);
		
		assertNull(line.get("void/retire"));
		assertEquals("a5d38e09-efcb-4d91-a526-50ce1ba5011a", line.get("identifier type"));
		assertEquals("c1d8a345-3f10-11e4-adec-0800271c1b75", line.get("identifier source"));
	}
}
