/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.core.api.mvc;

import jakarta.ws.rs.Path;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.commands.annotation.CommandType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Excludes JAX-RS beans that are already migrated to MVC based on their annotations.
 */
@ProfileMvc
@Slf4j
@Component
public class MvcDuplicateBeanFilter implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final HashMap<String, String> mvcPath = new HashMap<>();
        final HashMap<String, String> jerseyPath = new HashMap<>();
        final HashMap<CommandType, String> mvcCommand = new HashMap<>();
        final HashMap<CommandType, String> jerseyCommand = new HashMap<>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            AbstractBeanDefinition beanDefinition = ((AbstractBeanDefinition) beanFactory.getBeanDefinition(beanName));
            final Class<?> beanClass;
            try {
                beanClass = beanDefinition.getBeanClassName() != null ? Class.forName(beanDefinition.getBeanClassName()) : null;
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
            if (beanClass == null) {
                continue;
            }

            if (beanClass.isAnnotationPresent(RequestMapping.class)) {
                mvcPath.put(beanClass.getAnnotation(RequestMapping.class).value()[0], beanName);
            } else if (beanClass.isAnnotationPresent(Path.class)) {
                jerseyPath.put(beanClass.getAnnotation(Path.class).value(), beanName);
            }
            if (beanClass.isAnnotationPresent(CommandType.class)) {
                if (beanClass.isAnnotationPresent(ProfileMvc.class)) {
                    mvcCommand.put(beanClass.getAnnotation(CommandType.class), beanName);
                } else {
                    jerseyCommand.put(beanClass.getAnnotation(CommandType.class), beanName);
                }
            }
        }

        mvcPath.forEach((path, beanName) -> {
            final String jerseyBeanName = jerseyPath.get(path);
            if (jerseyBeanName != null) {
                log.info("Removing bean: {} with path: {} and request mapping: {}", jerseyBeanName, path, path);
                ((DefaultListableBeanFactory) beanFactory).removeBeanDefinition(jerseyBeanName);
            }
        });
        mvcCommand.forEach((command, beanName) -> {
            final String jerseyBeanName = jerseyCommand.get(command);
            if (jerseyBeanName != null) {
                log.info("Removing bean: {} with command: {}", jerseyBeanName, command);
                ((DefaultListableBeanFactory) beanFactory).removeBeanDefinition(jerseyBeanName);
            }
        });
    }
}
