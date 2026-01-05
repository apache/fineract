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

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class FineractClientConfiguration {

    private final ApiProperties apiProperties;

    @Bean
    public FineractFeignClient fineractFeignClient() {
        String baseUrl = apiProperties.getBaseUrl();
        String username = apiProperties.getUsername();
        String password = apiProperties.getPassword();
        String tenantId = apiProperties.getTenantId();
        long readTimeout = apiProperties.getReadTimeout();
        String apiBaseUrl = baseUrl + "/fineract-provider/api/";
        boolean debugEnabled = Boolean.parseBoolean(System.getProperty("fineract.feign.debug", "false"));

        FineractFeignClient client = FineractFeignClient.builder().baseUrl(apiBaseUrl).credentials(username, password).tenantId(tenantId)
                .disableSslVerification(true).debug(debugEnabled).connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout((int) readTimeout, TimeUnit.SECONDS).build();
        return client;
    }
}
