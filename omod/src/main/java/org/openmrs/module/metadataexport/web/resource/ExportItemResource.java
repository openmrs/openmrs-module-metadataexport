/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.web.resource;

import org.apache.commons.lang3.EnumUtils;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.MetadataExportConstants;
import org.openmrs.module.metadataexport.export.DomainExporter;
import org.openmrs.module.metadataexport.export.DomainExporterRegistry;
import org.openmrs.module.metadataexport.web.controller.MetadataExportRestConstants;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.Searchable;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingReadableResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * {@code /ws/rest/v1/metadataexport/items?domain=LOCATIONS&q=loc}: the rows of one exportable
 * domain a picker can choose from, paged, in the order the domain's exporter ranks them (see
 * {@link DomainExporter#search}). {@code GET /items/<uuid>?domain=<DOMAIN>} resolves one saved uuid
 * back to its label, which is how a UI renders a saved package's entries. Items are not stored, so
 * there is nothing to create, update or delete, and there is no unfiltered list: the domain is the
 * only selector, so a request without one is refused (400) and an unknown or unsupported domain is
 * not found (404), as an unknown package is for builds. Retired rows carry {@code retired: true}
 * and are left out of a search unless {@code includeAll=true}. Resolving finds any row the domain
 * exports, retired or not; a row the domain excludes is not found (404) with the reason the build
 * would give, so a UI can tell "excluded, and why" from "deleted". Paging is the exporter's
 * ({@link DomainExporter#search(String, boolean, int, int)}), so a dictionary is never loaded
 * whole.
 * <p>
 * The resource never touches {@code MetadataExportService}, so it checks the Get privilege itself,
 * throwing what {@code @Authorized} throws: the REST base controller maps only
 * {@link APIAuthenticationException} to 401/403, and would answer the
 * {@code ContextAuthenticationException} of {@code Context.requirePrivilege} with a 500. The
 * exporters then run as the caller, so a domain also needs the read privilege its own service
 * requires: from {@code @Authorized} where the exporter reads through a core or module service, and
 * from the exporter's own check where it reads a table directly (concept sets, reference ranges,
 * the blank concept listing, the two fhir2 domains). The cohort module declares no read privileges,
 * so its two domains need only the Get privilege.
 */
@Resource(name = MetadataExportRestConstants.NAMESPACE
        + "/items", supportedClass = ExportItem.class, supportedOpenmrsVersions = {
                MetadataExportRestConstants.SUPPORTED_OPENMRS_VERSIONS })
public class ExportItemResource extends BaseDelegatingReadableResource<ExportItem> implements Searchable {
	
	private static final String DOMAIN_REQUIRED = "Name the domain with ?domain=<DOMAIN>; search within it with &q=<text>";
	
	@Override
	public ExportItem getByUniqueId(String uuid) {
		throw new ResourceDoesNotSupportOperationException(DOMAIN_REQUIRED);
	}
	
	@Override
	public ExportItem newDelegate() {
		throw new ResourceDoesNotSupportOperationException("Items are read-only");
	}
	
	@Override
	public Object retrieve(String uuid, RequestContext context) throws ResponseException {
		if (!Context.hasPrivilege(MetadataExportConstants.GET_PRIVILEGE)) {
			throw new APIAuthenticationException("Privilege required: " + MetadataExportConstants.GET_PRIVILEGE);
		}
		DomainExporter<OpenmrsObject> exporter = exporter(context);
		for (OpenmrsObject row : exporter.candidatesFor(Collections.singleton(uuid))) {
			if (uuid.equals(row.getUuid())) {
				return asRepresentation(toItem(exporter, row), context.getRepresentation());
			}
		}
		String reason = exporter.exclusions().get(uuid);
		throw new ObjectNotFoundException(reason == null ? "No row " + uuid + " in domain " + exporter.getDomain()
		        : "Not exportable from domain " + exporter.getDomain() + ": " + reason);
	}
	
	@Override
	public PageableResult doGetAll(RequestContext context) {
		throw new ResourceDoesNotSupportOperationException(DOMAIN_REQUIRED);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		if (!Context.hasPrivilege(MetadataExportConstants.GET_PRIVILEGE)) {
			throw new APIAuthenticationException("Privilege required: " + MetadataExportConstants.GET_PRIVILEGE);
		}
		DomainExporter<OpenmrsObject> exporter = exporter(context);
		String q = context.getParameter("q");
		boolean includeRetired = Boolean.TRUE.equals(context.getIncludeAll());
		int startIndex = RestPaging.startIndex(context);
		List<ExportItem> page = new ArrayList<>();
		for (OpenmrsObject row : exporter.search(q, includeRetired, startIndex, context.getLimit())) {
			page.add(toItem(exporter, row));
		}
		long total = exporter.getCountOfSearch(q, includeRetired);
		return new AlreadyPaged<>(context, page, RestPaging.hasMore(startIndex, page.size(), total), total);
	}
	
	/** One shape for every representation: a picker needs nothing more, and lists are refs. */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		if (rep instanceof RefRepresentation || rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("domain");
			description.addProperty("retired");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * The self link must carry the domain, or it would not resolve (uuids are only unique per table).
	 */
	@Override
	public String getUri(Object delegate) {
		return super.getUri(delegate) + "?domain=" + ((ExportItem) delegate).getDomain();
	}
	
	/**
	 * The exporter the request names. Resources are instantiated reflectively by the REST module, not
	 * as Spring beans, hence the registry lookup instead of injection. The cast is sound because the
	 * rows an exporter hands out are the rows it accepts back, which is all this resource does with it.
	 */
	@SuppressWarnings("unchecked")
	private static DomainExporter<OpenmrsObject> exporter(RequestContext context) {
		String name = context.getParameter("domain");
		if (name == null) {
			throw new ResourceDoesNotSupportOperationException(DOMAIN_REQUIRED);
		}
		List<DomainExporterRegistry> registries = Context.getRegisteredComponents(DomainExporterRegistry.class);
		if (registries.isEmpty()) {
			throw new IllegalStateException(
			        "DomainExporterRegistry is not registered in the Spring context; is the module started?");
		}
		Domain domain = EnumUtils.getEnum(Domain.class, name.trim().toUpperCase(Locale.ROOT));
		DomainExporter<?> exporter = domain == null ? null : registries.get(0).forDomain(domain);
		if (exporter == null) {
			throw new ObjectNotFoundException("No exportable domain '" + name + "'");
		}
		return (DomainExporter<OpenmrsObject>) exporter;
	}
	
	private static ExportItem toItem(DomainExporter<OpenmrsObject> exporter, OpenmrsObject row) {
		return new ExportItem(exporter.getDomain().name(), row.getUuid(), exporter.display(row),
		        exporter.isRetiredOrVoided(row));
	}
}
