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

package org.apache.fineract.infrastructure.core.jersey;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

    JerseyConfig() {
        register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
    }

    @Autowired
    ApplicationContext appCtx;

    @PostConstruct
    public void setup() {
        appCtx.getBeansWithAnnotation(Path.class).values().forEach(component -> register(component.getClass()));

        appCtx.getBeansWithAnnotation(Provider.class).values().forEach(this::register);
    }
}
