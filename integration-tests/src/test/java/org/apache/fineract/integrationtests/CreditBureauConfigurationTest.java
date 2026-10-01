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

import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.PostCreditBureauConfigurationRequest;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCreditBureauHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditBureauConfigurationTest extends FeignIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreditBureauConfigurationTest.class);

    @Test
    public void creditBureauConfigurationTest() {
        final FeignCreditBureauHelper creditBureauHelper = new FeignCreditBureauHelper(fineractClient());

        // create creditBureauConfiguration
        CommandProcessingResult createResponse = creditBureauHelper.createConfiguration(1L, new PostCreditBureauConfigurationRequest()
                .configkey(Utils.randomStringGenerator("testConfigKey_", 5)).value("testConfigKeyValue").description("description"));
        Long configurationId = createResponse.getResourceId();
        Assertions.assertNotNull(configurationId);

        // update creditBureauConfiguration
        CommandProcessingResult updateResponse = creditBureauHelper.updateConfiguration(configurationId, null, "updateConfigKeyValue");
        Object updateconfiguration = updateResponse.getChanges().get("value");

        Assertions.assertEquals("updateConfigKeyValue", updateconfiguration);
    }

}
