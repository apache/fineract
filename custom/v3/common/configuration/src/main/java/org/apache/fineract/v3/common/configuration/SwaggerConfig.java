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

package org.apache.fineract.v3.common.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class SwaggerConfig {

    @Bean
    @Profile("v3")
    public OpenAPI v3OpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Apache Fineract v3 API").version("v3")
                        .description("This is the v3 API documentation for custom extensions to the Fineract platform.")
                        .contact(new Contact().email("dev@fineract.apache.org"))
                        .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .addServersItem(new Server().url("/fineract-provider/v3"));
    }

    @Bean
    @Profile("api")
    public OpenAPI v1OpenAPI() {
        return new OpenAPI().info(new Info().title("Apache Fineract REST API").version("v1")
                .description("Apache Fineract is a secure, multi-tenanted microfinance platform. "
                        + "The goal of the Apache Fineract API is to empower developers to build apps on top of the Apache Fineract Platform. "
                        + "The https://cui.fineract.dev[reference app] (username: mifos, password: password) works on the same demo tenant as the interactive links in this documentation. "
                        + "Until we complete the new REST API documentation you still have the legacy documentation available [here](https://fineract.apache.org/legacy-docs/apiLive.htm). "
                        + "Please check [the Fineract documentation](https://fineract.apache.org/docs/current) for more information.")
                .contact(new Contact().email("dev@fineract.apache.org"))
                .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .addServersItem(new Server().url("/fineract-provider/api/v1"));
    }

    @Bean
    @Profile("v3")
    public GroupedOpenApi v3Api() {
        return GroupedOpenApi.builder() //
                .group("v3") //
                .packagesToScan("org.apache.fineract.v3") //
                .pathsToMatch("/v3/**") //
                .build();
    }

    @Bean
    @Profile("api")
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder() //
                .group("v1") //
                .packagesToScan("org.apache.fineract") //
                .pathsToMatch("/api/**") //
                .build();
    }
}
