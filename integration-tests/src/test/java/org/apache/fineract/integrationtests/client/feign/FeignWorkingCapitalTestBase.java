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
package org.apache.fineract.integrationtests.client.feign;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachActionData;
import org.apache.fineract.client.models.WorkingCapitalLoanBreachScheduleData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class FeignWorkingCapitalTestBase extends FeignIntegrationTest {

    protected static final BigDecimal DEFAULT_PERIOD_PAYMENT_RATE = BigDecimal.valueOf(18);

    protected static FeignWorkingCapitalLoanHelper wcLoanHelper;
    protected static FeignClientHelper clientHelper;
    protected static FeignBusinessDateHelper businessDateHelper;
    protected static WorkingCapitalLoanProductHelper productHelper;
    protected static WorkingCapitalBreachHelper breachHelper;

    private final List<Long> createdWcLoanIds = new ArrayList<>();

    @BeforeAll
    public static void setupWorkingCapitalHelpers() {
        final FineractFeignClient feignClient = FineractFeignClientHelper.getFineractFeignClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
        breachHelper = new WorkingCapitalBreachHelper();
    }

    @AfterAll
    public void cleanupWorkingCapitalLoans() {
        createdWcLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdWcLoanIds.clear();
    }

    protected void runAt(String isoDate, Runnable action) {
        businessDateHelper.runAt(isoDate, action);
    }

    protected void setBusinessDate(String isoDate) {
        businessDateHelper.updateBusinessDate("BUSINESS_DATE", isoDate);
    }

    /**
     * The inline WC COB replays every skipped business day inside one request, so an unbounded leap blows the client
     * read timeout; the business date is advanced in chunks of this size instead.
     */
    private static final int MAX_COB_CATCH_UP_DAYS = 14;

    protected void advanceBusinessDateWithCob(Long loanId, String fromIso, String toIso) {
        LocalDate current = LocalDate.parse(fromIso);
        final LocalDate target = LocalDate.parse(toIso);
        if (!target.isAfter(current)) {
            throw new IllegalArgumentException("advanceBusinessDateWithCob: target " + target + " must be after " + current);
        }
        while (current.isBefore(target)) {
            final LocalDate next = current.plusDays(MAX_COB_CATCH_UP_DAYS);
            current = next.isBefore(target) ? next : target;
            setBusinessDate(current.toString());
            runInlineWcCob(loanId);
        }
    }

    protected Long createClient(String activationDate) {
        return clientHelper.createClient(activationDate);
    }

    protected Long createWcProductWithBreachConfig(int breachFrequency, String breachFrequencyType, String breachAmountCalculationType,
            BigDecimal breachAmount, int breachGraceDays) {
        final Long breachId = breachHelper.create(breachHelper.createBreachRequest(Utils.randomStringGenerator("WC_BREACH_", 8),
                breachFrequency, breachFrequencyType, breachAmountCalculationType, breachAmount));
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder()
                .withName("WCL Breach " + Utils.uniqueRandomStringGenerator("", 8)).withShortName(Utils.uniqueRandomStringGenerator("", 4))
                .withBreachId(breachId).withBreachGraceDays(breachGraceDays).build()).getResourceId();
    }

    protected Long createApproveAndDisburseWcLoan(Long clientId, Long productId, BigDecimal principal, String date) {
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                principal, DEFAULT_PERIOD_PAYMENT_RATE, date, date));
        createdWcLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    protected Long makeWcRepayment(Long loanId, BigDecimal amount, String transactionDate) {
        return wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(amount, transactionDate));
    }

    protected void runInlineWcCob(Long loanId) {
        wcLoanHelper.executeInlineWCCOB(loanId);
    }

    protected Long createBreachReset(Long loanId) {
        return wcLoanHelper.createBreachAction(loanId, WorkingCapitalLoanRequestBuilders.breachReset());
    }

    protected Long createBreachResetWithRestartPeriod(Long loanId) {
        return wcLoanHelper.createBreachAction(loanId, WorkingCapitalLoanRequestBuilders.breachResetWithRestartPeriod());
    }

    protected Long createBreachUndoReset(Long loanId) {
        return wcLoanHelper.createBreachAction(loanId, WorkingCapitalLoanRequestBuilders.breachUndoReset());
    }

    protected Long createBreachReschedule(Long loanId, int frequency, String frequencyType) {
        return wcLoanHelper.createBreachAction(loanId, WorkingCapitalLoanRequestBuilders.breachReschedule(frequency, frequencyType));
    }

    protected Long createBreachPause(Long loanId, String startDate, String endDate) {
        return wcLoanHelper.createBreachAction(loanId, WorkingCapitalLoanRequestBuilders.breachPause(startDate, endDate));
    }

    protected List<WorkingCapitalLoanBreachScheduleData> getBreachSchedule(Long loanId) {
        return wcLoanHelper.getBreachSchedule(loanId);
    }

    protected List<WorkingCapitalLoanBreachActionData> getBreachActions(Long loanId) {
        return wcLoanHelper.getBreachActions(loanId);
    }

    protected BigDecimal getBreachPastDueAmount(Long loanId) {
        return wcLoanHelper.getBreachPastDueAmount(loanId);
    }
}
