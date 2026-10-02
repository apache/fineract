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

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.failVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ExecuteJobRequest;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.support.instancemode.ConfigureInstanceMode;
import org.apache.fineract.integrationtests.support.instancemode.InstanceModeSupportExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InstanceModeSupportExtension.class)
public class InstanceModeIntegrationTest {

    private final FeignClientHelper clientHelper = new FeignClientHelper(FineractFeignClientHelper.getFineractFeignClient());
    private Long jobId;

    @BeforeEach
    public void setup() throws InterruptedException {
        // Apply Annual Fee For Savings
        jobId = ok(() -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob().retrieveByShortName("SA_AANF")).getJobId();
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testGetHeadOfficeWorks_WhenInstanceModeIsReadOnly() {
        // given
        // when
        GetOfficesResponse result = OfficeHelper.getHeadOffice();
        // then
        assertNotNull(result);
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = true, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testGetHeadOfficeWorks_WhenInstanceModeIsWriteOnly() {
        // given
        // when
        GetOfficesResponse result = OfficeHelper.getHeadOffice();
        // then
        assertNotNull(result);
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = true, batchManagerEnabled = true)
    @Test
    public void testGetHeadOfficeDoesntWork_WhenInstanceModeIsBatchOnly() {
        // given
        // when
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> OfficeHelper.getHeadOffice());
        // then
        assertEquals(405, exception.getStatus());
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testCreateClientDoesntWork_WhenReadOnly() {
        // given
        PostClientsRequest request = ClientHelper.defaultClientCreationRequest();
        // when/then
        assertEquals(405, clientHelper.createClientExpectingError(request).getStatus());
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = true, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testCreateClientWorks_WhenWriteOnly() {
        // given
        PostClientsRequest request = ClientHelper.defaultClientCreationRequest();
        // when
        var result = clientHelper.createClient(request);
        // then
        assertNotNull(result);
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = true, batchManagerEnabled = true)
    @Test
    public void testCreateClientDoesntWork_WhenBatchOnly() {
        // given
        PostClientsRequest request = ClientHelper.defaultClientCreationRequest();
        // when/then
        assertEquals(405, clientHelper.createClientExpectingError(request).getStatus());
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testRunSchedulerJobDoesntWork_WhenReadOnly() {
        // when/then
        CallFailedRuntimeException exception = failVoid(() -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob()
                .executeJob(jobId, "executeJob", new ExecuteJobRequest()));
        assertEquals(405, exception.getStatus());
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = true, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testRunSchedulerJobDoesntWork_WhenWriteOnly() {
        // when/then
        CallFailedRuntimeException exception = failVoid(() -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob()
                .executeJob(jobId, "executeJob", new ExecuteJobRequest()));
        assertEquals(405, exception.getStatus());
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = true, batchManagerEnabled = true)
    @Test
    public void testRunSchedulerJobWorks_WhenBatchOnly() {
        // when
        executeVoid(() -> FineractFeignClientHelper.getFineractFeignClient().schedulerJob().executeJob(jobId, "executeJob",
                new ExecuteJobRequest()));
        // then no exception thrown
    }
}
