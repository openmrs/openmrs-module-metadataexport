/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.appointment;

import org.openmrs.OpenmrsObject;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.service.AppointmentServiceDefinitionService;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@OpenmrsProfile(modules = "appointments:1.2.1 - 9.*")
public class AppointmentServiceTypeDomainExporter extends CsvDomainExporter<AppointmentServiceType> {
	
	@Override
	protected List<BaseLineExporter<AppointmentServiceType>> chain() {
		return Collections.singletonList(new AppointmentServiceTypeLineExporter());
	}
	
	@Override
	protected String fileName() {
		return "appointmentServiceTypes.csv";
	}
	
	@Override
	public Domain getDomain() {
		return Domain.APPOINTMENT_SERVICE_TYPES;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof AppointmentServiceType;
	}
	
	@Override
	public Collection<AppointmentServiceType> getAllInstances() {
		List<AppointmentServiceType> types = new ArrayList<>();
		for (AppointmentServiceDefinition definition : Context.getService(AppointmentServiceDefinitionService.class)
		        .getAllAppointmentServices(false)) {
			types.addAll(definition.getServiceTypes(false));
		}
		return types;
	}
	
	@Override
	public Map<String, String> exclusions() {
		List<AppointmentServiceType> all = new ArrayList<>();
		for (AppointmentServiceDefinition definition : Context.getService(AppointmentServiceDefinitionService.class)
		        .getAllAppointmentServices(true)) {
			all.addAll(definition.getServiceTypes(true));
		}
		return DomainExporter.exclusions(all, type -> type.getVoided() || type.getAppointmentServiceDefinition().getVoided(),
		    type -> type.getVoided() ? "('" + type.getName() + "') is voided"
		            : "('" + type.getName() + "') belongs to voided service definition "
		                    + type.getAppointmentServiceDefinition().getUuid());
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(AppointmentServiceType instance) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		dependencies.add(instance.getAppointmentServiceDefinition());
		return dependencies;
	}
}
