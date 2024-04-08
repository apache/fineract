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

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.io.IOException;
import org.apache.fineract.test.helper.GlobalConfigurationHelper;
import org.springframework.beans.factory.annotation.Autowired;

public class GlobalConfigurationStepDef {

    @Autowired
    private GlobalConfigurationHelper globalConfigurationHelper;

    @Given("Global configuration {string} is disabled")
    public void disableGlobalConfiguration(String configKey) throws IOException {
        globalConfigurationHelper.disableGlobalConfiguration(configKey, 0L);
    }

    @Given("Global configuration {string} is enabled")
    public void enableGlobalConfiguration(String configKey) throws IOException {
        globalConfigurationHelper.enableGlobalConfiguration(configKey, 0L);
    }

    @When("Global config {string} value set to {string}")
    public void setGlobalConfigValueString(String configKey, String configValue) throws IOException {
        globalConfigurationHelper.setGlobalConfigValueString(configKey, configValue);

    }
}
