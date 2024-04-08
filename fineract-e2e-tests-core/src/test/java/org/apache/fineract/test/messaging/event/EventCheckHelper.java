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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.avro.client.v1.ClientDataV1;
import org.apache.fineract.avro.loan.v1.DelinquencyPausePeriodV1;
import org.apache.fineract.avro.loan.v1.LoanAccountDataV1;
import org.apache.fineract.avro.loan.v1.LoanAmountDataV1;
import org.apache.fineract.avro.loan.v1.LoanInstallmentDelinquencyBucketDataV1;
import org.apache.fineract.avro.loan.v1.LoanOwnershipTransferDataV1;
import org.apache.fineract.avro.loan.v1.LoanTransactionDataV1;
import org.apache.fineract.client.models.ExternalTransferData;
import org.apache.fineract.client.models.GetClientsClientIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdDelinquencyPausePeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PageExternalTransferData;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.ClientApi;
import org.apache.fineract.client.services.ExternalAssetOwnersApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.test.data.AssetExternalizationTransferStatus;
import org.apache.fineract.test.data.AssetExternalizationTransferStatusReason;
import org.apache.fineract.test.data.TransactionType;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.messaging.EventAssertion;
import org.apache.fineract.test.messaging.event.assetexternalization.LoanAccountSnapshotEvent;
import org.apache.fineract.test.messaging.event.assetexternalization.LoanOwnershipTransferEvent;
import org.apache.fineract.test.messaging.event.client.ClientActivatedEvent;
import org.apache.fineract.test.messaging.event.client.ClientCreatedEvent;
import org.apache.fineract.test.messaging.event.loan.LoanApprovedEvent;
import org.apache.fineract.test.messaging.event.loan.LoanCreatedEvent;
import org.apache.fineract.test.messaging.event.loan.LoanDisbursalEvent;
import org.apache.fineract.test.messaging.event.loan.delinquency.LoanDelinquencyPauseChangedEvent;
import org.apache.fineract.test.messaging.event.loan.delinquency.LoanDelinquencyRangeChangeEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.AbstractLoanTransactionEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanDisbursalTransactionEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanRefundPostBusinessEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionGoodwillCreditPostEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionMakeRepaymentPostEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionMerchantIssuedRefundPostEvent;
import org.apache.fineract.test.messaging.event.loan.transaction.LoanTransactionPayoutRefundPostEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventCheckHelper {

    private static final DateTimeFormatter FORMATTER_EVENTS = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private LoansApi loansApi;

    @Autowired
    private EventAssertion eventAssertion;

    @Autowired
    private ExternalAssetOwnersApi externalAssetOwnersApi;

    public void clientEventCheck(Response<PostClientsResponse> clientCreationResponse) throws IOException {
        Response<GetClientsClientIdResponse> clientDetails = clientApi.retrieveOne11(clientCreationResponse.body().getClientId(), false)
                .execute();

        GetClientsClientIdResponse body = clientDetails.body();
        Long clientId = Long.valueOf(body.getId());
        Integer status = body.getStatus().getId().intValue();
        String firstname = body.getFirstname();
        String lastname = body.getLastname();
        Boolean active = body.getActive();

        eventAssertion.assertEvent(ClientCreatedEvent.class, clientCreationResponse.body().getClientId())//
                .extractingData(ClientDataV1::getId).isEqualTo(clientId)//
                .extractingData(clientDataV1 -> clientDataV1.getStatus().getId()).isEqualTo(status)//
                .extractingData(ClientDataV1::getFirstname).isEqualTo(firstname)//
                .extractingData(ClientDataV1::getLastname).isEqualTo(lastname)//
                .extractingData(ClientDataV1::getActive).isEqualTo(active);//

        eventAssertion.assertEvent(ClientActivatedEvent.class, clientCreationResponse.body().getClientId())//
                .extractingData(ClientDataV1::getActive).isEqualTo(true)//
                .extractingData(clientDataV1 -> clientDataV1.getStatus().getId()).isEqualTo(status);//

    }

    public void createLoanEventCheck(Response<PostLoansResponse> createLoanResponse) throws IOException {
        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(createLoanResponse.body().getLoanId(), false, "", "", "")
                .execute();
        GetLoansLoanIdResponse body = loanDetails.body();

        eventAssertion.assertEvent(LoanCreatedEvent.class, createLoanResponse.body().getLoanId()).extractingData(LoanAccountDataV1::getId)
                .isEqualTo(body.getId()).extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getId())
                .isEqualTo(body.getStatus().getId()).extractingData(LoanAccountDataV1::getClientId)
                .isEqualTo(Long.valueOf(body.getClientId()))
                .extractingData(loanAccountDataV11 -> loanAccountDataV11.getPrincipal().longValue())
                .isEqualTo(body.getPrincipal().longValue())
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getCurrency().getCode())
                .isEqualTo(body.getCurrency().getCode());
    }

    public void approveLoanEventCheck(Response<PostLoansLoanIdResponse> loanApproveResponse) throws IOException {
        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanApproveResponse.body().getLoanId(), false, "", "", "")
                .execute();
        GetLoansLoanIdResponse body = loanDetails.body();

        eventAssertion.assertEvent(LoanApprovedEvent.class, loanApproveResponse.body().getLoanId()).extractingData(LoanAccountDataV1::getId)
                .isEqualTo(body.getId()).extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getId())
                .isEqualTo(body.getStatus().getId()).extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getCode())
                .isEqualTo(body.getStatus().getCode()).extractingData(LoanAccountDataV1::getClientId)
                .isEqualTo(Long.valueOf(body.getClientId()))
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getApprovedPrincipal().longValue())
                .isEqualTo(body.getApprovedPrincipal().longValue())
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getTimeline().getApprovedOnDate())
                .isEqualTo(FORMATTER_EVENTS.format(body.getTimeline().getApprovedOnDate()))
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getCurrency().getCode())
                .isEqualTo(body.getCurrency().getCode());
    }

    public void disburseLoanEventCheck(Response<PostLoansLoanIdResponse> loanDisburseResponse) throws IOException {
        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanDisburseResponse.body().getLoanId(), false, "", "", "")
                .execute();
        GetLoansLoanIdResponse body = loanDetails.body();

        eventAssertion.assertEvent(LoanDisbursalEvent.class, loanDisburseResponse.body().getLoanId())//
                .extractingData(LoanAccountDataV1::getId).isEqualTo(body.getId())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getId()).isEqualTo(body.getStatus().getId())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getStatus().getCode()).isEqualTo(body.getStatus().getCode())//
                .extractingData(LoanAccountDataV1::getClientId).isEqualTo(Long.valueOf(body.getClientId()))//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getPrincipalDisbursed().longValue())
                .isEqualTo(body.getSummary().getPrincipalDisbursed().longValue())//
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getTimeline().getActualDisbursementDate())
                .isEqualTo(FORMATTER_EVENTS.format(body.getTimeline().getActualDisbursementDate()))
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getCurrency().getCode())
                .isEqualTo(body.getCurrency().getCode());//
    }

    public void loanDisbursalTransactionEventCheck(Response<PostLoansLoanIdResponse> loanDisburseResponse) throws IOException {
        Long disbursementTransactionId = loanDisburseResponse.body().getSubResourceId();

        Response<GetLoansLoanIdResponse> loanDetails = loansApi
                .retrieveLoan(loanDisburseResponse.body().getLoanId(), false, "transactions", "", "").execute();
        GetLoansLoanIdResponse body = loanDetails.body();
        List<GetLoansLoanIdTransactions> transactions = body.getTransactions();
        GetLoansLoanIdTransactions disbursementTransaction = transactions//
                .stream()//
                .filter(t -> t.getId().equals(disbursementTransactionId))//
                .findFirst()//
                .orElseThrow(() -> new IllegalStateException("Disbursement transaction not found"));//

        eventAssertion.assertEvent(LoanDisbursalTransactionEvent.class, disbursementTransaction.getId())//
                .extractingData(LoanTransactionDataV1::getLoanId).isEqualTo(body.getId())//
                .extractingData(LoanTransactionDataV1::getDate).isEqualTo(FORMATTER_EVENTS.format(disbursementTransaction.getDate()))//
                .extractingData(loanTransactionDataV1 -> loanTransactionDataV1.getAmount().longValue())
                .isEqualTo(disbursementTransaction.getAmount().longValue());//
    }

    public EventAssertion.EventAssertionBuilder<LoanTransactionDataV1> transactionEventCheck(
            Response<PostLoansLoanIdTransactionsResponse> transactionResponse, TransactionType transactionType, String externalOwnerId)
            throws IOException {
        Long loanId = transactionResponse.body().getLoanId();
        Long transactionId = transactionResponse.body().getResourceId();
        Response<GetLoansLoanIdResponse> loanDetailsResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
        List<GetLoansLoanIdTransactions> transactions = loanDetailsResponse.body().getTransactions();
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
            default -> throw new IllegalStateException(String.format("transaction type %s cannot be found", transactionType.getValue()));
        };

        EventAssertion.EventAssertionBuilder<LoanTransactionDataV1> eventBuilder = eventAssertion.assertEvent(eventClass, transactionId);
        eventBuilder.extractingData(LoanTransactionDataV1::getLoanId).isEqualTo(loanDetailsResponse.body().getId())//
                .extractingData(LoanTransactionDataV1::getDate).isEqualTo(FORMATTER_EVENTS.format(transactionFound.getDate()))//
                .extractingData(loanTransactionDataV1 -> loanTransactionDataV1.getAmount().longValue())
                .isEqualTo(transactionFound.getAmount().longValue())//
                .extractingData(LoanTransactionDataV1::getExternalOwnerId).isEqualTo(externalOwnerId);//
        return eventBuilder;
    }

    public void loanOwnershipTransferBusinessEventCheck(Long loanId, Long transferId) throws IOException {
        Response<PageExternalTransferData> response = externalAssetOwnersApi.getTransfers(null, loanId, null, null, null).execute();
        List<ExternalTransferData> content = response.body().getContent();

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
                .extractingData(LoanOwnershipTransferDataV1::getTotalOutstandingBalanceAmount)
                .isEqualTo(totalOutstandingBalanceAmountExpected)
                .extractingData(LoanOwnershipTransferDataV1::getOutstandingPrincipalPortion).isEqualTo(outstandingPrincipalPortionExpected)
                .extractingData(LoanOwnershipTransferDataV1::getOutstandingFeePortion).isEqualTo(outstandingFeePortionExpected)
                .extractingData(LoanOwnershipTransferDataV1::getOutstandingPenaltyPortion).isEqualTo(outstandingPenaltyPortionExpected)
                .extractingData(LoanOwnershipTransferDataV1::getOutstandingInterestPortion).isEqualTo(outstandingInterestPortionExpected)
                .extractingData(LoanOwnershipTransferDataV1::getOverPaymentPortion).isEqualTo(overPaymentPortionExpected);
    }

    public void loanOwnershipTransferBusinessEventWithStatusCheck(Long loanId, Long transferId, String transferStatus,
            String transferStatusReason) throws IOException {
        Response<PageExternalTransferData> response = externalAssetOwnersApi.getTransfers(null, loanId, null, null, null).execute();
        List<ExternalTransferData> content = response.body().getContent();

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
                .extractingData(LoanOwnershipTransferDataV1::getTotalOutstandingBalanceAmount)
                .isEqualTo(totalOutstandingBalanceAmountExpected)
                .extractingData(LoanOwnershipTransferDataV1::getOutstandingPrincipalPortion).isEqualTo(outstandingPrincipalPortionExpected)
                .extractingData(LoanOwnershipTransferDataV1::getOutstandingFeePortion).isEqualTo(outstandingFeePortionExpected)
                .extractingData(LoanOwnershipTransferDataV1::getOutstandingPenaltyPortion).isEqualTo(outstandingPenaltyPortionExpected)
                .extractingData(LoanOwnershipTransferDataV1::getOutstandingInterestPortion).isEqualTo(outstandingInterestPortionExpected)
                .extractingData(LoanOwnershipTransferDataV1::getOverPaymentPortion).isEqualTo(overPaymentPortionExpected)
                .extractingData(LoanOwnershipTransferDataV1::getTransferStatus).isEqualTo(transferStatusExpected)
                .extractingData(LoanOwnershipTransferDataV1::getTransferStatusReason).isEqualTo(transferStatusReasonExpected);
    }

    public void loanAccountSnapshotBusinessEventCheck(Long loanId, Long transferId) throws IOException {
        Response<PageExternalTransferData> response = externalAssetOwnersApi.getTransfers(null, loanId, null, null, null).execute();
        List<ExternalTransferData> content = response.body().getContent();

        ExternalTransferData filtered = content.stream().filter(t -> transferId.equals(t.getTransferId())).reduce((first, second) -> second)
                .orElseThrow(() -> new IllegalStateException("No element found"));

        String ownerExternalIdExpected = filtered.getStatus().getValue().equals("BUYBACK") ? null : filtered.getOwner().getExternalId();
        String settlementDateExpected = filtered.getStatus().getValue().equals("BUYBACK") ? null
                : FORMATTER_EVENTS.format(filtered.getSettlementDate());
        BigDecimal totalOutstandingBalanceAmountExpected = zeroConversion(filtered.getDetails().getTotalOutstanding());
        BigDecimal outstandingPrincipalPortionExpected = zeroConversion(filtered.getDetails().getTotalPrincipalOutstanding());
        BigDecimal outstandingFeePortionExpected = zeroConversion(filtered.getDetails().getTotalFeeChargesOutstanding());
        BigDecimal outstandingPenaltyPortionExpected = zeroConversion(filtered.getDetails().getTotalPenaltyChargesOutstanding());
        BigDecimal outstandingInterestPortionExpected = zeroConversion(filtered.getDetails().getTotalInterestOutstanding());
        BigDecimal overPaymentPortionExpected = zeroConversion(filtered.getDetails().getTotalOverpaid());

        eventAssertion.assertEvent(LoanAccountSnapshotEvent.class, loanId).extractingData(LoanAccountDataV1::getId).isEqualTo(loanId)
                .extractingData(LoanAccountDataV1::getExternalOwnerId).isEqualTo(ownerExternalIdExpected)
                .extractingData(LoanAccountDataV1::getSettlementDate).isEqualTo(settlementDateExpected)
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getTotalOutstanding())
                .isEqualTo(totalOutstandingBalanceAmountExpected)
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getPrincipalOutstanding())
                .isEqualTo(outstandingPrincipalPortionExpected)
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getFeeChargesOutstanding())
                .isEqualTo(outstandingFeePortionExpected)
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getPenaltyChargesOutstanding())
                .isEqualTo(outstandingPenaltyPortionExpected)
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getInterestOutstanding())
                .isEqualTo(outstandingInterestPortionExpected)
                .extractingData(loanAccountDataV1 -> loanAccountDataV1.getSummary().getTotalOverdue())
                .isEqualTo(overPaymentPortionExpected);
    }

    public void loanAccountDelinquencyPauseChangedBusinessEventCheck(Long loanId) throws IOException {
        Response<GetLoansLoanIdResponse> loanDetails = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
        List<GetLoansLoanIdDelinquencyPausePeriod> delinquencyPausePeriodsActual = loanDetails.body().getDelinquent()
                .getDelinquencyPausePeriods();

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

                        log.info("LoanAccountDelinquencyPauseChangedBusinessEvent -> isActiveActual:\s{}", isActiveActual);
                        log.info("LoanAccountDelinquencyPauseChangedBusinessEvent -> pausePeriodStartActual:\s{}", pausePeriodStartActual);
                        log.info("LoanAccountDelinquencyPauseChangedBusinessEvent -> pausePeriodEndActual:\s{}", pausePeriodEndActual);
                    }
                    return null;
                });
    }

    public void installmentLevelDelinquencyRangeChangeEventCheck(Long loanId) throws IOException {
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
                    .isEqualTo(totalAmountSumActual);

            log.info("Nr of installment level delinquency buckets: {}",
                    loanAccountDelinquencyRangeDataV1.getInstallmentDelinquencyBuckets().size());
            log.info("Buckets:");
            loanAccountDelinquencyRangeDataV1.getInstallmentDelinquencyBuckets().forEach(e -> {
                log.info("{}\s-\sTotal amount:\s{}", e.getDelinquencyRange().getClassification(), e.getAmount().getTotalAmount());
            });

            return null;
        });
    }

    private BigDecimal zeroConversion(BigDecimal input) {
        return input.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal(input.toEngineeringString()) : input.setScale(8);
    }
}
