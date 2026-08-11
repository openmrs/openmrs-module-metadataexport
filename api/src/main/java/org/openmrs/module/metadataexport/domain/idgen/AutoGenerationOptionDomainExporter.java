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
import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.idgen.AutoGenerationOption;
import org.openmrs.module.idgen.IdentifierPool;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.RemoteIdentifierSource;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
@OpenmrsProfile(modules = { "idgen:4.6.* - 9.*" })
public class AutoGenerationOptionDomainExporter extends CsvDomainExporter<AutoGenerationOption> {
	
	@Override
	protected List<BaseLineExporter<AutoGenerationOption>> chain() {
		return Collections.singletonList(new AutoGenerationOptionLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "autoGenerationOptions.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.AUTO_GENERATION_OPTIONS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof AutoGenerationOption;
	}
	
	@Override
	public Collection<AutoGenerationOption> getAllInstances() {
		IdentifierSourceService service = Context.getService(IdentifierSourceService.class);
		List<AutoGenerationOption> options = new ArrayList<>();
		for (PatientIdentifierType type : Context.getPatientService().getAllPatientIdentifierTypes(true)) {
			List<AutoGenerationOption> forType = service.getAutoGenerationOptions(type);
			if (forType != null) {
				options.addAll(forType);
			}
		}
		return exportable(options);
	}
	
	/** The subset of options that can round-trip through Iniz, in a stable order. */
	static List<AutoGenerationOption> exportable(List<AutoGenerationOption> options) {
		List<AutoGenerationOption> result = new ArrayList<>();
		for (AutoGenerationOption option : options) {
			if (BooleanUtils.isTrue(option.getRetired())) {
				continue;
			}
			IdentifierSource source = HibernateUtil.getRealObjectFromProxy(option.getSource());
			if (source != null && !(source instanceof SequentialIdentifierGenerator)
			        && !(source instanceof RemoteIdentifierSource) && !(source instanceof IdentifierPool)) {
				log.warn("Idgen: skipping auto generation option {} whose source {} has unsupported type {}",
				    option.getUuid(), source.getUuid(), source.getClass().getName());
				continue;
			}
			result.add(option);
		}
		result.sort(Comparator
		        .comparing(
		            (AutoGenerationOption o) -> o.getIdentifierType() == null || o.getIdentifierType().getName() == null ? ""
		                    : o.getIdentifierType().getName())
		        .thenComparing(o -> o.getLocation() == null ? "" : o.getLocation().getName(),
		            Comparator.nullsFirst(Comparator.naturalOrder()))
		        .thenComparing(AutoGenerationOption::getUuid, Comparator.nullsFirst(Comparator.naturalOrder())));
		return result;
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(AutoGenerationOption instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		if (instance.getIdentifierType() != null) {
			dependencies.add(instance.getIdentifierType());
		}
		if (instance.getSource() != null) {
			dependencies.add(instance.getSource());
		}
		if (instance.getLocation() != null) {
			dependencies.add(instance.getLocation());
		}
		return dependencies;
	}
}
