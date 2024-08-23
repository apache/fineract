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
package org.apache.fineract.investor.service;

import static org.apache.fineract.infrastructure.core.service.DateUtils.getBusinessLocalDate;
import static org.apache.fineract.investor.data.ExternalTransferStatus.BUYBACK;
import static org.apache.fineract.investor.data.ExternalTransferStatus.CANCELLED;
import static org.apache.fineract.investor.data.ExternalTransferStatus.DECLINED;
import static org.apache.fineract.investor.data.ExternalTransferStatus.PENDING;
import static org.apache.fineract.investor.data.ExternalTransferSubStatus.BALANCE_NEGATIVE;
import static org.apache.fineract.investor.data.ExternalTransferSubStatus.BALANCE_ZERO;
import static org.apache.fineract.investor.data.ExternalTransferSubStatus.SAMEDAY_TRANSFERS;
import static org.apache.fineract.investor.data.ExternalTransferSubStatus.UNSOLD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountSnapshotBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferDetails;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.domain.LoanOwnershipTransferBusinessEvent;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LoanAccountOwnerTransferServiceImpl implements LoanAccountOwnerTransferService {

    public static final LocalDate FUTURE_DATE_9999_12_31 = LocalDate.of(9999, 12, 31);
    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalAssetOwnerTransferLoanMappingRepository externalAssetOwnerTransferLoanMappingRepository;
    private final AccountingService accountingService;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public void handleLoanClosedOrOverpaid(Loan loan) {
        Long loanId = loan.getId();
        List<ExternalAssetOwnerTransfer> transferDataList = findAllPendingOrBuybackTransfers(loanId);

        if (transferDataList.size() == 2) {
            ExternalTransferSubStatus subStatus;
            ExternalAssetOwnerTransfer pendingSaleTransfer = transferDataList.get(0);
            ExternalAssetOwnerTransfer pendingBuybackTransfer = transferDataList.get(1);
            if (isSameDayTransfers(transferDataList)) {
                subStatus = SAMEDAY_TRANSFERS;
                cancelTransfer(loan, pendingSaleTransfer, subStatus);
                cancelTransfer(loan, pendingBuybackTransfer, subStatus);
            } else {
                declineTransfer(loan, pendingSaleTransfer);
                cancelTransfer(loan, pendingBuybackTransfer, UNSOLD);
            }
        } else if (transferDataList.size() == 1) {
            ExternalAssetOwnerTransfer transfer = transferDataList.get(0);
            if (PENDING.equals(transfer.getStatus())) {
                declineTransfer(loan, transfer);
            } else if (BUYBACK.equals(transfer.getStatus())) {
                executePendingBuybackTransfer(loan, transfer);
            }
        }
    }

    private void cancelTransfer(Loan loan, ExternalAssetOwnerTransfer pendingTransfer, ExternalTransferSubStatus subStatus) {
        updatePendingTransfer(pendingTransfer);
        ExternalAssetOwnerTransfer cancelledTransfer = createCancelledTransfer(pendingTransfer, subStatus);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanOwnershipTransferBusinessEvent(cancelledTransfer, loan));
    }

    private void declineTransfer(Loan loan, ExternalAssetOwnerTransfer pendingTransfer) {
        ExternalAssetOwnerTransfer declinedSaleTransfer = createDeclinedTransfer(pendingTransfer, loan);
        updatePendingTransfer(pendingTransfer);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanOwnershipTransferBusinessEvent(declinedSaleTransfer, loan));
    }

    private void executePendingBuybackTransfer(final Loan loan, ExternalAssetOwnerTransfer buybackTransfer) {
        ExternalAssetOwnerTransfer activeTransfer = findActiveTransfer(loan, buybackTransfer);
        updateActiveTransfer(activeTransfer);
        buybackTransfer = updatePendingBuybackTransfer(loan, buybackTransfer);

        externalAssetOwnerTransferLoanMappingRepository.deleteByLoanIdAndOwnerTransfer(loan.getId(), activeTransfer);
        accountingService.createJournalEntriesForBuybackAssetTransfer(loan, buybackTransfer);

        businessEventNotifierService.notifyPostBusinessEvent(new LoanOwnershipTransferBusinessEvent(buybackTransfer, loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountSnapshotBusinessEvent(loan));
    }

    private ExternalAssetOwnerTransfer createCancelledTransfer(ExternalAssetOwnerTransfer pendingTransfer,
            ExternalTransferSubStatus subStatus) {
        ExternalAssetOwnerTransfer cancelledTransfer = new ExternalAssetOwnerTransfer();
        cancelledTransfer.setOwner(pendingTransfer.getOwner());
        cancelledTransfer.setExternalId(pendingTransfer.getExternalId());
        cancelledTransfer.setStatus(CANCELLED);
        cancelledTransfer.setSubStatus(subStatus);
        cancelledTransfer.setSettlementDate(pendingTransfer.getSettlementDate());
        cancelledTransfer.setLoanId(pendingTransfer.getLoanId());
        cancelledTransfer.setExternalLoanId(pendingTransfer.getExternalLoanId());
        cancelledTransfer.setPurchasePriceRatio(pendingTransfer.getPurchasePriceRatio());
        cancelledTransfer.setEffectiveDateFrom(getBusinessLocalDate());
        cancelledTransfer.setEffectiveDateTo(getBusinessLocalDate());
        return externalAssetOwnerTransferRepository.save(cancelledTransfer);
    }

    private ExternalAssetOwnerTransfer createDeclinedTransfer(ExternalAssetOwnerTransfer pendingSaleTransfer, Loan loan) {
        ExternalAssetOwnerTransfer declinedTransfer = new ExternalAssetOwnerTransfer();
        declinedTransfer.setOwner(pendingSaleTransfer.getOwner());
        declinedTransfer.setExternalId(pendingSaleTransfer.getExternalId());
        declinedTransfer.setStatus(DECLINED);
        declinedTransfer.setSubStatus(isBiggerThanZero(loan.getTotalOverpaid()) ? BALANCE_NEGATIVE : BALANCE_ZERO);
        declinedTransfer.setSettlementDate(pendingSaleTransfer.getSettlementDate());
        declinedTransfer.setLoanId(pendingSaleTransfer.getLoanId());
        declinedTransfer.setExternalLoanId(pendingSaleTransfer.getExternalLoanId());
        declinedTransfer.setPurchasePriceRatio(pendingSaleTransfer.getPurchasePriceRatio());
        declinedTransfer.setEffectiveDateFrom(getBusinessLocalDate());
        declinedTransfer.setEffectiveDateTo(getBusinessLocalDate());
        return externalAssetOwnerTransferRepository.save(declinedTransfer);
    }

    private void updatePendingTransfer(ExternalAssetOwnerTransfer pendingTransfer) {
        pendingTransfer.setEffectiveDateTo(getBusinessLocalDate());
        externalAssetOwnerTransferRepository.save(pendingTransfer);
    }

    private ExternalAssetOwnerTransfer updatePendingBuybackTransfer(Loan loan, ExternalAssetOwnerTransfer buybackTransfer) {
        buybackTransfer.setEffectiveDateTo(getBusinessLocalDate());
        buybackTransfer.setExternalAssetOwnerTransferDetails(createAssetOwnerTransferDetails(loan, buybackTransfer));
        return externalAssetOwnerTransferRepository.save(buybackTransfer);
    }

    private void updateActiveTransfer(ExternalAssetOwnerTransfer activeTransfer) {
        activeTransfer.setEffectiveDateTo(getBusinessLocalDate());
        externalAssetOwnerTransferRepository.save(activeTransfer);
    }

    private ExternalAssetOwnerTransferDetails createAssetOwnerTransferDetails(Loan loan,
            ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        ExternalAssetOwnerTransferDetails details = new ExternalAssetOwnerTransferDetails();
        details.setExternalAssetOwnerTransfer(externalAssetOwnerTransfer);
        details.setTotalOutstanding(Objects.requireNonNullElse(loan.getSummary().getTotalOutstanding(), BigDecimal.ZERO));
        details.setTotalPrincipalOutstanding(Objects.requireNonNullElse(loan.getSummary().getTotalPrincipalOutstanding(), BigDecimal.ZERO));
        details.setTotalInterestOutstanding(Objects.requireNonNullElse(loan.getSummary().getTotalInterestOutstanding(), BigDecimal.ZERO));
        details.setTotalFeeChargesOutstanding(
                Objects.requireNonNullElse(loan.getSummary().getTotalFeeChargesOutstanding(), BigDecimal.ZERO));
        details.setTotalPenaltyChargesOutstanding(
                Objects.requireNonNullElse(loan.getSummary().getTotalPenaltyChargesOutstanding(), BigDecimal.ZERO));
        details.setTotalOverpaid(Objects.requireNonNullElse(loan.getTotalOverpaid(), BigDecimal.ZERO));
        return details;
    }

    private ExternalAssetOwnerTransfer findActiveTransfer(Loan loan, ExternalAssetOwnerTransfer buybackTransfer) {
        return externalAssetOwnerTransferRepository
                .findOne((root, query, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get("loanId"), loan.getId()),
                        criteriaBuilder.equal(root.get("owner"), buybackTransfer.getOwner()),
                        criteriaBuilder.equal(root.get("status"), ExternalTransferStatus.ACTIVE),
                        criteriaBuilder.equal(root.get("effectiveDateTo"), FUTURE_DATE_9999_12_31)))
                .orElseThrow();
    }

    private List<ExternalAssetOwnerTransfer> findAllPendingOrBuybackTransfers(Long loanId) {
        return externalAssetOwnerTransferRepository
                .findAll(
                        (root, query, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get("loanId"), loanId),
                                root.get("status").in(List.of(PENDING, BUYBACK)),
                                criteriaBuilder.equal(root.get("effectiveDateTo"), FUTURE_DATE_9999_12_31)),
                        Sort.by(Sort.Direction.ASC, "id"));
    }

    private boolean isBiggerThanZero(BigDecimal loanTotalOverpaid) {
        return MathUtil.nullToDefault(loanTotalOverpaid, BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
    }

    private static boolean isSameDayTransfers(List<ExternalAssetOwnerTransfer> transferDataList) {
        return Objects.equals(transferDataList.get(0).getSettlementDate(), transferDataList.get(1).getSettlementDate());
    }
}
