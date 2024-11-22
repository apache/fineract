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
package org.apache.fineract.test.api;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.util.FineractClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FineractClientConfiguration {

    @Autowired
    private ApiProperties apiProperties;

    @Bean
    public FineractClient fineractClient() {
        String baseUrl = apiProperties.getBaseUrl();
        String username = apiProperties.getUsername();
        String password = apiProperties.getPassword();
        String tenantId = apiProperties.getTenantId();
        long readTimeout = apiProperties.getReadTimeout();
        String apiBaseUrl = baseUrl + "/fineract-provider/api/";
        log.info("Using base URL '{}'", apiBaseUrl);
        return FineractClient.builder().readTimeout(Duration.ofSeconds(readTimeout)).basicAuth(username, password).tenant(tenantId)
                .baseURL(apiBaseUrl).insecure(true).build();
    }
}
