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
package org.apache.fineract.investor.external_assets_owner.service;

import static org.apache.fineract.investor.data.ExternalTransferStatus.ACTIVE_INTERMEDIATE;
import static org.apache.fineract.investor.data.ExternalTransferStatus.PENDING;
import static org.apache.fineract.investor.data.ExternalTransferStatus.PENDING_INTERMEDIATE;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanStatus.ACTIVE;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanStatus.CLOSED_OBLIGATIONS_MET;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanStatus.OVERPAID;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanStatus.TRANSFER_IN_PROGRESS;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanStatus.TRANSFER_ON_HOLD;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.cob.data.LoanDataForExternalTransfer;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerInitiateTransferException;
import org.apache.fineract.investor.external_assets_owner.data.BuyBackLoanExternalAssetRequest;
import org.apache.fineract.investor.external_assets_owner.data.CancelTransactionExternalAssetRequest;
import org.apache.fineract.investor.external_assets_owner.data.ExternalAssetOwnerResponse;
import org.apache.fineract.investor.external_assets_owner.data.IntermediarySaleLoanExternalAssetRequest;
import org.apache.fineract.investor.external_assets_owner.data.SaleLoanExternalAssetRequest;
import org.apache.fineract.investor.external_assets_owner.mapping.ExternalAssetOwnerMapper;
import org.apache.fineract.investor.service.DelayedSettlementAttributeService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAssetOwnersWriteServiceImpl implements ExternalAssetOwnersWriteService {

    private static final LocalDate FUTURE_DATE_9999_12_31 = LocalDate.of(9999, 12, 31);
    private static final List<LoanStatus> ACTIVE_LOAN_STATUSES = List.of(ACTIVE, TRANSFER_IN_PROGRESS, TRANSFER_ON_HOLD);
    private static final List<LoanStatus> VALID_DELAYED_SETTLEMENT_LOAN_STATUSES_BUYBACK_AND_SALE = List.of(ACTIVE, TRANSFER_IN_PROGRESS,
            TRANSFER_ON_HOLD, OVERPAID, CLOSED_OBLIGATIONS_MET);
    private static final List<ExternalTransferStatus> BUYBACK_READY_STATUSES = List.of(ExternalTransferStatus.PENDING,
            ExternalTransferStatus.ACTIVE);
    private static final List<ExternalTransferStatus> BUYBACK_READY_STATUSES_FOR_DELAY_SETTLEMENT = List
            .of(ExternalTransferStatus.ACTIVE_INTERMEDIATE, ExternalTransferStatus.ACTIVE);
    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalAssetOwnerRepository externalAssetOwnerRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepository loanRepository;
    private final DelayedSettlementAttributeService delayedSettlementAttributeService;
    private final ExternalAssetOwnerMapper externalAssetOwnerMapper;

    @Override
    @Transactional
    public ExternalAssetOwnerResponse intermediarySaleLoanByLoanId(IntermediarySaleLoanExternalAssetRequest request) {
        Long loanId = request.getLoanId();
        LoanDataForExternalTransfer loanDataForExternalTransfer = fetchAndValidateLoanDataForExternalTransfer(loanId);
        if (!delayedSettlementAttributeService.isEnabled(loanDataForExternalTransfer.getLoanProductId())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("Delayed Settlement Configuration is not enabled for the loan product: %s",
                            loanDataForExternalTransfer.getLoanProductShortName()));
        }
        ExternalId externalId = getTransferExternalIdFromJson(request.getTransferExternalId());
        validateExternalId(externalId);
        validateLoanStatusIntermediarySale(loanDataForExternalTransfer);
        ExternalAssetOwnerTransfer intermediarySaleTransfer = createIntermediarySaleTransfer(loanId, request,
                loanDataForExternalTransfer.getExternalId());
        validateIntermediarySale(intermediarySaleTransfer);
        externalAssetOwnerTransferRepository.saveAndFlush(intermediarySaleTransfer);
        return externalAssetOwnerMapper.map(intermediarySaleTransfer);
    }

    @Override
    @Transactional
    public ExternalAssetOwnerResponse saleLoanByLoanId(SaleLoanExternalAssetRequest request) {
        final LoanDataForExternalTransfer loanDataForExternalTransfer = fetchAndValidateLoanDataForExternalTransfer(request.getLoanId());
        final boolean isDelayedSettlementEnabled = delayedSettlementAttributeService
                .isEnabled(loanDataForExternalTransfer.getLoanProductId());
        ExternalId externalId = getTransferExternalIdFromJson(request.getTransferExternalId());
        validateExternalId(externalId);
        Long loanId = request.getLoanId();
        validateLoanStatus(loanDataForExternalTransfer, isDelayedSettlementEnabled);
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = createSaleTransfer(loanId, request,
                loanDataForExternalTransfer.getExternalId());
        validateSale(externalAssetOwnerTransfer, isDelayedSettlementEnabled);
        externalAssetOwnerTransferRepository.saveAndFlush(externalAssetOwnerTransfer);
        return externalAssetOwnerMapper.map(externalAssetOwnerTransfer);
    }

    @Override
    @Transactional
    public ExternalAssetOwnerResponse buybackLoanByLoanId(BuyBackLoanExternalAssetRequest request) {
        LoanDataForExternalTransfer loanDataForExternalTransfer = fetchAndValidateLoanDataForExternalTransfer(request.getLoanId());
        LocalDate settlementDate = DateUtils.parseLocalDate(request.getSettlementDate(), request.getDateFormat(),
                Locale.forLanguageTag(request.getLocale()));
        ExternalId externalId = getTransferExternalIdFromJson(request.getTransferExternalId());
        validateSettlementDate(settlementDate);
        validateExternalId(externalId);
        ExternalAssetOwnerTransfer effectiveTransfer = fetchAndValidateEffectiveTransferForBuyback(loanDataForExternalTransfer,
                settlementDate);
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = createBuybackTransfer(effectiveTransfer, settlementDate, externalId);
        externalAssetOwnerTransferRepository.saveAndFlush(externalAssetOwnerTransfer);
        return externalAssetOwnerMapper.map(externalAssetOwnerTransfer);
    }

    private void validateExternalId(ExternalId externalId) {
        boolean alreadyExists = externalAssetOwnerTransferRepository
                .exists((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("externalId"), externalId));
        if (alreadyExists) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("Already existing an asset transfer with the provided transfer external id: %s", externalId.getValue()));
        }
    }

    private LoanDataForExternalTransfer fetchAndValidateLoanDataForExternalTransfer(Long loanId) {
        return loanRepository.findLoanDataForExternalTransferByLoanId(loanId).orElseThrow(() -> new LoanNotFoundException(loanId));
    }

    @Override
    public ExternalAssetOwnerResponse cancelTransactionById(CancelTransactionExternalAssetRequest request) {
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = fetchAndValidateEffectiveTransferForCancel(request.getTransferId());
        externalAssetOwnerTransfer.setEffectiveDateTo(DateUtils.getBusinessLocalDate());
        ExternalAssetOwnerTransfer cancelTransfer = createCancelTransfer(externalAssetOwnerTransfer);
        externalAssetOwnerTransferRepository.save(cancelTransfer);
        externalAssetOwnerTransferRepository.save(externalAssetOwnerTransfer);
        return externalAssetOwnerMapper.map(cancelTransfer);
    }

    private void validateEffectiveTransferForSale(final List<ExternalAssetOwnerTransfer> effectiveTransfers) {
        if (effectiveTransfers.size() == 2) {
            throw new ExternalAssetOwnerInitiateTransferException("This loan cannot be sold, there is already an in progress transfer");
        } else if (effectiveTransfers.size() == 1) {
            if (PENDING.equals(effectiveTransfers.get(0).getStatus())) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        "External asset owner transfer is already in PENDING state for this loan");
            } else if (ExternalTransferStatus.ACTIVE.equals(effectiveTransfers.get(0).getStatus())) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        "This loan cannot be sold, because it is owned by an external asset owner");
            } else {
                throw new ExternalAssetOwnerInitiateTransferException(String.format(
                        "This loan cannot be sold, because it is incorrect state! (transferId = %s)", effectiveTransfers.get(0).getId()));
            }
        }
    }

    private void validateEffectiveTransferForDelayedSettlementSale(final List<ExternalAssetOwnerTransfer> effectiveTransfers) {
        if (effectiveTransfers.size() > 1) {
            throw new ExternalAssetOwnerInitiateTransferException("This loan cannot be sold, there is already an in progress transfer");
        } else if (effectiveTransfers.size() == 1) {
            if (!ACTIVE_INTERMEDIATE.equals(effectiveTransfers.get(0).getStatus())) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        "This loan cannot be sold, because it is not in ACTIVE-INTERMEDIATE state.");
            }
        } else {
            throw new ExternalAssetOwnerInitiateTransferException("This loan cannot be sold, no effective transfer found.");
        }
    }

    private void validateEffectiveTransferForIntermediarySale(final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        List<ExternalAssetOwnerTransfer> effectiveTransfers = externalAssetOwnerTransferRepository
                .findEffectiveTransfersOrderByIdDesc(externalAssetOwnerTransfer.getLoanId(), DateUtils.getBusinessLocalDate());

        if (effectiveTransfers.size() > 1) {
            throw new ExternalAssetOwnerInitiateTransferException("This loan cannot be sold, there is already an in progress transfer");
        } else if (effectiveTransfers.size() == 1) {
            if (PENDING_INTERMEDIATE.equals(effectiveTransfers.get(0).getStatus())) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        "External asset owner transfer is already in PENDING_INTERMEDIATE state for this loan");
            } else if (ExternalTransferStatus.ACTIVE.equals(effectiveTransfers.get(0).getStatus())) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        "This loan cannot be sold, because it is owned by an external asset owner");
            } else {
                throw new ExternalAssetOwnerInitiateTransferException(String.format(
                        "This loan cannot be sold, because it is incorrect state! (transferId = %s)", effectiveTransfers.get(0).getId()));
            }
        }
    }

    private ExternalAssetOwnerTransfer fetchAndValidateEffectiveTransferForBuyback(
            final LoanDataForExternalTransfer loanDataForExternalTransfer, final LocalDate settlementDate) {
        if (delayedSettlementAttributeService.isEnabled(loanDataForExternalTransfer.getLoanProductId())) {
            return fetchAndValidateEffectiveTransferForBuybackWithDelayedSettlement(loanDataForExternalTransfer, settlementDate);
        }

        List<ExternalAssetOwnerTransfer> effectiveTransfers = externalAssetOwnerTransferRepository
                .findEffectiveTransfersOrderByIdDesc(loanDataForExternalTransfer.getId(), DateUtils.getBusinessLocalDate());

        if (effectiveTransfers.size() == 0) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    "This loan cannot be bought back, it is not owned by an external asset owner");
        } else if (effectiveTransfers.size() == 2) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    "This loan cannot be bought back, external asset owner buyback transfer is already in progress");
        } else if (!BUYBACK_READY_STATUSES.contains(effectiveTransfers.get(0).getStatus())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("This loan cannot be bought back, effective transfer is not in right state: %s",
                            effectiveTransfers.get(0).getStatus()));
        } else if (DateUtils.isBefore(settlementDate, effectiveTransfers.get(0).getSettlementDate())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("This loan cannot be bought back, settlement date is earlier than effective transfer settlement date: %s",
                            effectiveTransfers.get(0).getSettlementDate()));
        }

        return effectiveTransfers.get(0);
    }

    private ExternalAssetOwnerTransfer fetchAndValidateEffectiveTransferForBuybackWithDelayedSettlement(
            final LoanDataForExternalTransfer loanDataForExternalTransfer, final LocalDate settlementDate) {
        List<ExternalAssetOwnerTransfer> effectiveTransfers = externalAssetOwnerTransferRepository
                .findEffectiveTransfersOrderByIdDesc(loanDataForExternalTransfer.getId(), DateUtils.getBusinessLocalDate());

        if (effectiveTransfers.isEmpty()) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    "This loan cannot be bought back, it is not owned by an external asset owner");
        }

        Set<ExternalTransferStatus> effectiveTransferStatuses = effectiveTransfers.stream().map(ExternalAssetOwnerTransfer::getStatus)
                .collect(Collectors.toSet());

        if (Set.of(ExternalTransferStatus.ACTIVE_INTERMEDIATE, ExternalTransferStatus.PENDING).equals(effectiveTransferStatuses)) {
            throw new ExternalAssetOwnerInitiateTransferException("This loan cannot be bought back, external asset owner sale is pending");
        } else if (Set.of(ExternalTransferStatus.ACTIVE_INTERMEDIATE, ExternalTransferStatus.BUYBACK_INTERMEDIATE)
                .equals(effectiveTransferStatuses)
                || Set.of(ExternalTransferStatus.ACTIVE, ExternalTransferStatus.BUYBACK).equals(effectiveTransferStatuses)) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    "This loan cannot be bought back, external asset owner buyback transfer is already in progress");
        } else if (!BUYBACK_READY_STATUSES_FOR_DELAY_SETTLEMENT.contains(effectiveTransfers.get(0).getStatus())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("This loan cannot be bought back, effective transfer is not in right state: %s",
                            effectiveTransfers.get(0).getStatus()));
        } else if (DateUtils.isBefore(settlementDate, effectiveTransfers.get(0).getSettlementDate())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("This loan cannot be bought back, settlement date is earlier than effective transfer settlement date: %s",
                            effectiveTransfers.get(0).getSettlementDate()));
        }

        return effectiveTransfers.get(0);
    }

    private ExternalAssetOwnerTransfer fetchAndValidateEffectiveTransferForCancel(final Long transferId) {
        ExternalAssetOwnerTransfer selectedTransfer = externalAssetOwnerTransferRepository.findById(transferId)
                .orElseThrow(() -> new ExternalAssetOwnerInitiateTransferException(
                        String.format("This loan cannot be cancelled, transfer with id %s does not exist", transferId)));

        List<ExternalAssetOwnerTransfer> effective = externalAssetOwnerTransferRepository
                .findEffectiveTransfersOrderByIdDesc(selectedTransfer.getLoanId(), DateUtils.getBusinessLocalDate());
        if (effective.isEmpty()) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("This loan cannot be cancelled, there is no effective transfer for this loan"));
        } else if (!Objects.equals(effective.get(0).getId(), selectedTransfer.getId())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("This loan cannot be cancelled, selected transfer is not the latest"));
        } else if (selectedTransfer.getStatus() != PENDING && selectedTransfer.getStatus() != ExternalTransferStatus.BUYBACK) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    "This loan cannot be cancelled, the selected transfer status is not pending or buyback");
        }
        return selectedTransfer;
    }

    private ExternalAssetOwnerTransfer createBuybackTransfer(ExternalAssetOwnerTransfer effectiveTransfer, LocalDate settlementDate,
            ExternalId externalId) {
        LocalDate effectiveDateFrom = DateUtils.getBusinessLocalDate();

        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();
        externalAssetOwnerTransfer.setExternalId(externalId);
        externalAssetOwnerTransfer.setOwner(effectiveTransfer.getOwner());
        externalAssetOwnerTransfer.setStatus(determineStatusAfterBuyback(effectiveTransfer));
        externalAssetOwnerTransfer.setLoanId(effectiveTransfer.getLoanId());
        externalAssetOwnerTransfer.setExternalLoanId(effectiveTransfer.getExternalLoanId());
        externalAssetOwnerTransfer.setSettlementDate(settlementDate);
        externalAssetOwnerTransfer.setEffectiveDateFrom(effectiveDateFrom);
        externalAssetOwnerTransfer.setEffectiveDateTo(FUTURE_DATE_9999_12_31);
        externalAssetOwnerTransfer.setPurchasePriceRatio(effectiveTransfer.getPurchasePriceRatio());
        return externalAssetOwnerTransfer;
    }

    private ExternalTransferStatus determineStatusAfterBuyback(ExternalAssetOwnerTransfer effectiveTransfer) {
        return switch (effectiveTransfer.getStatus()) {
            case PENDING -> ExternalTransferStatus.BUYBACK;
            case ACTIVE -> ExternalTransferStatus.BUYBACK;
            case ACTIVE_INTERMEDIATE -> ExternalTransferStatus.BUYBACK_INTERMEDIATE;
            default -> throw new ExternalAssetOwnerInitiateTransferException(String.format(
                    "This loan cannot be bought back, effective transfer is not in right state: %s", effectiveTransfer.getStatus()));
        };
    }

    private ExternalAssetOwnerTransfer createCancelTransfer(ExternalAssetOwnerTransfer effectiveTransfer) {
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();
        externalAssetOwnerTransfer.setExternalId(effectiveTransfer.getExternalId());
        externalAssetOwnerTransfer.setStatus(ExternalTransferStatus.CANCELLED);
        externalAssetOwnerTransfer.setSubStatus(ExternalTransferSubStatus.USER_REQUESTED);
        externalAssetOwnerTransfer.setLoanId(effectiveTransfer.getLoanId());
        externalAssetOwnerTransfer.setExternalLoanId(effectiveTransfer.getExternalLoanId());
        externalAssetOwnerTransfer.setExternalGroupId(effectiveTransfer.getExternalGroupId());
        externalAssetOwnerTransfer.setOwner(effectiveTransfer.getOwner());
        externalAssetOwnerTransfer.setSettlementDate(effectiveTransfer.getSettlementDate());
        externalAssetOwnerTransfer.setEffectiveDateFrom(effectiveTransfer.getEffectiveDateFrom());
        externalAssetOwnerTransfer.setEffectiveDateTo(effectiveTransfer.getEffectiveDateTo());
        externalAssetOwnerTransfer.setPurchasePriceRatio(effectiveTransfer.getPurchasePriceRatio());
        return externalAssetOwnerTransfer;
    }

    private void validateSale(ExternalAssetOwnerTransfer externalAssetOwnerTransfer, boolean isDelayedSettlementEnabled) {
        validateSettlementDate(externalAssetOwnerTransfer);

        final List<ExternalAssetOwnerTransfer> effectiveTransfers = externalAssetOwnerTransferRepository
                .findEffectiveTransfersOrderByIdDesc(externalAssetOwnerTransfer.getLoanId(), DateUtils.getBusinessLocalDate());

        if (isDelayedSettlementEnabled) {
            validateEffectiveTransferForDelayedSettlementSale(effectiveTransfers);
        } else {
            validateEffectiveTransferForSale(effectiveTransfers);
        }
    }

    private void validateIntermediarySale(ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        validateSettlementDate(externalAssetOwnerTransfer);
        validateEffectiveTransferForIntermediarySale(externalAssetOwnerTransfer);
    }

    private void validateSettlementDate(ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        validateSettlementDate(externalAssetOwnerTransfer.getSettlementDate());
    }

    private void validateSettlementDate(LocalDate settlementDate) {
        if (DateUtils.isBeforeBusinessDate(settlementDate)) {
            throw new ExternalAssetOwnerInitiateTransferException("Settlement date cannot be in the past");
        }
    }

    private void validateLoanStatus(LoanDataForExternalTransfer loanDataForExternalTransfer, boolean isDelayedSettlementEnabled) {
        LoanStatus loanStatus = loanDataForExternalTransfer.getLoanStatus();
        if (!getValidLoanStatusList(isDelayedSettlementEnabled).contains(loanStatus)) {
            throw new ExternalAssetOwnerInitiateTransferException(String.format("Loan status %s is not valid for transfer.", loanStatus));
        }
    }

    private void validateLoanStatusIntermediarySale(LoanDataForExternalTransfer loanDataForExternalTransfer) {
        LoanStatus loanStatus = loanDataForExternalTransfer.getLoanStatus();
        if (!ACTIVE_LOAN_STATUSES.contains(loanStatus)) {
            throw new ExternalAssetOwnerInitiateTransferException(String.format("Loan status %s is not valid for transfer.", loanStatus));
        }
    }

    private List<LoanStatus> getValidLoanStatusList(boolean isDelayedSettlementEnabled) {
        if (isDelayedSettlementEnabled) {
            return VALID_DELAYED_SETTLEMENT_LOAN_STATUSES_BUYBACK_AND_SALE;
        } else {
            return ACTIVE_LOAN_STATUSES;
        }
    }

    private ExternalAssetOwnerTransfer createSaleTransfer(Long loanId, SaleLoanExternalAssetRequest request, ExternalId externalLoanId) {
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();
        LocalDate effectiveFrom = ThreadLocalContextUtil.getBusinessDate();

        ExternalAssetOwner owner = getOwner(request.getOwnerExternalId());
        externalAssetOwnerTransfer.setOwner(owner);
        externalAssetOwnerTransfer.setExternalId(getTransferExternalIdFromJson(request.getTransferExternalId()));
        externalAssetOwnerTransfer.setStatus(PENDING);
        externalAssetOwnerTransfer.setPurchasePriceRatio(request.getPurchasePriceRatio());
        externalAssetOwnerTransfer.setSettlementDate(
                DateUtils.parseLocalDate(request.getSettlementDate(), request.getDateFormat(), Locale.forLanguageTag(request.getLocale())));
        externalAssetOwnerTransfer.setEffectiveDateFrom(effectiveFrom);
        externalAssetOwnerTransfer.setEffectiveDateTo(FUTURE_DATE_9999_12_31);
        externalAssetOwnerTransfer.setLoanId(loanId);
        externalAssetOwnerTransfer.setExternalLoanId(externalLoanId);
        externalAssetOwnerTransfer.setExternalGroupId(getTransferExternalGroupIdFromJson(request.getTransferExternalGroupId()));
        return externalAssetOwnerTransfer;
    }

    private ExternalAssetOwnerTransfer createIntermediarySaleTransfer(Long loanId, IntermediarySaleLoanExternalAssetRequest request,
            ExternalId externalLoanId) {
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();
        LocalDate effectiveFrom = ThreadLocalContextUtil.getBusinessDate();

        ExternalAssetOwner owner = getOwner(request.getOwnerExternalId());
        externalAssetOwnerTransfer.setOwner(owner);
        externalAssetOwnerTransfer.setExternalId(getTransferExternalIdFromJson(request.getTransferExternalId()));
        externalAssetOwnerTransfer.setStatus(PENDING_INTERMEDIATE);
        externalAssetOwnerTransfer.setPurchasePriceRatio(request.getPurchasePriceRatio());
        externalAssetOwnerTransfer.setSettlementDate(
                DateUtils.parseLocalDate(request.getSettlementDate(), request.getDateFormat(), Locale.forLanguageTag(request.getLocale())));
        externalAssetOwnerTransfer.setEffectiveDateFrom(effectiveFrom);
        externalAssetOwnerTransfer.setEffectiveDateTo(FUTURE_DATE_9999_12_31);
        externalAssetOwnerTransfer.setLoanId(loanId);
        externalAssetOwnerTransfer.setExternalLoanId(externalLoanId);
        externalAssetOwnerTransfer.setExternalGroupId(getTransferExternalGroupIdFromJson(request.getTransferExternalGroupId()));
        return externalAssetOwnerTransfer;
    }

    private ExternalId getTransferExternalIdFromJson(String transferExternalId) {
        return StringUtils.isEmpty(transferExternalId) ? ExternalId.generate() : ExternalIdFactory.produce(transferExternalId);
    }

    private ExternalId getTransferExternalGroupIdFromJson(String transferExternalGroupId) {
        return StringUtils.isEmpty(transferExternalGroupId) ? null : ExternalIdFactory.produce(transferExternalGroupId);
    }

    private ExternalAssetOwner getOwner(String ownerExternalId) {
        Optional<ExternalAssetOwner> byExternalId = externalAssetOwnerRepository
                .findByExternalId(ExternalIdFactory.produce(ownerExternalId));
        return byExternalId.orElseGet(() -> createAndGetAssetOwner(ownerExternalId));
    }

    private ExternalAssetOwner createAndGetAssetOwner(String externalId) {
        ExternalAssetOwner externalAssetOwner = new ExternalAssetOwner();
        externalAssetOwner.setExternalId(ExternalIdFactory.produce(externalId));
        return externalAssetOwnerRepository.saveAndFlush(externalAssetOwner);
    }
}
