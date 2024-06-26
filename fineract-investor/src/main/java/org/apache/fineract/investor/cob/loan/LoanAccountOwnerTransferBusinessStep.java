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
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.loan.LoanCOBBusinessStep;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAccountSnapshotBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.investor.config.InvestorModuleIsEnabledCondition;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferDetails;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMapping;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.domain.LoanOwnershipTransferBusinessEvent;
import org.apache.fineract.investor.service.AccountingService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Conditional(InvestorModuleIsEnabledCondition.class)
public class LoanAccountOwnerTransferBusinessStep implements LoanCOBBusinessStep {

    public static final LocalDate FUTURE_DATE_9999_12_31 = LocalDate.of(9999, 12, 31);
    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalAssetOwnerTransferLoanMappingRepository externalAssetOwnerTransferLoanMappingRepository;
    private final AccountingService accountingService;
    private final BusinessEventNotifierService businessEventNotifierService;

    @Override
    public Loan execute(Loan loan) {
        Long loanId = loan.getId();
        log.debug("start processing loan ownership transfer business step for loan with Id [{}]", loanId);

        LocalDate settlementDate = DateUtils.getBusinessLocalDate();
        List<ExternalAssetOwnerTransfer> transferDataList = externalAssetOwnerTransferRepository.findAll(
                (root, query, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get("loanId"), loanId),
                        criteriaBuilder.equal(root.get("settlementDate"), settlementDate),
                        root.get("status").in(List.of(ExternalTransferStatus.PENDING, ExternalTransferStatus.BUYBACK)),
                        criteriaBuilder.greaterThanOrEqualTo(root.get("effectiveDateTo"), FUTURE_DATE_9999_12_31)),
                Sort.by(Sort.Direction.ASC, "id"));
        int size = transferDataList.size();

        if (size == 2) {
            ExternalTransferStatus firstTransferStatus = transferDataList.get(0).getStatus();
            ExternalTransferStatus secondTransferStatus = transferDataList.get(1).getStatus();

            if (!ExternalTransferStatus.PENDING.equals(firstTransferStatus)
                    || !ExternalTransferStatus.BUYBACK.equals(secondTransferStatus)) {
                throw new IllegalStateException(String.format("Illegal transfer found. Expected %s and %s, found: %s and %s",
                        ExternalTransferStatus.PENDING, ExternalTransferStatus.BUYBACK, firstTransferStatus, secondTransferStatus));
            }
            handleSameDaySaleAndBuyback(settlementDate, transferDataList, loan);
        } else if (size == 1) {
            ExternalAssetOwnerTransfer transfer = transferDataList.get(0);
            if (ExternalTransferStatus.PENDING.equals(transfer.getStatus())) {
                handleSale(loan, settlementDate, transfer);
            } else if (ExternalTransferStatus.BUYBACK.equals(transfer.getStatus())) {
                handleBuyback(loan, settlementDate, transfer);
            }
        }

        log.debug("end processing loan ownership transfer business step for loan Id [{}]", loan.getId());
        return loan;
    }

    private void handleSale(final Loan loan, final LocalDate settlementDate, final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        ExternalAssetOwnerTransfer newExternalAssetOwnerTransfer = sellAsset(loan, settlementDate, externalAssetOwnerTransfer);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanOwnershipTransferBusinessEvent(newExternalAssetOwnerTransfer, loan));
        if (!ExternalTransferStatus.DECLINED.equals(newExternalAssetOwnerTransfer.getStatus())) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountSnapshotBusinessEvent(loan));
        }
    }

    private void handleBuyback(final Loan loan, final LocalDate settlementDate,
            final ExternalAssetOwnerTransfer buybackExternalAssetOwnerTransfer) {
        Optional<ExternalAssetOwnerTransfer> optActiveExternalAssetOwnerTransfer = externalAssetOwnerTransferRepository
                .findOne((root, query, criteriaBuilder) -> criteriaBuilder.and(criteriaBuilder.equal(root.get("loanId"), loan.getId()),
                        criteriaBuilder.equal(root.get("owner"), buybackExternalAssetOwnerTransfer.getOwner()),
                        criteriaBuilder.equal(root.get("status"), ExternalTransferStatus.ACTIVE),
                        criteriaBuilder.equal(root.get("effectiveDateTo"), FUTURE_DATE_9999_12_31)));
        ExternalAssetOwnerTransfer newExternalAssetOwnerTransfer;
        if (!optActiveExternalAssetOwnerTransfer.isPresent()) {
            newExternalAssetOwnerTransfer = createNewEntry(settlementDate, buybackExternalAssetOwnerTransfer,
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
        accountingService.createJournalEntriesForBuybackAssetTransfer(loan, buybackExternalAssetOwnerTransfer);
        return buybackExternalAssetOwnerTransfer;
    }

    private ExternalAssetOwnerTransfer sellAsset(final Loan loan, final LocalDate settlementDate,
            ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        ExternalAssetOwnerTransfer newExternalAssetOwnerTransfer;
        if (isTransferable(loan)) {
            newExternalAssetOwnerTransfer = createActiveEntry(settlementDate, externalAssetOwnerTransfer);
            createActiveMapping(loan.getId(), newExternalAssetOwnerTransfer);
            newExternalAssetOwnerTransfer
                    .setExternalAssetOwnerTransferDetails(createAssetOwnerTransferDetails(loan, newExternalAssetOwnerTransfer));
            accountingService.createJournalEntriesForSaleAssetTransfer(loan, newExternalAssetOwnerTransfer);
        } else {
            ExternalTransferSubStatus subStatus = ExternalTransferSubStatus.BALANCE_ZERO;
            if (MathUtil.nullToDefault(loan.getTotalOverpaid(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
                subStatus = ExternalTransferSubStatus.BALANCE_NEGATIVE;
            }
            newExternalAssetOwnerTransfer = createNewEntry(settlementDate, externalAssetOwnerTransfer, ExternalTransferStatus.DECLINED,
                    subStatus, settlementDate, settlementDate);
        }
        return newExternalAssetOwnerTransfer;
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

    private void createActiveMapping(Long loanId, ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        ExternalAssetOwnerTransferLoanMapping externalAssetOwnerTransferLoanMapping = new ExternalAssetOwnerTransferLoanMapping();
        externalAssetOwnerTransferLoanMapping.setLoanId(loanId);
        externalAssetOwnerTransferLoanMapping.setOwnerTransfer(externalAssetOwnerTransfer);
        externalAssetOwnerTransferLoanMappingRepository.save(externalAssetOwnerTransferLoanMapping);
    }

    private boolean isTransferable(final Loan loan) {
        return MathUtil.nullToDefault(loan.getSummary().getTotalOutstanding(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
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
        return createNewEntry(settlementDate, externalAssetOwnerTransfer, ExternalTransferStatus.CANCELLED,
                ExternalTransferSubStatus.SAMEDAY_TRANSFERS, settlementDate, settlementDate);
    }

    private ExternalAssetOwnerTransfer createNewEntry(final LocalDate settlementDate,
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
        newExternalAssetOwnerTransfer.setPurchasePriceRatio(externalAssetOwnerTransfer.getPurchasePriceRatio());
        newExternalAssetOwnerTransfer.setEffectiveDateFrom(effectiveDateFrom);
        newExternalAssetOwnerTransfer.setEffectiveDateTo(effectiveDateTo);

        externalAssetOwnerTransfer.setEffectiveDateTo(settlementDate);
        externalAssetOwnerTransferRepository.save(externalAssetOwnerTransfer);
        return externalAssetOwnerTransferRepository.save(newExternalAssetOwnerTransfer);
    }

    private ExternalAssetOwnerTransfer createActiveEntry(final LocalDate settlementDate,
            final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        LocalDate effectiveFrom = settlementDate.plusDays(1);
        return createNewEntry(settlementDate, externalAssetOwnerTransfer, ExternalTransferStatus.ACTIVE, null, effectiveFrom,
                FUTURE_DATE_9999_12_31);
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
