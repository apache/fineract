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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;


public class GlobalConfigInterestChargedFromDateSameAsDisbursalDateTest {
    
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private GlobalConfigurationHelper globalConfigurationHelper;
    
    @Before
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }
    
    @SuppressWarnings( {"static-access", "rawtypes", "unchecked"})
    @Test
    public void testInterestChargedFromDateSameAsDisbursalDate(){
        this.globalConfigurationHelper = new GlobalConfigurationHelper(this.requestSpec, this.responseSpec);
        
     // Retrieving All Global Configuration details
        final ArrayList<HashMap> globalConfig = this.globalConfigurationHelper
                        .getAllGlobalConfigurations(this.requestSpec, this.responseSpec);
        Assert.assertNotNull(globalConfig);
        
        String configName = "interest-charged-from-date-same-as-disbursal-date";
        boolean newBooleanValue = true;
        
        for (Integer configIndex = 0; configIndex < (globalConfig.size()); configIndex++) {
                if (globalConfig.get(configIndex).get("name")
                                .equals(configName)) {
                        String configId = (globalConfig.get(configIndex).get("id"))
                                        .toString();
                        Integer updateConfigId = this.globalConfigurationHelper
                                        .updateEnabledFlagForGlobalConfiguration(this.requestSpec, this.responseSpec,
                                                configId.toString(), newBooleanValue);;
                        Assert.assertNotNull(updateConfigId);
                        break;
                }
        }
        
    }

}
