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
package org.apache.fineract.integrationtests.common.externalevents;

import static org.apache.fineract.integrationtests.common.Utils.initializeRESTAssured;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.integrationtests.common.ExternalEventConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
public class ExternalEventsExtension implements AfterEachCallback, BeforeEachCallback {

    private Map<String, Boolean> original;
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    public ExternalEventsExtension() {
        initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
    }

    @Override
    public void afterEach(ExtensionContext context) {
        ArrayList<Map<String, Object>> allExternalEventConfigurations = ExternalEventConfigurationHelper
                .getAllExternalEventConfigurations(requestSpec, responseSpec);
        Map<String, Boolean> collected = allExternalEventConfigurations.stream()
                .map(map -> Map.entry((String) map.get("type"), (Boolean) map.get("enabled")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, MapDifference.ValueDifference<Boolean>> diff = Maps.difference(original, collected).entriesDiffering();
        diff.keySet().forEach(key -> {
            MapDifference.ValueDifference<Boolean> valueDifference = diff.get(key);
            log.debug("External event {} changed from {} to {}. Restoring to its original state.", key, valueDifference.leftValue(),
                    valueDifference.rightValue());
            restore(key, valueDifference.leftValue());
        });
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        ArrayList<Map<String, Object>> allExternalEventConfigurations = ExternalEventConfigurationHelper
                .getAllExternalEventConfigurations(requestSpec, responseSpec);
        original = allExternalEventConfigurations.stream().map(map -> Map.entry((String) map.get("type"), (Boolean) map.get("enabled")))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void restore(String key, Boolean value) {
        final Map<String, Boolean> updatedConfigurations = ExternalEventConfigurationHelper.updateExternalEventConfigurations(requestSpec,
                responseSpec, "{\"externalEventConfigurations\":{\"" + key + "\":" + value + "}}\n");
        Assertions.assertEquals(updatedConfigurations.size(), 1);
        Assertions.assertTrue(updatedConfigurations.containsKey(key));
        Assertions.assertEquals(value, updatedConfigurations.get(key));
    }
}
