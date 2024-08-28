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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.support.instancemode.ConfigureInstanceMode;
import org.apache.fineract.integrationtests.support.instancemode.InstanceModeSupportExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InstanceModeSupportExtension.class)
public class InstanceModeIntegrationTest {

    private ResponseSpecification responseSpec200;
    private ResponseSpecification responseSpec405;
    private RequestSpecification requestSpec;
    private SchedulerJobHelper schedulerJobHelper;
    private int jobId;

    @BeforeEach
    public void setup() throws InterruptedException {
        Utils.initializeRESTAssured();

        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec200 = new ResponseSpecBuilder().expectStatusCode(200).build();
        responseSpec405 = new ResponseSpecBuilder().expectStatusCode(405).build();

        schedulerJobHelper = new SchedulerJobHelper(requestSpec);
        // Apply Annual Fee For Savings"
        jobId = schedulerJobHelper.getSchedulerJobIdByShortName("SA_AANF");
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testGetHeadOfficeWorks_WhenInstanceModeIsReadOnly() {
        // given
        // when
        GetOfficesResponse result = OfficeHelper.getHeadOffice(requestSpec, responseSpec200);
        // then
        assertNotNull(result);
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = true, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testGetHeadOfficeWorks_WhenInstanceModeIsWriteOnly() {
        // given
        // when
        GetOfficesResponse result = OfficeHelper.getHeadOffice(requestSpec, responseSpec200);
        // then
        assertNotNull(result);
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = true, batchManagerEnabled = true)
    @Test
    public void testGetHeadOfficeDoesntWork_WhenInstanceModeIsBatchOnly() {
        // given
        // when
        OfficeHelper.getHeadOffice(requestSpec, responseSpec405);
        // then no exception is thrown
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testCreateClientDoesntWork_WhenReadOnly() {
        // given
        PostClientsRequest request = ClientHelper.defaultClientCreationRequest();
        // when
        ClientHelper.createClient(requestSpec, responseSpec405, request);
        // then no exception thrown
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = true, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testCreateClientWorks_WhenWriteOnly() {
        // given
        PostClientsRequest request = ClientHelper.defaultClientCreationRequest();
        // when
        Integer result = ClientHelper.createClient(requestSpec, responseSpec200, request);
        // then
        assertNotNull(result);
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = true, batchManagerEnabled = true)
    @Test
    public void testCreateClientDoesntWork_WhenBatchOnly() {
        // given
        PostClientsRequest request = ClientHelper.defaultClientCreationRequest();
        // when
        ClientHelper.createClient(requestSpec, responseSpec405, request);
        // then no exception thrown
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testRunSchedulerJobDoesntWork_WhenReadOnly() {
        // given
        // when
        schedulerJobHelper.runSchedulerJob(jobId, responseSpec405);
        // then no exception thrown
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = true, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testRunSchedulerJobDoesntWork_WhenWriteOnly() {
        // given
        // when
        schedulerJobHelper.runSchedulerJob(jobId, responseSpec405);
        // then no exception thrown
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = true, batchManagerEnabled = true)
    @Test
    public void testRunSchedulerJobWorks_WhenBatchOnly() {
        // given
        // when
        schedulerJobHelper.runSchedulerJob(jobId);
        // then no exception thrown
    }
}
