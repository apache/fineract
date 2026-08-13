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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlimHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Group-loan repayment schedule and GLIM (Group Loan Individual Monitoring) lifecycle and retrieval tests. */
public class GroupLoanIntegrationTest extends FeignLoanTestBase {

    private static final String PRODUCT_PRINCIPAL = "12,000.00";
    private static final String PRODUCT_NUMBER_OF_REPAYMENTS = "4";
    private static final String PRODUCT_INTEREST_RATE = "1";
    private static final BigDecimal APPLICATION_PRINCIPAL = new BigDecimal("12000.00");
    private static final Integer APPLICATION_NUMBER_OF_REPAYMENTS = 4;
    private static final BigDecimal APPLICATION_INTEREST_RATE = new BigDecimal("2");
    private static final BigDecimal MAX_OUTSTANDING_LOAN_BALANCE = new BigDecimal("36000");
    private static final String LOAN_DATE = "20 September 2011";
    private static final String DISBURSE_DATE = "25 September 2011";
    private static final String REJECT_DATE = "22 September 2011";
    /** Per-child GLIM principal supplied at approval. */
    private static final BigDecimal GLIM_PRINCIPAL = new BigDecimal("1000");
    /** The total principal of the parent GLIM account. */
    private static final BigDecimal GLIM_TOTAL_LOAN = new BigDecimal("10000");
    /** Fineract loan status id for a rejected loan. */
    private static final Long LOAN_STATUS_REJECTED = 500L;

    private static FeignGroupHelper groupHelper;
    private static FeignGlimHelper glimHelper;

    /** The group id created by the most recent {@link #applyGlim()} call (GLIM retrieval tests need it). */
    private Long currentGroupId;

    @BeforeAll
    public static void setupGroupLoanHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        groupHelper = new FeignGroupHelper(client);
        glimHelper = new FeignGlimHelper(client);
    }

    @Test
    public void checkGroupLoanCreateAndDisburseFlow() {
        final Long clientId = createClient();
        final Long groupId = groupHelper.createActiveGroup().getResourceId();
        groupHelper.associateClient(groupId, clientId);

        final Long loanProductId = createLoanProductFromJson(loanProductJson());
        final Long loanId = applyGroupLoan(groupId, loanProductId);

        final List<GetLoansLoanIdRepaymentPeriod> periods = getLoanDetails(loanId).getRepaymentSchedule().getPeriods();
        verifyLoanRepaymentSchedule(periods);
    }

    @Test
    public void checkGlimAccountCommands() {
        final FeignGlimHelper.GlimApplication glim = applyGlim();
        final Long glimId = glim.glimId();
        final Long childLoanId = glim.loanId();

        glimHelper.approveGlim(glimId, childLoanId, LOAN_DATE, GLIM_PRINCIPAL);
        assertFalse(loanStatus(childLoanId).getPendingApproval(), "GLIM account should be approved");

        glimHelper.disburseGlim(glimId, DISBURSE_DATE);
        assertTrue(loanStatus(childLoanId).getActive(), "GLIM account should be active");

        glimHelper.undoDisbursalGlim(glimId);
        assertTrue(loanStatus(childLoanId).getWaitingForDisbursal(), "GLIM account should be waiting for disbursal");

        glimHelper.undoApprovalGlim(glimId);
        assertTrue(loanStatus(childLoanId).getPendingApproval(), "GLIM account should be pending approval");

        glimHelper.rejectGlim(glimId, REJECT_DATE);
        assertEquals(LOAN_STATUS_REJECTED, loanStatus(childLoanId).getId(), "GLIM account should be rejected");
    }

    @Test
    public void getGlimAccountByGroupId() {
        final FeignGlimHelper.GlimApplication glim = applyGlim();
        assertReferencesGlim(glimHelper.retrieveGlimAccountsByGroup(currentGroupId), glim.glimId());
    }

    @Test
    public void getGlimAccountByGlimId() {
        final FeignGlimHelper.GlimApplication glim = applyGlim();
        assertReferencesGlim(glimHelper.retrieveGlimAccountByGlimId(glim.glimId()), glim.glimId());
    }

    /** Applies a group loan and returns its loan id. */
    private Long applyGroupLoan(Long groupId, Long loanProductId) {
        return applyForLoan(loanApplication(loanProductId).groupId(groupId).loanType("group"));
    }

    /** Creates client + active group + product, associates the client, and submits a GLIM application. */
    private FeignGlimHelper.GlimApplication applyGlim() {
        final Long clientId = createClient();
        currentGroupId = groupHelper.createActiveGroup().getResourceId();
        groupHelper.associateClient(currentGroupId, clientId);
        final Long loanProductId = createLoanProductFromJson(loanProductJson());

        return glimHelper.applyGlim(loanApplication(loanProductId)//
                .groupId(currentGroupId)//
                .clientId(clientId)//
                .loanType("glim")//
                .isParentAccount(true)//
                .totalLoan(GLIM_TOTAL_LOAN));
    }

    /** The application shared by both tests; the caller adds the loan type and its owner. */
    private PostLoansRequest loanApplication(Long loanProductId) {
        return new PostLoansRequest()//
                .productId(loanProductId)//
                .principal(APPLICATION_PRINCIPAL)//
                .loanTermFrequency(APPLICATION_NUMBER_OF_REPAYMENTS)//
                .loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .numberOfRepayments(APPLICATION_NUMBER_OF_REPAYMENTS)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)//
                .interestRatePerPeriod(APPLICATION_INTEREST_RATE)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .maxOutstandingLoanBalance(MAX_OUTSTANDING_LOAN_BALANCE)//
                .expectedDisbursementDate(LOAN_DATE)//
                .submittedOnDate(LOAN_DATE)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);
    }

    private String loanProductJson() {
        return new LoanProductTestBuilder()//
                .withPrincipal(PRODUCT_PRINCIPAL)//
                .withNumberOfRepayments(PRODUCT_NUMBER_OF_REPAYMENTS)//
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth()//
                .withinterestRatePerPeriod(PRODUCT_INTEREST_RATE).withInterestRateFrequencyTypeAsMonths()//
                .withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()//
                .build(null);
    }

    private GetLoansLoanIdStatus loanStatus(Long loanId) {
        return getLoanDetails(loanId).getStatus();
    }

    private void verifyLoanRepaymentSchedule(final List<GetLoansLoanIdRepaymentPeriod> periods) {
        assertEquals(LocalDate.of(2011, 10, 20), periods.get(1).getDueDate(), "Checking for Due Date for 1st Month");
        assertEquals(0, periods.get(1).getPrincipalOriginalDue().compareTo(new BigDecimal("2911.49")),
                "Checking for Principal Due for 1st Month");
        assertEquals(0, periods.get(1).getInterestOriginalDue().compareTo(new BigDecimal("240.00")),
                "Checking for Interest Due for 1st Month");

        assertEquals(LocalDate.of(2011, 11, 20), periods.get(2).getDueDate(), "Checking for Due Date for 2nd Month");
        assertEquals(0, periods.get(2).getPrincipalDue().compareTo(new BigDecimal("2969.72")), "Checking for Principal Due for 2nd Month");
        assertEquals(0, periods.get(2).getInterestOriginalDue().compareTo(new BigDecimal("181.77")),
                "Checking for Interest Due for 2nd Month");

        assertEquals(LocalDate.of(2011, 12, 20), periods.get(3).getDueDate(), "Checking for Due Date for 3rd Month");
        assertEquals(0, periods.get(3).getPrincipalDue().compareTo(new BigDecimal("3029.11")), "Checking for Principal Due for 3rd Month");
        assertEquals(0, periods.get(3).getInterestOriginalDue().compareTo(new BigDecimal("122.38")),
                "Checking for Interest Due for 3rd Month");

        assertEquals(LocalDate.of(2012, 1, 20), periods.get(4).getDueDate(), "Checking for Due Date for 4th Month");
        assertEquals(0, periods.get(4).getPrincipalDue().compareTo(new BigDecimal("3089.68")), "Checking for Principal Due for 4th Month");
        assertEquals(0, periods.get(4).getInterestOriginalDue().compareTo(new BigDecimal("61.79")),
                "Checking for Interest Due for 4th Month");
    }

    /** Asserts the retrieved GLIM JSON (array or object, per endpoint) references {@code glimId}. */
    private static void assertReferencesGlim(String json, Long glimId) {
        JsonElement parsed = JsonParser.parseString(json);
        boolean found = parsed.isJsonArray() ? parsed.getAsJsonArray().asList().stream().anyMatch(element -> hasGlimId(element, glimId))
                : hasGlimId(parsed, glimId);
        assertTrue(found, "Retrieved GLIM data should reference glimId " + glimId);
    }

    private static boolean hasGlimId(JsonElement element, Long glimId) {
        if (!element.isJsonObject()) {
            return false;
        }
        JsonObject object = element.getAsJsonObject();
        return object.has("glimId") && glimId.equals(object.get("glimId").getAsLong());
    }
}
