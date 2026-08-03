/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.api.db.hibernate;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.module.metadataexport.api.db.MetadataExportDao;
import org.openmrs.module.metadataexport.api.model.ExportBuild;
import org.openmrs.module.metadataexport.api.model.ExportPackage;
import org.openmrs.module.metadataexport.api.model.ExportStatus;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class HibernateMetadataExportDao implements MetadataExportDao {
	
	private final SessionFactory sessionFactory;
	
	@Override
	public ExportPackage savePackage(ExportPackage exportPackage) {
		sessionFactory.getCurrentSession().saveOrUpdate(exportPackage);
		return exportPackage;
	}
	
	@Override
	public ExportPackage getPackageByUuid(String uuid) {
		TypedQuery<ExportPackage> query = sessionFactory.getCurrentSession()
		        .createQuery("from ExportPackage pkg where  pkg.uuid = :uuid", ExportPackage.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	@Override
	public ExportPackage getPackageByName(String name) {
		TypedQuery<ExportPackage> query = sessionFactory.getCurrentSession().createQuery(
		    "from ExportPackage pkg where pkg.name = :name and pkg.retired = false", ExportPackage.class);
		query.setParameter("name", name);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	@Override
	public List<ExportPackage> getAllPackages(boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<ExportPackage> cq = cb.createQuery(ExportPackage.class);
		Root<ExportPackage> root = cq.from(ExportPackage.class);
		
		List<Predicate> predicates = new ArrayList<>();
		
		if (!includeRetired) {
			predicates.add(cb.isFalse(root.get("retired")));
		}
		
		if (!predicates.isEmpty()) {
			cq.where(predicates.toArray(new Predicate[0]));
		}
		
		TypedQuery<ExportPackage> query = session.createQuery(cq);
		return query.getResultList();
	}
	
	@Override
	public ExportBuild saveBuild(ExportBuild exportBuild) {
		sessionFactory.getCurrentSession().saveOrUpdate(exportBuild);
		return exportBuild;
	}
	
	@Override
	public ExportBuild getBuildByUuid(String uuid) {
		TypedQuery<ExportBuild> query = sessionFactory.getCurrentSession()
		        .createQuery("from ExportBuild build where build.uuid = :uuid", ExportBuild.class);
		query.setParameter("uuid", uuid);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	@Override
	public List<ExportBuild> getBuilds(ExportPackage exportPackage) {
		TypedQuery<ExportBuild> query = sessionFactory.getCurrentSession().createQuery(
		    "from ExportBuild build where build.exportPackage = :exportPackage order by build.version desc",
		    ExportBuild.class);
		query.setParameter("exportPackage", exportPackage);
		return query.getResultList();
	}
	
	@Override
	public ExportBuild getLatestBuild(ExportPackage exportPackage) {
		TypedQuery<ExportBuild> query = sessionFactory.getCurrentSession().createQuery(
		    "from ExportBuild build where build.exportPackage = :exportPackage order by build.version desc",
		    ExportBuild.class);
		query.setParameter("exportPackage", exportPackage);
		query.setMaxResults(1);
		return query.getResultStream().findFirst().orElse(null);
	}
	
	@Override
	public List<ExportBuild> getActiveBuilds() {
		TypedQuery<ExportBuild> query = sessionFactory.getCurrentSession()
		        .createQuery("from ExportBuild build where build.exportStatus in (:statuses)", ExportBuild.class);
		query.setParameter("statuses", Arrays.asList(ExportStatus.QUEUED, ExportStatus.RUNNING));
		return query.getResultList();
	}
}
