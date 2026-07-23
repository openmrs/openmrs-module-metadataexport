/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.location;

import org.apache.commons.lang3.BooleanUtils;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.module.initializer.api.BaseLineProcessor;
import org.openmrs.module.initializer.api.loc.LocationLineProcessor;
import org.openmrs.module.metadataexport.export.ExportLine;
import org.openmrs.module.metadataexport.export.MetadataLineExporter;

public class LocationLineExporter extends MetadataLineExporter<Location> {
	
	public static final String HEADER_PARENT = "parent";
	
	public static final String HEADER_CITY_VILLAGE = "city/village";
	
	public static final String HEADER_COUNTY_DISTRICT = "county/district";
	
	public static final String HEADER_STATE_PROVINCE = "state/province";
	
	public static final String HEADER_POSTAL_CODE = "postal code";
	
	public static final String HEADER_COUNTRY = "country";
	
	public static final String HEADER_ADDRESS_1 = "address 1";
	
	public static final String HEADER_ADDRESS_2 = "address 2";
	
	public static final String HEADER_ADDRESS_3 = "address 3";
	
	public static final String HEADER_ADDRESS_4 = "address 4";
	
	public static final String HEADER_ADDRESS_5 = "address 5";
	
	public static final String HEADER_ADDRESS_6 = "address 6";
	
	@Override
	public void export(Location location, ExportLine line) {
		line.put(BaseLineProcessor.HEADER_NAME, location.getName());
		line.put(BaseLineProcessor.HEADER_DESC, location.getDescription());
		
		Location parentLocation = location.getParentLocation();
		if (parentLocation != null) {
			line.put(HEADER_PARENT, parentLocation.getUuid());
		}
		
		if (location.getTags() != null) {
			for (LocationTag locationTag : location.getTags()) {
				if (BooleanUtils.isTrue(locationTag.getRetired())) {
					continue;
				}
				line.put(LocationLineProcessor.HEADER_TAG_PREFIX + locationTag.getName(), "TRUE");
			}
		}
		
		line.put(HEADER_CITY_VILLAGE, location.getCityVillage());
		line.put(HEADER_COUNTY_DISTRICT, location.getCountyDistrict());
		line.put(HEADER_STATE_PROVINCE, location.getStateProvince());
		line.put(HEADER_POSTAL_CODE, location.getPostalCode());
		line.put(HEADER_COUNTRY, location.getCountry());
		line.put(HEADER_ADDRESS_1, location.getAddress1());
		line.put(HEADER_ADDRESS_2, location.getAddress2());
		line.put(HEADER_ADDRESS_3, location.getAddress3());
		line.put(HEADER_ADDRESS_4, location.getAddress4());
		line.put(HEADER_ADDRESS_5, location.getAddress5());
		line.put(HEADER_ADDRESS_6, location.getAddress6());
	}
}
