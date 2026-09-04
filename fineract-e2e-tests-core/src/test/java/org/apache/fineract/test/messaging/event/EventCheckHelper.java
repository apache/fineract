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
package org.apache.fineract.test.messaging.event;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.apache.fineract.test.stepdef.loan.LoanRepaymentStepDef.DATE_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.client.v1.ClientDataV1;
import org.apache.fineract.avro.loan.v1.DelinquencyPausePeriodV1;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.avro.loan.v1.LoanAmountDataV1;
import org.apache.fineract.avro.loan.v1.LoanApplicationTimelineDataV1;
import org.apache.fineract.avro.loan.v1.LoanInstallmentDelinquencyBucketDataV1;
import org.apache.fineract.avro.loan.v1.LoanOwnershipTransferDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalBreachDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanAccountDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanChargeDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanCollectionDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDelinquencyDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDelinquencySchedulePeriodDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanDelinquencyScheduleTagDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanJournalEntryDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanSummaryDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.workingcapitalloan.v1.WorkingCapitalLoanTransactionDataV1;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.ExternalTransferDataDetails;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencyPausePeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetWorkingCapitalLoanSummary;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionsResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdTimeline;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.WorkingCapitalCollection;
import org.apache.fineract.client.models.WorkingCapitalCollectionDelinquencyPausePeriod;
import org.apache.fineract.client.models.WorkingCapitalCollectionRangeScheduleDelinquency;
import org.apache.fineract.test.data.AssetExternalizationTransferStatus;
import org.apache.fineract.test.data.AssetExternalizationTransferStatusReason;
import org.apache.fineract.test.data.LoanStatus;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.GlobalConfigurationHelper;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.EventMessage;
import org.apache.fineract.test.messaging.event.assetexternalization.LoanAccountSnapshotEvent;
import org.apache.fineract.test.messaging.event.assetexternalization.LoanOwnershipTransferEvent;
import org.apache.fineract.test.messaging.event.client.ClientActivatedEvent;
import org.apache.fineract.test.messaging.event.client.ClientCreatedEvent;
import org.apache.fineract.test.messaging.event.loan.AbstractLoanEvent;
import org.apache.fineract.test.messaging.event.loan.LoanApprovedEvent;
import org.apache.fineract.test.messaging.event.loan.LoanBalanceChangedEvent;
import org.apache.fineract.test.messaging.event.loan.LoanCreatedEvent;
import org.apache.fineract.test.messaging.event.loan.LoanDisbursalEvent;
import org.apache.fineract.test.messaging.event.loan.LoanRejectedEvent;
import org.apache.fineract.test.messaging.event.loan.LoanStatusChangedEvent;
import org.apache.fineract.test.messaging.event.loan.LoanUndoApprovalEvent;
import org.apache.fineract.test.messaging.event.loan.delinquency.LoanDelinquencyPauseChangedEvent;
import org.apache.fineract.test.messaging.event.loan.delinquency.LoanDelinquencyRangeChangeEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.AbstractLoanTransactionEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanDisbursalTransactionEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanRefundPostBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionGoodwillCreditPostEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionInterestPaymentWaiverPostEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionInterestRefundPostEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionMakeRepaymentPostEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionMerchantIssuedRefundPostEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionPayoutRefundPostEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanUndoContractTerminationBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.charge.WorkingCapitalLoanAddChargeEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.journalentry.WorkingCapitalLoanJournalEntryCreatedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.AbstractWorkingCapitalLoanEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanApplicationModifiedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanApprovedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBalanceChangedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBreachChangeEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBreachDisableEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBreachEnableEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBreachPastDueChangeEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBreachPauseEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBreachRescheduleEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBreachResetEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBreachResumeEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBreachScheduleChangedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanBreachUndoResetEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanChargeOffEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanCreatedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanDelinquencyDisableEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanDelinquencyEnableEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanDelinquencyPauseEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanDelinquencyRangeChangeEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanDelinquencyRescheduleEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanDelinquencyResetEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanDelinquencyResumeEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanDelinquencyScheduleChangedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanDelinquencyUndoResetEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanDisbursalEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanFraudChangedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanNearBreachChangeEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanPeriodPaymentRateChangedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanRejectedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanStatusChangedEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanUndoApprovalEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanUndoChargeOffEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.loan.WorkingCapitalLoanUndoDisbursalEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.AbstractWorkingCapitalLoanTransactionEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanAccrualAdjustmentTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanAccrualTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanAdjustTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanChargeAdjustmentTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanChargeOffTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanCreditBalanceRefundTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanDisbursalTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanDiscountFeeAdjustmentTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanDiscountFeeAmortizationAdjustmentTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanDiscountFeeAmortizationTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanDiscountFeeTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanGoodwillCreditTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanPayoutRefundTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanRepaymentTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanUndoDisbursalTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanUndoWriteOffTransactionBusinessEvent;
import org.apache.fineract.test.messaging.event.workingcapitalloan.transaction.WorkingCapitalLoanWriteOffTransactionBusinessEvent;
import org.apache.fineract.test.messaging.store.EventStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventCheckHelper {

    private static final DateTimeFormatter FORMATTER_EVENTS = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final long TRANSACTION_COMMIT_DELAY_MS = 100L;
    private static final String JOURNAL_ENTRY_TYPE_DEBIT = "DEBIT";
    private static final String JOURNAL_ENTRY_TYPE_CREDIT = "CREDIT";
    private static final int EVENT_VERIFICATION_MAX_ATTEMPTS = 3;

    @Autowired
    private FineractFeignClient fineractClient;
    @Autowired
    private EventAssertion eventAssertion;
    @Autowired
    private EventStore eventStore;
    @Autowired
    private GlobalConfigurationHelper configurationHelper;
    @Autowired
    private org.apache.fineract.test.messaging.config.EventProperties eventProperties;

    public void waitForTransactionCommit() {
        sleepIfEventVerificationEnabled(TRANSACTION_COMMIT_DELAY_MS);
    }

    public void sleepIfEventVerificationEnabled(long sleepInMs) {
        if (eventProperties.isEventVerificationEnabled()) {
            try {
                Thread.sleep(sleepInMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting...", e);
            }
        }
    }

    public void clientEventCheck(PostClientsResponse clientCreationResponse) {
        waitForTransactionCommit();
        GetClientsClientIdResponse body = ok(() -> fineractClient.clients().retrieveOneClient(clientCreationResponse.getClientId(),
                Map.of("staffInSelectedOfficeOnly", false)));

        Long clientId = Long.valueOf(body.getId());
        Integer status = body.getStatus().getId().intValue();
        String firstname = body.getFirstname();
        String lastname = body.getLastname();
        Boolean active = body.getActive();

        eventAssertion.assertEvent(ClientCreatedEvent.class, clientCreationResponse.getClientId())//
                .extractingData(ClientDataV1::getId).isEqualTo(clientId)//
                .extractingData(clientDataV1 -> clientDataV1.getStatus().getId()).isEqualTo(status)//
                .extractingData(ClientDataV1::getFirstname).isEqualTo(firstname)//
                .extractingData(ClientDataV1::getLastname).isEqualTo(lastname)//
                .extractingData(ClientDataV1::getActive).isEqualTo(active);//

        eventAssertion.assertEvent(ClientActivatedEvent.class, clientCreationResponse.getClientId())//
                .extractingData(ClientDataV1::getActive).isEqualTo(true)//
                .extractingData(clientDataV1 -> clientDataV1.getStatus().getId()).isEqualTo(status);//
    }

    public void undoApproveLoanEventCheck(PostLoansLoanIdResponse loanUndoApproveResponse) {
        waitForTransactionCommit();
        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveOneLoan(loanUndoApproveResponse.getLoanId(),
                Map.of("staffInSelectedOfficeOnly", false, "associations", "", "exclude", "", "fields", "")));

        eventAssertion.assertEventRaised(LoanUndoApprovalEvent.class, body.getId());
    }

    public void loanRejectedEventCheck(PostLoansLoanIdResponse loanRejectedResponse) {
        waitForTransactionCommit();
        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveOneLoan(loanRejectedResponse.getLoanId(),
                Map.of("staffInSelectedOfficeOnly", false, "associations", "", "exclude", "", "fields", "")));

        eventAssertion.assertEventRaised(LoanRejectedEvent.class, body.getId());
    }

    public void disburseLoanEventCheck(Long loanId) {
        waitForTransactionCommit();
        loanAccountDataV1Check(LoanDisbursalEvent.class, loanId);
    }

    public void loanBalanceChangedEventCheck(Long loanId) {
        waitForTransactionCommit();
        loanAccountDataV1Check(LoanBalanceChangedEvent.class, loanId);
    }

    public void loanStatusChangedEventCheck(Long loanId) {
        waitForTransactionCommit();
        loanAccountDataV1Check(LoanStatusChangedEvent.class, loanId);
    }

    private void loanAccountDataV1Check(Class<? extends AbstractLoanEvent> eventClazz, Long loanId) {
        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveOneLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", false, "associations", "all", "exclude", "", "fields", "")));

        // Earlier steps may have raised events of the same type for this loan; assertEvent consumes the
        // latest one received, which is a stale predecessor when the newest event is still in transit.
        // Each attempt consumes one event, so retrying waits for the next one to arrive.
        retryOnAssertionError(EVENT_VERIFICATION_MAX_ATTEMPTS, () -> verifyLoanAccountDataV1(eventClazz, loanId, body));
    }

    private void retryOnAssertionError(int maxAttempts, Runnable verification) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                verification.run();
                return;
            } catch (AssertionError e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                log.debug("Event verification attempt {} of {} failed, retrying with the next received event", attempt, maxAttempts);
            }
        }
    }

    private void verifyLoanAccountDataV1(Class<? extends AbstractLoanEvent> eventClazz, Long loanId, GetLoansLoanIdResponse body) {
        eventAssertion.assertEvent(eventClazz, loanId)//
                .extractingData(loanAccountDataV1 -> {
                    assertThat(loanAccountDataV1.getId()).as("id").isEqualTo(body.getId());
                    assertThat(loanAccountDataV1.getStatus().getId().longValue()).as("status.id").isEqualTo(body.getStatus().getId());
                    assertThat(loanAccountDataV1.getStatus().getCode()).as("status.code").isEqualTo(body.getStatus().getCode());
                    assertThat(loanAccountDataV1.getClientId()).as("clientId").isEqualTo(body.getClientId());
                    assertAmountEquals("summary.principalDisbursed", loanAccountDataV1.getSummary().getPrincipalDisbursed(),
                            BigDecimal.valueOf(body.getSummary().getPrincipalDisbursed().doubleValue()));
                    assertThat(loanAccountDataV1.getTimeline().getActualDisbursementDate()).as("timeline.actualDisbursementDate")
                            .isEqualTo(FORMATTER_EVENTS.format(body.getTimeline().getActualDisbursementDate()));
                    assertThat(loanAccountDataV1.getSummary().getCurrency().getCode()).as("summary.currency.code")
                            .isEqualTo(body.getSummary().getCurrency().getCode());
                    assertAmountEquals("summary.totalUnpaidPayableDueInterest",
                            loanAccountDataV1.getSummary().getTotalUnpaidPayableDueInterest(),
                            body.getSummary().getTotalUnpaidPayableDueInterest());
                    assertAmountEquals("summary.totalUnpaidPayableNotDueInterest",
                            loanAccountDataV1.getSummary().getTotalUnpaidPayableNotDueInterest(),
                            body.getSummary().getTotalUnpaidPayableNotDueInterest());
                    assertAmountEquals("summary.totalInterestPaymentWaiver", loanAccountDataV1.getSummary().getTotalInterestPaymentWaiver(),
                            new BigDecimal(body.getSummary().getTotalInterestPaymentWaiver().doubleValue(), MathContext.DECIMAL64));
                    assertAmountEquals("delinquent.delinquentInterest", loanAccountDataV1.getDelinquent().getDelinquentInterest(),
                            body.getDelinquent().getDelinquentInterest());
                    assertAmountEquals("delinquent.delinquentFee", loanAccountDataV1.getDelinquent().getDelinquentFee(),
                            body.getDelinquent().getDelinquentFee());
                    assertAmountEquals("delinquent.delinquentPenalty", loanAccountDataV1.getDelinquent().getDelinquentPenalty(),
                            body.getDelinquent().getDelinquentPenalty());
                    assertThat(loanAccountDataV1.getActualNoTerm()).as("actualNoTerm").isEqualTo(body.getActualNoTerm());
                    return null;
                });
    }

    public GetLoansLoanIdTransactions getNthTransactionType(String nthItemStr, String transactionType, String transactionDate,
            List<GetLoansLoanIdTransactions> transactions) {
        int nthItem = Integer.parseInt(nthItemStr) - 1;
        return transactions//
                .stream()//
                .filter(t -> transactionDate.equals(DATE_FORMATTER.format(t.getDate())) && transactionType.equals(t.getType().getValue()))//
                .toList()//
                .get(nthItem);//
    }

    public GetLoansLoanIdTransactions findNthTransaction(String nthItemStr, String transactionType, String transactionDate, long loanId) {
        GetLoansLoanIdResponse loanResponse = ok(() -> fineractClient.loans().retrieveOneLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", false, "associations", "transactions", "exclude", "", "fields", "")));
        List<GetLoansLoanIdTransactions> transactions = loanResponse.getTransactions();
        GetLoansLoanIdTransactions targetTransaction = getNthTransactionType(nthItemStr, transactionType, transactionDate, transactions);
        return targetTransaction;
    }

    public void checkTransactionWithLoanTransactionAdjustmentBizEvent(GetLoansLoanIdTransactions transaction) {
        EventAssertion.EventAssertionBuilder<LoanTransactionAdjustmentDataV1> eventAssertionBuilder = eventAssertion
                .assertEvent(LoanAdjustTransactionBusinessEvent.class, transaction.getId());
        eventAssertionBuilder
                .extractingData(loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getId())
                .isEqualTo(transaction.getId());
        eventAssertionBuilder
                .extractingData(
                        loanTransactionAdjustmentDataV1 -> loanTransactionAdjustmentDataV1.getTransactionToAdjust().getManuallyReversed())
                .isEqualTo(Boolean.TRUE);
        eventAssertionBuilder.extractingData(LoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null);
    }

    public void loanUndoContractTerminationEventCheck(final GetLoansLoanIdTransactions transaction) {
        waitForTransactionCommit();
        eventAssertion.assertEventRaised(LoanUndoContractTerminationBusinessEvent.class, transaction.getId());
    }

    private static void assertAmountEquals(final String description, final BigDecimal actual, final BigDecimal expected) {
        assertThat(actual).as(description).isEqualByComparingTo(expected);
    }

    public void loanDisbursalTransactionEventCheck(PostLoansLoanIdResponse loanDisburseResponse) {
        waitForTransactionCommit();
        Long disbursementTransactionId = loanDisburseResponse.getSubResourceId();

        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveOneLoan(loanDisburseResponse.getLoanId(),
                Map.of("staffInSelectedOfficeOnly", false, "associations", "transactions", "exclude", "", "fields", "")));
        List<GetLoansLoanIdTransactions> transactions = body.getTransactions();
        GetLoansLoanIdTransactions disbursementTransaction = transactions//
                .stream()//
                .filter(t -> t.getId().equals(disbursementTransactionId))//
                .findFirst()//
                .orElseThrow(() -> new IllegalStateException("Disbursement transaction not found"));//

        eventAssertion.assertEvent(LoanDisbursalTransactionEvent.class, disbursementTransaction.getId())//
                .extractingData(LoanTransactionDataV1::getLoanId).isEqualTo(body.getId())//
                .extractingData(LoanTransactionDataV1::getDate).isEqualTo(FORMATTER_EVENTS.format(disbursementTransaction.getDate()))//
                .extractingBigDecimal(LoanTransactionDataV1::getAmount).isEqualTo(disbursementTransaction.getAmount());//
    }

    public void workingCapitalLoanDisbursalTransactionEventCheck(final Long loanId) {
        workingCapitalLoanDisbursalTransactionEventCheck(loanId, null);
    }

    public void workingCapitalLoanDisbursalTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse disbursementTransaction = findLastWorkingCapitalLoanTransaction(loanId,
                "disbursement", false, "Disbursement transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanDisbursalTransactionBusinessEvent.class, loanId, disbursementTransaction,
                expectedAmount, false);
    }

    public void workingCapitalLoanCreditBalanceRefundTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse cbrTransaction = findLastWorkingCapitalLoanTransaction(loanId,
                "creditBalanceRefund", false, "Credit balance refund transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanCreditBalanceRefundTransactionBusinessEvent.class, loanId, cbrTransaction,
                expectedAmount, false);
    }

    public void workingCapitalLoanUndoDisbursalTransactionEventCheck(final Long loanId) {
        workingCapitalLoanUndoDisbursalTransactionEventCheck(loanId, null);
    }

    public void workingCapitalLoanUndoDisbursalTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse reversedDisbursementTransaction = findLastWorkingCapitalLoanTransaction(loanId,
                "disbursement", true, "Reversed disbursement transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanUndoDisbursalTransactionBusinessEvent.class, loanId,
                reversedDisbursementTransaction, expectedAmount, true);
    }

    public void workingCapitalLoanDiscountFeeTransactionEventCheck(final Long loanId, String transactionType,
            final BigDecimal expectedAmount, String transactionDate) {
        final GetWorkingCapitalLoanTransactionIdResponse discountFeeTransaction = workingCapitalLoanTransactionDetails(loanId,
                transactionType, transactionDate);
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanDiscountFeeTransactionBusinessEvent.class, loanId, discountFeeTransaction,
                expectedAmount, false);
    }

    public void workingCapitalLoanDiscountFeeAdjustmentTransactionEventCheck(final Long loanId, String transactionType,
            final BigDecimal expectedAmount, String transactionDate) {
        final GetWorkingCapitalLoanTransactionIdResponse discountFeeTransaction = workingCapitalLoanTransactionDetails(loanId,
                transactionType, transactionDate);
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanDiscountFeeAdjustmentTransactionBusinessEvent.class, loanId,
                discountFeeTransaction, expectedAmount, false);
    }

    private GetWorkingCapitalLoanTransactionIdResponse findLastWorkingCapitalLoanTransaction(final Long loanId,
            final String transactionType, final boolean reversed, final String notFoundMessage) {
        final GetWorkingCapitalLoanTransactionsResponse body = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId));
        if (body.getContent() == null || body.getContent().isEmpty()) {
            throw new IllegalStateException("No Working Capital Loan transactions found");
        }
        final String expectedCode = "loanTransactionType." + transactionType;
        return body.getContent().stream().filter(
                t -> t.getType() != null && expectedCode.equals(t.getType().getCode()) && reversed == Boolean.TRUE.equals(t.getReversed()))
                .reduce((first, second) -> second).orElseThrow(() -> new IllegalStateException(notFoundMessage));
    }

    private void workingCapitalLoanTransactionEventCheck(final Class<? extends AbstractWorkingCapitalLoanTransactionEvent> eventClazz,
            final Long loanId, final GetWorkingCapitalLoanTransactionIdResponse transaction, final BigDecimal expectedAmount,
            final boolean expectedReversed) {
        eventAssertion.assertEvent(eventClazz, transaction.getId())//
                .extractingData(WorkingCapitalLoanTransactionDataV1::getWcLoanId).isEqualTo(loanId)//
                .extractingBigDecimal(WorkingCapitalLoanTransactionDataV1::getTransactionAmount)
                .isEqualTo(expectedAmount == null ? transaction.getTransactionAmount() : expectedAmount)//
                .extractingData(data -> data.getType().getCode()).isEqualTo(transaction.getType().getCode())//
                .extractingData(WorkingCapitalLoanTransactionDataV1::getReversed).isEqualTo(expectedReversed);
    }

    public GetWorkingCapitalLoanTransactionIdResponse workingCapitalLoanTransactionDetails(final Long loanId, String transactionType,
            String transactionDate) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionsResponse body = ok(
                () -> fineractClient.workingCapitalLoanTransactions().retrieveWorkingCapitalLoanTransactionsById(loanId));
        if (body.getContent() == null || body.getContent().isEmpty()) {
            throw new IllegalStateException("No Working Capital Loan transactions found");
        }

        String expectedCode = "loanTransactionType." + transactionType;

        return body.getContent().stream().filter(t -> {
            if (t.getType() == null) {
                return false;
            }
            assert t.getTransactionDate() != null;
            return transactionDate.equals(DATE_FORMATTER.format(t.getTransactionDate())) && expectedCode.equals(t.getType().getCode())
                    && !Boolean.TRUE.equals(t.getReversed());
        }).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException(String.format("%s transaction not found", transactionType)));
    }

    public EventAssertion.EventAssertionBuilder<LoanTransactionDataV1> transactionEventCheck(
            PostLoansLoanIdTransactionsResponse transactionResponse, TransactionType transactionType, String externalOwnerId) {
        Long loanId = transactionResponse.getLoanId();
        Long transactionId = transactionResponse.getResourceId();
        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveOneLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", false, "associations", "transactions", "exclude", "", "fields", "")));
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.getTransactions();
        GetLoansLoanIdTransactions transactionFound = transactions//
                .stream()//
                .filter(t -> t.getId().equals(transactionId))//
                .findAny()//
                .orElseThrow(() -> new IllegalStateException("Transaction cannot be found"));//

        Class<? extends AbstractLoanTransactionEvent> eventClass = switch (transactionType) {
            case REPAYMENT -> LoanTransactionMakeRepaymentPostEvent.class;
            case GOODWILL_CREDIT -> LoanTransactionGoodwillCreditPostEvent.class;
            case PAYOUT_REFUND -> LoanTransactionPayoutRefundPostEvent.class;
            case MERCHANT_ISSUED_REFUND -> LoanTransactionMerchantIssuedRefundPostEvent.class;
            case REFUND_BY_CASH -> LoanRefundPostBusinessEvent.class;
            case INTEREST_PAYMENT_WAIVER -> LoanTransactionInterestPaymentWaiverPostEvent.class;
            case INTEREST_REFUND -> LoanTransactionInterestRefundPostEvent.class;
            default -> throw new IllegalStateException(String.format("transaction type %s cannot be found", transactionType.getValue()));
        };

        EventAssertion.EventAssertionBuilder<LoanTransactionDataV1> eventBuilder = eventAssertion.assertEvent(eventClass, transactionId);
        eventBuilder.extractingData(LoanTransactionDataV1::getLoanId).isEqualTo(loanDetailsResponse.getId())//
                .extractingData(LoanTransactionDataV1::getDate).isEqualTo(FORMATTER_EVENTS.format(transactionFound.getDate()))//
                .extractingBigDecimal(LoanTransactionDataV1::getAmount).isEqualTo(transactionFound.getAmount())//
                .extractingData(LoanTransactionDataV1::getExternalOwnerId).isEqualTo(externalOwnerId);//
        return eventBuilder;
    }

    public void loanOwnershipTransferBusinessEventCheck(Long loanId, Long transferId) {
        waitForTransactionCommit();
        ExternalTransferData filtered = findLastTransfer(loanId, transferId);
        loanOwnershipTransferEventBaseCheck(loanId, filtered, expectedAmountsOf(filtered.getDetails()));
    }

    public void loanOwnershipTransferBusinessEventWithStatusCheck(Long loanId, Long transferId, String transferStatus,
            String transferStatusReason) {
        ExternalTransferData filtered = findLastTransfer(loanId, transferId);
        ExpectedTransferAmounts expected = filtered.getDetails() == null ? ExpectedTransferAmounts.EMPTY
                : expectedAmountsOf(filtered.getDetails());

        loanOwnershipTransferEventBaseCheck(loanId, filtered, expected)//
                .extractingData(LoanOwnershipTransferDataV1::getTransferStatus)
                .isEqualTo(AssetExternalizationTransferStatus.valueOf(transferStatus).getValue())//
                .extractingData(LoanOwnershipTransferDataV1::getTransferStatusReason)
                .isEqualTo(AssetExternalizationTransferStatusReason.valueOf(transferStatusReason).getValue());
    }

    public void loanOwnershipTransferBusinessEventWithTypeCheck(Long loanId, ExternalTransferData transferData, String transferType,
            String previousAssetOwner) {
        String assetOwner = transferData.getOwner() == null ? null : transferData.getOwner().getExternalId();
        ExternalTransferData filtered = findLastTransfer(loanId, transferData.getTransferId());
        ExpectedTransferAmounts expected = filtered.getDetails() == null ? ExpectedTransferAmounts.EMPTY
                : expectedAmountsOf(filtered.getDetails());

        loanOwnershipTransferEventBaseCheck(loanId, filtered, expected)//
                .extractingData(LoanOwnershipTransferDataV1::getType).isEqualTo(transferType)//
                .extractingData(LoanOwnershipTransferDataV1::getAssetOwnerExternalId).isEqualTo(assetOwner)//
                .extractingData(LoanOwnershipTransferDataV1::getPreviousOwnerExternalId).isEqualTo(previousAssetOwner);
    }

    private ExternalTransferData findLastTransfer(final Long loanId, final Long transferId) {
        final PageExternalTransferData response = ok(() -> fineractClient.externalAssetOwners().getTransfers(Map.of("loanId", loanId)));
        return response.getContent().stream().filter(t -> transferId.equals(t.getTransferId())).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No element found"));
    }

    private record ExpectedTransferAmounts(BigDecimal totalOutstanding, BigDecimal principal, BigDecimal fee, BigDecimal penalty,
            BigDecimal interest, BigDecimal overpayment) {

        static final ExpectedTransferAmounts EMPTY = new ExpectedTransferAmounts(null, null, null, null, null, null);
    }

    private ExpectedTransferAmounts expectedAmountsOf(final ExternalTransferDataDetails details) {
        return new ExpectedTransferAmounts(zeroConversion(details.getTotalOutstanding()),
                zeroConversion(details.getTotalPrincipalOutstanding()), zeroConversion(details.getTotalFeeChargesOutstanding()),
                zeroConversion(details.getTotalPenaltyChargesOutstanding()), zeroConversion(details.getTotalInterestOutstanding()),
                zeroConversion(details.getTotalOverpaid()));
    }

    private EventAssertion.EventAssertionBuilder<LoanOwnershipTransferDataV1> loanOwnershipTransferEventBaseCheck(final Long loanId,
            final ExternalTransferData filtered, final ExpectedTransferAmounts expected) {
        return eventAssertion.assertEvent(LoanOwnershipTransferEvent.class, loanId)//
                .extractingData(LoanOwnershipTransferDataV1::getLoanId).isEqualTo(loanId)//
                .extractingData(LoanOwnershipTransferDataV1::getAssetOwnerExternalId).isEqualTo(filtered.getOwner().getExternalId())//
                .extractingData(LoanOwnershipTransferDataV1::getTransferExternalId).isEqualTo(filtered.getTransferExternalId())//
                .extractingData(LoanOwnershipTransferDataV1::getSettlementDate)
                .isEqualTo(FORMATTER_EVENTS.format(filtered.getSettlementDate()))//
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getTotalOutstandingBalanceAmount).isEqualTo(expected.totalOutstanding())//
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingPrincipalPortion).isEqualTo(expected.principal())//
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingFeePortion).isEqualTo(expected.fee())//
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingPenaltyPortion).isEqualTo(expected.penalty())//
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingInterestPortion).isEqualTo(expected.interest())//
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOverPaymentPortion).isEqualTo(expected.overpayment());
    }

    public void loanAccountSnapshotBusinessEventCheck(Long loanId, Long transferId) {
        waitForTransactionCommit();
        ExternalTransferData filtered = findLastTransfer(loanId, transferId);

        BigDecimal totalOutstandingBalanceAmountExpected = zeroConversion(filtered.getDetails().getTotalOutstanding());
        BigDecimal outstandingInterestPortionExpected = zeroConversion(filtered.getDetails().getTotalInterestOutstanding());

        GlobalConfigurationPropertyData outstandingInterestStrategy = configurationHelper
                .getGlobalConfiguration("outstanding-interest-calculation-strategy-for-external-asset-transfer");
        if ("PAYABLE_OUTSTANDING_INTEREST".equals(outstandingInterestStrategy.getStringValue())) {
            GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveOneLoan(loanId,
                    Map.of("staffInSelectedOfficeOnly", false, "associations", "all", "exclude", "", "fields", "")));
            totalOutstandingBalanceAmountExpected = zeroConversion(loanDetails.getSummary().getTotalOutstanding());
            outstandingInterestPortionExpected = zeroConversion(loanDetails.getSummary().getInterestOutstanding());
        }

        String ownerExternalIdExpected = filtered.getStatus().getValue().equals("BUYBACK") ? null : filtered.getOwner().getExternalId();
        String settlementDateExpected = filtered.getStatus().getValue().equals("BUYBACK") ? null
                : FORMATTER_EVENTS.format(filtered.getSettlementDate());
        BigDecimal outstandingPrincipalPortionExpected = zeroConversion(filtered.getDetails().getTotalPrincipalOutstanding());
        BigDecimal outstandingFeePortionExpected = zeroConversion(filtered.getDetails().getTotalFeeChargesOutstanding());
        BigDecimal outstandingPenaltyPortionExpected = zeroConversion(filtered.getDetails().getTotalPenaltyChargesOutstanding());

        BigDecimal overPaymentPortionExpected = zeroConversion(filtered.getDetails().getTotalOverpaid());

        eventAssertion.assertEvent(LoanAccountSnapshotEvent.class, loanId).extractingData(LoanAccountDataV1::getId).isEqualTo(loanId)
                .extractingData(LoanAccountDataV1::getExternalOwnerId).isEqualTo(ownerExternalIdExpected)
                .extractingData(LoanAccountDataV1::getSettlementDate).isEqualTo(settlementDateExpected)
                .extractingBigDecimal(loanAccountDataV1 -> loanAccountDataV1.getSummary().getTotalOutstanding())
                .isEqualTo(totalOutstandingBalanceAmountExpected)
                .extractingBigDecimal(loanAccountDataV1 -> loanAccountDataV1.getSummary().getPrincipalOutstanding())
                .isEqualTo(outstandingPrincipalPortionExpected)
                .extractingBigDecimal(loanAccountDataV1 -> loanAccountDataV1.getSummary().getFeeChargesOutstanding())
                .isEqualTo(outstandingFeePortionExpected)
                .extractingBigDecimal(loanAccountDataV1 -> loanAccountDataV1.getSummary().getPenaltyChargesOutstanding())
                .isEqualTo(outstandingPenaltyPortionExpected)
                .extractingBigDecimal(loanAccountDataV1 -> loanAccountDataV1.getSummary().getInterestOutstanding())
                .isEqualTo(outstandingInterestPortionExpected)
                .extractingBigDecimal(loanAccountDataV1 -> loanAccountDataV1.getSummary().getTotalOverdue())
                .isEqualTo(overPaymentPortionExpected);
    }

    public void loanAccountDelinquencyPauseChangedBusinessEventCheck(Long loanId) {
        waitForTransactionCommit();
        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveOneLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", false, "associations", "all", "exclude", "", "fields", "")));
        List<GetLoansLoanIdDelinquencyPausePeriod> delinquencyPausePeriodsActual = loanDetails.getDelinquent().getDelinquencyPausePeriods();

        eventAssertion.assertEvent(LoanDelinquencyPauseChangedEvent.class, loanId)//
                .extractingData(LoanAccountDataV1::getId).isEqualTo(loanId)//
                .extractingData(loanAccountDataV1 -> {
                    List<DelinquencyPausePeriodV1> delinquencyPausePeriodsExpected = loanAccountDataV1.getDelinquent()
                            .getDelinquencyPausePeriods();

                    for (int i = 0; i < delinquencyPausePeriodsActual.size(); i++) {
                        Boolean isActiveActual = delinquencyPausePeriodsActual.get(i).getActive();
                        String pausePeriodStartActual = FORMATTER_EVENTS.format(delinquencyPausePeriodsActual.get(i).getPausePeriodStart());
                        String pausePeriodEndActual = FORMATTER_EVENTS.format(delinquencyPausePeriodsActual.get(i).getPausePeriodEnd());

                        Boolean isActiveExpected = delinquencyPausePeriodsExpected.get(i).getActive();
                        String pausePeriodStartExpected = delinquencyPausePeriodsExpected.get(i).getPausePeriodStart();
                        String pausePeriodEndExpected = delinquencyPausePeriodsExpected.get(i).getPausePeriodEnd();

                        assertThat(isActiveActual)//
                                .as(ErrorMessageHelper.wrongValueInPauseDelinquencyEventActive(i, isActiveActual, isActiveExpected))//
                                .isEqualTo(isActiveExpected);//
                        assertThat(pausePeriodStartActual)//
                                .as(ErrorMessageHelper.wrongValueInPauseDelinquencyEventStartDate(i, pausePeriodStartActual,
                                        pausePeriodStartExpected))//
                                .isEqualTo(pausePeriodStartExpected);//
                        assertThat(pausePeriodEndActual)//
                                .as(ErrorMessageHelper.wrongValueInPauseDelinquencyEventEndDate(i, pausePeriodEndActual,
                                        pausePeriodEndExpected))//
                                .isEqualTo(pausePeriodEndExpected);//

                        log.debug("LoanAccountDelinquencyPauseChangedBusinessEvent -> isActiveActual: {}", isActiveActual);
                        log.debug("LoanAccountDelinquencyPauseChangedBusinessEvent -> pausePeriodStartActual: {}", pausePeriodStartActual);
                        log.debug("LoanAccountDelinquencyPauseChangedBusinessEvent -> pausePeriodEndActual: {}", pausePeriodEndActual);
                    }
                    return null;
                });
    }

    public void installmentLevelDelinquencyRangeChangeEventCheck(Long loanId) {
        waitForTransactionCommit();
        eventAssertion.assertEvent(LoanDelinquencyRangeChangeEvent.class, loanId).extractingData(loanAccountDelinquencyRangeDataV1 -> {
            // check if sum of total amounts equal the sum of amount types in installmentDelinquencyBuckets
            BigDecimal totalAmountSum = loanAccountDelinquencyRangeDataV1.getInstallmentDelinquencyBuckets().stream()//
                    .map(LoanInstallmentDelinquencyBucketDataV1::getAmount)//
                    .map(LoanAmountDataV1::getTotalAmount)//
                    .reduce(BigDecimal.ZERO, BigDecimal::add);//
            BigDecimal principalAmountSum = loanAccountDelinquencyRangeDataV1.getInstallmentDelinquencyBuckets().stream()//
                    .map(LoanInstallmentDelinquencyBucketDataV1::getAmount)//
                    .map(LoanAmountDataV1::getPrincipalAmount)//
                    .reduce(BigDecimal.ZERO, BigDecimal::add);//
            BigDecimal interestAmountSum = loanAccountDelinquencyRangeDataV1.getInstallmentDelinquencyBuckets().stream()//
                    .map(LoanInstallmentDelinquencyBucketDataV1::getAmount)//
                    .map(LoanAmountDataV1::getInterestAmount)//
                    .reduce(BigDecimal.ZERO, BigDecimal::add);//
            BigDecimal feeAmountSum = loanAccountDelinquencyRangeDataV1.getInstallmentDelinquencyBuckets().stream()//
                    .map(LoanInstallmentDelinquencyBucketDataV1::getAmount)//
                    .map(LoanAmountDataV1::getFeeAmount)//
                    .reduce(BigDecimal.ZERO, BigDecimal::add);//
            BigDecimal penaltyAmountSum = loanAccountDelinquencyRangeDataV1.getInstallmentDelinquencyBuckets().stream()//
                    .map(LoanInstallmentDelinquencyBucketDataV1::getAmount)//
                    .map(LoanAmountDataV1::getPenaltyAmount)//
                    .reduce(BigDecimal.ZERO, BigDecimal::add);//

            BigDecimal totalAmountSumActual = principalAmountSum.add(interestAmountSum).add(feeAmountSum).add(penaltyAmountSum);

            assertThat(totalAmountSum)
                    .as(ErrorMessageHelper.wrongAmountInLoanDelinquencyRangeChangedEventTotalAmount(totalAmountSum, totalAmountSumActual))
                    .isEqualByComparingTo(totalAmountSumActual);

            log.debug("Nr of installment level delinquency buckets: {}",
                    loanAccountDelinquencyRangeDataV1.getInstallmentDelinquencyBuckets().size());
            log.debug("Buckets:");
            loanAccountDelinquencyRangeDataV1.getInstallmentDelinquencyBuckets().forEach(e -> {
                log.debug("{} - Total amount: {}", e.getDelinquencyRange().getClassification(), e.getAmount().getTotalAmount());
            });

            return null;
        });
    }

    private BigDecimal zeroConversion(BigDecimal input) {
        return input.compareTo(new BigDecimal("0.000000")) == 0 ? new BigDecimal(input.toEngineeringString()) : input.setScale(8);
    }

    public void createLoanEventCheck(PostLoansResponse createLoanResponse) {
        waitForTransactionCommit();
        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveOneLoan(createLoanResponse.getLoanId(),
                Map.of("staffInSelectedOfficeOnly", false, "associations", "all", "exclude", "", "fields", "")));

        eventAssertion.assertEvent(LoanCreatedEvent.class, createLoanResponse.getLoanId())//
                .extractingData(LoanAccountDataV1::getId).isEqualTo(body.getId())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getId().intValue())
                .isEqualTo(body.getStatus().getId().intValue())//
                .extractingData(LoanAccountDataV1::getClientId).isEqualTo(body.getClientId())//
                .extractingBigDecimal(LoanAccountDataV1::getPrincipal).isEqualTo(body.getPrincipal())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getCurrency().getCode())
                .isEqualTo(body.getCurrency().getCode());//
    }

    public void approveLoanEventCheck(PostLoansLoanIdResponse loanApproveResponse) {
        waitForTransactionCommit();
        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveOneLoan(loanApproveResponse.getLoanId(),
                Map.of("staffInSelectedOfficeOnly", false, "associations", "", "exclude", "", "fields", "")));

        eventAssertion.assertEvent(LoanApprovedEvent.class, loanApproveResponse.getLoanId())//
                .extractingData(LoanAccountDataV1::getId).isEqualTo(body.getId())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getId().intValue())
                .isEqualTo(body.getStatus().getId().intValue())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getCode()).isEqualTo(body.getStatus().getCode())//
                .extractingData(LoanAccountDataV1::getClientId).isEqualTo(Long.valueOf(body.getClientId()))//
                .extractingBigDecimal(LoanAccountDataV1::getApprovedPrincipal).isEqualTo(body.getApprovedPrincipal())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getTimeline().getApprovedOnDate())//
                .isEqualTo(FORMATTER_EVENTS.format(body.getTimeline().getApprovedOnDate()))//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getCurrency().getCode())
                .isEqualTo(body.getCurrency().getCode());//
    }

    public void workingCapitalLoanCreatedEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanCreatedEvent.class, loanId);
    }

    public void workingCapitalLoanApplicationModifiedEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanApplicationModifiedEvent.class, loanId);
    }

    public void workingCapitalLoanApprovedEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanApprovedEvent.class, loanId);
    }

    public void workingCapitalLoanUndoApprovalEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanUndoApprovalEvent.class, loanId);
    }

    public void workingCapitalLoanRejectedEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanRejectedEvent.class, loanId);
    }

    public void workingCapitalLoanDisbursalEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanDisbursalEvent.class, loanId);
    }

    public void workingCapitalLoanUndoDisbursalEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanUndoDisbursalEvent.class, loanId);
    }

    public void workingCapitalLoanStatusChangedEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanStatusChangedEvent.class, loanId);
    }

    private GetWorkingCapitalLoansLoanIdResponse fetchWorkingCapitalLoan(final Long loanId) {
        return ok(() -> fineractClient.workingCapitalLoans().retrieveWorkingCapitalLoanById(loanId));
    }

    private void workingCapitalLoanEventMatchesApiCheck(final Class<? extends AbstractWorkingCapitalLoanEvent> eventClazz,
            final Long loanId, final BiConsumer<WorkingCapitalLoanAccountDataV1, GetWorkingCapitalLoansLoanIdResponse> payloadAsserts) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoansLoanIdResponse body = fetchWorkingCapitalLoan(loanId);
        eventAssertion.assertEvent(eventClazz, loanId).extractingData(event -> {
            payloadAsserts.accept(event, body);
            return null;
        });
    }

    private void workingCapitalLoanEventPayloadCheck(final Class<? extends AbstractWorkingCapitalLoanEvent> eventClazz, final Long loanId,
            final Consumer<WorkingCapitalLoanAccountDataV1> payloadAsserts) {
        waitForTransactionCommit();
        eventAssertion.assertEvent(eventClazz, loanId).extractingData(event -> {
            payloadAsserts.accept(event);
            return null;
        });
    }

    public void workingCapitalLoanBalanceChangedEventCheck(final Long loanId) {
        workingCapitalLoanEventMatchesApiCheck(WorkingCapitalLoanBalanceChangedEvent.class, loanId, (event, body) -> {
            assertWorkingCapitalLoanAccountData(event, body);

            final WorkingCapitalLoanSummaryDataV1 eventSummary = event.getSummary();
            final GetWorkingCapitalLoanSummary bodySummary = body.getSummary();
            assertThat(eventSummary).isNotNull();
            assertThat(bodySummary).isNotNull();
            assertAmountEquals("summary.principalPaid", eventSummary.getPrincipalPaid(), bodySummary.getPrincipalPaid());
            assertAmountEquals("summary.principalOutstanding", eventSummary.getPrincipalOutstanding(),
                    bodySummary.getPrincipalOutstanding());
            assertAmountEquals("summary.totalOutstanding", eventSummary.getTotalOutstanding(), bodySummary.getTotalOutstanding());
            assertAmountEquals("totalOverpaid", event.getTotalOverpaid(), bodySummary.getOverpayment());
        });
    }

    public void workingCapitalLoanBalanceChangedEventChargesCheck(final Long loanId, final List<Map<String, String>> expectedCharges) {
        workingCapitalLoanEventPayloadCheck(WorkingCapitalLoanBalanceChangedEvent.class, loanId, event -> {
            final List<WorkingCapitalLoanChargeDataV1> eventCharges = event.getCharges();
            assertThat(eventCharges).isNotNull().hasSize(expectedCharges.size());
            IntStream.range(0, expectedCharges.size()).forEach(i -> {
                final Map<String, String> expected = expectedCharges.get(i);
                final WorkingCapitalLoanChargeDataV1 actual = eventCharges.get(i);
                assertAmountEquals("charges[" + i + "].amount", actual.getAmount(), new BigDecimal(expected.get("amount")));
                assertAmountEquals("charges[" + i + "].amountAccrued", actual.getAmountAccrued(),
                        new BigDecimal(expected.get("amountAccrued")));
                assertAmountEquals("charges[" + i + "].amountUnrecognized", actual.getAmountUnrecognized(),
                        new BigDecimal(expected.get("amountUnrecognized")));
            });
        });
    }

    public void workingCapitalLoanDelinquencyRangeChangeEventCheck(final Long loanId) {
        workingCapitalLoanEventMatchesApiCheck(WorkingCapitalLoanDelinquencyRangeChangeEvent.class, loanId, (event, body) -> {
            assertWorkingCapitalLoanAccountData(event, body);

            final WorkingCapitalLoanCollectionDataV1 eventDelinquent = event.getDelinquent();
            final WorkingCapitalCollection bodyDelinquent = body.getDelinquent();
            assertThat(eventDelinquent).isNotNull();
            assertThat(bodyDelinquent).isNotNull();
            assertAmountEquals("delinquent.delinquentAmount", eventDelinquent.getDelinquentAmount(), bodyDelinquent.getDelinquentAmount());
            assertAmountEquals("delinquent.totalDelinquentAmount", eventDelinquent.getTotalDelinquentAmount(),
                    bodyDelinquent.getDelinquentPrincipal());
            assertThat(eventDelinquent.getDelinquencySchedule()).isNotEmpty();
        });
    }

    public void workingCapitalLoanDelinquencyRangeChangeEventNamesRangeCheck(final Long loanId) {
        workingCapitalLoanEventMatchesApiCheck(WorkingCapitalLoanDelinquencyRangeChangeEvent.class, loanId, (event, body) -> {
            assertWorkingCapitalLoanAccountData(event, body);

            final WorkingCapitalLoanCollectionDataV1 eventDelinquent = event.getDelinquent();
            final WorkingCapitalCollection bodyDelinquent = body.getDelinquent();
            assertThat(eventDelinquent).isNotNull();
            assertThat(bodyDelinquent).isNotNull();
            assertAmountEquals("delinquent.delinquentAmount", eventDelinquent.getDelinquentAmount(), bodyDelinquent.getDelinquentAmount());
            assertAmountEquals("delinquent.totalDelinquentAmount", eventDelinquent.getTotalDelinquentAmount(),
                    bodyDelinquent.getDelinquentPrincipal());

            final List<WorkingCapitalLoanDelinquencySchedulePeriodDataV1> schedule = eventDelinquent.getDelinquencySchedule();
            assertThat(schedule).as("delinquent.delinquencySchedule").isNotEmpty();
            final List<WorkingCapitalLoanDelinquencyScheduleTagDataV1> eventTags = schedule.stream()
                    .map(WorkingCapitalLoanDelinquencySchedulePeriodDataV1::getTags).filter(Objects::nonNull).flatMap(List::stream)
                    .sorted(Comparator
                            .comparing(WorkingCapitalLoanDelinquencyScheduleTagDataV1::getRangeId,
                                    Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(WorkingCapitalLoanDelinquencyScheduleTagDataV1::getDelinquentAmount,
                                    Comparator.nullsFirst(Comparator.naturalOrder())))
                    .toList();
            assertThat(eventTags).as("delinquent.delinquencySchedule[].tags").isNotEmpty();

            final List<WorkingCapitalCollectionRangeScheduleDelinquency> apiTags = Optional
                    .ofNullable(bodyDelinquent.getInstallmentLevelDelinquency()).orElse(List.of()).stream()
                    .sorted(Comparator
                            .comparing(WorkingCapitalCollectionRangeScheduleDelinquency::getRangeId,
                                    Comparator.nullsFirst(Comparator.naturalOrder()))
                            .thenComparing(WorkingCapitalCollectionRangeScheduleDelinquency::getDelinquentAmount,
                                    Comparator.nullsFirst(Comparator.naturalOrder())))
                    .toList();
            assertThat(eventTags).as("delinquent.delinquencySchedule[].tags vs API delinquent.installmentLevelDelinquency")
                    .hasSize(apiTags.size());
            IntStream.range(0, eventTags.size()).forEach(i -> {
                final WorkingCapitalLoanDelinquencyScheduleTagDataV1 actual = eventTags.get(i);
                final WorkingCapitalCollectionRangeScheduleDelinquency expected = apiTags.get(i);
                assertThat(actual.getRangeId()).as("tags[%s].rangeId", i).isEqualTo(expected.getRangeId());
                assertThat(actual.getClassification()).as("tags[%s].classification", i).isNotBlank()
                        .isEqualTo(expected.getClassification());
                assertThat(actual.getMinimumAgeDays()).as("tags[%s].minimumAgeDays", i).isEqualTo(expected.getMinimumAgeDays());
                assertThat(actual.getMaximumAgeDays()).as("tags[%s].maximumAgeDays", i).isEqualTo(expected.getMaximumAgeDays());
                if (expected.getDelinquentAmount() == null) {
                    assertThat(actual.getDelinquentAmount()).as("tags[%s].delinquentAmount", i).isNull();
                } else {
                    assertAmountEquals("tags[" + i + "].delinquentAmount", actual.getDelinquentAmount(), expected.getDelinquentAmount());
                }
            });
        });
    }

    public void workingCapitalLoanBalanceChangedEventSummaryTotalsCheck(final Long loanId, final Map<String, String> expectedTotals) {
        workingCapitalLoanEventPayloadCheck(WorkingCapitalLoanBalanceChangedEvent.class, loanId, event -> {
            final WorkingCapitalLoanSummaryDataV1 eventSummary = event.getSummary();
            assertThat(eventSummary).isNotNull();
            expectedTotals.forEach((column, expectedValue) -> assertAmountEquals("summary." + column, summaryTotalOf(eventSummary, column),
                    new BigDecimal(expectedValue)));
        });
    }

    private BigDecimal summaryTotalOf(final WorkingCapitalLoanSummaryDataV1 summary, final String columnName) {
        return switch (columnName) {
            case "totalPayoutRefund" -> summary.getTotalPayoutRefund();
            case "totalPayoutRefundReversed" -> summary.getTotalPayoutRefundReversed();
            case "totalGoodwillCredit" -> summary.getTotalGoodwillCredit();
            case "totalGoodwillCreditReversed" -> summary.getTotalGoodwillCreditReversed();
            case "totalChargeAdjustment" -> summary.getTotalChargeAdjustment();
            case "totalChargeAdjustmentReversed" -> summary.getTotalChargeAdjustmentReversed();
            case "totalCreditBalanceRefund" -> summary.getTotalCreditBalanceRefund();
            case "totalCreditBalanceRefundReversed" -> summary.getTotalCreditBalanceRefundReversed();
            case "totalRepaymentTransaction" -> summary.getTotalRepaymentTransaction();
            case "totalRepaymentTransactionReversed" -> summary.getTotalRepaymentTransactionReversed();
            case "totalPayment" -> summary.getTotalPayment();
            case "totalPaymentReversed" -> summary.getTotalPaymentReversed();
            default -> throw new IllegalArgumentException("Unsupported summary transaction type total column: " + columnName);
        };
    }

    public void workingCapitalLoanBalanceChangedEventChargesWithoutAccrualCheck(final Long loanId) {
        workingCapitalLoanEventPayloadCheck(WorkingCapitalLoanBalanceChangedEvent.class, loanId, event -> {
            final List<WorkingCapitalLoanChargeDataV1> eventCharges = event.getCharges();
            assertThat(eventCharges).isNotNull().isNotEmpty();
            eventCharges.forEach(charge -> {
                assertThat(charge.getAmountAccrued()).as("amountAccrued of charge %s", charge.getId()).isNull();
                assertThat(charge.getAmountUnrecognized()).as("amountUnrecognized of charge %s", charge.getId()).isNull();
            });
        });
    }

    public void workingCapitalLoanDisbursalEventDeepPayloadCheck(final Long loanId) {
        workingCapitalLoanEventMatchesApiCheck(WorkingCapitalLoanDisbursalEvent.class, loanId, (event, body) -> {
            assertWorkingCapitalLoanAccountData(event, body);
            assertAmountEquals("principal", event.getPrincipal(), body.getPrincipal());
            assertAmountEquals("approvedPrincipal", event.getApprovedPrincipal(), body.getApprovedPrincipal());
            assertAmountEquals("netDisbursalAmount", event.getNetDisbursalAmount(), body.getNetDisbursalAmount());
            assertWorkingCapitalLoanTimelineData(event.getTimeline(), body.getTimeline());
            assertWorkingCapitalLoanBreachData(event.getBreach(), body);
            assertWorkingCapitalLoanDelinquencyData(event.getDelinquency(), body);
        });
    }

    private void assertWorkingCapitalLoanTimelineData(final LoanApplicationTimelineDataV1 eventTimeline,
            final GetWorkingCapitalLoansLoanIdTimeline bodyTimeline) {
        assertThat(eventTimeline).isNotNull();
        assertThat(bodyTimeline).isNotNull();
        assertEventDateEqualsApiDate("timeline.submittedOnDate", eventTimeline.getSubmittedOnDate(), bodyTimeline.getSubmittedOnDate());
        assertEventDateEqualsApiDate("timeline.approvedOnDate", eventTimeline.getApprovedOnDate(), bodyTimeline.getApprovedOnDate());
        assertEventDateEqualsApiDate("timeline.expectedDisbursementDate", eventTimeline.getExpectedDisbursementDate(),
                bodyTimeline.getExpectedDisbursementDate());
        assertEventDateEqualsApiDate("timeline.actualDisbursementDate", eventTimeline.getActualDisbursementDate(),
                bodyTimeline.getActualDisbursementDate());
        assertEventDateEqualsApiDate("timeline.expectedMaturityDate", eventTimeline.getExpectedMaturityDate(),
                bodyTimeline.getExpectedMaturityDate());
        assertEventDateEqualsApiDate("timeline.closedOnDate", eventTimeline.getClosedOnDate(), bodyTimeline.getClosedOnDate());
        assertThat(eventTimeline.getSubmittedByUsername()).as("timeline.submittedByUsername")
                .isEqualTo(bodyTimeline.getSubmittedByUsername());
        assertThat(eventTimeline.getApprovedByUsername()).as("timeline.approvedByUsername").isEqualTo(bodyTimeline.getApprovedByUsername());
        assertThat(eventTimeline.getDisbursedByUsername()).as("timeline.disbursedByUsername")
                .isEqualTo(bodyTimeline.getDisbursedByUsername());
    }

    private void assertWorkingCapitalLoanBreachData(final WorkingCapitalBreachDataV1 eventBreach,
            final GetWorkingCapitalLoansLoanIdResponse body) {
        if (body.getBreach() == null && body.getBreachGraceDays() == null && body.getBreachStartDate() == null) {
            return;
        }
        assertThat(eventBreach).as("breach").isNotNull();
        Optional.ofNullable(body.getBreach()).ifPresent(bodyBreach -> {
            assertThat(eventBreach.getId()).as("breach.id").isEqualTo(bodyBreach.getId());
            assertThat(eventBreach.getName()).as("breach.name").isEqualTo(bodyBreach.getName());
            assertThat(eventBreach.getBreachFrequency()).as("breach.breachFrequency").isEqualTo(bodyBreach.getBreachFrequency());
            assertAmountEquals("breach.breachAmount", eventBreach.getBreachAmount(), bodyBreach.getBreachAmount());
        });
        assertThat(eventBreach.getBreachGraceDays()).as("breach.breachGraceDays").isEqualTo(body.getBreachGraceDays());
        assertEventDateEqualsApiDate("breach.breachStartDate", eventBreach.getBreachStartDate(), body.getBreachStartDate());
        Optional.ofNullable(body.getNearBreach()).ifPresent(bodyNearBreach -> {
            assertThat(eventBreach.getNearBreach()).as("breach.nearBreach").isNotNull();
            assertThat(eventBreach.getNearBreach().getFrequency()).as("breach.nearBreach.frequency")
                    .isEqualTo(bodyNearBreach.getFrequency());
            assertAmountEquals("breach.nearBreach.threshold", eventBreach.getNearBreach().getThreshold(), bodyNearBreach.getThreshold());
        });
    }

    private void assertWorkingCapitalLoanDelinquencyData(final WorkingCapitalLoanDelinquencyDataV1 eventDelinquency,
            final GetWorkingCapitalLoansLoanIdResponse body) {
        if (body.getDelinquencyStartType() == null && body.getDelinquencyStartDate() == null) {
            return;
        }
        assertThat(eventDelinquency).as("delinquency").isNotNull();
        Optional.ofNullable(body.getDelinquencyStartType()).ifPresent(bodyStartType -> {
            assertThat(eventDelinquency.getDelinquencyStartType()).as("delinquency.delinquencyStartType").isNotNull();
            assertThat(eventDelinquency.getDelinquencyStartType().getCode()).as("delinquency.delinquencyStartType.code")
                    .isEqualTo(bodyStartType.getCode());
        });
        assertEventDateEqualsApiDate("delinquency.delinquencyStartDate", eventDelinquency.getDelinquencyStartDate(),
                body.getDelinquencyStartDate());
    }

    public void workingCapitalLoanStatusChangedEventTimelineCheck(final Long loanId) {
        workingCapitalLoanEventMatchesApiCheck(WorkingCapitalLoanStatusChangedEvent.class, loanId, (event, body) -> {
            assertWorkingCapitalLoanAccountData(event, body);
            assertWorkingCapitalLoanTimelineData(event.getTimeline(), body.getTimeline());
        });
    }

    public void workingCapitalLoanBalanceChangedEventPausePeriodsCheck(final Long loanId) {
        workingCapitalLoanEventMatchesApiCheck(WorkingCapitalLoanBalanceChangedEvent.class, loanId, (event, body) -> {
            assertThat(event.getDelinquent()).isNotNull();
            final List<DelinquencyPausePeriodV1> eventPausePeriods = event.getDelinquent().getDelinquencyPausePeriods();
            assertThat(eventPausePeriods).isNotNull().isNotEmpty();
            assertThat(body.getDelinquent()).isNotNull();
            final List<WorkingCapitalCollectionDelinquencyPausePeriod> bodyPausePeriods = body.getDelinquent().getDelinquencyPausePeriods();
            assertThat(bodyPausePeriods).isNotNull().hasSize(eventPausePeriods.size());
            IntStream.range(0, eventPausePeriods.size()).forEach(i -> {
                final DelinquencyPausePeriodV1 actual = eventPausePeriods.get(i);
                final WorkingCapitalCollectionDelinquencyPausePeriod expected = bodyPausePeriods.get(i);
                assertThat(actual.getActive()).as("delinquencyPausePeriods[%s].active", i).isEqualTo(expected.getActive());
                assertEventDateEqualsApiDate("delinquencyPausePeriods[" + i + "].pausePeriodStart", actual.getPausePeriodStart(),
                        expected.getPausePeriodStart());
                assertEventDateEqualsApiDate("delinquencyPausePeriods[" + i + "].pausePeriodEnd", actual.getPausePeriodEnd(),
                        expected.getPausePeriodEnd());
            });
        });
    }

    public void workingCapitalLoanBalanceChangedOnApprovalEventCheck(final Long loanId) {
        workingCapitalLoanEventPayloadCheck(WorkingCapitalLoanBalanceChangedEvent.class, loanId, event -> {
            assertThat(event.getStatus()).isNotNull();
            assertThat(event.getStatus().getId()).as("status.id").isEqualTo(LoanStatus.APPROVED.getValue());
            assertThat(event.getApprovedPrincipal()).as("approvedPrincipal").isNotNull();
        });
    }

    public void workingCapitalLoanBalanceChangedOnUndoApprovalEventCheck(final Long loanId) {
        workingCapitalLoanEventPayloadCheck(WorkingCapitalLoanBalanceChangedEvent.class, loanId, event -> {
            assertThat(event.getStatus()).isNotNull();
            assertThat(event.getStatus().getId()).as("status.id").isEqualTo(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue());
        });
    }

    public void workingCapitalLoanPeriodPaymentRateChangedEventCheck(final Long loanId) {
        workingCapitalLoanEventMatchesApiCheck(WorkingCapitalLoanPeriodPaymentRateChangedEvent.class, loanId,
                (event, body) -> assertAmountEquals("paymentRate", event.getPaymentRate(), body.getPaymentRate()));
    }

    public void workingCapitalLoanDelinquencyScheduleChangedEventCheck(final Long loanId) {
        workingCapitalLoanDelinquencyScheduleEventCheck(WorkingCapitalLoanDelinquencyScheduleChangedEvent.class, loanId);
    }

    public void workingCapitalLoanBreachScheduleChangedEventCheck(final Long loanId) {
        workingCapitalLoanBreachScheduleEventCheck(WorkingCapitalLoanBreachScheduleChangedEvent.class, loanId);
    }

    public void workingCapitalLoanBreachPauseEventCheck(final Long loanId) {
        workingCapitalLoanBreachScheduleEventCheck(WorkingCapitalLoanBreachPauseEvent.class, loanId);
    }

    public void workingCapitalLoanBreachResumeEventCheck(final Long loanId) {
        workingCapitalLoanBreachScheduleEventCheck(WorkingCapitalLoanBreachResumeEvent.class, loanId);
    }

    public void workingCapitalLoanBreachRescheduleEventCheck(final Long loanId) {
        workingCapitalLoanBreachScheduleEventCheck(WorkingCapitalLoanBreachRescheduleEvent.class, loanId);
    }

    public void workingCapitalLoanBreachResetEventCheck(final Long loanId) {
        workingCapitalLoanBreachScheduleEventCheck(WorkingCapitalLoanBreachResetEvent.class, loanId);
    }

    public void workingCapitalLoanBreachUndoResetEventCheck(final Long loanId) {
        workingCapitalLoanBreachScheduleEventCheck(WorkingCapitalLoanBreachUndoResetEvent.class, loanId);
    }

    public void workingCapitalLoanDelinquencyPauseEventCheck(final Long loanId) {
        workingCapitalLoanDelinquencyScheduleEventCheck(WorkingCapitalLoanDelinquencyPauseEvent.class, loanId);
    }

    public void workingCapitalLoanDelinquencyResumeEventCheck(final Long loanId) {
        workingCapitalLoanDelinquencyScheduleEventCheck(WorkingCapitalLoanDelinquencyResumeEvent.class, loanId);
    }

    public void workingCapitalLoanDelinquencyRescheduleEventCheck(final Long loanId) {
        workingCapitalLoanDelinquencyScheduleEventCheck(WorkingCapitalLoanDelinquencyRescheduleEvent.class, loanId);
    }

    public void workingCapitalLoanDelinquencyResetEventCheck(final Long loanId) {
        workingCapitalLoanDelinquencyScheduleEventCheck(WorkingCapitalLoanDelinquencyResetEvent.class, loanId);
    }

    public void workingCapitalLoanDelinquencyUndoResetEventCheck(final Long loanId) {
        workingCapitalLoanDelinquencyScheduleEventCheck(WorkingCapitalLoanDelinquencyUndoResetEvent.class, loanId);
    }

    private void workingCapitalLoanBreachScheduleEventCheck(final Class<? extends AbstractWorkingCapitalLoanEvent> eventClazz,
            final Long loanId) {
        workingCapitalLoanEventPayloadCheck(eventClazz, loanId, event -> {
            assertThat(event.getBreach()).isNotNull();
            assertThat(event.getBreach().getBreachSchedule()).isNotNull().isNotEmpty();
        });
    }

    private void workingCapitalLoanDelinquencyScheduleEventCheck(final Class<? extends AbstractWorkingCapitalLoanEvent> eventClazz,
            final Long loanId) {
        workingCapitalLoanEventPayloadCheck(eventClazz, loanId, event -> {
            assertThat(event.getDelinquent()).isNotNull();
            assertThat(event.getDelinquent().getDelinquencySchedule()).isNotNull().isNotEmpty();
        });
    }

    public void workingCapitalLoanBreachPastDueChangeEventCheck(final Long loanId, final BigDecimal expectedPastDueAmount) {
        workingCapitalLoanEventPayloadCheck(WorkingCapitalLoanBreachPastDueChangeEvent.class, loanId, event -> {
            assertThat(event.getBreach()).isNotNull();
            assertThat(event.getBreach().getBreachSchedule()).isNotNull().isNotEmpty();
            assertAmountEquals("breach.breachPastDueAmount", event.getBreach().getBreachPastDueAmount(), expectedPastDueAmount);
        });
    }

    public void workingCapitalLoanBreachChangeEventCheck(final Long loanId, final Boolean expectedBreach) {
        workingCapitalLoanEventPayloadCheck(WorkingCapitalLoanBreachChangeEvent.class, loanId, event -> {
            assertThat(event.getBreach()).isNotNull();
            assertThat(event.getBreach().getBreachSchedule()).isNotNull().isNotEmpty();
            assertThat(event.getBreach().getBreachSchedule()).as("breach.breachSchedule has a period with breach=%s", expectedBreach)
                    .anyMatch(period -> expectedBreach.equals(period.getBreach()));
        });
    }

    public void workingCapitalLoanNearBreachChangeEventCheck(final Long loanId, final Boolean expectedNearBreach) {
        workingCapitalLoanEventPayloadCheck(WorkingCapitalLoanNearBreachChangeEvent.class, loanId, event -> {
            assertThat(event.getBreach()).isNotNull();
            assertThat(event.getBreach().getBreachSchedule()).isNotNull().isNotEmpty();
            assertThat(event.getBreach().getBreachSchedule())
                    .as("breach.breachSchedule has a period with nearBreach=%s", expectedNearBreach)
                    .anyMatch(period -> expectedNearBreach.equals(period.getNearBreach()));
        });
    }

    public void workingCapitalLoanDelinquencyDisableEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanDelinquencyDisableEvent.class, loanId);
    }

    public void workingCapitalLoanDelinquencyEnableEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanDelinquencyEnableEvent.class, loanId);
    }

    public void workingCapitalLoanBreachDisableEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanBreachDisableEvent.class, loanId);
    }

    public void workingCapitalLoanBreachEnableEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanBreachEnableEvent.class, loanId);
    }

    public void workingCapitalLoanChargeOffEventCheck(final Long loanId, final String chargedOffOnDate) {
        workingCapitalLoanEventPayloadCheck(WorkingCapitalLoanChargeOffEvent.class, loanId, event -> {
            assertThat(event.getChargedOff()).as("chargedOff").isTrue();
            assertThat(event.getSummary()).isNotNull();
            assertThat(event.getSummary().getChargeOffReason()).as("summary.chargeOffReason").isNotBlank();
            assertThat(event.getTimeline()).isNotNull();
            assertThat(event.getTimeline().getChargedOffOnDate()).as("timeline.chargedOffOnDate")
                    .isEqualTo(FORMATTER_EVENTS.format(LocalDate.parse(chargedOffOnDate, DATE_FORMATTER)));
        });
    }

    public void workingCapitalLoanUndoChargeOffEventCheck(final Long loanId) {
        workingCapitalLoanEventPayloadCheck(WorkingCapitalLoanUndoChargeOffEvent.class, loanId, event -> {
            assertThat(event.getChargedOff()).as("chargedOff").isFalse();
            assertThat(event.getSummary()).isNotNull();
            assertThat(event.getSummary().getChargeOffReason()).as("summary.chargeOffReason").isNullOrEmpty();
        });
    }

    public void workingCapitalLoanFraudChangedEventCheck(final Long loanId) {
        workingCapitalLoanAccountDataV1Check(WorkingCapitalLoanFraudChangedEvent.class, loanId);
    }

    public void workingCapitalLoanPayoutRefundTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse transaction = findLastWorkingCapitalLoanTransaction(loanId, "payoutRefund", false,
                "Payout refund transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanPayoutRefundTransactionBusinessEvent.class, loanId, transaction,
                expectedAmount, false);
    }

    public void workingCapitalLoanRepaymentTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse transaction = findLastWorkingCapitalLoanTransaction(loanId, "repayment", false,
                "Repayment transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanRepaymentTransactionBusinessEvent.class, loanId, transaction,
                expectedAmount, false);
    }

    public void workingCapitalLoanGoodwillCreditTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse transaction = findLastWorkingCapitalLoanTransaction(loanId, "goodwillCredit",
                false, "Goodwill credit transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanGoodwillCreditTransactionBusinessEvent.class, loanId, transaction,
                expectedAmount, false);
    }

    public void workingCapitalLoanAdjustTransactionReversalEventCheck(final Long loanId, final String transactionType) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse transaction = findLastWorkingCapitalLoanTransaction(loanId, transactionType, true,
                "Reversed " + transactionType + " transaction not found");
        eventAssertion.assertEvent(WorkingCapitalLoanAdjustTransactionBusinessEvent.class, transaction.getId())//
                .extractingData(data -> data.getTransactionToAdjust().getWcLoanId()).isEqualTo(loanId)//
                .extractingBigDecimal(data -> data.getTransactionToAdjust().getTransactionAmount())
                .isEqualTo(transaction.getTransactionAmount())//
                .extractingData(data -> data.getTransactionToAdjust().getType().getCode()).isEqualTo(transaction.getType().getCode())//
                .extractingData(data -> data.getTransactionToAdjust().getReversed()).isEqualTo(true)//
                .extractingData(WorkingCapitalLoanTransactionAdjustmentDataV1::getNewTransactionDetail).isEqualTo(null);
    }

    public void workingCapitalLoanAdjustTransactionReprocessEventCheck(final Long loanId, final String transactionType,
            final String transactionDate, final BigDecimal previousPrincipalPortion, final BigDecimal newPrincipalPortion,
            final BigDecimal previousFeeChargesPortion, final BigDecimal newFeeChargesPortion) {
        final GetWorkingCapitalLoanTransactionIdResponse transaction = workingCapitalLoanTransactionDetails(loanId, transactionType,
                transactionDate);
        eventAssertion.assertEvent(WorkingCapitalLoanAdjustTransactionBusinessEvent.class, transaction.getId())//
                .extractingData(data -> data.getTransactionToAdjust().getWcLoanId()).isEqualTo(loanId)//
                .extractingBigDecimal(data -> data.getTransactionToAdjust().getPrincipalPortion()).isEqualTo(previousPrincipalPortion)//
                .extractingBigDecimal(data -> data.getTransactionToAdjust().getFeeChargesPortion()).isEqualTo(previousFeeChargesPortion)//
                .extractingBigDecimal(data -> data.getNewTransactionDetail().getPrincipalPortion()).isEqualTo(newPrincipalPortion)//
                .extractingBigDecimal(data -> data.getNewTransactionDetail().getFeeChargesPortion()).isEqualTo(newFeeChargesPortion);
    }

    public void workingCapitalLoanAccrualTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse transaction = findLastWorkingCapitalLoanTransaction(loanId, "accrual", false,
                "Accrual transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanAccrualTransactionBusinessEvent.class, loanId, transaction,
                expectedAmount, false);
    }

    public void workingCapitalLoanAccrualAdjustmentTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse transaction = findLastWorkingCapitalLoanTransaction(loanId, "accrual", true,
                "Reversed accrual transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanAccrualAdjustmentTransactionBusinessEvent.class, loanId, transaction,
                expectedAmount, true);
    }

    public void workingCapitalLoanWriteOffTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse transaction = findLastWorkingCapitalLoanTransaction(loanId, "writeOff", false,
                "Write-off transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanWriteOffTransactionBusinessEvent.class, loanId, transaction,
                expectedAmount, false);
    }

    public void workingCapitalLoanUndoWriteOffTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse transaction = findLastWorkingCapitalLoanTransaction(loanId, "writeOff", true,
                "Reversed write-off transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanUndoWriteOffTransactionBusinessEvent.class, loanId, transaction,
                expectedAmount, true);
    }

    public void workingCapitalLoanChargeAdjustmentTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse transaction = findLastWorkingCapitalLoanTransaction(loanId, "chargeAdjustment",
                false, "Charge adjustment transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanChargeAdjustmentTransactionBusinessEvent.class, loanId, transaction,
                expectedAmount, false);
    }

    public void workingCapitalLoanChargeOffTransactionEventCheck(final Long loanId, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        final GetWorkingCapitalLoanTransactionIdResponse transaction = findLastWorkingCapitalLoanTransaction(loanId, "chargeOff", false,
                "Charge-off transaction not found");
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanChargeOffTransactionBusinessEvent.class, loanId, transaction,
                expectedAmount, false);
    }

    public void workingCapitalLoanDiscountFeeAmortizationTransactionEventCheck(final Long loanId, final String transactionDate) {
        final GetWorkingCapitalLoanTransactionIdResponse transaction = workingCapitalLoanTransactionDetails(loanId,
                "discountFeeAmortization", transactionDate);
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanDiscountFeeAmortizationTransactionBusinessEvent.class, loanId,
                transaction, null, false);
    }

    public void workingCapitalLoanDiscountFeeAmortizationAdjustmentTransactionEventCheck(final Long loanId, final String transactionDate) {
        final GetWorkingCapitalLoanTransactionIdResponse transaction = workingCapitalLoanTransactionDetails(loanId,
                "discountFeeAmortizationAdjustment", transactionDate);
        workingCapitalLoanTransactionEventCheck(WorkingCapitalLoanDiscountFeeAmortizationAdjustmentTransactionBusinessEvent.class, loanId,
                transaction, null, false);
    }

    public void workingCapitalLoanAddChargeEventCheck(final Long loanId, final String chargeName, final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        eventAssertion.assertEvent(WorkingCapitalLoanAddChargeEvent.class, loanId)//
                .extractingData(WorkingCapitalLoanChargeDataV1::getLoanId).isEqualTo(loanId)//
                .extractingData(WorkingCapitalLoanChargeDataV1::getName).isEqualTo(chargeName)//
                .extractingBigDecimal(WorkingCapitalLoanChargeDataV1::getAmount).isEqualTo(expectedAmount);
    }

    public void workingCapitalLoanAddChargeEventCheck(final Long loanId, final String isPenalty, final String chargeName,
            final BigDecimal expectedAmount) {
        waitForTransactionCommit();
        eventAssertion.assertEvent(WorkingCapitalLoanAddChargeEvent.class, loanId)//
                .extractingData(WorkingCapitalLoanChargeDataV1::getLoanId).isEqualTo(loanId)//
                .extractingData(WorkingCapitalLoanChargeDataV1::getPenalty).isEqualTo(isPenalty.equalsIgnoreCase("penalty"))//
                .extractingData(WorkingCapitalLoanChargeDataV1::getName).isEqualTo(chargeName)//
                .extractingBigDecimal(WorkingCapitalLoanChargeDataV1::getAmount).isEqualTo(expectedAmount);
    }

    public void workingCapitalLoanJournalEntriesEventCheck(final Long loanId) {
        if (eventProperties.isEventVerificationDisabled()) {
            return;
        }
        waitForTransactionCommit();
        final WorkingCapitalLoanJournalEntryCreatedEvent eventType = new WorkingCapitalLoanJournalEntryCreatedEvent();
        await().atMost(Duration.ofMillis(eventProperties.getWaitTimeoutInMillis())).untilAsserted(() -> {
            final List<WorkingCapitalLoanJournalEntryDataV1> entries = eventStore.findAllEventsById(eventType, loanId).stream()
                    .map(EventMessage::getData).toList();
            assertThat(entries).as("journal entry events").isNotEmpty();

            entries.forEach(entry -> {
                assertThat(entry.getLoanId()).as("journalEntry.loanId").isEqualTo(loanId);
                assertThat(entry.getGlAccount()).as("journalEntry.glAccount").isNotNull();
                assertThat(entry.getGlAccount().getGlCode()).as("journalEntry.glAccount.glCode").isNotBlank();
                assertThat(entry.getWcLoanTransactionId()).as("journalEntry.wcLoanTransactionId").isNotNull();
            });

            entries.stream().collect(Collectors.groupingBy(WorkingCapitalLoanJournalEntryDataV1::getWcLoanTransactionId))
                    .forEach((transactionId, lines) -> {
                        assertThat(lines).as("journal entry lines of transaction %s", transactionId).hasSizeGreaterThanOrEqualTo(2);
                        assertThat(sumJournalEntries(lines, JOURNAL_ENTRY_TYPE_DEBIT)).as("debits of transaction %s", transactionId)
                                .isEqualByComparingTo(sumJournalEntries(lines, JOURNAL_ENTRY_TYPE_CREDIT));
                    });
        });
    }

    private static BigDecimal sumJournalEntries(final List<WorkingCapitalLoanJournalEntryDataV1> entries, final String type) {
        return entries.stream().filter(entry -> entry.getType() != null && type.equals(entry.getType().getId()))
                .map(WorkingCapitalLoanJournalEntryDataV1::getAmount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void workingCapitalLoanStatusChangedEventNotRaisedCheck(final Long loanId) {
        waitForTransactionCommit();
        eventAssertion.assertEventNotRaised(WorkingCapitalLoanStatusChangedEvent.class, loanId);
    }

    public void workingCapitalLoanDelinquencyScheduleChangedEventNotRaisedCheck(final Long loanId) {
        waitForTransactionCommit();
        eventAssertion.assertEventNotRaised(WorkingCapitalLoanDelinquencyScheduleChangedEvent.class, loanId);
    }

    public void workingCapitalLoanBreachScheduleChangedEventNotRaisedCheck(final Long loanId) {
        waitForTransactionCommit();
        eventAssertion.assertEventNotRaised(WorkingCapitalLoanBreachScheduleChangedEvent.class, loanId);
    }

    public void workingCapitalLoanBreachPastDueChangeEventNotRaisedCheck(final Long loanId) {
        waitForTransactionCommit();
        eventAssertion.assertEventNotRaised(WorkingCapitalLoanBreachPastDueChangeEvent.class, loanId);
    }

    public void workingCapitalLoanBreachChangeEventNotRaisedCheck(final Long loanId) {
        waitForTransactionCommit();
        eventAssertion.assertEventNotRaised(WorkingCapitalLoanBreachChangeEvent.class, loanId);
    }

    public void workingCapitalLoanNearBreachChangeEventNotRaisedCheck(final Long loanId) {
        waitForTransactionCommit();
        eventAssertion.assertEventNotRaised(WorkingCapitalLoanNearBreachChangeEvent.class, loanId);
    }

    private static void assertEventDateEqualsApiDate(final String description, final String eventDate, final LocalDate apiDate) {
        assertThat(eventDate).as(description).isEqualTo(apiDate == null ? null : FORMATTER_EVENTS.format(apiDate));
    }

    private void workingCapitalLoanAccountDataV1Check(final Class<? extends AbstractWorkingCapitalLoanEvent> eventClazz,
            final Long loanId) {
        workingCapitalLoanEventMatchesApiCheck(eventClazz, loanId, this::assertWorkingCapitalLoanAccountData);
    }

    private void assertWorkingCapitalLoanAccountData(final WorkingCapitalLoanAccountDataV1 event,
            final GetWorkingCapitalLoansLoanIdResponse body) {
        assertThat(body.getStatus()).isNotNull();
        assertThat(event.getStatus().getId().longValue()).as("status.id").isEqualTo(body.getStatus().getId());

        assertThat(event.getDelinquent()).isNotNull();
        assertThat(event.getDelinquent().getDelinquencySchedule()).isNotNull();

        assertThat(event.getBreach()).isNotNull();
        assertThat(event.getBreach().getBreachSchedule()).isNotNull();

        if (event.getDisbursementDetails() != null && !event.getDisbursementDetails().isEmpty() && body.getNetDisbursalAmount() != null) {
            event.getDisbursementDetails().forEach(tranche -> assertAmountEquals("disbursementDetails.netDisbursalAmount",
                    tranche.getNetDisbursalAmount(), body.getNetDisbursalAmount()));
        }
    }

}
