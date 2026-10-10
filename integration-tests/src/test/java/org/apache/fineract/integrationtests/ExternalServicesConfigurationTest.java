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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.ExternalServicesPropertiesData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalServicesHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalServicesConfigurationTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalServicesConfigurationTest.class);
    private FeignExternalServicesHelper externalServicesHelper;

    @BeforeAll
    public void setup() {
        this.externalServicesHelper = new FeignExternalServicesHelper(fineractClient());
    }

    @Test
    public void testExternalServicesConfiguration() {
        // Checking for S3
        String configName = "s3_access_key";
        List<ExternalServicesPropertiesData> externalServicesConfig = externalServicesHelper.retrieveProperties("S3");
        Assertions.assertNotNull(externalServicesConfig);
        for (ExternalServicesPropertiesData config : externalServicesConfig) {
            String name = config.getName();
            String value = null;
            if (name.equals(configName)) {
                value = config.getValue();
                if (value == null) {
                    value = "testnull";
                }
                String newValue = "test";
                LOG.info("{} : {}", name, value);
                Map<String, Object> arrayListValue = externalServicesHelper.updateProperty("S3", name, newValue);
                Assertions.assertNotNull(arrayListValue.get("value"));
                Assertions.assertEquals(arrayListValue.get("value"), newValue);
                Map<String, Object> arrayListValue1 = externalServicesHelper.updateProperty("S3", name, value);
                Assertions.assertNotNull(arrayListValue1.get("value"));
                Assertions.assertEquals(arrayListValue1.get("value"), value);
            }

        }

        // Checking for SMTP:
        configName = "username";
        externalServicesConfig = externalServicesHelper.retrieveProperties("SMTP");
        Assertions.assertNotNull(externalServicesConfig);

        for (ExternalServicesPropertiesData config : externalServicesConfig) {
            String name = config.getName();
            String value = null;
            if (name.equals(configName)) {
                value = config.getValue();
                if (value == null) {
                    value = "testnull";
                }
                String newValue = "test";
                LOG.info("{} : {}", name, value);
                Map<String, Object> arrayListValue = externalServicesHelper.updateProperty("SMTP", name, newValue);
                Assertions.assertNotNull(arrayListValue.get("value"));
                Assertions.assertEquals(arrayListValue.get("value"), newValue);
                Map<String, Object> arrayListValue1 = externalServicesHelper.updateProperty("SMTP", name, value);
                Assertions.assertNotNull(arrayListValue1.get("value"));
                Assertions.assertEquals(arrayListValue1.get("value"), value);
            }

        }

        // Checking for Notifications:
        configName = "server_key";
        externalServicesConfig = externalServicesHelper.retrieveProperties("NOTIFICATION");
        Assertions.assertNotNull(externalServicesConfig);

        for (ExternalServicesPropertiesData config : externalServicesConfig) {
            String name = config.getName();
            String value = null;
            if (name.equals(configName)) {
                value = config.getValue();
                if (value == null) {
                    value = "testnull";
                }
                LOG.info("{} : {}", name, value);
                assertTrue(hasMoreThanThreeStars(value));
            }

        }
    }

    private boolean hasMoreThanThreeStars(String input) {
        return input != null && input.matches("(.*\\*.*){4,}");
    }
}
