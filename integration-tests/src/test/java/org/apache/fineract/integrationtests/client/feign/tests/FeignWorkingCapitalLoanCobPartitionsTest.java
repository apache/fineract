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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * One scheduled Working Capital Loan COB run over more loans than one partition holds (102 against the default
 * partition size of 100) must close every loan. Partitions of one job may run at the same time, so each needs its own
 * reader: with a shared one, a partition's loans were locked but never processed, and they stayed behind for good.
 */
public class FeignWorkingCapitalLoanCobPartitionsTest extends FeignIntegrationTest {

    private static final int LOAN_COUNT = 102;
    private static final BigDecimal PRINCIPAL = new BigDecimal("1000");
    private static final String DISBURSEMENT_DATE = "01 January 2026";
    private static final String DISBURSEMENT_DATE_ISO = "2026-01-01";
    private static final LocalDate COB_DATE = LocalDate.of(2026, 1, 1);

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
    }

    @Test
    void testOneCobRunClosesEveryLoanAcrossPartitions() {
        businessDateHelper.runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long clientId = clientHelper.createClient(DISBURSEMENT_DATE);
            final Long productId = productHelper.createWorkingCapitalLoanProduct(
                    new WorkingCapitalLoanProductTestBuilder().withName("WCL COB partitions " + Utils.uniqueRandomStringGenerator("", 8))
                            .withShortName(Utils.uniqueRandomStringGenerator("", 4)).build())
                    .getResourceId();
            for (int i = 0; i < LOAN_COUNT; i++) {
                final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                        PRINCIPAL, WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT, DISBURSEMENT_DATE,
                        DISBURSEMENT_DATE));
                createdLoanIds.add(loanId);
                wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(DISBURSEMENT_DATE, PRINCIPAL, DISBURSEMENT_DATE));
                wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(DISBURSEMENT_DATE, PRINCIPAL));
            }

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            SchedulerJobHelper.executeAndAwaitJob("Working Capital Loan COB");

            final Map<Long, LocalDate> notClosed = createdLoanIds.stream()
                    .collect(Collectors.toMap(Function.identity(), loanId -> lastClosedBusinessDate(loanId)));
            notClosed.values().removeIf(COB_DATE::equals);
            assertEquals(Map.of(), notClosed, "every loan must be closed for " + COB_DATE + " by one COB run; these were left behind "
                    + "(loan id -> last closed business date)");
        });
    }

    private LocalDate lastClosedBusinessDate(final Long loanId) {
        final LocalDate date = wcLoanHelper.getLoanDetails(loanId).getLastClosedBusinessDate();
        return date == null ? LocalDate.MIN : date;
    }
}
