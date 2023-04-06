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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import org.apache.fineract.client.util.CallFailedRuntimeException;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanCOBCatchUpHelper;
import org.apache.fineract.integrationtests.support.instancemode.ConfigureInstanceMode;
import org.apache.fineract.integrationtests.support.instancemode.InstanceModeSupportExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Order(1)
@ExtendWith(InstanceModeSupportExtension.class)
public class LoanCOBCatchUpInstanceModeIntegrationTest {

    private LoanCOBCatchUpHelper loanCOBCatchUpHelper;
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SchedulerJobHelper schedulerJobHelper;
    private Boolean originalSchedulerStatus;

    @BeforeEach
    public void setup() throws InterruptedException {
        Utils.initializeRESTAssured();
        loanCOBCatchUpHelper = new LoanCOBCatchUpHelper();
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        schedulerJobHelper = new SchedulerJobHelper(requestSpec);
        originalSchedulerStatus = schedulerJobHelper.getSchedulerStatus();
        final LocalDate todaysDate = Utils.getLocalDateOfTenant();
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
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
        assertEquals(405, exception.getResponse().code());
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
        assertEquals(405, exception.getResponse().code());
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
        schedulerJobHelper.updateSchedulerStatus(false);
    }

    @ConfigureInstanceMode(readEnabled = true, writeEnabled = true, batchWorkerEnabled = true, batchManagerEnabled = false)
    @Test
    public void testSchedulerDoesNotWorksWhenNotInBatchManagerMode() {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> schedulerJobHelper.updateSchedulerStatus(false));
        assertEquals(405, exception.getResponse().code());
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        schedulerJobHelper.updateSchedulerStatus(originalSchedulerStatus);
    }

}
