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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.fineract.client.feign.FeignException;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.AllowAttributeOverrides;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.BuyDownFeeAmortizationDetails;
import org.apache.fineract.client.models.CapitalizedIncomeDetails;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.DeleteLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.DeleteLoansLoanIdResponse;
import org.apache.fineract.client.models.DisbursementDetail;
import org.apache.fineract.client.models.GetDelinquencyTagHistoryResponse;
import org.apache.fineract.client.models.GetJournalEntriesTransactionIdResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoanProductsTemplateResponse;
import org.apache.fineract.client.models.GetLoansApprovalTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdChargesTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.GetLoansResponse;
import org.apache.fineract.client.models.LoanApprovedAmountHistoryData;
import org.apache.fineract.client.models.LoanCapitalizedIncomeData;
import org.apache.fineract.client.models.LoanPointInTimeData;
import org.apache.fineract.client.models.LoanScheduleData;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostCreateRescheduleLoansRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostUpdateRescheduleLoansRequest;
import org.apache.fineract.client.models.PutChargeTransactionChangesRequest;
import org.apache.fineract.client.models.PutChargeTransactionChangesResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.client.models.PutLoansApprovedAmountResponse;
import org.apache.fineract.client.models.PutLoansAvailableDisbursementAmountRequest;
import org.apache.fineract.client.models.PutLoansAvailableDisbursementAmountResponse;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdChargesChargeIdResponse;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdResponse;
import org.apache.fineract.client.models.RetrieveLoansPointInTimeRequest;
import org.apache.fineract.client.models.TransactionType;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBatchHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCodeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalEventHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignJournalEntryHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSchedulerHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanProductTemplates;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestAccounts;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestValidators;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.error.ErrorResponse;
import org.apache.fineract.integrationtests.common.externalevents.BusinessEvent;
import org.apache.fineract.integrationtests.common.externalevents.ExternalEventsExtension;
import org.apache.fineract.integrationtests.common.loans.LoanAccountLockHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({ LoanTestLifecycleExtension.class, ExternalEventsExtension.class })
public abstract class FeignLoanTestBase extends FeignIntegrationTest implements LoanProductTemplates {

    protected static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(LoanTestData.DATETIME_PATTERN, Locale.ENGLISH);
    protected static final String DATETIME_PATTERN = LoanTestData.DATETIME_PATTERN;

    protected static FeignAccountHelper accountHelper;
    protected static FeignLoanHelper loanHelper;
    protected static FeignTransactionHelper transactionHelper;
    protected static FeignJournalEntryHelper journalHelper;
    protected static FeignBusinessDateHelper businessDateHelper;
    protected static FeignClientHelper clientHelper;
    protected static FeignChargesHelper chargesHelper;
    protected static FeignCodeHelper codeHelper;
    protected static FeignGlobalConfigurationHelper globalConfigurationHelper;
    protected static FeignSchedulerHelper schedulerHelper;
    protected static FeignExternalEventHelper externalEventHelper;
    protected static LoanTestAccounts accounts;

    private FineractFeignClient activeBatchClient = FineractFeignClientHelper.getFineractFeignClient();
    private FineractFeignClient nonByPassClient;

    @BeforeAll
    public static void setupHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        accountHelper = new FeignAccountHelper(client);
        loanHelper = new FeignLoanHelper(client);
        transactionHelper = new FeignTransactionHelper(client);
        journalHelper = new FeignJournalEntryHelper(client);
        businessDateHelper = new FeignBusinessDateHelper(client);
        clientHelper = new FeignClientHelper(client);
        chargesHelper = new FeignChargesHelper(client);
        codeHelper = new FeignCodeHelper(client);
        globalConfigurationHelper = new FeignGlobalConfigurationHelper(client);
        schedulerHelper = new FeignSchedulerHelper(client);
        externalEventHelper = new FeignExternalEventHelper(client);
    }

    protected LoanTestAccounts getAccounts() {
        if (accounts == null) {
            accounts = new LoanTestAccounts(accountHelper);
        }
        return accounts;
    }

    @Override
    public Long getAssetAccountId(String accountName) {
        return getAccounts().getAssetAccountId(accountName);
    }

    @Override
    public Long getLiabilityAccountId(String accountName) {
        return getAccounts().getLiabilityAccountId(accountName);
    }

    @Override
    public Long getIncomeAccountId(String accountName) {
        return getAccounts().getIncomeAccountId(accountName);
    }

    @Override
    public Long getExpenseAccountId(String accountName) {
        return getAccounts().getExpenseAccountId(accountName);
    }

    protected Long createClient() {
        return clientHelper.createClient();
    }

    protected Long createClient(String activationDate) {
        return clientHelper.createClient(activationDate);
    }

    protected Long createLoanProduct(PostLoanProductsRequest request) {
        return loanHelper.createLoanProduct(request).getResourceId();
    }

    protected Long createLoanProductFromJson(String loanProductJson) {
        return loanHelper.createLoanProductFromJson(loanProductJson);
    }

    protected GetLoanProductsProductIdResponse retrieveLoanProduct(Long productId) {
        return loanHelper.retrieveLoanProduct(productId);
    }

    protected PutLoanProductsProductIdResponse updateLoanProduct(Long productId, PutLoanProductsProductIdRequest request) {
        return loanHelper.updateLoanProduct(productId, request);
    }

    protected GetLoanProductsTemplateResponse getLoanProductTemplate(boolean isProductMixTemplate) {
        return loanHelper.getLoanProductTemplate(isProductMixTemplate);
    }

    protected PutLoanProductsProductIdRequest update4IProgressive(String name, String shortName, Long delinquencyBucketId) {
        return new PutLoanProductsProductIdRequest().name(name).shortName(shortName).description("4 installment product - progressive")//
                .includeInBorrowerCycle(false)//
                .useBorrowerCycle(false)//
                .currencyCode("EUR")//
                .digitsAfterDecimal(2)//
                .principal(1000.0)//
                .minPrincipal(100.0)//
                .maxPrincipal(10000.0)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS_L.intValue())//
                .interestRatePerPeriod(10D)//
                .minInterestRatePerPeriod(0D)//
                .maxInterestRatePerPeriod(120D)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                .isLinkedToFloatingInterestRates(false)//
                .allowVariableInstallments(false)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_INSTALLMENTS)//
                .interestType(LoanTestData.InterestType.DECLINING_BALANCE)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY)//
                .allowPartialPeriodInterestCalculation(false)//
                .transactionProcessingStrategyCode(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                .paymentAllocation(List.of(createDefaultPaymentAllocation("NEXT_INSTALLMENT")))//
                .creditAllocation(List.of())//
                .overdueDaysForNPA(179)//
                .daysInMonthType(30L)//
                .daysInYearType(360L)//
                .isInterestRecalculationEnabled(true)//
                .interestRecalculationCompoundingMethod(0)//
                .rescheduleStrategyMethod(LoanTestData.RescheduleStrategyMethod.ADJUST_LAST_UNPAID_PERIOD)//
                .recalculationRestFrequencyType(LoanTestData.RecalculationRestFrequencyType.DAILY)//
                .recalculationRestFrequencyInterval(1)//
                .isArrearsBasedOnOriginalSchedule(false)//
                .isCompoundingToBePostedAsTransaction(false)//
                .preClosureInterestCalculationStrategy(1)//
                .allowCompoundingOnEod(false)//
                .canDefineInstallmentAmount(true)//
                .repaymentStartDateType(1)//
                .charges(List.of())//
                .principalVariationsForBorrowerCycle(List.of())//
                .interestRateVariationsForBorrowerCycle(List.of())//
                .numberOfRepaymentVariationsForBorrowerCycle(List.of())//
                .accountingRule(3)//
                .canUseForTopup(false)//
                .fundSourceAccountId(getAccounts().getFundSource().getAccountID().longValue())//
                .loanPortfolioAccountId(getAccounts().getLoansReceivableAccount().getAccountID().longValue())//
                .transfersInSuspenseAccountId(getAccounts().getSuspenseAccount().getAccountID().longValue())//
                .interestOnLoanAccountId(getAccounts().getInterestIncomeAccount().getAccountID().longValue())//
                .incomeFromFeeAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())//
                .incomeFromPenaltyAccountId(getAccounts().getPenaltyIncomeAccount().getAccountID().longValue())//
                .incomeFromRecoveryAccountId(getAccounts().getRecoveriesAccount().getAccountID().longValue())//
                .writeOffAccountId(getAccounts().getWrittenOffAccount().getAccountID().longValue())//
                .overpaymentLiabilityAccountId(getAccounts().getOverpaymentAccount().getAccountID().longValue())//
                .receivableInterestAccountId(getAccounts().getInterestReceivableAccount().getAccountID().longValue())//
                .receivableFeeAccountId(getAccounts().getFeeReceivableAccount().getAccountID().longValue())//
                .receivablePenaltyAccountId(getAccounts().getPenaltyReceivableAccount().getAccountID().longValue())//
                .goodwillCreditAccountId(getAccounts().getGoodwillExpenseAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(getAccounts().getInterestIncomeChargeOffAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(getAccounts().getInterestIncomeChargeOffAccount().getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(getAccounts().getPenaltyChargeOffAccount().getAccountID().longValue())//
                .chargeOffExpenseAccountId(getAccounts().getChargeOffExpenseAccount().getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(getAccounts().getChargeOffFraudExpenseAccount().getAccountID().longValue())//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale("en")//
                .enableAccrualActivityPosting(false)//
                .multiDisburseLoan(true)//
                .maxTrancheCount(10)//
                .outstandingLoanBalance(10000.0)//
                .disallowExpectedDisbursements(true)//
                .allowApprovedDisbursedAmountsOverApplied(true)//
                .overAppliedCalculationType("percentage")//
                .overAppliedNumber(50)//
                .principalThresholdForLastInstallment(50)//
                .holdGuaranteeFunds(false)//
                .accountMovesOutOfNPAOnlyOnArrearsCompletion(false)//
                .allowAttributeOverrides(new AllowAttributeOverrides()//
                        .amortizationType(true)//
                        .interestType(true)//
                        .transactionProcessingStrategyCode(true)//
                        .interestCalculationPeriodType(true)//
                        .inArrearsTolerance(true)//
                        .repaymentEvery(true)//
                        .graceOnPrincipalAndInterestPayment(true)//
                        .graceOnArrearsAgeing(true)//
                ).isEqualAmortization(false)//
                .delinquencyBucketId(delinquencyBucketId)//
                .enableDownPayment(false)//
                .enableInstallmentLevelDelinquency(false)//
                .loanScheduleType("PROGRESSIVE")//
                .loanScheduleProcessingType("HORIZONTAL");
    }

    protected Long applyForLoan(PostLoansRequest request) {
        return loanHelper.applyForLoan(request).getLoanId();
    }

    protected PostLoansResponse calculateLoanSchedule(PostLoansRequest request) {
        return loanHelper.calculateLoanSchedule(request);
    }

    protected Long applyForLoanFromJson(String loanApplicationJson) {
        return loanHelper.applyForLoanFromJson(loanApplicationJson);
    }

    protected PostLoansLoanIdResponse approveLoan(Long loanId, PostLoansLoanIdRequest request) {
        return loanHelper.approveLoan(loanId, request);
    }

    protected GetLoansResponse retrieveAllLoans(String accountNumber, String associations, Long clientId) {
        return loanHelper.retrieveAllLoans(accountNumber, associations, clientId);
    }

    protected PostLoansLoanIdRequest approveLoanRequest(Double amount, String approvalDate) {
        return LoanRequestBuilders.approveLoan(amount, approvalDate);
    }

    protected PostLoansLoanIdRequest approveLoanRequest(Double amount, String approvalDate, String expectedDisbursementDate) {
        return LoanRequestBuilders.approveLoan(amount, approvalDate, expectedDisbursementDate);
    }

    protected PostLoansLoanIdResponse disburseLoan(Long loanId, PostLoansLoanIdRequest request) {
        return loanHelper.disburseLoan(loanId, request);
    }

    protected GetLoansLoanIdResponse getLoanDetails(Long loanId) {
        return loanHelper.getLoanDetails(loanId);
    }

    protected void undoApproval(Long loanId) {
        loanHelper.undoApproval(loanId);
    }

    protected void undoDisbursement(Long loanId) {
        loanHelper.undoDisbursement(loanId);
    }

    protected void undoLastDisbursement(Long loanId) {
        loanHelper.undoLastDisbursement(loanId, new PostLoansLoanIdRequest());
    }

    protected void undoLastDisbursement(Long loanId, PostLoansLoanIdRequest request) {
        loanHelper.undoLastDisbursement(loanId, request);
    }

    protected PostLoansLoanIdResponse disburseToSavings(Long loanId, PostLoansLoanIdRequest request) {
        return loanHelper.disburseToSavings(loanId, request);
    }

    protected PostLoansLoanIdResponse rejectLoan(Long loanId, PostLoansLoanIdRequest request) {
        return loanHelper.rejectLoan(loanId, request);
    }

    protected PostLoansLoanIdResponse withdrawLoan(Long loanId, PostLoansLoanIdRequest request) {
        return loanHelper.withdrawLoan(loanId, request);
    }

    protected PostLoansLoanIdResponse moveLoanState(Long loanId, PostLoansLoanIdRequest request, String command) {
        return loanHelper.moveLoanState(loanId, request, command);
    }

    protected PostLoansLoanIdTransactionsResponse closeLoan(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return loanHelper.closeLoan(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse forecloseLoan(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return loanHelper.forecloseLoan(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse forecloseLoan(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.forecloseLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdChargesResponse addLoanCharge(Long loanId, PostLoansLoanIdChargesRequest request) {
        return loanHelper.addLoanCharge(loanId, request);
    }

    protected PostChargesResponse createCharge(Double amount) {
        return chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(amount));
    }

    protected PostChargesResponse createCharge(Double amount, String currencyCode) {
        return chargesHelper.createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(amount, currencyCode));
    }

    protected PostLoansLoanIdChargesResponse addLoanCharge(Long loanId, Long chargeId, String date, Double amount) {
        return addLoanCharge(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeId).amount(amount).dueDate(date)
                .dateFormat("dd MMMM yyyy").locale("en"));
    }

    protected List<GetLoansLoanIdChargesChargeIdResponse> getLoanCharges(Long loanId) {
        return loanHelper.getLoanCharges(loanId);
    }

    protected GetLoansLoanIdChargesChargeIdResponse getLoanCharge(Long loanId, Long loanChargeId) {
        return loanHelper.getLoanCharge(loanId, loanChargeId);
    }

    protected DeleteLoansLoanIdChargesChargeIdResponse deleteLoanCharge(Long loanId, Long loanChargeId) {
        return loanHelper.deleteLoanCharge(loanId, loanChargeId);
    }

    protected DeleteLoansLoanIdChargesChargeIdResponse deleteLoanCharge(Long loanId, String loanChargeExternalId) {
        return loanHelper.deleteLoanCharge(loanId, loanChargeExternalId);
    }

    protected PostLoansLoanIdChargesChargeIdResponse waiveLoanCharge(Long loanId, Long loanChargeId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.waiveLoanCharge(loanId, loanChargeId, request);
    }

    protected void waiveLoanCharge(Long loanId, Long loanChargeId, Integer installmentNumber) {
        waiveLoanCharge(loanId, loanChargeId,
                new PostLoansLoanIdChargesChargeIdRequest().locale(LoanTestData.LOCALE).installmentNumber(installmentNumber.longValue()));
    }

    protected PostLoansLoanIdChargesChargeIdResponse payLoanCharge(Long loanId, Long loanChargeId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.payLoanCharge(loanId, loanChargeId, request);
    }

    protected PostLoansLoanIdChargesChargeIdResponse chargeAdjustment(Long loanId, Long loanChargeId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.adjustLoanCharge(loanId, loanChargeId, request);
    }

    protected Long createLoanSpecifiedDueDateCharge(double amount) {
        return chargesHelper.createLoanSpecifiedDueDateCharge(amount).getResourceId();
    }

    protected Long createLoanDisbursementCharge(double amount) {
        return chargesHelper.createLoanDisbursementCharge(amount).getResourceId();
    }

    protected Long addRepayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.addRepayment(loanId, request).getResourceId();
    }

    protected Long addInterestWaiver(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.addInterestWaiver(loanId, request).getResourceId();
    }

    protected Long chargeOff(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.chargeOff(loanId, request).getResourceId();
    }

    protected Long addChargeback(Long loanId, Long transactionId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.addChargeback(loanId, transactionId, request).getResourceId();
    }

    protected void undoRepayment(Long loanId, Long transactionId, String transactionDate) {
        transactionHelper.undoRepayment(loanId, transactionId, transactionDate);
    }

    protected PostLoansLoanIdTransactionsResponse makeMerchantIssuedRefund(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeMerchantIssuedRefund(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makePayoutRefund(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makePayoutRefund(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makePayoutRefund(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makePayoutRefund(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeGoodwillCredit(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeGoodwillCredit(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeGoodwillCredit(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeGoodwillCredit(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeInterestPaymentWaiver(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeInterestPaymentWaiver(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeLoanRepayment(Long loanId, String command, String date, Double amount) {
        return transactionHelper.makeLoanRepayment(loanId, command, date, amount);
    }

    protected PostLoansLoanIdTransactionsResponse makeLoanRepayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeLoanRepayment(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeLoanRepayment(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeLoanRepayment(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse chargeOffLoan(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.chargeOffLoan(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeMerchantIssuedRefund(String loanExternalId,
            PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeMerchantIssuedRefund(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeCreditBalanceRefund(String loanExternalId,
            PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeCreditBalanceRefund(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse reverseLoanTransaction(String loanExternalId, Long transactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.reverseLoanTransaction(loanExternalId, transactionId, request);
    }

    protected PostLoansLoanIdTransactionsResponse chargebackLoanTransaction(String loanExternalId, String transactionExternalId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.chargebackLoanTransaction(loanExternalId, transactionExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse chargebackLoanTransaction(Long loanId, String transactionExternalId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.chargebackLoanTransaction(loanId, transactionExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse chargebackLoanTransaction(Long loanId, Long transactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.chargebackLoanTransaction(loanId, transactionId, request);
    }

    protected PostLoansLoanIdTransactionsResponse chargebackLoanTransaction(String loanExternalId, Long transactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.chargebackLoanTransaction(loanExternalId, transactionId, request);
    }

    protected Long applyChargebackTransaction(Long loanId, Long transactionId, Double amount, Long paymentTypeId) {
        return transactionHelper.applyChargebackTransaction(loanId, transactionId, amount, paymentTypeId).getResourceId();
    }

    protected PostLoansLoanIdTransactionsResponse makeLoanDownPayment(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeLoanDownPayment(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeLoanDownPayment(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeLoanDownPayment(loanExternalId, request);
    }

    protected GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransaction(Long loanId, Long transactionId) {
        return transactionHelper.getLoanTransaction(loanId, transactionId);
    }

    protected GetLoansLoanIdResponse getLoanDetails(String loanExternalId) {
        return loanHelper.getLoanDetailsByExternalId(loanExternalId);
    }

    protected void disburseLoanWithAmount(Long loanId, String date, double amount) {
        loanHelper.disburseLoanWithAmount(loanId, date, amount);
    }

    protected void reverseRepayment(Long loanId, Long transactionId, String transactionDate) {
        reverseLoanTransaction(loanId, transactionId, transactionDate);
    }

    protected void updateGlobalConfiguration(String configName, PutGlobalConfigurationsRequest request) {
        globalConfigurationHelper.updateGlobalConfiguration(configName, request);
    }

    protected void runPeriodicAccrualAccounting(String date) {
        PeriodicAccrualAccountingHelper.runPeriodicAccrualAccounting(date);
    }

    protected GetJournalEntriesTransactionIdResponse getJournalEntries(String transactionId) {
        return journalHelper.getJournalEntries(transactionId);
    }

    protected PostLoansLoanIdTransactionsResponse reverseLoanTransaction(String loanExternalId, String transactionExternalId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.reverseLoanTransaction(loanExternalId, transactionExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse reverseLoanTransaction(Long loanId, Long transactionId, String transactionDate) {
        return transactionHelper.reverseLoanTransaction(loanId, transactionId, transactionDate);
    }

    protected PostLoansLoanIdTransactionsResponse reverseLoanTransaction(Long loanId, Long transactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.reverseLoanTransaction(loanId, transactionId, request);
    }

    protected PostLoansLoanIdTransactionsResponse reverseLoanTransaction(Long loanId, String transactionExternalId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.reverseLoanTransaction(loanId, transactionExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeCreditBalanceRefund(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeCreditBalanceRefund(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse createManualInterestRefund(Long loanId, Long targetTransactionId, String transactionDate,
            Double amount, String externalId) {
        return transactionHelper.createManualInterestRefund(loanId, targetTransactionId, transactionDate, amount, externalId);
    }

    protected void undoLoanApproval(Long loanId) {
        undoApproval(loanId);
    }

    protected void rejectLoan(Long loanId, String rejectedOnDate) {
        rejectLoan(loanId, LoanRequestBuilders.rejectLoan(rejectedOnDate));
    }

    protected void changeLoanFraudState(Long loanId, boolean fraudState) {
        loanHelper.markAsFraud(loanId, fraudState);
    }

    protected Long getTransactionId(Long loanId, String type, String date) {
        GetLoansLoanIdResponse loan = getLoanDetails(loanId);
        return loan.getTransactions().stream().filter(tr -> Objects.equals(tr.getType().getValue(), type)
                && Objects.equals(tr.getDate(), LocalDate.parse(date, dateTimeFormatter))).findAny().orElseThrow().getId();
    }

    protected GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransactionDetails(Long loanId, Long transactionId) {
        return transactionHelper.getLoanTransactionDetails(loanId, transactionId);
    }

    protected GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransactionDetails(Long loanId, String transactionExternalId) {
        return transactionHelper.getLoanTransactionDetails(loanId, transactionExternalId);
    }

    protected void verifyTRJournalEntries(Long transactionId, LoanTestData.Journal... expectedEntries) {
        journalHelper.verifyTRJournalEntries(transactionId, expectedEntries);
    }

    protected void verifyJournalEntries(Long loanId, LoanTestData.Journal... expectedEntries) {
        journalHelper.verifyJournalEntries(loanId, expectedEntries);
    }

    protected void verifyJournalEntriesSequentially(Long loanId, LoanTestData.Journal... expectedEntries) {
        journalHelper.verifyJournalEntriesSequentially(loanId, expectedEntries);
    }

    protected void checkJournalEntryForAssetAccount(Account account, String date, LoanTestData.Journal... entries) {
        journalHelper.checkJournalEntryForAssetAccount(account, date, entries);
    }

    protected void checkJournalEntryForLiabilityAccount(Account account, String date, LoanTestData.Journal... entries) {
        journalHelper.checkJournalEntryForLiabilityAccount(account, date, entries);
    }

    protected void checkJournalEntryForIncomeAccount(Account account, String date, LoanTestData.Journal... entries) {
        journalHelper.checkJournalEntryForIncomeAccount(account, date, entries);
    }

    protected void checkJournalEntryForExpenseAccount(Account account, String date, LoanTestData.Journal... entries) {
        journalHelper.checkJournalEntryForExpenseAccount(account, date, entries);
    }

    protected static void assertErrorGlobalisationCode(CallFailedRuntimeException exception, String expectedCode) {
        assertEquals(expectedCode, extractErrorGlobalisationCode(exception));
    }

    protected static String extractErrorGlobalisationCode(CallFailedRuntimeException exception) {
        if (!(exception.getCause() instanceof FeignException feignException)) {
            return exception.getUserMessageGlobalisationCode();
        }
        String topLevelCode = feignException.getUserMessageGlobalisationCode();
        if (topLevelCode != null && !topLevelCode.equals("validation.msg.validation.errors.exist")
                && !topLevelCode.equals("validation.msg.domain.rule.violation")) {
            return topLevelCode;
        }
        try {
            Map<String, Object> body = ObjectMapperFactory.getShared().readValue(feignException.responseBodyAsString(),
                    new TypeReference<Map<String, Object>>() {});
            Object errors = body.get("errors");
            if (errors instanceof List<?> errorList && !errorList.isEmpty() && errorList.get(0) instanceof Map<?, ?> firstError) {
                return (String) firstError.get("userMessageGlobalisationCode");
            }
        } catch (Exception ignored) {
            // fall through to top-level code
        }
        return topLevelCode;
    }

    protected LoanTestData.Journal journalEntry(double amount, Account account, String type) {
        return "DEBIT".equals(type) ? LoanTestData.Journal.debit(account.getAccountID().longValue(), amount)
                : LoanTestData.Journal.credit(account.getAccountID().longValue(), amount);
    }

    protected GetLoansLoanIdTransactionsTemplateResponse getPrepayAmount(Long loanId, String date) {
        return transactionHelper.getPrepaymentAmount(loanId, date, LoanTestData.DATETIME_PATTERN);
    }

    protected Long verifyPrepayAmountByRepayment(Long loanId, String date) {
        GetLoansLoanIdTransactionsTemplateResponse prepayAmount = getPrepayAmount(loanId, date);
        Double amountToPrepayLoan = prepayAmount.getAmount() != null ? prepayAmount.getAmount().doubleValue() : null;
        Long repaymentId = null;
        if (amountToPrepayLoan != null && amountToPrepayLoan > 0) {
            PostLoansLoanIdTransactionsResponse repayment = transactionHelper.makeLoanRepayment(loanId, "repayment", date,
                    amountToPrepayLoan);
            assertNotNull(repayment);
            assertNotNull(repayment.getResourceId());
            repaymentId = repayment.getResourceId();
        }
        verifyLoanStatus(loanId, LoanStatus.CLOSED_OBLIGATIONS_MET);
        return repaymentId;
    }

    protected void runAt(String date, Runnable action) {
        businessDateHelper.runAt(date, detectDateFormat(date), action);
    }

    protected void updateBusinessDate(String type, String date) {
        businessDateHelper.updateBusinessDate(type, date, detectDateFormat(date));
    }

    protected void updateBusinessDate(String date) {
        updateBusinessDate("BUSINESS_DATE", date);
    }

    private static String detectDateFormat(String date) {
        return date.matches("\\d{4}-\\d{2}-\\d{2}") ? LoanTestData.ISO_DATE_PATTERN : LoanTestData.DATETIME_PATTERN;
    }

    protected void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double principalPaid, double principalOutstanding) {
        LoanTestValidators.validateRepaymentPeriod(loanDetails, index, dueDate, principalDue, principalPaid, principalOutstanding, 0.0,
                0.0);
    }

    protected void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double principalPaid, double principalOutstanding, double paidInAdvance, double paidLate) {
        LoanTestValidators.validateRepaymentPeriod(loanDetails, index, dueDate, principalDue, principalPaid, principalOutstanding,
                paidInAdvance, paidLate);
    }

    protected void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double feeDue, double penaltyDue, double interestDue) {
        LoanTestValidators.validateRepaymentPeriod(loanDetails, index, dueDate, principalDue, feeDue, penaltyDue, interestDue);
    }

    protected void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, double principalDue, double principalPaid,
            double principalOutstanding, double paidInAdvance, double paidLate) {
        LoanTestValidators.validateRepaymentPeriod(loanDetails, index, principalDue, principalPaid, principalOutstanding, paidInAdvance,
                paidLate);
    }

    protected void verifyLoanStatus(GetLoansLoanIdResponse loanDetails, Function<GetLoansLoanIdStatus, Boolean> extractor) {
        LoanTestValidators.verifyLoanStatus(loanDetails, extractor);
    }

    protected void verifyLoanStatus(GetLoansLoanIdResponse loanDetails, LoanStatus loanStatus) {
        assertEquals(loanStatus.getCode(), loanDetails.getStatus().getCode());
    }

    protected void verifyLoanStatus(long loanId, LoanStatus loanStatus) {
        verifyLoanStatus(getLoanDetails(loanId), loanStatus);
    }

    protected Long createApproveAndDisburseLoan(Long clientId, Long productId, String date, Double principal, Integer numberOfRepayments) {
        PostLoansRequest applyRequest = LoanRequestBuilders.applyLoan(clientId, productId, date, principal, numberOfRepayments);
        Long loanId = applyForLoan(applyRequest);

        PostLoansLoanIdRequest approveRequest = LoanRequestBuilders.approveLoan(principal, date);
        approveLoan(loanId, approveRequest);

        PostLoansLoanIdRequest disburseRequest = LoanRequestBuilders.disburseLoan(principal, date);
        disburseLoan(loanId, disburseRequest);

        return loanId;
    }

    protected Long createApproveAndDisburseProgressiveLoan(Long clientId, Long productId, String date, Double principal,
            Integer numberOfRepayments) {
        PostLoansRequest applyRequest = LoanRequestBuilders.applyProgressiveLoan(clientId, productId, date, principal, numberOfRepayments);
        Long loanId = applyForLoan(applyRequest);

        PostLoansLoanIdRequest approveRequest = LoanRequestBuilders.approveLoan(principal, date);
        approveLoan(loanId, approveRequest);

        PostLoansLoanIdRequest disburseRequest = LoanRequestBuilders.disburseLoan(principal, date);
        disburseLoan(loanId, disburseRequest);

        return loanId;
    }

    protected Long createApprovedLoan(Long clientId, Long productId, String date, Double principal, Integer numberOfRepayments) {
        PostLoansRequest applyRequest = LoanRequestBuilders.applyLoan(clientId, productId, date, principal, numberOfRepayments);
        Long loanId = applyForLoan(applyRequest);

        PostLoansLoanIdRequest approveRequest = LoanRequestBuilders.approveLoan(principal, date);
        approveLoan(loanId, approveRequest);

        return loanId;
    }

    protected LoanTestData.Journal debit(Long glAccountId, double amount) {
        return LoanTestData.Journal.debit(glAccountId, amount);
    }

    protected LoanTestData.Journal credit(Long glAccountId, double amount) {
        return LoanTestData.Journal.credit(glAccountId, amount);
    }

    protected LoanTestData.Journal debit(Account account, double amount) {
        return new LoanTestData.Journal(amount, account, "DEBIT");
    }

    protected LoanTestData.Journal credit(Account account, double amount) {
        return new LoanTestData.Journal(amount, account, "CREDIT");
    }

    protected PostLoansLoanIdTransactionsRequest repayment(double amount, String date) {
        return LoanRequestBuilders.repayLoan(amount, date);
    }

    protected PostLoansLoanIdTransactionsRequest waiveInterest(double amount, String date) {
        return LoanRequestBuilders.waiveInterest(amount, date);
    }

    protected PostLoansLoanIdTransactionsRequest chargeOff(String date) {
        return LoanRequestBuilders.chargeOff(date);
    }

    protected void executeInlineCOB(Long loanId) {
        transactionHelper.executeInlineCOB(loanId);
    }

    protected void executeInlineCOB(List<Long> loanIds) {
        transactionHelper.executeInlineCOB(loanIds);
    }

    protected void placeHardLockOnLoan(Long loanId) {
        LoanAccountLockHelper.placeSoftLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING");
    }

    /** A lock that carries an error message is a hard lock: it stays until a catch-up COB clears it. */
    protected void placeHardLockOnLoan(Long loanId, String error) {
        LoanAccountLockHelper.placeSoftLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING", error);
    }

    protected void verifyLastClosedBusinessDate(Long loanId, String lastClosedBusinessDate) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertNotNull(loanDetails.getLastClosedBusinessDate());
        assertEquals(lastClosedBusinessDate, loanDetails.getLastClosedBusinessDate().format(dateTimeFormatter));
    }

    /**
     * Runs the given action with batch requests issued as a user that lacks the loan-checker bypass permission, so
     * loan-lock enforcement is not bypassed. Mirrors {@code BaseLoanIntegrationTest.runAsNonByPass}.
     */
    protected void runAsNonByPass(Runnable runnable) {
        FineractFeignClient previous = activeBatchClient;
        activeBatchClient = getNonByPassClient();
        try {
            runnable.run();
        } finally {
            activeBatchClient = previous;
        }
    }

    private FineractFeignClient getNonByPassClient() {
        if (nonByPassClient == null) {
            nonByPassClient = FeignUserHelper.getSimpleUserWithoutBypassPermissionClient();
        }
        return nonByPassClient;
    }

    protected BatchRequestBuilder batchRequest() {
        return new BatchRequestBuilder(new FeignBatchHelper(activeBatchClient));
    }

    public static final class BatchRequestBuilder {

        private final FeignBatchHelper batchHelper;
        private final List<BatchRequest> requests = new ArrayList<>();

        private BatchRequestBuilder(FeignBatchHelper batchHelper) {
            this.batchHelper = batchHelper;
        }

        public BatchRequestBuilder rescheduleLoan(Long requestId, Long loanId, String submittedOnDate, String rescheduleFromDate,
                String adjustedDueDate) {
            requests.add(new BatchRequest().requestId(requestId).relativeUrl("rescheduleloans").method("POST").body("""
                        {
                            "loanId": %d,
                            "rescheduleFromDate": "%s",
                            "rescheduleReasonId": 1,
                            "submittedOnDate": "%s",
                            "rescheduleReasonComment": "",
                            "adjustedDueDate": "%s",
                            "graceOnPrincipal": "",
                            "graceOnInterest": "",
                            "extraTerms": "",
                            "newInterestRate": "",
                            "dateFormat": "%s",
                            "locale": "en"
                        }
                    """.formatted(loanId, rescheduleFromDate, submittedOnDate, adjustedDueDate, DATETIME_PATTERN)));
            return this;
        }

        public BatchRequestBuilder approveRescheduleLoan(Long requestId, Long rescheduleBatchRequestId, String approvedOnDate) {
            requests.add(new BatchRequest().requestId(requestId).relativeUrl("rescheduleloans/$.resourceId?command=approve").method("POST")
                    .reference(rescheduleBatchRequestId).body("""
                                {
                                    "approvedOnDate": "%s",
                                    "dateFormat": "%s",
                                    "locale": "en"
                                }
                            """.formatted(approvedOnDate, DATETIME_PATTERN)));
            return this;
        }

        public BatchRequestBuilder repayLoan(Long requestId, Long loanId, String amount, String transactionDate) {
            requests.add(new BatchRequest().requestId(requestId).relativeUrl("v1/loans/" + loanId + "/transactions?command=repayment")
                    .method("POST").body("""
                                {
                                    "locale": "en",
                                    "dateFormat": "%s",
                                    "transactionDate": "%s",
                                    "transactionAmount": %s
                                }
                            """.formatted(DATETIME_PATTERN, transactionDate, amount)));
            return this;
        }

        /**
         * Repayment batch request using the pre-versioned {@code loans/{id}/transactions} relative URL (without the
         * {@code v1/} prefix), to exercise the batch API's backward compatibility with legacy relative URLs.
         */
        public BatchRequestBuilder repayLoanLegacyRelativeUrl(Long requestId, Long loanId, String amount, String transactionDate) {
            requests.add(new BatchRequest().requestId(requestId).relativeUrl("loans/" + loanId + "/transactions?command=repayment")
                    .method("POST").body("""
                                {
                                    "locale": "en",
                                    "dateFormat": "%s",
                                    "transactionDate": "%s",
                                    "transactionAmount": %s
                                }
                            """.formatted(DATETIME_PATTERN, transactionDate, amount)));
            return this;
        }

        public List<BatchResponse> executeEnclosingTransaction() {
            return batchHelper.executeEnclosingTransaction(requests);
        }

        public List<BatchResponse> executeWithoutEnclosingTransaction() {
            return batchHelper.executeWithoutEnclosingTransaction(requests);
        }

        public ErrorResponse executeEnclosingTransactionError() {
            return batchHelper.executeWithoutEnclosingTransactionExpectingError(requests);
        }
    }

    protected void addCapitalizedIncome(Long loanId, String transactionDate, double amount) {
        transactionHelper.addCapitalizedIncome(loanId, transactionDate, amount);
    }

    protected PostLoansLoanIdTransactionsResponse addCapitalizedIncomeTransaction(Long loanId, String transactionDate, double amount) {
        return transactionHelper.addCapitalizedIncome(loanId, transactionDate, amount);
    }

    protected PostLoansLoanIdTransactionsResponse addCapitalizedIncomeTransaction(Long loanId, String transactionDate, double amount,
            Long classificationId) {
        return transactionHelper.addCapitalizedIncome(loanId, transactionDate, amount, classificationId);
    }

    protected PostLoansLoanIdTransactionsResponse capitalizedIncomeAdjustment(Long loanId, Long capitalizedIncomeTransactionId,
            String transactionDate, double amount) {
        return transactionHelper.capitalizedIncomeAdjustment(loanId, capitalizedIncomeTransactionId, transactionDate, amount);
    }

    protected List<CapitalizedIncomeDetails> fetchCapitalizedIncomeDetails(Long loanId) {
        return transactionHelper.fetchCapitalizedIncomeDetails(loanId);
    }

    protected LoanCapitalizedIncomeData fetchLoanCapitalizedIncomeData(Long loanId) {
        return transactionHelper.fetchLoanCapitalizedIncomeData(loanId);
    }

    protected PostLoansLoanIdTransactionsResponse makeLoanBuyDownFee(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeLoanBuyDownFee(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeLoanBuyDownFee(Long loanId, String date, double amount) {
        return transactionHelper.makeLoanBuyDownFee(loanId, date, amount);
    }

    protected PostLoansLoanIdTransactionsResponse buyDownFeeAdjustment(Long loanId, Long buyDownFeeTransactionId, String transactionDate,
            double amount) {
        return transactionHelper.buyDownFeeAdjustment(loanId, buyDownFeeTransactionId, transactionDate, amount);
    }

    protected PostLoansLoanIdTransactionsResponse buyDownFeeAdjustment(Long loanId, Long buyDownFeeTransactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.buyDownFeeAdjustment(loanId, buyDownFeeTransactionId, request);
    }

    protected List<BuyDownFeeAmortizationDetails> fetchBuyDownFeeAmortizationDetails(Long loanId) {
        return transactionHelper.fetchBuyDownFeeAmortizationDetails(loanId);
    }

    protected PutLoansApprovedAmountResponse modifyLoanApprovedAmount(Long loanId, BigDecimal approvedAmount) {
        return loanHelper.modifyApprovedAmount(loanId, approvedAmount);
    }

    protected List<LoanApprovedAmountHistoryData> getLoanApprovedAmountHistory(Long loanId) {
        return loanHelper.getLoanApprovedAmountHistory(loanId);
    }

    protected PutLoansAvailableDisbursementAmountResponse modifyLoanAvailableDisbursementAmount(Long loanId, BigDecimal amount) {
        return loanHelper.modifyAvailableDisbursementAmount(loanId,
                new PutLoansAvailableDisbursementAmountRequest().amount(amount).locale("en"));
    }

    protected PostLoansLoanIdResponse undoDisbursement(Long loanId, PostLoansLoanIdRequest request) {
        return loanHelper.undoDisbursement(loanId, request);
    }

    protected void verifyBusinessEvents(BusinessEvent... businessEvents) {
        assertNotNull(businessEvents);
        Awaitility.await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            List<ExternalEventResponse> allExternalEvents = externalEventHelper.getAllExternalEvents();
            assertNotNull(allExternalEvents);
            assertTrue(businessEvents.length <= allExternalEvents.size(), "Expected business event count is less than actual. Expected: "
                    + businessEvents.length + " Actual: " + allExternalEvents.size());
            for (BusinessEvent businessEvent : businessEvents) {
                long count = allExternalEvents.stream().filter(externalEvent -> businessEvent.verify(externalEvent, dateTimeFormatter))
                        .count();
                assertEquals(1, count, "Expected business event not found " + businessEvent);
            }
        });
    }

    protected Integer getLoanProductId(String loanProductJson) {
        return createLoanProductFromJson(loanProductJson).intValue();
    }

    protected PostLoansResponse applyForLoanApplication(Integer clientId, Integer loanProductId, String externalId) {
        return applyForLoanApplication(clientId, loanProductId, externalId, null);
    }

    protected PostLoansResponse applyForLoanApplication(Integer clientId, Integer loanProductId, String externalId, String linkAccountId) {
        final String loanApplicationJSON = new org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder()
                .withPrincipal("1000").withLoanTermFrequency("1").withLoanTermFrequencyAsMonths().withNumberOfRepayments("1")
                .withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0")
                .withInterestTypeAsDecliningBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 September 2022")
                .withSubmittedOnDate("01 September 2022").withLoanType("individual").withInArrearsTolerance("1001")
                .withExternalId(externalId).build(clientId.toString(), loanProductId.toString(), linkAccountId);
        return getLoanIdFromApplication(loanApplicationJSON);
    }

    protected PostLoansResponse getLoanIdFromApplication(String loanApplicationJson) {
        Long loanId = applyForLoanFromJson(loanApplicationJson);
        PostLoansResponse result = new PostLoansResponse();
        result.setResourceId(loanId);
        result.setResourceExternalId(getLoanDetails(loanId).getExternalId());
        return result;
    }

    protected PostLoansLoanIdResponse disburseLoan(String date, Integer loanId, String transactionAmount, String externalId) {
        return loanHelper.disburseLoanWithExternalId(date, loanId.longValue(), transactionAmount, externalId);
    }

    protected PostLoansLoanIdResponse disburseLoan(String date, Integer loanId, String transactionAmount) {
        return loanHelper.disburseLoan(date, loanId.longValue(), transactionAmount);
    }

    protected PostLoansLoanIdChargesResponse addChargesForLoan(Long loanId, PostLoansLoanIdChargesRequest request) {
        return loanHelper.addChargesForLoan(loanId, request);
    }

    protected Long addChargesForLoan(Integer loanId, String chargeJson) {
        Gson gson = new Gson();
        HashMap<String, Object> chargeMap = gson.fromJson(chargeJson, new TypeToken<HashMap<String, Object>>() {}.getType());
        PostLoansLoanIdChargesRequest request = new PostLoansLoanIdChargesRequest();
        if (chargeMap.get("chargeId") != null) {
            request.chargeId(Long.parseLong(chargeMap.get("chargeId").toString()));
        }
        if (chargeMap.get("amount") != null) {
            request.amount(Double.valueOf(chargeMap.get("amount").toString()));
        }
        if (chargeMap.get("dueDate") != null) {
            request.dueDate(chargeMap.get("dueDate").toString());
        }
        if (chargeMap.get("externalId") != null) {
            request.externalId(chargeMap.get("externalId").toString());
        }
        request.dateFormat("dd MMMM yyyy").locale("en");
        return loanHelper.addChargesForLoan(loanId.longValue(), request).getResourceId();
    }

    protected PostLoansLoanIdTransactionsResponse makeWriteoff(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return writeOffLoan(loanExternalId, request);
    }

    protected GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransactionDetails(String loanExternalId, Long transactionId) {
        return transactionHelper.getLoanTransactionDetails(loanExternalId, transactionId);
    }

    protected GetLoansLoanIdTransactionsTransactionIdResponse getLoanTransactionDetails(String loanExternalId,
            String transactionExternalId) {
        return transactionHelper.getLoanTransactionDetails(loanExternalId, transactionExternalId);
    }

    protected PutChargeTransactionChangesResponse undoWaiveLoanCharge(Long loanId, Long transactionId,
            PutChargeTransactionChangesRequest request) {
        return transactionHelper.undoWaiveLoanCharge(loanId, transactionId, request);
    }

    protected PutChargeTransactionChangesResponse undoWaiveLoanCharge(Long loanId, String transactionExternalId,
            PutChargeTransactionChangesRequest request) {
        return transactionHelper.undoWaiveLoanCharge(loanId, transactionExternalId, request);
    }

    protected PutChargeTransactionChangesResponse undoWaiveLoanCharge(String loanExternalId, Long transactionId,
            PutChargeTransactionChangesRequest request) {
        return transactionHelper.undoWaiveLoanCharge(loanExternalId, transactionId, request);
    }

    protected PutChargeTransactionChangesResponse undoWaiveLoanCharge(String loanExternalId, String transactionExternalId,
            PutChargeTransactionChangesRequest request) {
        return transactionHelper.undoWaiveLoanCharge(loanExternalId, transactionExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeChargeRefund(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeChargeRefund(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeWaiveInterest(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeWaiveInterest(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeUndoWriteoff(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeUndoWriteoff(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeRecoveryPayment(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeRecoveryPayment(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse adjustLoanTransaction(String loanExternalId, String transactionExternalId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.adjustLoanTransaction(loanExternalId, transactionExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse adjustLoanTransaction(String loanExternalId, Long transactionId,
            PostLoansLoanIdTransactionsTransactionIdRequest request) {
        return transactionHelper.adjustLoanTransaction(loanExternalId, transactionId, request);
    }

    protected GetLoansLoanIdTransactionsTemplateResponse retrieveTransactionTemplate(String loanExternalId, String command,
            String dateFormat, String transactionDate, String locale) {
        return transactionHelper.retrieveTransactionTemplate(loanExternalId, command, dateFormat, transactionDate, locale);
    }

    protected PostLoansLoanIdChargesResponse addLoanCharge(String loanExternalId, PostLoansLoanIdChargesRequest request) {
        return loanHelper.addLoanCharge(loanExternalId, request);
    }

    protected List<GetLoansLoanIdChargesChargeIdResponse> getLoanCharges(String loanExternalId) {
        return loanHelper.getLoanCharges(loanExternalId);
    }

    protected GetLoansLoanIdChargesChargeIdResponse getLoanCharge(String loanExternalId, Long loanChargeId) {
        return loanHelper.getLoanCharge(loanExternalId, loanChargeId);
    }

    protected GetLoansLoanIdChargesChargeIdResponse getLoanCharge(Long loanId, String loanChargeExternalId) {
        return loanHelper.getLoanCharge(loanId, loanChargeExternalId);
    }

    protected GetLoansLoanIdChargesChargeIdResponse getLoanCharge(String loanExternalId, String loanChargeExternalId) {
        return loanHelper.getLoanCharge(loanExternalId, loanChargeExternalId);
    }

    protected GetLoansLoanIdChargesTemplateResponse getLoanChargeTemplate(String loanExternalId) {
        return loanHelper.getLoanChargeTemplate(loanExternalId);
    }

    protected GetLoansLoanIdChargesTemplateResponse getLoanChargeTemplate(Long loanId) {
        return loanHelper.getLoanChargeTemplate(loanId);
    }

    protected PostLoansLoanIdChargesChargeIdResponse waiveLoanCharge(String loanExternalId, Long loanChargeId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.waiveLoanCharge(loanExternalId, loanChargeId, request);
    }

    protected PostLoansLoanIdChargesChargeIdResponse waiveLoanCharge(String loanExternalId, String loanChargeExternalId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.waiveLoanCharge(loanExternalId, loanChargeExternalId, request);
    }

    protected PostLoansLoanIdChargesChargeIdResponse payLoanCharge(String loanExternalId, Long loanChargeId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.payLoanCharge(loanExternalId, loanChargeId, request);
    }

    protected PostLoansLoanIdChargesChargeIdResponse payLoanCharge(String loanExternalId, String loanChargeExternalId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.payLoanCharge(loanExternalId, loanChargeExternalId, request);
    }

    protected PostLoansLoanIdChargesChargeIdResponse chargeAdjustment(String loanExternalId, String loanChargeExternalId,
            PostLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.chargeAdjustment(loanExternalId, loanChargeExternalId, request);
    }

    protected PutLoansLoanIdChargesChargeIdResponse updateLoanCharge(Long loanId, Long loanChargeId,
            PutLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.updateLoanCharge(loanId, loanChargeId, request);
    }

    protected PutLoansLoanIdChargesChargeIdResponse updateLoanCharge(Long loanId, String loanChargeExternalId,
            PutLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.updateLoanCharge(loanId, loanChargeExternalId, request);
    }

    protected PutLoansLoanIdChargesChargeIdResponse updateLoanCharge(String loanExternalId, Long loanChargeId,
            PutLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.updateLoanCharge(loanExternalId, loanChargeId, request);
    }

    protected PutLoansLoanIdChargesChargeIdResponse updateLoanCharge(String loanExternalId, String loanChargeExternalId,
            PutLoansLoanIdChargesChargeIdRequest request) {
        return loanHelper.updateLoanCharge(loanExternalId, loanChargeExternalId, request);
    }

    protected DeleteLoansLoanIdChargesChargeIdResponse deleteLoanCharge(String loanExternalId, Long loanChargeId) {
        return loanHelper.deleteLoanCharge(loanExternalId, loanChargeId);
    }

    protected DeleteLoansLoanIdChargesChargeIdResponse deleteLoanCharge(String loanExternalId, String loanChargeExternalId) {
        return loanHelper.deleteLoanCharge(loanExternalId, loanChargeExternalId);
    }

    protected GetLoansApprovalTemplateResponse getLoanApprovalTemplate(String loanExternalId) {
        return loanHelper.getLoanApprovalTemplate(loanExternalId);
    }

    protected PutLoansLoanIdResponse modifyLoanApplication(String loanExternalId, String command, PutLoansLoanIdRequest request) {
        return loanHelper.modifyLoanApplication(loanExternalId, command, request);
    }

    protected PutLoansLoanIdResponse modifyLoanApplication(Long loanId, String command, PutLoansLoanIdRequest request) {
        return loanHelper.modifyLoanApplication(loanId, command, request);
    }

    protected List<GetDelinquencyTagHistoryResponse> getLoanDelinquencyTags(String loanExternalId) {
        return loanHelper.getLoanDelinquencyTags(loanExternalId);
    }

    protected List<GetDelinquencyTagHistoryResponse> getLoanDelinquencyTags(Long loanId) {
        return loanHelper.getLoanDelinquencyTags(loanId);
    }

    protected DeleteLoansLoanIdResponse deleteLoanApplication(String loanExternalId) {
        return loanHelper.deleteLoanApplication(loanExternalId);
    }

    protected PostLoansLoanIdResponse approveLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.approveLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse approveLoan(String date, Integer loanId) {
        return loanHelper.approveLoan(date, loanId.longValue());
    }

    protected PostLoansLoanIdResponse disburseLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.disburseLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse undoApprovalLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.undoApprovalLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse undoDisbursalLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.undoDisbursalLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse undoLastDisbursalLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.undoLastDisbursalLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse withdrawnByApplicantLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.withdrawnByApplicantLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse assignLoanOfficerLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.assignLoanOfficerLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse unassignLoanOfficerLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.unassignLoanOfficerLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse recoverGuaranteesLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.recoverGuaranteesLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse assignDelinquencyLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.assignDelinquencyLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse rejectLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.rejectLoanByExternalId(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse chargeOffLoan(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.chargeOffLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdResponse disburseToSavingsLoan(String loanExternalId, PostLoansLoanIdRequest request) {
        return loanHelper.disburseToSavingsLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse makeRefundByCash(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.makeRefundByCash(loanExternalId, request);
    }

    protected Integer openSavingsAccount(Long clientId, String minimumOpeningBalance) {
        return openSavingsAccount(clientId, minimumOpeningBalance, Utils.getLocalDateOfTenant().format(dateTimeFormatter));
    }

    protected Integer openSavingsAccount(Long clientId, String minimumOpeningBalance, String submittedOnDate) {
        FeignSavingsProductHelper savingsProductHelper = new FeignSavingsProductHelper(FineractFeignClientHelper.getFineractFeignClient());
        FeignSavingsHelper savingsHelper = new FeignSavingsHelper(FineractFeignClientHelper.getFineractFeignClient());
        FeignSavingsTransactionHelper savingsTransactionHelper = new FeignSavingsTransactionHelper(
                FineractFeignClientHelper.getFineractFeignClient());
        Long productId = savingsProductHelper.createDefaultSavingsProduct().getResourceId();
        Long savingsId = savingsHelper.createApproveActivateSavings(clientId, productId, submittedOnDate);
        if (minimumOpeningBalance != null && !minimumOpeningBalance.isBlank()) {
            savingsTransactionHelper.deposit(savingsId, minimumOpeningBalance, submittedOnDate);
        }
        return savingsId.intValue();
    }

    protected GetLoansLoanIdTransactionsTemplateResponse getPrepaymentAmount(Long loanId, String transactionDate, String dateFormat) {
        return transactionHelper.getPrepaymentAmount(loanId, transactionDate, dateFormat);
    }

    protected Long createRescheduleRequest(PostCreateRescheduleLoansRequest request) {
        return loanHelper.createRescheduleRequest(request).getResourceId();
    }

    protected Long approveRescheduleRequest(Long scheduleId, PostUpdateRescheduleLoansRequest request) {
        return loanHelper.approveRescheduleRequest(scheduleId, request).getResourceId();
    }

    protected void createAndApproveReschedule(Long loanId, String submittedOnDate, String rescheduleFromDate, String adjustedDueDate) {
        loanHelper.createAndApproveRescheduleRequest(
                LoanRequestBuilders.rescheduleRequest(loanId, submittedOnDate, rescheduleFromDate, adjustedDueDate),
                LoanRequestBuilders.approveReschedule(submittedOnDate));
    }

    protected Long reAge(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.reAge(loanId, request).getResourceId();
    }

    protected void reAgeLoan(Long loanId, String frequencyType, int frequencyNumber, String startDate, Integer numberOfInstallments,
            String reAgeInterestHandling) {
        reAgeLoan(loanId, frequencyType, frequencyNumber, startDate, numberOfInstallments, reAgeInterestHandling, null);
    }

    protected void reAgeLoan(Long loanId, String frequencyType, int frequencyNumber, String startDate, Integer numberOfInstallments,
            String reAgeInterestHandling, Double transactionAmount) {
        reAge(loanId, LoanRequestBuilders.reAge(startDate, frequencyType, frequencyNumber, numberOfInstallments, reAgeInterestHandling,
                transactionAmount));
    }

    protected void undoReAgeLoan(Long loanId) {
        transactionHelper.undoReAge(loanId, new PostLoansLoanIdTransactionsRequest());
    }

    protected GetLoansLoanIdTransactionsTemplateResponse getReAgeTemplate(Long loanId) {
        return transactionHelper.getReAgeTemplate(loanId);
    }

    protected LoanScheduleData previewReAgeSchedule(Long loanId, Map<String, Object> queryParams) {
        return transactionHelper.previewReAgeSchedule(loanId, queryParams);
    }

    protected void checkMaturityDates(long loanId, LocalDate expectedMaturityDate, LocalDate actualMaturityDate) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertEquals(expectedMaturityDate, loanDetails.getTimeline().getExpectedMaturityDate());
        assertEquals(actualMaturityDate, loanDetails.getTimeline().getActualMaturityDate());
    }

    protected PostLoansLoanIdTransactionsRequest reAge(String startDate, String frequencyType, Integer frequencyNumber,
            Integer numberOfInstallments) {
        return LoanRequestBuilders.reAge(startDate, frequencyType, frequencyNumber, numberOfInstallments);
    }

    protected LoanTestData.TransactionExt transaction(double amount, String type, String date, double outstandingPrincipal,
            double principalPortion, double interestPortion, double feePortion, double penaltyPortion, double unrecognizedIncomePortion,
            double overpaymentPortion) {
        return new LoanTestData.TransactionExt(amount, type, date, outstandingPrincipal, principalPortion, interestPortion, feePortion,
                penaltyPortion, unrecognizedIncomePortion, overpaymentPortion, false);
    }

    protected LoanTestData.TransactionExt transaction(double amount, String type, String date, double outstandingPrincipal,
            double principalPortion, double interestPortion, double feePortion, double penaltyPortion, double unrecognizedIncomePortion,
            double overpaymentPortion, boolean reversed) {
        return new LoanTestData.TransactionExt(amount, type, date, outstandingPrincipal, principalPortion, interestPortion, feePortion,
                penaltyPortion, unrecognizedIncomePortion, overpaymentPortion, reversed);
    }

    protected LoanTestData.Installment installment(double principalAmount, Boolean completed, String dueDate) {
        return new LoanTestData.Installment(principalAmount, null, null, null, null, completed, dueDate, null, null);
    }

    protected LoanTestData.Installment installment(double principalAmount, double interestAmount, double totalOutstandingAmount,
            Boolean completed, String dueDate) {
        return new LoanTestData.Installment(principalAmount, interestAmount, null, null, totalOutstandingAmount, completed, dueDate, null,
                null);
    }

    protected LoanTestData.Installment installment(double principalAmount, double interestAmount, double feeAmount,
            double totalOutstandingAmount, Boolean completed, String dueDate) {
        return new LoanTestData.Installment(principalAmount, interestAmount, feeAmount, null, totalOutstandingAmount, completed, dueDate,
                null, null);
    }

    protected LoanTestData.Installment installment(double principalAmount, double interestAmount, double feeAmount, double penaltyAmount,
            double totalOutstandingAmount, Boolean completed, String dueDate) {
        return new LoanTestData.Installment(principalAmount, interestAmount, feeAmount, penaltyAmount, totalOutstandingAmount, completed,
                dueDate, null, null);
    }

    protected LoanTestData.Installment installment(double principalAmount, double interestAmount, double feeAmount, double penaltyAmount,
            LoanTestData.OutstandingAmounts outstandingAmounts, Boolean completed, String dueDate) {
        return new LoanTestData.Installment(principalAmount, interestAmount, feeAmount, penaltyAmount, null, completed, dueDate,
                outstandingAmounts, null);
    }

    protected LoanTestData.Installment installment(double principalAmount, double interestAmount, double feeAmount, double penaltyAmount,
            double totalOutstanding, Boolean completed, String dueDate, double loanBalance) {
        return new LoanTestData.Installment(principalAmount, interestAmount, feeAmount, penaltyAmount, totalOutstanding, completed, dueDate,
                null, loanBalance);
    }

    protected LoanTestData.OutstandingAmounts outstanding(double principal, double interestOutstanding, double fee, double penalty,
            double total) {
        return new LoanTestData.OutstandingAmounts(principal, interestOutstanding, fee, penalty, total);
    }

    protected LoanPointInTimeData getPointInTimeData(Long loanId, String date) {
        return ok(() -> fineractClient().loansPointInTime().retrieveLoanPointInTime(loanId, date, LoanTestData.DATETIME_PATTERN,
                LoanTestData.LOCALE));
    }

    protected List<LoanPointInTimeData> getPointInTimeData(List<Long> loanIds, String date) {
        RetrieveLoansPointInTimeRequest request = new RetrieveLoansPointInTimeRequest().loanIds(loanIds).date(date)
                .dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE);
        return ok(() -> fineractClient().loansPointInTime().retrieveLoansPointInTime(request));
    }

    protected void verifyOutstanding(LoanPointInTimeData loan, LoanTestData.OutstandingAmounts outstanding) {
        assertThat(BigDecimal.valueOf(outstanding.principalOutstanding))
                .isEqualByComparingTo(loan.getPrincipal().getPrincipalOutstanding());
        assertThat(BigDecimal.valueOf(outstanding.interestOutstanding)).isEqualByComparingTo(loan.getInterest().getInterestOutstanding());
        assertThat(BigDecimal.valueOf(outstanding.feeOutstanding)).isEqualByComparingTo(loan.getFee().getFeeChargesOutstanding());
        assertThat(BigDecimal.valueOf(outstanding.penaltyOutstanding))
                .isEqualByComparingTo(loan.getPenalty().getPenaltyChargesOutstanding());
        assertThat(BigDecimal.valueOf(outstanding.totalOutstanding)).isEqualByComparingTo(loan.getTotal().getTotalOutstanding());
    }

    protected void verifyArrears(LoanPointInTimeData pointInTimeData, boolean isOverDue, String overdueSince) {
        assertThat(Objects.requireNonNull(pointInTimeData.getArrears()).getOverdue()).isEqualTo(isOverDue);
        if (isOverDue) {
            assertThat(Objects.requireNonNull(pointInTimeData.getArrears().getOverDueSince()).toString()).isEqualTo(overdueSince);
        } else {
            assertThat(pointInTimeData.getArrears().getOverDueSince()).isNull();
        }
    }

    protected Long createInstallmentFeeCharge(double amount) {
        return chargesHelper.createCharge(ChargeRequestBuilders.loanInstallmentFee(amount)).getResourceId();
    }

    protected Long createInstallmentPenaltyCharge(double amount) {
        return chargesHelper.createCharge(ChargeRequestBuilders.loanInstallmentFee(amount).penalty(true)).getResourceId();
    }

    protected LoanTestData.Transaction reversedTransaction(double principalAmount, String type, String date) {
        return new LoanTestData.Transaction(principalAmount, type, date, true);
    }

    protected LoanTestData.Transaction transaction(double amount, String type, String date) {
        return new LoanTestData.Transaction(amount, type, date, null);
    }

    protected void verifyTransactions(Long loanId, LoanTestData.Transaction... transactions) {
        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient().loans().retrieveOneLoan(loanId, false, "all", null, null));
        LoanTestValidators.verifyTransactions(loanDetails, transactions);
    }

    protected void verifyTransactions(Long loanId, LoanTestData.TransactionExt... transactions) {
        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient().loans().retrieveOneLoan(loanId, false, "all", null, null));
        LoanTestValidators.verifyTransactions(loanDetails, transactions);
    }

    protected void verifyNoTransactions(Long loanId) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertTrue(loanDetails.getTransactions() == null || loanDetails.getTransactions().isEmpty(),
                "No transaction is expected on loan " + loanId);
    }

    protected void verifyUndoLastDisbursalShallFail(Long loanId, String expectedError) {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> undoLastDisbursement(loanId));
        assertTrue(exception.getMessage().contains(expectedError));
    }

    protected void verifyRepaymentSchedule(Long loanId, LoanTestData.Installment... installments) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        LoanTestValidators.verifyRepaymentSchedule(loanDetails, installments);
    }

    protected PostLoanProductsRequest createOnePeriod30DaysPeriodicAccrualProductWithAdvancedPaymentAllocationAndInterestRecalculation(
            double interestRatePerPeriod, Integer rescheduleStrategyMethod) {
        return createOnePeriod30DaysPeriodicAccrualProduct(interestRatePerPeriod)//
                .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                .loanScheduleType("PROGRESSIVE")//
                .loanScheduleProcessingType("HORIZONTAL")//
                .addPaymentAllocationItem(createDefaultPaymentAllocation("NEXT_INSTALLMENT"))//
                .enableDownPayment(false)//
                .isInterestRecalculationEnabled(true)//
                .interestRecalculationCompoundingMethod(LoanTestData.InterestRecalculationCompoundingMethod.NONE)//
                .preClosureInterestCalculationStrategy(1)//
                .recalculationRestFrequencyType(LoanTestData.RecalculationRestFrequencyType.SAME_AS_REPAYMENT_PERIOD)//
                .allowPartialPeriodInterestCalculation(true)//
                .rescheduleStrategyMethod(rescheduleStrategyMethod);
    }

    protected PostLoanProductsRequest createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation() {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct()
                .transactionProcessingStrategyCode("advanced-payment-allocation-strategy").loanScheduleType("PROGRESSIVE")
                .loanScheduleProcessingType("HORIZONTAL").addPaymentAllocationItem(LoanRequestBuilders.defaultPaymentAllocation());
    }

    protected PostLoanProductsRequest createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct() {
        return createOnePeriod30DaysPeriodicAccrualProduct(0);
    }

    protected PostLoanProductsRequest createOnePeriod30DaysPeriodicAccrualProduct(double interestRatePerPeriod) {
        return onePeriod30DaysPeriodicAccrual(interestRatePerPeriod);
    }

    protected PostLoanProductsRequest create4Period1MonthLongWithoutInterestProduct(String repaymentStrategy) {
        PostLoanProductsRequest productRequest = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().multiDisburseLoan(false)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .overAppliedCalculationType(null)//
                .overAppliedNumber(null)//
                .principal(1000.0)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS.longValue())//
                .transactionProcessingStrategyCode(repaymentStrategy);
        if (AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equals(repaymentStrategy)) {
            productRequest.loanScheduleType("PROGRESSIVE").loanScheduleProcessingType("HORIZONTAL")
                    .addPaymentAllocationItem(LoanRequestBuilders.defaultPaymentAllocation());
        } else {
            productRequest.loanScheduleType("CUMULATIVE").loanScheduleProcessingType(null).paymentAllocation(null);
        }
        return productRequest;
    }

    protected PostLoansRequest applyLoanRequest(Long clientId, Long productId, String submittedOnDate, Double principal,
            Integer numberOfRepayments) {
        return LoanRequestBuilders.applyLoanRequest(clientId, productId, submittedOnDate, principal, numberOfRepayments);
    }

    protected PostLoansRequest applyLoanRequest(Long clientId, Long productId, String submittedOnDate, Double principal,
            Integer numberOfRepayments, Consumer<PostLoansRequest> customizer) {
        return LoanRequestBuilders.applyLoanRequest(clientId, productId, submittedOnDate, principal, numberOfRepayments, customizer);
    }

    protected PostLoansRequest applyLP2ProgressiveLoanRequest(Long clientId, Long loanProductId, String loanDisbursementDate, Double amount,
            Double interestRate, Integer numberOfRepayments, Consumer<PostLoansRequest> customizer) {
        return LoanRequestBuilders.applyLP2ProgressiveLoanRequest(clientId, loanProductId, loanDisbursementDate, amount, interestRate,
                numberOfRepayments, customizer);
    }

    protected Long applyAndApproveLoan(Long clientId, Long productId, String date, Double amount) {
        return applyAndApproveLoan(clientId, productId, date, amount, 1, null);
    }

    protected Long applyAndApproveLoan(Long clientId, Long productId, String date, Double amount, int numberOfRepayments) {
        return applyAndApproveLoan(clientId, productId, date, amount, numberOfRepayments, null);
    }

    protected Long applyAndApproveLoan(Long clientId, Long productId, String date, Double amount, int numberOfRepayments,
            Consumer<PostLoansRequest> customizer) {
        PostLoansRequest request = LoanRequestBuilders.applyLoanRequest(clientId, productId, date, amount, numberOfRepayments, customizer);
        Long loanId = applyForLoan(request);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(amount, date));
        return loanId;
    }

    protected Long applyAndApproveProgressiveLoan(Long clientId, Long productId, String date, Double amount, Double interestRate,
            int numberOfRepayments, Consumer<PostLoansRequest> customizer) {
        PostLoansRequest request = LoanRequestBuilders.applyLP2ProgressiveLoanRequest(clientId, productId, date, amount, interestRate,
                numberOfRepayments, customizer);
        Long loanId = applyForLoan(request);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(amount, date));
        return loanId;
    }

    protected void disburseLoan(Long loanId, BigDecimal amount, String date) {
        disburseLoan(loanId, LoanRequestBuilders.disburseLoan(amount.doubleValue(), date));
    }

    protected PostLoansLoanIdResponse disburseLoan(Long loanId, String date, Double amount) {
        return disburseLoan(loanId, LoanRequestBuilders.disburseLoan(amount, date));
    }

    protected void disburseLoanWithRepaymentReschedule(Long loanId, String date, String adjustRepaymentDate) {
        loanHelper.disburseLoanFromJson(loanId, LoanRequestBuilders.disburseLoanWithRepaymentRescheduleJson(date, adjustRepaymentDate));
    }

    protected void disburseLoanWithNetDisbursalAmount(Long loanId, String date, String netDisbursalAmount) {
        loanHelper.disburseLoanFromJson(loanId, LoanRequestBuilders.disburseLoanWithNetDisbursalAmountJson(date, netDisbursalAmount));
    }

    protected void approveLoanFromJson(Long loanId, String approveLoanJson) {
        loanHelper.approveLoanFromJson(loanId, approveLoanJson);
    }

    protected Long addRepaymentForLoan(Long loanId, Double amount, String date) {
        return addRepayment(loanId, LoanRequestBuilders.repayLoan(amount, date));
    }

    protected Long chargeOffLoan(Long loanId, String date) {
        Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(
                Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6) + Utils.randomStringGenerator("is", 5), 1);
        return chargeOff(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate(date).locale("en")//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .externalId(UUID.randomUUID().toString())//
                .chargeOffReasonId(chargeOffReasonId));
    }

    protected void undoChargeOffLoan(Long loanId) {
        transactionHelper.undoChargeOff(loanId, new PostLoansLoanIdTransactionsRequest());
    }

    protected PostLoansLoanIdTransactionsResponse closeRescheduledLoan(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.closeRescheduledLoan(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse closeRescheduledLoan(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.closeRescheduledLoan(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse closeLoan(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.closeLoan(loanExternalId, request);
    }

    protected Long addCharge(Long loanId, boolean isPenalty, double amount, String dueDate) {
        ChargeRequest chargeRequest = ChargeRequestBuilders.loanSpecifiedDueDateFee(amount);
        if (isPenalty) {
            chargeRequest.penalty(true);
        }
        Long chargeId = chargesHelper.createCharge(chargeRequest).getResourceId();
        return loanHelper.addSpecifiedDueDateCharge(loanId, chargeId, amount, dueDate).getResourceId();
    }

    protected void deactivateOverdueLoanCharges(Long loanId, String fromDueDate) {
        loanHelper.deactivateOverdueLoanCharges(loanId, fromDueDate);
    }

    protected Long createDisbursementPercentageCharge(double percentageAmount) {
        return chargesHelper.createCharge(ChargeRequestBuilders.loanDisbursementPercentageFee(percentageAmount)).getResourceId();
    }

    protected Long createDisbursementPercentageCharge(double percentageAmount, String currencyCode) {
        return chargesHelper.createCharge(ChargeRequestBuilders.loanDisbursementPercentageFee(percentageAmount, currencyCode))
                .getResourceId();
    }

    protected void addAndDeleteDisbursementDetail(Long loanId, List<DisbursementDetail> disbursementDetails) {
        loanHelper.addAndDeleteDisbursementDetail(loanId, disbursementDetails);
    }

    protected Long addDisbursementCharge(Long loanId, Long chargeId, double amount) {
        return loanHelper.addDisbursementCharge(loanId, chargeId, amount).getResourceId();
    }

    protected PostLoanProductsRequest create1InstallmentAmountInMultiplesOf4Period1MonthLongWithInterestAndAmortizationProduct(
            int interestType, int amortizationType) {
        return createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().multiDisburseLoan(false)//
                .disallowExpectedDisbursements(false)//
                .allowApprovedDisbursedAmountsOverApplied(false)//
                .overAppliedCalculationType(null)//
                .overAppliedNumber(null)//
                .principal(1250.0)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS.longValue())//
                .interestType(interestType)//
                .amortizationType(amortizationType)//
                .graceOnArrearsAgeing(0);
    }

    protected void validateLoanSummaryBalances(GetLoansLoanIdResponse loanDetails, Double totalOutstanding, Double totalRepayment,
            Double principalOutstanding, Double principalPaid, Double totalOverpaid) {
        assertEquals(totalOutstanding, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
        assertEquals(totalRepayment, Utils.getDoubleValue(loanDetails.getSummary().getTotalRepayment()));
        assertEquals(principalOutstanding, Utils.getDoubleValue(loanDetails.getSummary().getPrincipalOutstanding()));
        assertEquals(principalPaid, Utils.getDoubleValue(loanDetails.getSummary().getPrincipalPaid()));
        assertEquals(totalOverpaid, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));
    }

    protected Long reAmortizeLoan(Long loanId, String reAmortizationInterestHandling) {
        PostLoansLoanIdTransactionsResponse response = transactionHelper.reAmortize(loanId,
                LoanRequestBuilders.reAmortize(reAmortizationInterestHandling));
        return response.getResourceId();
    }

    protected void undoReAmortizeLoan(Long loanId) {
        transactionHelper.undoReAmortize(loanId, new PostLoansLoanIdTransactionsRequest());
    }

    protected PostLoansLoanIdTransactionsResponse writeOffLoan(Long loanId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.writeOff(loanId, request);
    }

    protected PostLoansLoanIdTransactionsResponse writeOffLoan(String loanExternalId, PostLoansLoanIdTransactionsRequest request) {
        return transactionHelper.writeOff(loanExternalId, request);
    }

    protected PostLoansLoanIdTransactionsResponse writeOffLoan(Long loanId, String date) {
        return writeOffLoan(loanId, LoanRequestBuilders.writeOff(date));
    }

    protected Long applyAndApproveCumulativeLoan(Long clientId, Long productId, String date, Double amount, Double interestRate,
            int numberOfRepayments, Consumer<PostLoansRequest> customizer) {
        PostLoansRequest request = LoanRequestBuilders.applyCumulativeLoanRequest(clientId, productId, date, amount, interestRate,
                numberOfRepayments, customizer);
        Long loanId = applyForLoan(request);
        approveLoan(loanId, LoanRequestBuilders.approveLoan(amount, date));
        return loanId;
    }

    protected PostLoansLoanIdTransactionsResponse adjustLoanTransaction(Long loanId, Long transactionId, String transactionDate) {
        return transactionHelper.adjustLoanTransaction(loanId, transactionId, transactionDate);
    }

    protected CallFailedRuntimeException adjustLoanTransactionExpectingError(Long loanId, Long transactionId, String transactionDate,
            double transactionAmount) {
        return transactionHelper.adjustLoanTransactionExpectingError(loanId, transactionId, transactionDate, transactionAmount);
    }

    protected Long applyChargebackTransaction(Long loanId, Long transactionId, String amount, int paymentTypeIdx) {
        return applyChargebackTransaction(loanId, transactionId, Double.valueOf(amount), getPaymentTypeId(paymentTypeIdx));
    }

    protected Long getPaymentTypeId(int index) {
        return PaymentTypeHelper.getAllPaymentTypes(false).get(index).getId();
    }

    protected void reviewLoanTransactionRelations(Long loanId, Long transactionId, Integer expectedSize) {
        reviewLoanTransactionRelations(loanId, transactionId, expectedSize, null);
    }

    protected void reviewLoanTransactionRelations(Long loanId, Long transactionId, Integer expectedSize, Double outstandingBalance) {
        GetLoansLoanIdTransactionsTransactionIdResponse response = getLoanTransaction(loanId, transactionId);
        assertNotNull(response);
        assertNotNull(response.getTransactionRelations());
        assertEquals(expectedSize, response.getTransactionRelations().size());
        if (outstandingBalance != null) {
            assertEquals(outstandingBalance, response.getOutstandingLoanBalance());
        }
    }

    protected Long addChargebackForLoan(Long loanId, Long transactionId, Double amount) {
        return applyChargebackTransaction(loanId, transactionId, amount, getPaymentTypeId(0));
    }

    protected Long addInterestPaymentWaiverForLoan(Long loanId, Double amount, String date) {
        PostLoansLoanIdTransactionsResponse response = makeInterestPaymentWaiver(loanId,
                new PostLoansLoanIdTransactionsRequest().dateFormat(LoanTestData.DATETIME_PATTERN).transactionDate(date).locale("en")
                        .transactionAmount(amount).externalId(UUID.randomUUID().toString()));
        return response.getResourceId();
    }

    protected void validateLoanPrincipalOustandingBalance(GetLoansLoanIdResponse loanDetails, Double amountExpected) {
        assertEquals(amountExpected, Utils.getDoubleValue(loanDetails.getSummary().getPrincipalOutstanding()));
    }

    protected void validateLoanStatus(GetLoansLoanIdResponse loanDetails, String statusCodeExpected) {
        assertEquals(statusCodeExpected, loanDetails.getStatus().getCode());
    }

    protected void evaluateLoanSummaryAdjustments(GetLoansLoanIdResponse loanDetails, Double amountExpected) {
        assertEquals(amountExpected, Utils.getDoubleValue(loanDetails.getSummary().getPrincipalAdjustments()));
    }

    protected LoanTestData.Installment fullyRepaidInstallment(double principalAmount, double interestAmount, String dueDate) {
        return installment(principalAmount, interestAmount, 0.0, true, dueDate);
    }

    protected LoanTestData.Installment unpaidInstallment(double principalAmount, double interestAmount, String dueDate) {
        double totalOutstanding = Math.round((principalAmount + interestAmount) * 100.0) / 100.0;
        return installment(principalAmount, interestAmount, totalOutstanding, false, dueDate);
    }

    protected static AdvancedPaymentData createDefaultPaymentAllocation() {
        return LoanRequestBuilders.defaultPaymentAllocation();
    }

    protected static AdvancedPaymentData createDefaultPaymentAllocation(String futureInstallmentAllocationRule) {
        return LoanRequestBuilders.paymentAllocation("DEFAULT", futureInstallmentAllocationRule);
    }

    protected static AdvancedPaymentData createPaymentAllocation(String transactionType, String futureInstallmentAllocationRule) {
        return LoanRequestBuilders.paymentAllocation(transactionType, futureInstallmentAllocationRule);
    }

    protected Account loansReceivableAccount() {
        return getAccounts().getLoansReceivableAccount();
    }

    protected Account fundSource() {
        return getAccounts().getFundSource();
    }

    protected Account overpaymentAccount() {
        return getAccounts().getOverpaymentAccount();
    }

    protected List<AdvancedPaymentData> getAdvancedPaymentAllocationRules(Long loanId) {
        return loanHelper.getAdvancedPaymentAllocationRules(loanId);
    }

    protected <T> T getLoanProductError(String loanProductJson, String jsonAttributeToGetBack) {
        return loanHelper.getLoanProductError(loanProductJson, jsonAttributeToGetBack);
    }

    protected PostLoansLoanIdTransactionsResponse makeRefundByCash(Long loanId, String date, Double amount) {
        return transactionHelper.makeRefundByCash(loanId, LoanRequestBuilders.repayLoan(amount, date));
    }

    protected void validateFullyUnpaidRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, String dueDate,
            double principalDue, double feeDue, double penaltyDue, double interestDue) {
        LoanTestValidators.validateFullyUnpaidRepaymentPeriod(loanDetails, index, dueDate, principalDue, feeDue, penaltyDue, interestDue);
    }

    protected void validateFullyPaidRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, String dueDate, double principalDue,
            double feeDue, double penaltyDue, double interestDue) {
        LoanTestValidators.validateFullyPaidRepaymentPeriod(loanDetails, index, dueDate, principalDue, feeDue, penaltyDue, interestDue);
    }

    protected void validateFullyPaidRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, String dueDate, double principalDue,
            double feeDue, double penaltyDue, double interestDue, double paidLate) {
        LoanTestValidators.validateFullyPaidRepaymentPeriod(loanDetails, index, dueDate, principalDue, feeDue, penaltyDue, interestDue,
                paidLate);
    }

    protected void validateFullyPaidRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, String dueDate, double principalDue,
            double feeDue, double penaltyDue, double interestDue, double paidLate, double paidInAdvance) {
        LoanTestValidators.validateFullyPaidRepaymentPeriod(loanDetails, index, dueDate, principalDue, feeDue, penaltyDue, interestDue,
                paidLate, paidInAdvance);
    }

    protected void validateRepaymentPeriod(GetLoansLoanIdResponse loanDetails, Integer index, LocalDate dueDate, double principalDue,
            double principalPaid, double principalOutstanding, double feeDue, double feePaid, double feeOutstanding, double penaltyDue,
            double penaltyPaid, double penaltyOutstanding, double interestDue, double interestPaid, double interestOutstanding,
            double paidInAdvance, double paidLate) {
        LoanTestValidators.validateRepaymentPeriod(loanDetails, index, dueDate, principalDue, principalPaid, principalOutstanding, feeDue,
                feePaid, feeOutstanding, penaltyDue, penaltyPaid, penaltyOutstanding, interestDue, interestPaid, interestOutstanding,
                paidInAdvance, paidLate);
    }

    protected void deleteAllExternalEvents() {
        externalEventHelper.deleteAllExternalEvents();
    }

    protected GetLoansLoanIdTransactionsResponse getLoanTransactions(Long loanId) {
        return transactionHelper.getLoanTransactions(loanId);
    }

    protected GetLoansLoanIdTransactionsResponse getLoanTransactions(Long loanId, List<TransactionType> excludedTransactionTypes) {
        return transactionHelper.getLoanTransactions(loanId, excludedTransactionTypes);
    }

    protected GetLoansLoanIdTransactionsResponse getLoanTransactionsByExternalId(String loanExternalId) {
        return transactionHelper.getLoanTransactionsByExternalId(loanExternalId);
    }

    protected GetLoansLoanIdTransactionsResponse getLoanTransactionsByExternalId(String loanExternalId,
            List<TransactionType> excludedTransactionTypes) {
        return transactionHelper.getLoanTransactionsByExternalId(loanExternalId, excludedTransactionTypes);
    }

    protected GetLoansLoanIdTransactionsTemplateResponse retrieveTransactionTemplate(Long loanId, String command, String dateFormat,
            String transactionDate, String locale) {
        return transactionHelper.retrieveTransactionTemplate(loanId, command, dateFormat, transactionDate, locale);
    }

    protected GetLoansLoanIdTransactionsTemplateResponse retrieveTransactionTemplate(Long loanId, String command, String dateFormat,
            String transactionDate, String locale, Long transactionId) {
        return transactionHelper.retrieveTransactionTemplate(loanId, command, dateFormat, transactionDate, locale, transactionId);
    }

    protected PostLoansLoanIdTransactionsResponse executeLoanTransaction(Long loanId, PostLoansLoanIdTransactionsRequest request,
            String command) {
        return transactionHelper.executeLoanTransaction(loanId, request, command);
    }

    protected void makeRepayment(String date, float amount, long loanId) {
        transactionHelper.makeRepayment(date, amount, (int) loanId);
    }

    protected void makeRefundByCash(String date, float amount, long loanId) {
        transactionHelper.makeRefundByCash(date, amount, (int) loanId);
    }

    protected Account feeIncomeAccount() {
        return getAccounts().getFeeIncomeAccount();
    }

    protected Account deferredIncomeLiabilityAccount() {
        return getAccounts().getDeferredIncomeLiabilityAccount();
    }

    protected Account buyDownExpenseAccount() {
        return getAccounts().getBuyDownExpenseAccount();
    }
}
