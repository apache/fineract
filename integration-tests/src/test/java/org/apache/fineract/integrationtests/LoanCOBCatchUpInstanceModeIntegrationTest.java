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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanCOBCatchUpHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.support.instancemode.ConfigureInstanceMode;
import org.apache.fineract.integrationtests.support.instancemode.InstanceModeSupportExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Order(1)
@ExtendWith(InstanceModeSupportExtension.class)
public class LoanCOBCatchUpInstanceModeIntegrationTest extends FeignLoanTestBase {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US);

    private FeignLoanCOBCatchUpHelper loanCOBCatchUpHelper;
    private Boolean originalSchedulerStatus;

    @BeforeEach
    public void setup() {
        loanCOBCatchUpHelper = new FeignLoanCOBCatchUpHelper(FineractFeignClientHelper.getFineractFeignClient());
        originalSchedulerStatus = schedulerHelper.getSchedulerStatus();
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));
        updateBusinessDate(todaysDate.format(DATE_FORMATTER));
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = true)
    @Test
    public void testLoanCOBCatchUpWorksWhenInBatchManagerMode() {
        loanCOBCatchUpHelper.executeLoanCOBCatchUp();
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = true, batchManagerEnabled = false)
    @Test
    public void testLoanCOBCatchUpDoesNotWorksWhenNotInBatchManagerMode() {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> loanCOBCatchUpHelper.executeLoanCOBCatchUp());
        assertEquals(405, exception.getStatus());
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = true)
    @Test
    public void testLoanCOBCatchUpGetStatusWorksWhenInBatchManagerMode() {
        loanCOBCatchUpHelper.executeGetLoanCatchUpStatus();
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = true, batchManagerEnabled = false)
    @Test
    public void testLoanCOBCatchUpGetStatusDoesNotWorksWhenNotInBatchManagerMode() {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> loanCOBCatchUpHelper.executeGetLoanCatchUpStatus());
        assertEquals(405, exception.getStatus());
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = true)
    @Test
    public void testLoanCOBCatchUpOtherGetApisWorksWhenInBatchManagerAndReadMode() {
        loanCOBCatchUpHelper.executeRetrieveOldestCOBProcessedLoan();
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = false)
    @Test
    public void testLoanCOBCatchUpOtherGetApisWorksWhenInReadOnlyMode() {
        loanCOBCatchUpHelper.executeRetrieveOldestCOBProcessedLoan();
    }

    @ConfigureInstanceMode(readEnabled = false, writeEnabled = false, batchWorkerEnabled = false, batchManagerEnabled = true)
    @Test
    public void testSchedulerWorksWhenInBatchManagerMode() {
        schedulerHelper.updateSchedulerStatus(false);
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = true, batchWorkerEnabled = true, batchManagerEnabled = false)
    @Test
    public void testSchedulerDoesNotWorksWhenNotInBatchManagerMode() {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> schedulerHelper.updateSchedulerStatus(false));
        assertEquals(405, exception.getStatus());
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
        schedulerHelper.updateSchedulerStatus(originalSchedulerStatus);
    }
}
