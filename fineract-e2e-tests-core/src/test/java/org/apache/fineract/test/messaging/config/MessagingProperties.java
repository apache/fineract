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
package org.apache.fineract.test.messaging.config;

import static org.apache.commons.lang3.StringUtils.isBlank;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class MessagingProperties implements InitializingBean {

    @Value("${fineract-test.messaging.jms.broker-url}")
    private String brokerUrl;
    @Value("${fineract-test.messaging.jms.broker-username}")
    private String brokerUsername;
    @Value("${fineract-test.messaging.jms.broker-password}")
    private String brokerPassword;
    @Value("${fineract-test.messaging.jms.topic-name}")
    private String topicName;

    private final EventProperties eventProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (eventProperties.isEventVerificationEnabled()) {
            if (isBlank(brokerUrl) || isBlank(topicName)) {
                throw new IllegalStateException("Broker and topic must be configured in case event verification is enabled");
            }
        }
    }
}
