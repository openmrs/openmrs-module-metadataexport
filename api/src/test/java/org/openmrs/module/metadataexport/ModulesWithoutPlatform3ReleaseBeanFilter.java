/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.metadataexport;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.PriorityOrdered;

/**
 * Drops the Spring beans that come from the jars of optional modules that have no Platform 3.0
 * release (metadatasharing). Their jars are only on the test classpath to compile and unit test the
 * exporters integrating with them, but context-sensitive tests load every
 * moduleApplicationContext.xml on the classpath and their beans cannot start on Platform 3.0 (e.g.
 * they extend the removed {@code serializationServiceTarget} bean).
 */
public class ModulesWithoutPlatform3ReleaseBeanFilter implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {
	
	private static final List<String> JARS = Arrays.asList("/metadatasharing-api-common-");
	
	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
		for (String beanName : registry.getBeanDefinitionNames()) {
			String resource = registry.getBeanDefinition(beanName).getResourceDescription();
			if (resource != null && JARS.stream().anyMatch(resource::contains)) {
				registry.removeBeanDefinition(beanName);
			}
		}
	}
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
	}
	
	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}
}
