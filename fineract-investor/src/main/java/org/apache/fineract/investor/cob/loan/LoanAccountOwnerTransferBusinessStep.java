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
package org.apache.fineract.investor.cob.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.loan.LoanCOBBusinessStep;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountSnapshotBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.investor.config.InvestorModuleIsEnabledCondition;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferDetails;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.domain.LoanOwnershipTransferBusinessEvent;
import org.apache.fineract.investor.service.DelayedSettlementAttributeService;
import org.apache.fineract.investor.service.ExternalAssetOwnerTransferOutstandingInterestCalculation;
import org.apache.fineract.investor.service.LoanTransferabilityService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.LoanJournalEntryPoster;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Conditional(InvestorModuleIsEnabledCondition.class)
public class LoanAccountOwnerTransferBusinessStep implements LoanCOBBusinessStep {

    public static final LocalDate FUTURE_DATE_9999_12_31 = LocalDate.of(9999, 12, 31);
    public static final List<ExternalTransferStatus> PENDING_STATUSES = List.of(ExternalTransferStatus.PENDING_INTERMEDIATE,
            ExternalTransferStatus.PENDING);
    public static final List<ExternalTransferStatus> BUYBACK_STATUSES = List.of(ExternalTransferStatus.BUYBACK_INTERMEDIATE,
            ExternalTransferStatus.BUYBACK);
    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalAssetOwnerTransferLoanMappingRepository externalAssetOwnerTransferLoanMappingRepository;
    private final LoanJournalEntryPoster loanJournalEntryPoster;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransferabilityService loanTransferabilityService;
    private final DelayedSettlementAttributeService delayedSettlementAttributeService;
    private final ExternalAssetOwnerTransferOutstandingInterestCalculation externalAssetOwnerTransferOutstandingInterestCalculation;

    @Override
    public Loan execute(Loan loan) {
        Long loanId = loan.getId();
        log.debug("start processing loan ownership transfer business step for loan with Id [{}]", loanId);

        LocalDate settlementDate = DateUtils.getBusinessLocalDate();
        List<ExternalAssetOwnerTransfer> transferDataList = externalAssetOwnerTransferRepository.findAll(
                (root, query, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get("loanId"), loanId),
                        criteriaBuilder.equal(root.get("settlementDate"), settlementDate),
                        root.get("status").in(Stream.concat(PENDING_STATUSES.stream(), BUYBACK_STATUSES.stream()).toList()),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("effectiveDateTo"), FUTURE_DATE_9999_12_31)),
                Sort.by(Sort.Direction.ASC, "id"));
        int size = transferDataList.size();

        if (size == 2) {
            ExternalTransferStatus firstTransferStatus = transferDataList.get(0).getStatus();
            ExternalTransferStatus secondTransferStatus = transferDataList.get(1).getStatus();

            if (delayedSettlementAttributeService.isEnabled(loan.getLoanProduct().getId())) {
                throw new IllegalStateException(String.format("Delayed Settlement enabled, but found 2 transfers of statuses: %s and %s",
                        firstTransferStatus, secondTransferStatus));
            }

            if (!ExternalTransferStatus.PENDING.equals(firstTransferStatus)
                    || !ExternalTransferStatus.BUYBACK.equals(secondTransferStatus)) {
                throw new IllegalStateException(String.format("Illegal transfer found. Expected %s and %s, found: %s and %s",
                        ExternalTransferStatus.PENDING, ExternalTransferStatus.BUYBACK, firstTransferStatus, secondTransferStatus));
            }
            handleSameDaySaleAndBuyback(settlementDate, transferDataList, loan);
        } else if (size == 1) {
            ExternalAssetOwnerTransfer transfer = transferDataList.get(0);
            if (PENDING_STATUSES.contains(transfer.getStatus())) {
                handleSale(loan, settlementDate, transfer);
            } else if (BUYBACK_STATUSES.contains(transfer.getStatus())) {
                handleBuyback(loan, settlementDate, transfer);
            }
        }

        log.debug("end processing loan ownership transfer business step for loan Id [{}]", loan.getId());
        return loan;
    }

    private void handleSale(final Loan loan, final LocalDate settlementDate, final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        ExternalAssetOwnerTransfer newExternalAssetOwnerTransfer = sellAssetOrDecline(loan, settlementDate, externalAssetOwnerTransfer);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanOwnershipTransferBusinessEvent(newExternalAssetOwnerTransfer, loan));
        if (!ExternalTransferStatus.DECLINED.equals(newExternalAssetOwnerTransfer.getStatus())) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountSnapshotBusinessEvent(loan));
        }
    }

    private void handleBuyback(final Loan loan, final LocalDate settlementDate,
            final ExternalAssetOwnerTransfer buybackExternalAssetOwnerTransfer) {
        final ExternalTransferStatus expectedActiveStatus = determineExpectedActiveStatus(buybackExternalAssetOwnerTransfer);

        Optional<ExternalAssetOwnerTransfer> optActiveExternalAssetOwnerTransfer = externalAssetOwnerTransferRepository
                .findOne((root, query, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get("loanId"), loan.getId()),
                        criteriaBuilder.equal(root.get("owner"), buybackExternalAssetOwnerTransfer.getOwner()),
                        criteriaBuilder.equal(root.get("status"), expectedActiveStatus),
                        criteriaBuilder.equal(root.get("effectiveDateTo"), FUTURE_DATE_9999_12_31)));
        ExternalAssetOwnerTransfer newExternalAssetOwnerTransfer;
        if (!optActiveExternalAssetOwnerTransfer.isPresent()) {
            newExternalAssetOwnerTransfer = createNewEntryAndExpireOldEntry(settlementDate, buybackExternalAssetOwnerTransfer,
                    ExternalTransferStatus.CANCELLED, ExternalTransferSubStatus.UNSOLD, settlementDate, settlementDate);
        } else {
            newExternalAssetOwnerTransfer = buybackAsset(loan, settlementDate, buybackExternalAssetOwnerTransfer,
                    optActiveExternalAssetOwnerTransfer.get());
        }
        businessEventNotifierService.notifyPostBusinessEvent(new LoanOwnershipTransferBusinessEvent(newExternalAssetOwnerTransfer, loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountSnapshotBusinessEvent(loan));
    }

    private ExternalAssetOwnerTransfer buybackAsset(final Loan loan, final LocalDate settlementDate,
            ExternalAssetOwnerTransfer buybackExternalAssetOwnerTransfer, ExternalAssetOwnerTransfer activeExternalAssetOwnerTransfer) {
        activeExternalAssetOwnerTransfer.setEffectiveDateTo(settlementDate);
        buybackExternalAssetOwnerTransfer.setEffectiveDateTo(settlementDate);
        buybackExternalAssetOwnerTransfer
                .setExternalAssetOwnerTransferDetails(createAssetOwnerTransferDetails(loan, buybackExternalAssetOwnerTransfer));
        externalAssetOwnerTransferRepository.save(activeExternalAssetOwnerTransfer);
        buybackExternalAssetOwnerTransfer = externalAssetOwnerTransferRepository.save(buybackExternalAssetOwnerTransfer);
        externalAssetOwnerTransferLoanMappingRepository.deleteByLoanIdAndOwnerTransfer(loan.getId(), activeExternalAssetOwnerTransfer);
        loanJournalEntryPoster.postJournalEntriesForExternalOwnerTransfer(loan, buybackExternalAssetOwnerTransfer, null);
        return buybackExternalAssetOwnerTransfer;
    }

    private ExternalAssetOwnerTransfer sellAssetOrDecline(final Loan loan, final LocalDate settlementDate,
            final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        if (!loanTransferabilityService.isTransferable(loan, externalAssetOwnerTransfer)) {
            // Validation fails. Decline asset sell.
            ExternalTransferSubStatus declinedSubStatus = loanTransferabilityService.getDeclinedSubStatus(loan);
            return declinePendingEntry(loan, settlementDate, externalAssetOwnerTransfer, declinedSubStatus);
        }

        ExternalAssetOwnerTransfer newExternalAssetOwnerTransfer = sellAsset(loan, settlementDate, externalAssetOwnerTransfer);
        createActiveMapping(loan.getId(), newExternalAssetOwnerTransfer);
        newExternalAssetOwnerTransfer
                .setExternalAssetOwnerTransferDetails(createAssetOwnerTransferDetails(loan, newExternalAssetOwnerTransfer));

        return newExternalAssetOwnerTransfer;
    }

    private ExternalAssetOwnerTransfer sellAsset(final Loan loan, final LocalDate settlementDate,
            final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        ExternalAssetOwner previousOwner = determinePreviousOwnerAndCleanupIfNeeded(loan, settlementDate, externalAssetOwnerTransfer);
        ExternalTransferStatus activeStatus = determineActiveStatus(externalAssetOwnerTransfer);

        ExternalAssetOwnerTransfer newTransfer = activatePendingEntry(settlementDate, externalAssetOwnerTransfer, activeStatus);
        loanJournalEntryPoster.postJournalEntriesForExternalOwnerTransfer(loan, newTransfer, previousOwner);
        return newTransfer;
    }

    private ExternalAssetOwner determinePreviousOwnerAndCleanupIfNeeded(final Loan loan, final LocalDate settlementDate,
            final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        if (!delayedSettlementAttributeService.isEnabled(loan.getLoanProduct().getId())) {
            // When delayed settlement is disabled, asset is directly sold to investor, and we are the previous owner.
            return null;
        }

        if (ExternalTransferStatus.PENDING_INTERMEDIATE == externalAssetOwnerTransfer.getStatus()) {
            // When delayed settlement is enabled and asset is sold to intermediate, we are the previous owner.
            return null;
        }

        // When delayed settlement is enabled and asset is sold from intermediate to investor, the intermediate is the
        // previous owner.
        ExternalAssetOwnerTransfer activeIntermediateTransfer = getActiveIntermediateOrThrow(loan);
        expireTransfer(settlementDate, activeIntermediateTransfer);
        externalAssetOwnerTransferLoanMappingRepository.deleteByLoanIdAndOwnerTransfer(loan.getId(), activeIntermediateTransfer);

        return activeIntermediateTransfer.getOwner();
    }

    private ExternalTransferStatus determineActiveStatus(final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        if (ExternalTransferStatus.PENDING_INTERMEDIATE == externalAssetOwnerTransfer.getStatus()) {
            return ExternalTransferStatus.ACTIVE_INTERMEDIATE;
        }

        return ExternalTransferStatus.ACTIVE;
    }

    private ExternalAssetOwnerTransfer getActiveIntermediateOrThrow(final Loan loan) {
        Optional<ExternalAssetOwnerTransfer> optionalActiveIntermediateTransfer = externalAssetOwnerTransferRepository
                .findOne((root, query, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get("loanId"), loan.getId()),
                        criteriaBuilder.equal(root.get("status"), ExternalTransferStatus.ACTIVE_INTERMEDIATE),
                        criteriaBuilder.equal(root.get("effectiveDateTo"), FUTURE_DATE_9999_12_31)));

        return optionalActiveIntermediateTransfer
                .orElseThrow(() -> new IllegalStateException("Expected a effective transfer of ACTIVE_INTERMEDIATE status to be present."));
    }

    private ExternalAssetOwnerTransferDetails createAssetOwnerTransferDetails(Loan loan,
            ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        ExternalAssetOwnerTransferDetails details = new ExternalAssetOwnerTransferDetails();
        details.setExternalAssetOwnerTransfer(externalAssetOwnerTransfer);
        details.setTotalPrincipalOutstanding(loan.getSummary().getTotalPrincipalOutstanding());
        // We have different strategies to calculate oustanding interest
        final BigDecimal interestAmount = externalAssetOwnerTransferOutstandingInterestCalculation.calculateOutstandingInterest(loan);
        details.setTotalInterestOutstanding(interestAmount);
        details.setTotalFeeChargesOutstanding(loan.getSummary().getTotalFeeChargesOutstanding());
        details.setTotalPenaltyChargesOutstanding(loan.getSummary().getTotalPenaltyChargesOutstanding());
        details.setTotalOverpaid(loan.getTotalOverpaid());
        return details;
    }

    private void createActiveMapping(Long loanId, ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        ExternalAssetOwnerTransferLoanMapping externalAssetOwnerTransferLoanMapping = new ExternalAssetOwnerTransferLoanMapping();
        externalAssetOwnerTransferLoanMapping.setLoanId(loanId);
        externalAssetOwnerTransferLoanMapping.setOwnerTransfer(externalAssetOwnerTransfer);
        externalAssetOwnerTransferLoanMappingRepository.save(externalAssetOwnerTransferLoanMapping);
    }

    private void handleSameDaySaleAndBuyback(final LocalDate settlementDate, final List<ExternalAssetOwnerTransfer> transferDataList,
            Loan loan) {
        ExternalAssetOwnerTransfer cancelledPendingTransfer = cancelTransfer(settlementDate, transferDataList.get(0));
        ExternalAssetOwnerTransfer cancelledBuybackTransfer = cancelTransfer(settlementDate, transferDataList.get(1));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanOwnershipTransferBusinessEvent(cancelledPendingTransfer, loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanOwnershipTransferBusinessEvent(cancelledBuybackTransfer, loan));
    }

    private ExternalAssetOwnerTransfer cancelTransfer(final LocalDate settlementDate,
            final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        return createNewEntryAndExpireOldEntry(settlementDate, externalAssetOwnerTransfer, ExternalTransferStatus.CANCELLED,
                ExternalTransferSubStatus.SAMEDAY_TRANSFERS, settlementDate, settlementDate);
    }

    private ExternalAssetOwnerTransfer activatePendingEntry(final LocalDate settlementDate,
            final ExternalAssetOwnerTransfer pendingTransfer, final ExternalTransferStatus activeStatus) {
        LocalDate effectiveFrom = settlementDate.plusDays(1);
        return createNewEntryAndExpireOldEntry(settlementDate, pendingTransfer, activeStatus, null, effectiveFrom, FUTURE_DATE_9999_12_31);
    }

    private ExternalAssetOwnerTransfer declinePendingEntry(final Loan loan, final LocalDate settlementDate,
            final ExternalAssetOwnerTransfer pendingTransfer, ExternalTransferSubStatus subStatus) {
        return createNewEntryAndExpireOldEntry(settlementDate, pendingTransfer, ExternalTransferStatus.DECLINED, subStatus, settlementDate,
                settlementDate);
    }

    private ExternalAssetOwnerTransfer createNewEntryAndExpireOldEntry(final LocalDate settlementDate,
            final ExternalAssetOwnerTransfer externalAssetOwnerTransfer, final ExternalTransferStatus status,
            final ExternalTransferSubStatus subStatus, final LocalDate effectiveDateFrom, final LocalDate effectiveDateTo) {
        ExternalAssetOwnerTransfer newExternalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();
        newExternalAssetOwnerTransfer.setOwner(externalAssetOwnerTransfer.getOwner());
        newExternalAssetOwnerTransfer.setExternalId(externalAssetOwnerTransfer.getExternalId());
        newExternalAssetOwnerTransfer.setStatus(status);
        newExternalAssetOwnerTransfer.setSubStatus(subStatus);
        newExternalAssetOwnerTransfer.setSettlementDate(settlementDate);
        newExternalAssetOwnerTransfer.setLoanId(externalAssetOwnerTransfer.getLoanId());
        newExternalAssetOwnerTransfer.setExternalLoanId(externalAssetOwnerTransfer.getExternalLoanId());
        newExternalAssetOwnerTransfer.setExternalGroupId(externalAssetOwnerTransfer.getExternalGroupId());
        newExternalAssetOwnerTransfer.setPurchasePriceRatio(externalAssetOwnerTransfer.getPurchasePriceRatio());
        newExternalAssetOwnerTransfer.setEffectiveDateFrom(effectiveDateFrom);
        newExternalAssetOwnerTransfer.setEffectiveDateTo(effectiveDateTo);

        expireTransfer(settlementDate, externalAssetOwnerTransfer);

        return externalAssetOwnerTransferRepository.save(newExternalAssetOwnerTransfer);
    }

    private void expireTransfer(final LocalDate settlementDate, final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        externalAssetOwnerTransfer.setEffectiveDateTo(settlementDate);
        externalAssetOwnerTransferRepository.save(externalAssetOwnerTransfer);
    }

    private ExternalTransferStatus determineExpectedActiveStatus(final ExternalAssetOwnerTransfer buybackExternalAssetOwnerTransfer) {
        if (ExternalTransferStatus.BUYBACK_INTERMEDIATE == buybackExternalAssetOwnerTransfer.getStatus()) {
            return ExternalTransferStatus.ACTIVE_INTERMEDIATE;
        }
        return ExternalTransferStatus.ACTIVE;
    }

    @Override
    public String getEnumStyledName() {
        return "EXTERNAL_ASSET_OWNER_TRANSFER";
    }

    @Override
    public String getHumanReadableName() {
        return "Execute external asset owner transfer";
    }
}
