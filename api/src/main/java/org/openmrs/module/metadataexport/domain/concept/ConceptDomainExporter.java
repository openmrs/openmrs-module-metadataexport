/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport.domain.concept;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptAttributeType;
import org.openmrs.ConceptName;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.ConceptSearchResult;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.Domain;
import org.openmrs.module.metadataexport.export.BaseLineExporter;
import org.openmrs.module.metadataexport.export.CsvDomainExporter;
import org.openmrs.util.LocaleUtility;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ConceptDomainExporter extends CsvDomainExporter<Concept> {
	
	@Override
	public Domain getDomain() {
		return Domain.CONCEPTS;
	}
	
	@Override
	public boolean handles(OpenmrsObject instance) {
		return instance instanceof Concept;
	}
	
	@Override
	public Collection<Concept> getAllInstances() {
		return Context.getConceptService().getAllConcepts();
	}
	
	/** A concept has no name column of its own; the picker shows its preferred name. */
	@Override
	public String display(Concept concept) {
		return displayName(concept);
	}
	
	/**
	 * The preferred name of {@code concept} in the user's locale order, or its uuid when it has none;
	 * shared with the domains whose rows are identified by a concept (workflows, states, sets, ranges).
	 */
	public static String displayName(Concept concept) {
		ConceptName name = concept.getName();
		return name != null && StringUtils.isNotBlank(name.getName()) ? name.getName() : concept.getUuid();
	}
	
	/**
	 * Pages in the database, since a dictionary is too large to hold in memory per request: a blank
	 * query walks the concepts in id order (the only order that needs no name join), a search uses the
	 * concept name index in its relevance order. Every allowed locale is searched, since the locale a
	 * name was entered in need not be the picker user's own.
	 */
	@Override
	public List<Concept> search(String q, boolean includeRetired, int startIndex, int limit) {
		if (StringUtils.isBlank(q)) {
			return session()
			        .createQuery("from Concept c" + liveOnly(includeRetired) + " order by c.conceptId", Concept.class)
			        .setFirstResult(startIndex).setMaxResults(limit).list();
		}
		List<Concept> page = new ArrayList<>();
		for (ConceptSearchResult result : Context.getConceptService().getConcepts(q.trim(), locales(), includeRetired, null,
		    null, null, null, null, startIndex, limit)) {
			page.add(result.getConcept());
		}
		return page;
	}
	
	@Override
	public long getCountOfSearch(String q, boolean includeRetired) {
		if (StringUtils.isBlank(q)) {
			return session().createQuery("select count(c) from Concept c" + liveOnly(includeRetired), Long.class)
			        .uniqueResult();
		}
		return Context.getConceptService().getCountOfConcepts(q.trim(), locales(), includeRetired, null, null, null, null,
		    null);
	}
	
	private static Session session() {
		return Context.getRegisteredComponent("sessionFactory", SessionFactory.class).getCurrentSession();
	}
	
	private static String liveOnly(boolean includeRetired) {
		return includeRetired ? "" : " where c.retired = false";
	}
	
	private static List<Locale> locales() {
		return new ArrayList<>(LocaleUtility.getLocalesInOrder());
	}
	
	/** Every concept is exportable, so a package naming a few need not load the dictionary. */
	@Override
	public Collection<Concept> candidatesFor(Collection<String> uuids) {
		ConceptService service = Context.getConceptService();
		return uuids.stream().map(service::getConceptByUuid).filter(Objects::nonNull).collect(Collectors.toList());
	}
	
	@Override
	public Collection<? extends OpenmrsObject> getDependencies(Concept concept) {
		List<OpenmrsObject> dependencies = new ArrayList<>();
		for (ConceptAnswer answer : concept.getAnswers()) {
			dependencies.add(answer.getAnswerConcept());
		}
		dependencies.addAll(concept.getConceptSets());
		dependencies.add(concept.getConceptClass());
		
		concept.getConceptMappings().forEach(mapping -> {
			dependencies.add(mapping.getConceptReferenceTerm().getConceptSource());
			dependencies.add(mapping.getConceptMapType());
		});
		
		concept.getActiveAttributes().forEach(conceptAttribute -> {
			ConceptAttributeType attributeType = conceptAttribute.getAttributeType();
			dependencies.add(attributeType);
			if (StringUtils.isNotBlank(attributeType.getDatatypeClassname())) {
				Object value = conceptAttribute.getValue();
				if (value instanceof OpenmrsObject) {
					dependencies.add((OpenmrsObject) value);
				}
			}
		});
		
		return dependencies;
	}
	
	@Override
	protected List<BaseLineExporter<Concept>> chain() {
		return Arrays.asList(new ConceptLineExporter(), new ConceptNumericExporter(), new ConceptComplexExporter(),
		    new NestedConceptExporter(), new MappingsConceptExporter(), new ConceptAttributeExporter());
	}
	
	@Override
	protected String fileName() {
		return "concepts.csv";
	}
}
