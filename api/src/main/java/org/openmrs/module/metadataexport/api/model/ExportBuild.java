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
import org.openmrs.BaseOpenmrsData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

@Entity
@Table(name = "metadataexport_build", uniqueConstraints = @UniqueConstraint(columnNames = { "package_id", "version" }))
@Getter
@Setter
public class ExportBuild extends BaseOpenmrsData {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "build_id")
	private Integer buildId;
	
	@ManyToOne
	@JoinColumn(name = "package_id")
	private ExportPackage exportPackage;
	
	@Column(name = "version")
	private Integer version;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "export_status", length = 16)
	private ExportStatus exportStatus;
	
	@Column(name = "date_started")
	private Date dateStarted;
	
	@Column(name = "date_completed")
	private Date dateCompleted;
	
	@Column(name = "zip_path", length = 512)
	private String zipPath;
	
	@Lob
	@Column(name = "error_message")
	private String errorMessage;
	
	@Lob
	@Column(name = "manifest_json")
	private String manifestJson;
	
	@Override
	public Integer getId() {
		return getBuildId();
	}
	
	@Override
	public void setId(Integer id) {
		setBuildId(id);
	}
}
