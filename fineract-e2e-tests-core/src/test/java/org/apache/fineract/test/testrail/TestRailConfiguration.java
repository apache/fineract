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
package org.apache.fineract.test.testrail;

import static org.apache.commons.lang3.StringUtils.isBlank;

import feign.Feign;
import feign.Request;
import feign.Retryer;
import java.util.concurrent.TimeUnit;
import org.apache.fineract.client.feign.BasicAuthRequestInterceptor;
import org.apache.fineract.client.feign.Jackson3Encoder;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@Conditional(TestRailEnabledCondition.class)
public class TestRailConfiguration {

    @Autowired
    private TestRailProperties testRailProperties;

    @Bean
    public TestRailApiClient testRailApiClient() {
        String testRailBaseUrl = testRailProperties.getBaseUrl();
        String testRailUsername = testRailProperties.getUsername();
        String testRailPassword = testRailProperties.getPassword();

        if (isBlank(testRailBaseUrl)) {
            throw new IllegalStateException("TestRail base URL has not been set");
        }
        if (isBlank(testRailUsername)) {
            throw new IllegalStateException("TestRail username has not been set");
        }
        if (isBlank(testRailPassword)) {
            throw new IllegalStateException("TestRail password has not been set");
        }

        return Feign.builder().encoder(new Jackson3Encoder(ObjectMapperFactory.getShared()))
                .options(new Request.Options(30, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true)).retryer(Retryer.NEVER_RETRY)
                .requestInterceptor(new BasicAuthRequestInterceptor(testRailUsername, testRailPassword))
                .target(TestRailApiClient.class, testRailBaseUrl);
    }
}
