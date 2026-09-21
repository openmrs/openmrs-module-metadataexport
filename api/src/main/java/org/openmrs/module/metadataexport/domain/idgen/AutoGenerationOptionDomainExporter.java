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

import org.openmrs.OpenmrsObject;
import org.openmrs.PatientIdentifierType;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.AutoGenerationOption;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
		// an option is only exportable when the idgen domain also exports its source; enforcing
		// that here keeps future seeding paths from writing a dangling source uuid
		return instance instanceof AutoGenerationOption
		        && IdentifierSourceDomainExporter.exports(((AutoGenerationOption) instance).getSource());
	}
	
	@Override
	public Collection<AutoGenerationOption> getAllInstances() {
		return exportable(allOptions());
	}
	
	@Override
	public Map<String, String> exclusions() {
		return exclusionsOf(allOptions());
	}
	
	/**
	 * The options among the given ones whose source Iniz cannot import, naming the source and its
	 * problem.
	 */
	static Map<String, String> exclusionsOf(Collection<AutoGenerationOption> options) {
		return DomainExporter.exclusions(options, option -> !IdentifierSourceDomainExporter.exports(option.getSource()),
		    option -> "points at identifier source " + option.getSource().getUuid() + ", which "
		            + IdentifierSourceDomainExporter.importProblem(option.getSource()));
	}
	
	private static List<AutoGenerationOption> allOptions() {
		IdentifierSourceService service = Context.getService(IdentifierSourceService.class);
		List<AutoGenerationOption> options = new ArrayList<>();
		for (PatientIdentifierType type : Context.getPatientService().getAllPatientIdentifierTypes(true)) {
			options.addAll(service.getAutoGenerationOptions(type));
		}
		return options;
	}
	
	/** The subset of options that can round-trip through Iniz, in a stable order. */
	static List<AutoGenerationOption> exportable(List<AutoGenerationOption> options) {
		List<AutoGenerationOption> result = new ArrayList<>();
		for (AutoGenerationOption option : options) {
			// a source the idgen exporter drops would leave this row's source uuid dangling
			if (!IdentifierSourceDomainExporter.exports(option.getSource())) {
				continue;
			}
			result.add(option);
		}
		result.sort(Comparator.comparing((AutoGenerationOption o) -> o.getIdentifierType().getName())
		        .thenComparing(o -> o.getLocation() == null ? "" : o.getLocation().getName(),
		            Comparator.nullsFirst(Comparator.naturalOrder()))
		        .thenComparing(AutoGenerationOption::getUuid, Comparator.nullsFirst(Comparator.naturalOrder())));
		return result;
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(AutoGenerationOption instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		dependencies.add(instance.getIdentifierType());
		dependencies.add(instance.getSource());
		if (instance.getLocation() != null) {
			dependencies.add(instance.getLocation());
		}
		return dependencies;
	}
}
