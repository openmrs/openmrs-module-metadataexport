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

import org.openmrs.Location;
import org.openmrs.module.appointments.model.AppointmentServiceDefinition;
import org.openmrs.module.appointments.model.Speciality;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.ExportLine;

import java.sql.Time;
import java.time.format.DateTimeFormatter;

public class AppointmentServiceDefinitionLineExporter extends BaseLineExporter<AppointmentServiceDefinition> {
	
	public static final String HEADER_SPECIALITY = "speciality";
	
	public static final String HEADER_LOCATION = "location";
	
	public static final String HEADER_LABEL_COLOUR = "label colour";
	
	@Override
	public void export(AppointmentServiceDefinition instance, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_UUID, instance.getUuid());
		line.put(BaseLineProcessor.HEADER_NAME, instance.getName());
		line.put(BaseLineProcessor.HEADER_DESC, instance.getDescription());
		line.put(BaseLineProcessor.HEADER_DURATION, instance.getDurationMins());
		
		line.put(BaseLineProcessor.HEADER_START_TIME, getFormattedTime(instance.getStartTime()));
		line.put(BaseLineProcessor.HEADER_END_TIME, getFormattedTime(instance.getEndTime()));
		line.put(BaseLineProcessor.HEADER_MAX_LOAD, instance.getMaxAppointmentsLimit());
		
		line.put(HEADER_LABEL_COLOUR, instance.getColor());
		
		Speciality speciality = instance.getSpeciality();
		if (speciality != null) {
			line.put(HEADER_SPECIALITY, speciality.getUuid());
		}
		
		Location location = instance.getLocation();
		if (location != null) {
			line.put(HEADER_LOCATION, location.getUuid());
		}
	}
	
	private String getFormattedTime(Time time) {
		return time == null ? null : time.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
	}
	
}
