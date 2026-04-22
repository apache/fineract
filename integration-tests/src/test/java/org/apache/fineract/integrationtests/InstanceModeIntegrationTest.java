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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.support.instancemode.ConfigureInstanceMode;
import org.apache.fineract.integrationtests.support.instancemode.InstanceModeSupportExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(InstanceModeSupportExtension.class)
public class InstanceModeIntegrationTest {

    private Long jobId;

    @BeforeEach
    public void setup() throws InterruptedException {
        // Apply Annual Fee For Savings
        jobId = Calls.ok(FineractClientHelper.getFineractClient().jobs.retrieveByShortName("SA_AANF")).getJobId();
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
        assertThrows(RuntimeException.class, () -> ClientHelper.createClient(request));
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = true, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testCreateClientWorks_WhenWriteOnly() {
        // given
        PostClientsRequest request = ClientHelper.defaultClientCreationRequest();
        // when
        var result = ClientHelper.createClient(request);
        // then
        assertNotNull(result);
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = true, batchManagerEnabled = true)
    @Test
    public void testCreateClientDoesntWork_WhenBatchOnly() {
        // given
        PostClientsRequest request = ClientHelper.defaultClientCreationRequest();
        // when/then
        assertThrows(RuntimeException.class, () -> ClientHelper.createClient(request));
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testRunSchedulerJobDoesntWork_WhenReadOnly() {
        // when/then
        assertThrows(RuntimeException.class, () -> Calls.ok(FineractClientHelper.getFineractClient().jobs.executeJob(jobId, "executeJob")));
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = true, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testRunSchedulerJobDoesntWork_WhenWriteOnly() {
        // when/then
        assertThrows(RuntimeException.class, () -> Calls.ok(FineractClientHelper.getFineractClient().jobs.executeJob(jobId, "executeJob")));
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = true, batchManagerEnabled = true)
    @Test
    public void testRunSchedulerJobWorks_WhenBatchOnly() {
        // when
        Calls.ok(FineractClientHelper.getFineractClient().jobs.executeJob(jobId, "executeJob"));
        // then no exception thrown
    }
}
