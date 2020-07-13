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
package org.apache.fineract.infrastructure.core.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.webjars.WebJarAssetLocator;

@EnableWebMvc
@Configuration
public class WebFrontEndConfiguration implements WebMvcConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(WebFrontEndConfiguration.class);

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = { "classpath:/static/", "classpath:/public/" };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
        }

        WebJarAssetLocator locator = new WebJarAssetLocator();
        String fullPathToSwaggerUiJs = locator.getFullPath("swagger-ui.js");
        LOG.info("Found Swagger UI at {}", fullPathToSwaggerUiJs);
        String fullPathToSwaggerUi = fullPathToSwaggerUiJs.substring(0, fullPathToSwaggerUiJs.lastIndexOf("/") + 1);

        final String[] swaggerResourceLocations = { "classpath:/static/swagger-ui/", "classpath:" + fullPathToSwaggerUi };

        registry.addResourceHandler("/swagger-ui/**").addResourceLocations(swaggerResourceLocations);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/swagger-ui", "/swagger-ui/index.html");
        registry.addRedirectViewController("/swagger-ui/", "/swagger-ui/index.html");
    }
}
