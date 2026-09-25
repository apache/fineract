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
package org.apache.fineract.test.stepdef.common;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import io.cucumber.java.After;
import io.cucumber.java.en.When;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.CacheSwitchRequest;

@RequiredArgsConstructor
public class ApplicationCacheStepDef {

    private static final Map<String, Integer> CACHE_TYPES = Map.of("NO_CACHE", 1, "SINGLE_NODE", 2);

    private final FineractFeignClient fineractClient;

    @When("Admin switches the application cache to {string}")
    public void switchApplicationCache(final String cacheType) {
        switchCache(cacheType);
    }

    // restore the default even when a scenario fails mid-way, so a single node cache cannot leak into other scenarios
    @After("@ApplicationCacheScenario")
    public void restoreNoCache() {
        switchCache("NO_CACHE");
    }

    private void switchCache(final String cacheType) {
        ok(() -> fineractClient.cache().switchCache(new CacheSwitchRequest().cacheType(CACHE_TYPES.get(cacheType))));
    }
}
