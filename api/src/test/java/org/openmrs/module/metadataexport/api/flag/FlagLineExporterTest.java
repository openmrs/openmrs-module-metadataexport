/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.flag;

import org.junit.jupiter.api.Test;
import org.openmrs.module.metadataexport.api.export.ExportLine;
import org.openmrs.module.patientflags.Flag;
import org.openmrs.module.patientflags.Priority;
import org.openmrs.module.patientflags.Tag;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FlagLineExporterTest {
	
	@Test
	void exportsAllFieldsForActiveFlag() {
		Flag flag = new Flag();
		flag.setUuid("e168c141-f5fd-4eec-bd3e-633bed1c9606");
		flag.setName("HIV Positive");
		flag.setCriteria("SELECT c.patient_id FROM condition c where c.condition_name = 'HIV'");
		flag.setEvaluator("org.openmrs.module.patientflags.evaluator.SqlFlagEvaluator");
		flag.setMessage("patientflags.message.hivPositive");
		flag.setEnabled(true);
		flag.setDescription("Flag for HIV positive patients");
		
		Priority priority = new Priority();
		priority.setName("High Priority");
		flag.setPriority(priority);
		
		Set<Tag> tags = new HashSet<>();
		Tag hiv = new Tag();
		hiv.setName("HIV");
		Tag clinical = new Tag();
		clinical.setName("Clinical");
		tags.add(hiv);
		tags.add(clinical);
		flag.setTags(tags);
		
		ExportLine line = new ExportLine();
		new FlagLineExporter().writeLine(flag, line);
		
		assertEquals("e168c141-f5fd-4eec-bd3e-633bed1c9606", line.get("uuid"));
		assertEquals("HIV Positive", line.get("name"));
		assertEquals("SELECT c.patient_id FROM condition c where c.condition_name = 'HIV'", line.get("criteria"));
		assertEquals("org.openmrs.module.patientflags.evaluator.SqlFlagEvaluator", line.get("evaluator"));
		assertEquals("patientflags.message.hivPositive", line.get("message"));
		assertEquals("High Priority", line.get("priority"));
		assertEquals("true", line.get("enabled"));
		assertEquals("Clinical;HIV", line.get("tags"));
		assertEquals("Flag for HIV positive patients", line.get("description"));
		assertNull(line.get("void/retire"));
	}
	
	@Test
	void retiredFlagEmitsUuidAndFlagOnly() {
		Flag flag = new Flag();
		flag.setUuid("g38ae363-h7hf-6gge-df5g-855dfg3e1828");
		flag.setName("Retired Flag");
		flag.setCriteria("SELECT a.patient_id FROM allergy a where a.allergen_type = 'DOGS'");
		flag.setEvaluator("org.openmrs.module.patientflags.evaluator.SqlFlagEvaluator");
		flag.setMessage("Retired message");
		flag.setEnabled(true);
		flag.setRetired(true);
		flag.setDescription("This flag should be retired");
		
		Priority priority = new Priority();
		priority.setName("Low Priority");
		flag.setPriority(priority);
		
		ExportLine line = new ExportLine();
		new FlagLineExporter().writeLine(flag, line);
		
		assertEquals("g38ae363-h7hf-6gge-df5g-855dfg3e1828", line.get("uuid"));
		assertEquals("true", line.get("void/retire"));
		assertNull(line.get("name"), "retired rows carry only uuid + flag");
		assertNull(line.get("priority"), "retired rows carry only uuid + flag");
	}
	
	@Test
	void flagWithNoTagsOmitsTagsColumn() {
		Flag flag = new Flag();
		flag.setUuid("f279d252-g6ge-5ffd-ce4f-744cef2d0717");
		flag.setName("Test Flag");
		flag.setCriteria("SELECT a.patient_id FROM allergy a");
		flag.setEvaluator("org.openmrs.module.patientflags.evaluator.SqlFlagEvaluator");
		flag.setMessage("Test message");
		flag.setEnabled(true);
		flag.setTags(new HashSet<>());
		
		ExportLine line = new ExportLine();
		new FlagLineExporter().writeLine(flag, line);
		
		assertEquals("Test Flag", line.get("name"));
		assertNull(line.get("tags"));
	}
}
