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

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventConfigurationHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

@Slf4j
public class ExternalEventsExtension implements AfterEachCallback, BeforeEachCallback {

    private final FeignExternalEventConfigurationHelper configurationHelper = new FeignExternalEventConfigurationHelper(
            FineractFeignClientHelper.getFineractFeignClient());
    private Map<String, Boolean> original;

    @Override
    public void afterEach(ExtensionContext context) {
        Map<String, Boolean> collected = configurationHelper.retrieveEnabledByEventType();

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
        original = configurationHelper.retrieveEnabledByEventType();
    }

    private void restore(String key, Boolean value) {
        Object changes = configurationHelper.updateConfigurations(Map.of(key, value)).getChanges().get("externalEventConfigurations");
        Assertions.assertEquals(Map.of(key, value), changes);
    }
}
