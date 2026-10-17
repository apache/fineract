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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.PostFixedDepositAccountsCharges;
import org.apache.fineract.client.models.PostFixedDepositAccountsRequest;
import org.apache.fineract.client.models.PostFixedDepositProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFixedDepositHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignFixedDepositProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.InteropTransactionRequestsApi;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.DepositTestData;
import org.apache.fineract.integrationtests.client.feign.modules.FeignErrors;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.interoperation.domain.InteropInitiatorType;
import org.apache.fineract.interoperation.domain.InteropTransactionRole;
import org.apache.fineract.interoperation.domain.InteropTransactionScenario;
import org.apache.fineract.interoperation.util.InteropUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class DateValidationTest {

    private final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
    private final FeignClientHelper clientHelper = new FeignClientHelper(fineractClient);
    private final FeignLoanHelper loanHelper = new FeignLoanHelper(fineractClient);
    private final FeignAccountHelper accountHelper = new FeignAccountHelper(fineractClient);
    private final FeignFixedDepositProductHelper fixedDepositProductHelper = new FeignFixedDepositProductHelper(fineractClient);
    private final FeignFixedDepositHelper fixedDepositHelper = new FeignFixedDepositHelper(fineractClient);

    @Test
    public void testShouldFailIfDateIsInvalid() {

        String invalidDate = "31 June 2022";

        Long clientId = clientHelper.createClient(ClientRequestBuilders.defaultClient()).getClientId();

        final Long loanProductID = loanHelper.createLoanProduct(new LoanProductTestBuilder().withPrincipal("1000")
                .withRepaymentTypeAsMonth().withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth()
                .withinterestRatePerPeriod("0").withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment()
                .withInterestTypeAsDecliningBalance().withAccountingRuleAsNone().withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0").withInArrearsTolerance("1001").withMultiDisburse()
                .withDisallowExpectedDisbursements(true).buildRequest(null)).getResourceId();

        final PostLoansRequest loanApplication = new PostLoansRequest().clientId(clientId).productId(loanProductID)
                .principal(new BigDecimal("1000")).loanTermFrequency(1).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                .numberOfRepayments(1).repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                .interestRatePerPeriod(BigDecimal.ZERO).interestType(LoanTestData.InterestType.FLAT)
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)
                .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.MIFOS_STANDARD_STRATEGY)
                .expectedDisbursementDate(invalidDate).submittedOnDate("01 March 2022").loanType("individual")
                .maxOutstandingLoanBalance(new BigDecimal("36000")).collateral(List.of()).locale("en_GB").dateFormat("dd MMMM yyyy");
        CallFailedRuntimeException exception = loanHelper.applyForLoanExpectingError(loanApplication);
        assertEquals(400, exception.getStatus());
        assertEquals(
                "The parameter `expectedDisbursementDate` is invalid based on the dateFormat: `dd MMMM yyyy` and locale: `en_GB` provided:",
                FeignErrors.firstError(exception).developerMessage());
    }

    @Test
    public void testShouldFailWithInvalidDateTime() {
        String requestCode = UUID.randomUUID().toString();
        InteropTransactionRole role = InteropTransactionRole.PAYER;
        Map<String, Object> request = buildRequestBody(requestCode, role);
        CallFailedRuntimeException exception = fail(
                () -> fineractClient.create(InteropTransactionRequestsApi.class).createTransactionRequest(request));
        assertEquals(400, exception.getStatus());
        assertEquals("The parameter `expiration` is invalid based on the dateFormat: `dd MMMM yyyy HH:mm:ss` and locale: `en` provided:",
                FeignErrors.firstError(exception).developerMessage());
    }

    @Test
    public void testShouldFailWithInvalidMonthDay() {
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter(Locale.US);

        LocalDate todaysDate = LocalDate.now(ZoneId.systemDefault()).minusMonths(3);
        final String VALID_FROM = todaysDate.format(formatter);
        todaysDate = todaysDate.plusYears(10);
        final String VALID_TO = todaysDate.format(formatter);

        todaysDate = LocalDate.now(ZoneId.systemDefault()).minusMonths(1);
        final String SUBMITTED_ON_DATE = todaysDate.format(formatter);

        Long clientId = clientHelper.createClient(ClientRequestBuilders.defaultClient()).getClientId();
        Assertions.assertNotNull(clientId);

        Long fixedDepositProductId = createFixedDepositProduct(VALID_FROM, VALID_TO, assetAccount, liabilityAccount, incomeAccount,
                expenseAccount);
        Assertions.assertNotNull(fixedDepositProductId);

        final Integer maturityInstructionId = 400;
        CallFailedRuntimeException exception = fixedDepositHelper.submitApplicationExpectingError(
                fixedDepositApplication(clientId, fixedDepositProductId, SUBMITTED_ON_DATE, maturityInstructionId));
        assertEquals(400, exception.getStatus());
        assertEquals("The parameter `feeOnMonthDay` is invalid based on the monthDayFormat: `dd MMM` and locale: `en_GB` provided:",
                FeignErrors.firstError(exception).developerMessage());
    }

    private Map<String, Object> buildRequestBody(final String requestCode, final InteropTransactionRole role) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(InteropUtil.PARAM_TRANSACTION_CODE, UUID.randomUUID().toString());
        map.put(InteropUtil.PARAM_REQUEST_CODE, requestCode);
        map.put(InteropUtil.PARAM_ACCOUNT_ID, UUID.randomUUID().toString());
        map.put(InteropUtil.PARAM_TRANSACTION_ROLE, role);
        map.put(InteropUtil.PARAM_EXPIRATION, "31 November 2022 11:11:11");
        map.put(InteropUtil.PARAM_LOCALE, "en");
        map.put(InteropUtil.PARAM_DATE_FORMAT, "dd MMMM yyyy HH:mm:ss");

        HashMap<String, Object> amountMap = new HashMap<>();
        amountMap.put(InteropUtil.PARAM_AMOUNT, "10");
        amountMap.put(InteropUtil.PARAM_CURRENCY, "EUR");
        map.put(InteropUtil.PARAM_AMOUNT, amountMap);

        HashMap<String, Object> typeMap = new HashMap<>();
        typeMap.put(InteropUtil.PARAM_SCENARIO, InteropTransactionScenario.PAYMENT);
        typeMap.put(InteropUtil.PARAM_INITIATOR, InteropTransactionRole.PAYEE);
        typeMap.put(InteropUtil.PARAM_INITIATOR_TYPE, InteropInitiatorType.CONSUMER);
        map.put(InteropUtil.PARAM_TRANSACTION_TYPE, typeMap);

        return map;
    }

    private Long createFixedDepositProduct(final String validFrom, final String validTo, Account assetAccount, Account liabilityAccount,
            Account incomeAccount, Account expenseAccount) {
        log.info("------------------------------CREATING NEW FIXED DEPOSIT PRODUCT ---------------------------------------");
        PostFixedDepositProductsRequest request = DepositRequestBuilders.withCashBasedAccounting(
                DepositRequestBuilders.fixedDepositProduct(), assetAccount, liabilityAccount, incomeAccount, expenseAccount);
        request = DepositRequestBuilders.withChart(request, validFrom, validTo, DepositTestData.periodRangeChartSlabs());
        return fixedDepositProductHelper.createProduct(request).getResourceId();
    }

    private PostFixedDepositAccountsRequest fixedDepositApplication(final Long clientId, final Long productId, final String submittedOnDate,
            final Integer maturityInstructionId) {
        log.info("--------------------------------APPLYING FOR FIXED DEPOSIT ACCOUNT --------------------------------");
        return DepositRequestBuilders
                .fixedDepositAccount(clientId, productId, submittedOnDate, DepositTestData.PreClosurePenalInterestOnType.WHOLE_TERM)
                .locale("en_GB").monthDayFormat("dd MMM").maturityInstructionId(maturityInstructionId)
                .charges(List.of(new PostFixedDepositAccountsCharges().feeOnMonthDay("31 June")));
    }
}
