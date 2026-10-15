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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.WorkingCapitalNearBreachRequest;
import org.apache.fineract.integrationtests.client.feign.FeignWorkingCapitalTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.WorkingCapitalLoanCommandsApi;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Clearing breach, near-breach and delinquency bucket on the loan must also clear grace days and start types.
 */
public class FeignWorkingCapitalLoanParentClearTest extends FeignWorkingCapitalTestBase {

    private static final String BUSINESS_DATE = "2026-01-01";
    private static final String SUBMIT_DATE = "01 January 2026";

    private final List<Long> createdLoanIds = new ArrayList<>();

    @AfterAll
    void cleanup() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
    }

    @Test
    @DisplayName("clearing breach, near-breach and delinquency bucket on modify also clears grace days and start types")
    void modify_clearBreachNearBreachAndDelinquencyBucket_clearsGraceAndStartTypes() {
        runAt(BUSINESS_DATE, () -> {
            final Long clientId = createClient(SUBMIT_DATE);
            final Long productId = createOverridableProductWithBreachDelinquencyAndNearBreach();
            final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                    BigDecimal.valueOf(5000), BigDecimal.ONE, SUBMIT_DATE, SUBMIT_DATE));
            createdLoanIds.add(loanId);

            final GetWorkingCapitalLoansLoanIdResponse before = wcLoanHelper.getLoanDetails(loanId);
            assertNotNull(before.getBreach());
            assertNotNull(before.getNearBreach());
            assertNotNull(before.getDelinquencyBucket());
            assertNotNull(before.getDelinquencyGraceDays());
            assertNotNull(before.getBreachGraceDays());
            assertNotNull(before.getDelinquencyStartType());
            assertNotNull(before.getBreachStartType());

            ok(() -> FineractFeignClientHelper.getFineractFeignClient().create(WorkingCapitalLoanCommandsApi.class)
                    .modifyApplication(loanId, new WorkingCapitalLoanCommandsApi.ClearBreachNearBreachAndDelinquencyBucketRequest()));

            final GetWorkingCapitalLoansLoanIdResponse after = wcLoanHelper.getLoanDetails(loanId);
            assertNull(after.getBreach());
            assertNull(after.getNearBreach());
            assertNull(after.getDelinquencyBucket());
            assertNull(after.getDelinquencyGraceDays());
            assertNull(after.getDelinquencyStartType());
            assertNull(after.getBreachGraceDays());
            assertNull(after.getBreachStartType());
        });
    }

    private Long createOverridableProductWithBreachDelinquencyAndNearBreach() {
        final Long breachId = breachHelper.create(
                breachHelper.createBreachRequest(Utils.randomStringGenerator("WC_BREACH_", 8), 30, "DAYS", "PERCENTAGE", BigDecimal.TEN));
        final Long nearBreachId = nearBreachHelper
                .create(new WorkingCapitalNearBreachRequest().nearBreachName(Utils.randomStringGenerator("WC_NEAR_BREACH_", 8))
                        .nearBreachFrequency(10).nearBreachFrequencyType("DAYS").nearBreachThreshold(BigDecimal.valueOf(30)))
                .getResourceId();
        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultWorkingCapitalBucket();

        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName("WCL Parent Clear " + Utils.uniqueRandomStringGenerator("", 8)) //
                .withShortName(Utils.uniqueRandomStringGenerator("", 4)) //
                .withDelinquencyBucketId(delinquencyBucketId) //
                .withDelinquencyGraceDays(3) //
                .withDelinquencyStartType("DISBURSEMENT") //
                .withBreachId(breachId) //
                .withNearBreachId(nearBreachId) //
                .withBreachGraceDays(5) //
                .withBreachStartType("LOAN_CREATION") //
                .withAllowAttributeOverrides(Map.of("breach", Boolean.TRUE, "delinquencyBucketClassification", Boolean.TRUE)) //
                .build()).getResourceId();
    }
}
