/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.model;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.BaseChangeableOpenmrsMetadata;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "metadataexport_package")
@Getter
@Setter
public class ExportPackage extends BaseChangeableOpenmrsMetadata {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "package_id")
	private Integer packageId;
	
	@OneToMany(mappedBy = "exportPackage", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ExportPackageEntry> entries = new ArrayList<>();
	
	@Override
	public Integer getId() {
		return getPackageId();
	}
	
	@Override
	public void setId(Integer id) {
		setPackageId(id);
	}
}
