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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.fineract.integrationtests.common.CreditBureauConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreditBureauConfigurationTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreditBureauConfigurationTest.class);

    @Test
    public void creditBureauConfigurationTest() {

        // create creditBureauConfiguration
        String createResponse = CreditBureauConfigurationHelper.createCreditBureauConfiguration(1L,
                Utils.randomStringGenerator("testConfigKey_", 5), "testConfigKeyValue", "description");
        JsonObject createJson = JsonParser.parseString(createResponse).getAsJsonObject();
        Long configurationId = createJson.get("resourceId").getAsLong();
        Assertions.assertNotNull(configurationId);

        // update creditBureauConfiguration
        String updateResponse = CreditBureauConfigurationHelper.updateCreditBureauConfiguration(configurationId, null,
                "updateConfigKeyValue");
        String updateconfiguration = JsonParser.parseString(updateResponse).getAsJsonObject().get("changes").getAsJsonObject().get("value")
                .getAsString();

        Assertions.assertEquals("updateConfigKeyValue", updateconfiguration);
    }

}
