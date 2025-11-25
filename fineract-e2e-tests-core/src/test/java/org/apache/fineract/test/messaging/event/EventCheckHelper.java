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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.client.v1.ClientDataV1;
import org.apache.fineract.avro.loan.v1.DelinquencyPausePeriodV1;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.avro.loan.v1.LoanAmountDataV1;
import org.apache.fineract.avro.loan.v1.LoanInstallmentDelinquencyBucketDataV1;
import org.apache.fineract.avro.loan.v1.LoanOwnershipTransferDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionAdjustmentDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencyPausePeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.test.data.AssetExternalizationTransferStatus;
import org.apache.fineract.test.data.AssetExternalizationTransferStatusReason;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.GlobalConfigurationHelper;
import org.apache.fineract.test.messaging.EventAssertion;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventCheckHelper {

    private static final DateTimeFormatter FORMATTER_EVENTS = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long TRANSACTION_COMMIT_DELAY_MS = 100L;

    @Autowired
    private FineractFeignClient fineractClient;
    @Autowired
    private EventAssertion eventAssertion;
    @Autowired
    private GlobalConfigurationHelper configurationHelper;
    @Autowired
    private org.apache.fineract.test.messaging.config.EventProperties eventProperties;

    private void waitForTransactionCommit() {
        if (eventProperties.isEventVerificationEnabled() && TRANSACTION_COMMIT_DELAY_MS > 0) {
            try {
                Thread.sleep(TRANSACTION_COMMIT_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for transaction commit", e);
            }
        }
    }

    public void clientEventCheck(PostClientsResponse clientCreationResponse) {
        waitForTransactionCommit();
        GetClientsClientIdResponse body = ok(() -> fineractClient.clients().retrieveOne11(clientCreationResponse.getClientId(),
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
        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveLoan(loanUndoApproveResponse.getLoanId(),
                Map.of("staffInSelectedOfficeOnly", false, "associations", "", "exclude", "", "fields", "")));

        eventAssertion.assertEventRaised(LoanUndoApprovalEvent.class, body.getId());
    }

    public void loanRejectedEventCheck(PostLoansLoanIdResponse loanRejectedResponse) {
        waitForTransactionCommit();
        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveLoan(loanRejectedResponse.getLoanId(),
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
        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveLoan(loanId,
                Map.of("staffInSelectedOfficeOnly", false, "associations", "all", "exclude", "", "fields", "")));

        eventAssertion.assertEvent(eventClazz, loanId)//
                .extractingData(loanAccountDataV1 -> {
                    Long idActual = loanAccountDataV1.getId();
                    Long idExpected = body.getId();
                    Integer statusIdActual = loanAccountDataV1.getStatus().getId();
                    Integer statusIdExpected = body.getStatus().getId();
                    String statusCodeActual = loanAccountDataV1.getStatus().getCode();
                    String statusCodeExpected = body.getStatus().getCode();
                    Long clientIdActual = loanAccountDataV1.getClientId();
                    Long clientIdExpected = body.getClientId();
                    BigDecimal principalDisbursedActual = loanAccountDataV1.getSummary().getPrincipalDisbursed();
                    Double principalDisbursedExpectedDouble = body.getSummary().getPrincipalDisbursed().doubleValue();
                    BigDecimal principalDisbursedExpected = BigDecimal.valueOf(principalDisbursedExpectedDouble);
                    String actualDisbursementDateActual = loanAccountDataV1.getTimeline().getActualDisbursementDate();
                    String actualDisbursementDateExpected = FORMATTER_EVENTS.format(body.getTimeline().getActualDisbursementDate());
                    String currencyCodeActual = loanAccountDataV1.getSummary().getCurrency().getCode();
                    String currencyCodeExpected = body.getSummary().getCurrency().getCode();
                    BigDecimal totalUnpaidPayableDueInterestActual = loanAccountDataV1.getSummary().getTotalUnpaidPayableDueInterest();
                    BigDecimal totalUnpaidPayableDueInterestExpected = body.getSummary().getTotalUnpaidPayableDueInterest();
                    BigDecimal totalUnpaidPayableNotDueInterestActual = loanAccountDataV1.getSummary()
                            .getTotalUnpaidPayableNotDueInterest();
                    BigDecimal totalUnpaidPayableNotDueInterestExpected = body.getSummary().getTotalUnpaidPayableNotDueInterest();
                    BigDecimal totalInterestPaymentWaiverActual = loanAccountDataV1.getSummary().getTotalInterestPaymentWaiver();
                    Double totalInterestPaymentWaiverExpectedDouble = body.getSummary().getTotalInterestPaymentWaiver().doubleValue();
                    BigDecimal totalInterestPaymentWaiverExpected = new BigDecimal(totalInterestPaymentWaiverExpectedDouble,
                            MathContext.DECIMAL64);
                    BigDecimal delinquentInterestActual = loanAccountDataV1.getDelinquent().getDelinquentInterest();
                    BigDecimal delinquentInterestExpected = body.getDelinquent().getDelinquentInterest();
                    BigDecimal delinquentFeeActual = loanAccountDataV1.getDelinquent().getDelinquentFee();
                    BigDecimal delinquentFeeExpected = body.getDelinquent().getDelinquentFee();
                    BigDecimal delinquentPenaltyActual = loanAccountDataV1.getDelinquent().getDelinquentPenalty();
                    BigDecimal delinquentPenaltyExpected = body.getDelinquent().getDelinquentPenalty();

                    assertThat(idActual).isEqualTo(idExpected);
                    assertThat(statusIdActual).isEqualTo(statusIdExpected);
                    assertThat(statusCodeActual).isEqualTo(statusCodeExpected);
                    assertThat(clientIdActual).isEqualTo(clientIdExpected);
                    assertThat(areBigDecimalValuesEqual(principalDisbursedActual, principalDisbursedExpected)).isTrue();
                    assertThat(actualDisbursementDateActual).isEqualTo(actualDisbursementDateExpected);
                    assertThat(currencyCodeActual).isEqualTo(currencyCodeExpected);
                    assertThat(areBigDecimalValuesEqual(totalUnpaidPayableDueInterestActual, totalUnpaidPayableDueInterestExpected))
                            .isTrue();
                    assertThat(areBigDecimalValuesEqual(totalUnpaidPayableNotDueInterestActual, totalUnpaidPayableNotDueInterestExpected))
                            .isTrue();
                    assertThat(areBigDecimalValuesEqual(totalInterestPaymentWaiverActual, totalInterestPaymentWaiverExpected)).isTrue();
                    assertThat(areBigDecimalValuesEqual(delinquentInterestActual, delinquentInterestExpected)).isTrue();
                    assertThat(areBigDecimalValuesEqual(delinquentFeeActual, delinquentFeeExpected)).isTrue();
                    assertThat(areBigDecimalValuesEqual(delinquentPenaltyActual, delinquentPenaltyExpected)).isTrue();

                    return null;
                });
    }

    public GetLoansLoanIdTransactions getNthTransactionType(String nthItemStr, String transactionType, String transactionDate,
            List<GetLoansLoanIdTransactions> transactions) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        int nthItem = Integer.parseInt(nthItemStr) - 1;
        GetLoansLoanIdTransactions targetTransaction = transactions//
                .stream()//
                .filter(t -> transactionDate.equals(formatter.format(t.getDate())) && transactionType.equals(t.getType().getValue()))//
                .toList()//
                .get(nthItem);//
        return targetTransaction;
    }

    public GetLoansLoanIdTransactions findNthTransaction(String nthItemStr, String transactionType, String transactionDate, long loanId) {
        GetLoansLoanIdResponse loanResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
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

    private boolean areBigDecimalValuesEqual(BigDecimal actual, BigDecimal expected) {
        log.debug("--- Checking BigDecimal values.... ---");
        log.debug("Actual:   {}", actual);
        log.debug("Expected: {}", expected);
        return actual.compareTo(expected) == 0;
    }

    public void loanDisbursalTransactionEventCheck(PostLoansLoanIdResponse loanDisburseResponse) {
        waitForTransactionCommit();
        Long disbursementTransactionId = loanDisburseResponse.getSubResourceId();

        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveLoan(loanDisburseResponse.getLoanId(),
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

    public EventAssertion.EventAssertionBuilder<LoanTransactionDataV1> transactionEventCheck(
            PostLoansLoanIdTransactionsResponse transactionResponse, TransactionType transactionType, String externalOwnerId) {
        Long loanId = transactionResponse.getLoanId();
        Long transactionId = transactionResponse.getResourceId();
        GetLoansLoanIdResponse loanDetailsResponse = ok(() -> fineractClient.loans().retrieveLoan(loanId,
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
        PageExternalTransferData response = ok(() -> fineractClient.externalAssetOwners().getTransfers(Map.of("loanId", loanId)));
        List<ExternalTransferData> content = response.getContent();

        ExternalTransferData filtered = content.stream().filter(t -> transferId.equals(t.getTransferId())).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No element found"));

        BigDecimal totalOutstandingBalanceAmountExpected = zeroConversion(filtered.getDetails().getTotalOutstanding());
        BigDecimal outstandingPrincipalPortionExpected = zeroConversion(filtered.getDetails().getTotalPrincipalOutstanding());
        BigDecimal outstandingFeePortionExpected = zeroConversion(filtered.getDetails().getTotalFeeChargesOutstanding());
        BigDecimal outstandingPenaltyPortionExpected = zeroConversion(filtered.getDetails().getTotalPenaltyChargesOutstanding());
        BigDecimal outstandingInterestPortionExpected = zeroConversion(filtered.getDetails().getTotalInterestOutstanding());
        BigDecimal overPaymentPortionExpected = zeroConversion(filtered.getDetails().getTotalOverpaid());

        eventAssertion.assertEvent(LoanOwnershipTransferEvent.class, loanId).extractingData(LoanOwnershipTransferDataV1::getLoanId)
                .isEqualTo(loanId).extractingData(LoanOwnershipTransferDataV1::getAssetOwnerExternalId)
                .isEqualTo(filtered.getOwner().getExternalId()).extractingData(LoanOwnershipTransferDataV1::getTransferExternalId)
                .isEqualTo(filtered.getTransferExternalId()).extractingData(LoanOwnershipTransferDataV1::getSettlementDate)
                .isEqualTo(FORMATTER_EVENTS.format(filtered.getSettlementDate()))
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getTotalOutstandingBalanceAmount)
                .isEqualTo(totalOutstandingBalanceAmountExpected)
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingPrincipalPortion)
                .isEqualTo(outstandingPrincipalPortionExpected).extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingFeePortion)
                .isEqualTo(outstandingFeePortionExpected).extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingPenaltyPortion)
                .isEqualTo(outstandingPenaltyPortionExpected)
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingInterestPortion)
                .isEqualTo(outstandingInterestPortionExpected).extractingBigDecimal(LoanOwnershipTransferDataV1::getOverPaymentPortion)
                .isEqualTo(overPaymentPortionExpected);
    }

    public void loanOwnershipTransferBusinessEventWithStatusCheck(Long loanId, Long transferId, String transferStatus,
            String transferStatusReason) {
        PageExternalTransferData response = ok(() -> fineractClient.externalAssetOwners().getTransfers(Map.of("loanId", loanId)));
        List<ExternalTransferData> content = response.getContent();

        ExternalTransferData filtered = content.stream().filter(t -> transferId.equals(t.getTransferId())).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No element found"));

        BigDecimal totalOutstandingBalanceAmountExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalOutstanding());
        BigDecimal outstandingPrincipalPortionExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalPrincipalOutstanding());
        BigDecimal outstandingFeePortionExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalFeeChargesOutstanding());
        BigDecimal outstandingPenaltyPortionExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalPenaltyChargesOutstanding());
        BigDecimal outstandingInterestPortionExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalInterestOutstanding());
        BigDecimal overPaymentPortionExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalOverpaid());

        AssetExternalizationTransferStatus transferStatusType = AssetExternalizationTransferStatus.valueOf(transferStatus);
        String transferStatusExpected = transferStatusType.getValue();

        AssetExternalizationTransferStatusReason transferStatusReasonType = AssetExternalizationTransferStatusReason
                .valueOf(transferStatusReason);
        String transferStatusReasonExpected = transferStatusReasonType.getValue();

        eventAssertion.assertEvent(LoanOwnershipTransferEvent.class, loanId).extractingData(LoanOwnershipTransferDataV1::getLoanId)
                .isEqualTo(loanId).extractingData(LoanOwnershipTransferDataV1::getAssetOwnerExternalId)
                .isEqualTo(filtered.getOwner().getExternalId()).extractingData(LoanOwnershipTransferDataV1::getTransferExternalId)
                .isEqualTo(filtered.getTransferExternalId()).extractingData(LoanOwnershipTransferDataV1::getSettlementDate)
                .isEqualTo(FORMATTER_EVENTS.format(filtered.getSettlementDate()))
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getTotalOutstandingBalanceAmount)
                .isEqualTo(totalOutstandingBalanceAmountExpected)
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingPrincipalPortion)
                .isEqualTo(outstandingPrincipalPortionExpected).extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingFeePortion)
                .isEqualTo(outstandingFeePortionExpected).extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingPenaltyPortion)
                .isEqualTo(outstandingPenaltyPortionExpected)
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingInterestPortion)
                .isEqualTo(outstandingInterestPortionExpected).extractingBigDecimal(LoanOwnershipTransferDataV1::getOverPaymentPortion)
                .isEqualTo(overPaymentPortionExpected).extractingData(LoanOwnershipTransferDataV1::getTransferStatus)
                .isEqualTo(transferStatusExpected).extractingData(LoanOwnershipTransferDataV1::getTransferStatusReason)
                .isEqualTo(transferStatusReasonExpected);
    }

    public void loanOwnershipTransferBusinessEventWithTypeCheck(Long loanId, ExternalTransferData transferData, String transferType,
            String previousAssetOwner) {
        PageExternalTransferData response = ok(() -> fineractClient.externalAssetOwners().getTransfers(Map.of("loanId", loanId)));
        List<ExternalTransferData> content = response.getContent();
        Long transferId = transferData.getTransferId();
        String assetOwner = transferData.getOwner() == null ? null : transferData.getOwner().getExternalId();

        ExternalTransferData filtered = content.stream().filter(t -> transferId.equals(t.getTransferId())).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No element found"));

        BigDecimal totalOutstandingBalanceAmountExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalOutstanding());
        BigDecimal outstandingPrincipalPortionExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalPrincipalOutstanding());
        BigDecimal outstandingFeePortionExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalFeeChargesOutstanding());
        BigDecimal outstandingPenaltyPortionExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalPenaltyChargesOutstanding());
        BigDecimal outstandingInterestPortionExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalInterestOutstanding());
        BigDecimal overPaymentPortionExpected = filtered.getDetails() == null ? null
                : zeroConversion(filtered.getDetails().getTotalOverpaid());

        eventAssertion.assertEvent(LoanOwnershipTransferEvent.class, loanId).extractingData(LoanOwnershipTransferDataV1::getLoanId)
                .isEqualTo(loanId).extractingData(LoanOwnershipTransferDataV1::getAssetOwnerExternalId)
                .isEqualTo(filtered.getOwner().getExternalId()).extractingData(LoanOwnershipTransferDataV1::getTransferExternalId)
                .isEqualTo(filtered.getTransferExternalId()).extractingData(LoanOwnershipTransferDataV1::getSettlementDate)
                .isEqualTo(FORMATTER_EVENTS.format(filtered.getSettlementDate()))
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getTotalOutstandingBalanceAmount)
                .isEqualTo(totalOutstandingBalanceAmountExpected)
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingPrincipalPortion)
                .isEqualTo(outstandingPrincipalPortionExpected).extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingFeePortion)
                .isEqualTo(outstandingFeePortionExpected).extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingPenaltyPortion)
                .isEqualTo(outstandingPenaltyPortionExpected)
                .extractingBigDecimal(LoanOwnershipTransferDataV1::getOutstandingInterestPortion)
                .isEqualTo(outstandingInterestPortionExpected).extractingBigDecimal(LoanOwnershipTransferDataV1::getOverPaymentPortion)
                .isEqualTo(overPaymentPortionExpected).extractingData(LoanOwnershipTransferDataV1::getType).isEqualTo(transferType)
                .extractingData(LoanOwnershipTransferDataV1::getAssetOwnerExternalId).isEqualTo(assetOwner)
                .extractingData(LoanOwnershipTransferDataV1::getPreviousOwnerExternalId).isEqualTo(previousAssetOwner);
    }

    public void loanAccountSnapshotBusinessEventCheck(Long loanId, Long transferId) {
        waitForTransactionCommit();
        PageExternalTransferData response = ok(() -> fineractClient.externalAssetOwners().getTransfers(Map.of("loanId", loanId)));
        List<ExternalTransferData> content = response.getContent();

        ExternalTransferData filtered = content.stream().filter(t -> transferId.equals(t.getTransferId())).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No element found"));

        BigDecimal totalOutstandingBalanceAmountExpected = zeroConversion(filtered.getDetails().getTotalOutstanding());
        BigDecimal outstandingInterestPortionExpected = zeroConversion(filtered.getDetails().getTotalInterestOutstanding());

        GlobalConfigurationPropertyData outstandingInterestStrategy = configurationHelper
                .getGlobalConfiguration("outstanding-interest-calculation-strategy-for-external-asset-transfer");
        if ("PAYABLE_OUTSTANDING_INTEREST".equals(outstandingInterestStrategy.getStringValue())) {
            GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId,
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
        GetLoansLoanIdResponse loanDetails = ok(() -> fineractClient.loans().retrieveLoan(loanId,
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
        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveLoan(createLoanResponse.getLoanId(),
                Map.of("staffInSelectedOfficeOnly", false, "associations", "all", "exclude", "", "fields", "")));

        eventAssertion.assertEvent(LoanCreatedEvent.class, createLoanResponse.getLoanId())//
                .extractingData(LoanAccountDataV1::getId).isEqualTo(body.getId())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getId()).isEqualTo(body.getStatus().getId())//
                .extractingData(LoanAccountDataV1::getClientId).isEqualTo(body.getClientId())//
                .extractingBigDecimal(LoanAccountDataV1::getPrincipal).isEqualTo(body.getPrincipal())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getCurrency().getCode())
                .isEqualTo(body.getCurrency().getCode());//
    }

    public void approveLoanEventCheck(PostLoansLoanIdResponse loanApproveResponse) {
        waitForTransactionCommit();
        GetLoansLoanIdResponse body = ok(() -> fineractClient.loans().retrieveLoan(loanApproveResponse.getLoanId(),
                Map.of("staffInSelectedOfficeOnly", false, "associations", "", "exclude", "", "fields", "")));

        eventAssertion.assertEvent(LoanApprovedEvent.class, loanApproveResponse.getLoanId())//
                .extractingData(LoanAccountDataV1::getId).isEqualTo(body.getId())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getId()).isEqualTo(body.getStatus().getId())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getCode()).isEqualTo(body.getStatus().getCode())//
                .extractingData(LoanAccountDataV1::getClientId).isEqualTo(Long.valueOf(body.getClientId()))//
                .extractingBigDecimal(LoanAccountDataV1::getApprovedPrincipal).isEqualTo(body.getApprovedPrincipal())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getTimeline().getApprovedOnDate())//
                .isEqualTo(FORMATTER_EVENTS.format(body.getTimeline().getApprovedOnDate()))//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getCurrency().getCode())
                .isEqualTo(body.getCurrency().getCode());//
    }

}
