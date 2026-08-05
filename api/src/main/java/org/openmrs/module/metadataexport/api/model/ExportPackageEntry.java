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
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.module.initializer.Domain;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "metadataexport_package_entry")
@Getter
@Setter
public class ExportPackageEntry extends BaseOpenmrsObject {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "entry_id")
	private Integer entryId;
	
	@ManyToOne
	@JoinColumn(name = "package_id")
	private ExportPackage exportPackage;
	
	@Column(name = "domain")
	private String domain;
	
	@ElementCollection
	@CollectionTable(name = "metadataexport_package_entry_item", joinColumns = @JoinColumn(name = "entry_id"), uniqueConstraints = @UniqueConstraint(columnNames = {
	        "entry_id", "item_uuid" }))
	@Column(name = "item_uuid", length = 38, nullable = false)
	private List<String> itemUuids = new ArrayList<>(); // Empty = whole domain
	
	public Domain getDomainEnum() {
		return Domain.valueOf(domain);
	}
	
	@Override
	public Integer getId() {
		return getEntryId();
	}
	
	@Override
	public void setId(Integer id) {
		setEntryId(id);
	}
	
}
