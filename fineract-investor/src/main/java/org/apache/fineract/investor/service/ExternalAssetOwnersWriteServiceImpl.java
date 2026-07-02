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

import static org.apache.fineract.investor.data.ExternalTransferStatus.ACTIVE_INTERMEDIATE;
import static org.apache.fineract.investor.data.ExternalTransferStatus.PENDING;
import static org.apache.fineract.investor.data.ExternalTransferStatus.PENDING_INTERMEDIATE;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.cob.data.LoanDataForExternalTransfer;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.investor.data.ExternalAssetOwnerCreateResponse;
import org.apache.fineract.investor.data.ExternalAssetOwnerTransferResponse;
import org.apache.fineract.investor.data.ExternalTransferData;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerBuybackRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerCancelRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerCreateRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerIntermediarySaleRequest;
import org.apache.fineract.investor.data.request.ExternalAssetOwnerSaleRequest;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerDuplicateException;
import org.apache.fineract.investor.exception.ExternalAssetOwnerInitiateTransferException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(value = ExternalAssetOwnersWriteService.class, ignored = ExternalAssetOwnersWriteServiceImpl.class)
public class ExternalAssetOwnersWriteServiceImpl implements ExternalAssetOwnersWriteService {

    private static final LocalDate FUTURE_DATE_9999_12_31 = LocalDate.of(9999, 12, 31);
    private static final List<ExternalTransferStatus> BUYBACK_READY_STATUSES = List.of(ExternalTransferStatus.PENDING,
            ExternalTransferStatus.ACTIVE);
    private static final List<ExternalTransferStatus> BUYBACK_READY_STATUSES_FOR_DELAY_SETTLEMENT = List
            .of(ExternalTransferStatus.ACTIVE_INTERMEDIATE, ExternalTransferStatus.ACTIVE);
    private static final String SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION = "23";

    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalAssetOwnerRepository externalAssetOwnerRepository;
    private final LoanRepository loanRepository;
    private final DelayedSettlementAttributeService delayedSettlementAttributeService;
    private final ConfigurationDomainService configurationDomainService;
    private final ExternalAssetOwnersReadService externalAssetOwnersReadService;
    private final ExternalAssetOwnerHelper externalAssetOwnerHelper;

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

    private void validateEffectiveTransferForSale(final List<ExternalAssetOwnerTransfer> effectiveTransfers) {
        if (effectiveTransfers.size() == 2) {
            throw new ExternalAssetOwnerInitiateTransferException("This loan cannot be sold, there is already an in progress transfer");
        } else if (effectiveTransfers.size() == 1) {
            ExternalAssetOwnerTransfer transfer = effectiveTransfers.getFirst();
            ExternalTransferStatus transferStatus = transfer.getStatus();
            if (PENDING.equals(transferStatus)) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        "External asset owner transfer is already in PENDING state for this loan");
            }
            if (!ExternalTransferStatus.ACTIVE.equals(transferStatus)) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        String.format("This loan cannot be sold, because it is incorrect state! (transferId = %s)", transfer.getId()));
            }
        }
    }

    private void validateEffectiveTransferForDelayedSettlementSale(final List<ExternalAssetOwnerTransfer> effectiveTransfers) {
        if (effectiveTransfers.size() > 1) {
            throw new ExternalAssetOwnerInitiateTransferException("This loan cannot be sold, there is already an in progress transfer");
        } else if (effectiveTransfers.size() == 1) {
            if (!ACTIVE_INTERMEDIATE.equals(effectiveTransfers.getFirst().getStatus())) {
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
            ExternalAssetOwnerTransfer transfer = effectiveTransfers.getFirst();
            ExternalTransferStatus transferStatus = transfer.getStatus();
            if (PENDING_INTERMEDIATE.equals(transferStatus)) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        "External asset owner transfer is already in PENDING_INTERMEDIATE state for this loan");
            }
            if (!ExternalTransferStatus.ACTIVE.equals(transferStatus)) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        String.format("This loan cannot be sold, because it is incorrect state! (transferId = %s)", transfer.getId()));
            }
            // Owner-to-owner transfer with delayed settlement: allow intermediarySale when loan is currently
            // owned. The actual ownership switch happens atomically in the COB step.
        }
    }

    private ExternalAssetOwnerTransfer fetchAndValidateEffectiveTransferForBuyback(
            final LoanDataForExternalTransfer loanDataForExternalTransfer, final LocalDate settlementDate) {
        if (delayedSettlementAttributeService.isEnabled(loanDataForExternalTransfer.getLoanProductId())) {
            return fetchAndValidateEffectiveTransferForBuybackWithDelayedSettlement(loanDataForExternalTransfer, settlementDate);
        }

        List<ExternalAssetOwnerTransfer> effectiveTransfers = externalAssetOwnerTransferRepository
                .findEffectiveTransfersOrderByIdDesc(loanDataForExternalTransfer.getId(), DateUtils.getBusinessLocalDate());

        if (effectiveTransfers.isEmpty()) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    "This loan cannot be bought back, it is not owned by an external asset owner");
        } else if (effectiveTransfers.size() == 2) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    "This loan cannot be bought back, external asset owner buyback transfer is already in progress");
        } else if (!BUYBACK_READY_STATUSES.contains(effectiveTransfers.getFirst().getStatus())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("This loan cannot be bought back, effective transfer is not in right state: %s",
                            effectiveTransfers.getFirst().getStatus()));
        } else if (DateUtils.isBefore(settlementDate, effectiveTransfers.getFirst().getSettlementDate())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("This loan cannot be bought back, settlement date is earlier than effective transfer settlement date: %s",
                            effectiveTransfers.getFirst().getSettlementDate()));
        }

        return effectiveTransfers.getFirst();
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
        } else if (!BUYBACK_READY_STATUSES_FOR_DELAY_SETTLEMENT.contains(effectiveTransfers.getFirst().getStatus())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("This loan cannot be bought back, effective transfer is not in right state: %s",
                            effectiveTransfers.getFirst().getStatus()));
        } else if (DateUtils.isBefore(settlementDate, effectiveTransfers.getFirst().getSettlementDate())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("This loan cannot be bought back, settlement date is earlier than effective transfer settlement date: %s",
                            effectiveTransfers.getFirst().getSettlementDate()));
        }

        return effectiveTransfers.getFirst();
    }

    private ExternalAssetOwnerTransfer fetchAndValidateEffectiveTransferForCancel(final Long transferId) {
        ExternalAssetOwnerTransfer selectedTransfer = externalAssetOwnerTransferRepository.findById(transferId)
                .orElseThrow(() -> new ExternalAssetOwnerInitiateTransferException(
                        String.format("This loan cannot be cancelled, transfer with id %s does not exist", transferId)));

        List<ExternalAssetOwnerTransfer> effective = externalAssetOwnerTransferRepository
                .findEffectiveTransfersOrderByIdDesc(selectedTransfer.getLoanId(), DateUtils.getBusinessLocalDate());
        if (effective.isEmpty()) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    "This loan cannot be cancelled, there is no effective transfer for this loan");
        } else if (!Objects.equals(effective.getFirst().getId(), selectedTransfer.getId())) {
            throw new ExternalAssetOwnerInitiateTransferException("This loan cannot be cancelled, selected transfer is not the latest");
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
        externalAssetOwnerTransfer.setPreviousOwner(effectiveTransfer.getOwner());
        return externalAssetOwnerTransfer;
    }

    private ExternalTransferStatus determineStatusAfterBuyback(ExternalAssetOwnerTransfer effectiveTransfer) {
        return switch (effectiveTransfer.getStatus()) {
            case PENDING, ACTIVE -> ExternalTransferStatus.BUYBACK;
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
        if (!getAllowedLoanStatuses().contains(loanStatus)) {
            throw new ExternalAssetOwnerInitiateTransferException(String.format("Loan status %s is not valid for transfer.", loanStatus));
        }
    }

    private List<LoanStatus> getValidLoanStatusList(boolean isDelayedSettlementEnabled) {
        if (isDelayedSettlementEnabled) {
            return getAllowedLoanStatusesForDelayedSettlement();
        } else {
            return getAllowedLoanStatuses();
        }
    }

    private Optional<ExternalAssetOwner> findPreviousAssetOwner(final Long loanId) {
        final ExternalTransferData activeTransfer = externalAssetOwnersReadService.retrieveActiveTransferData(loanId, null, null);

        if (activeTransfer != null && activeTransfer.getOwner() != null) {
            final String activeOwnerExternalId = activeTransfer.getOwner().getExternalId();
            return externalAssetOwnerRepository.findByExternalId(ExternalIdFactory.produce(activeOwnerExternalId));
        }

        return Optional.empty();
    }

    private Long findOrCreateOwnerId(final ExternalId externalId) {
        try {
            return externalAssetOwnerHelper.findOrCreateId(externalId);
        } catch (JpaSystemException | DataIntegrityViolationException e) {
            if (!isConstraintViolation(e)) {
                throw e;
            }
            // Another thread created the owner concurrently - retry
            return externalAssetOwnerHelper.findOrCreateId(externalId);
        }
    }

    private boolean isConstraintViolation(final DataAccessException e) {
        return e.getMostSpecificCause() instanceof SQLException sqlEx && sqlEx.getSQLState() != null
                && sqlEx.getSQLState().startsWith(SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION);
    }

    private ExternalAssetOwner createAndGetAssetOwner(String externalId) {
        ExternalAssetOwner externalAssetOwner = new ExternalAssetOwner();
        externalAssetOwner.setExternalId(ExternalIdFactory.produce(externalId));
        return externalAssetOwnerRepository.saveAndFlush(externalAssetOwner);
    }

    private List<LoanStatus> getAllowedLoanStatuses() {
        return configurationDomainService.getAllowedLoanStatusesForExternalAssetTransfer().stream().map(LoanStatus::valueOf)
                .collect(Collectors.toList());
    }

    private List<LoanStatus> getAllowedLoanStatusesForDelayedSettlement() {
        return configurationDomainService.getAllowedLoanStatusesOfDelayedSettlementForExternalAssetTransfer().stream()
                .map(LoanStatus::valueOf).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExternalAssetOwnerTransferResponse saleLoan(ExternalAssetOwnerSaleRequest request) {
        final LocalDate settlementDate = parseDate(request.getSettlementDate(), request.getDateFormat(), request.getLocale());
        final ExternalId transferExternalId = resolveExternalId(request.getTransferExternalId());
        final ExternalId transferExternalGroupId = resolveGroupExternalId(request.getTransferExternalGroupId());
        final LoanDataForExternalTransfer loanData = fetchAndValidateLoanDataForExternalTransfer(request.getLoanId());
        final boolean isDelayedSettlement = delayedSettlementAttributeService.isEnabled(loanData.getLoanProductId());
        validateExternalId(transferExternalId);
        validateLoanStatus(loanData, isDelayedSettlement);
        final ExternalAssetOwnerTransfer transfer = buildSaleTransfer(request.getLoanId(), loanData.getExternalId(),
                request.getOwnerExternalId(), settlementDate, transferExternalId, transferExternalGroupId, request.getPurchasePriceRatio());
        validateSale(transfer, isDelayedSettlement);
        externalAssetOwnerTransferRepository.saveAndFlush(transfer);
        return toTransferResponse(transfer);
    }

    @Override
    @Transactional
    public ExternalAssetOwnerTransferResponse intermediarySaleLoan(ExternalAssetOwnerIntermediarySaleRequest request) {
        final LocalDate settlementDate = parseDate(request.getSettlementDate(), request.getDateFormat(), request.getLocale());
        final ExternalId transferExternalId = resolveExternalId(request.getTransferExternalId());
        final ExternalId transferExternalGroupId = resolveGroupExternalId(request.getTransferExternalGroupId());
        final LoanDataForExternalTransfer loanData = fetchAndValidateLoanDataForExternalTransfer(request.getLoanId());
        if (!delayedSettlementAttributeService.isEnabled(loanData.getLoanProductId())) {
            throw new ExternalAssetOwnerInitiateTransferException(String.format(
                    "Delayed Settlement Configuration is not enabled for the loan product: %s", loanData.getLoanProductShortName()));
        }
        validateExternalId(transferExternalId);
        validateLoanStatusIntermediarySale(loanData);
        final ExternalAssetOwnerTransfer transfer = buildIntermediarySaleTransfer(request.getLoanId(), loanData.getExternalId(),
                request.getOwnerExternalId(), settlementDate, transferExternalId, transferExternalGroupId, request.getPurchasePriceRatio());
        validateIntermediarySale(transfer);
        externalAssetOwnerTransferRepository.saveAndFlush(transfer);
        return toTransferResponse(transfer);
    }

    @Override
    @Transactional
    public ExternalAssetOwnerTransferResponse buybackLoan(ExternalAssetOwnerBuybackRequest request) {
        final LocalDate settlementDate = parseDate(request.getSettlementDate(), request.getDateFormat(), request.getLocale());
        final ExternalId transferExternalId = resolveExternalId(request.getTransferExternalId());
        final LoanDataForExternalTransfer loanData = fetchAndValidateLoanDataForExternalTransfer(request.getLoanId());
        validateSettlementDate(settlementDate);
        validateExternalId(transferExternalId);
        final ExternalAssetOwnerTransfer effectiveTransfer = fetchAndValidateEffectiveTransferForBuyback(loanData, settlementDate);
        final ExternalAssetOwnerTransfer transfer = createBuybackTransfer(effectiveTransfer, settlementDate, transferExternalId);
        externalAssetOwnerTransferRepository.saveAndFlush(transfer);
        return toTransferResponse(transfer);
    }

    @Override
    @Transactional
    public ExternalAssetOwnerTransferResponse cancelTransfer(ExternalAssetOwnerCancelRequest request) {
        final ExternalAssetOwnerTransfer transfer = fetchAndValidateEffectiveTransferForCancel(request.getTransferId());
        transfer.setEffectiveDateTo(DateUtils.getBusinessLocalDate());
        final ExternalAssetOwnerTransfer cancelTransfer = createCancelTransfer(transfer);
        externalAssetOwnerTransferRepository.save(cancelTransfer);
        externalAssetOwnerTransferRepository.save(transfer);
        return toTransferResponse(cancelTransfer);
    }

    @Override
    public ExternalAssetOwnerCreateResponse createOwner(ExternalAssetOwnerCreateRequest request) {
        final String ownerExternalId = request.getOwnerExternalId();
        final ExternalId externalId = ExternalIdFactory.produce(ownerExternalId);
        if (externalAssetOwnerRepository.findByExternalId(externalId).isPresent()) {
            throw new ExternalAssetOwnerDuplicateException(ownerExternalId);
        }
        final ExternalAssetOwner owner = createAndGetAssetOwner(ownerExternalId);
        return ExternalAssetOwnerCreateResponse.builder().resourceId(owner.getId()).build();
    }

    // Helper methods
    private LocalDate parseDate(String date, String dateFormat, String locale) {
        if (date == null) {
            return null;
        }
        return JsonParserHelper.convertFrom(date, "settlementDate", dateFormat != null ? dateFormat : "yyyy-MM-dd",
                JsonParserHelper.localeFromString(locale != null ? locale : "en"));
    }

    private ExternalId resolveExternalId(String id) {
        return StringUtils.isEmpty(id) ? ExternalId.generate() : ExternalIdFactory.produce(id);
    }

    private ExternalId resolveGroupExternalId(String id) {
        return StringUtils.isEmpty(id) ? null : ExternalIdFactory.produce(id);
    }

    private ExternalAssetOwnerTransfer buildSaleTransfer(Long loanId, ExternalId externalLoanId, String ownerExternalId,
            LocalDate settlementDate, ExternalId transferExternalId, ExternalId transferExternalGroupId, String purchasePriceRatio) {
        final ExternalAssetOwner owner = getOwnerByExternalId(ownerExternalId);
        final ExternalAssetOwnerTransfer transfer = new ExternalAssetOwnerTransfer();
        transfer.setOwner(owner);
        transfer.setExternalId(transferExternalId);
        transfer.setStatus(PENDING);
        transfer.setPurchasePriceRatio(purchasePriceRatio);
        transfer.setSettlementDate(settlementDate);
        transfer.setEffectiveDateFrom(ThreadLocalContextUtil.getBusinessDate());
        transfer.setEffectiveDateTo(FUTURE_DATE_9999_12_31);
        transfer.setLoanId(loanId);
        transfer.setExternalLoanId(externalLoanId);
        transfer.setExternalGroupId(transferExternalGroupId);
        findPreviousAssetOwner(loanId).ifPresent(transfer::setPreviousOwner);
        return transfer;
    }

    private ExternalAssetOwnerTransfer buildIntermediarySaleTransfer(Long loanId, ExternalId externalLoanId, String ownerExternalId,
            LocalDate settlementDate, ExternalId transferExternalId, ExternalId transferExternalGroupId, String purchasePriceRatio) {
        final ExternalAssetOwner owner = getOwnerByExternalId(ownerExternalId);
        final ExternalAssetOwnerTransfer transfer = new ExternalAssetOwnerTransfer();
        transfer.setOwner(owner);
        transfer.setExternalId(transferExternalId);
        transfer.setStatus(PENDING_INTERMEDIATE);
        transfer.setPurchasePriceRatio(purchasePriceRatio);
        transfer.setSettlementDate(settlementDate);
        transfer.setEffectiveDateFrom(ThreadLocalContextUtil.getBusinessDate());
        transfer.setEffectiveDateTo(FUTURE_DATE_9999_12_31);
        transfer.setLoanId(loanId);
        transfer.setExternalLoanId(externalLoanId);
        transfer.setExternalGroupId(transferExternalGroupId);
        findPreviousAssetOwner(loanId).ifPresent(transfer::setPreviousOwner);
        return transfer;
    }

    private ExternalAssetOwner getOwnerByExternalId(String ownerExternalId) {
        final ExternalId externalId = ExternalIdFactory.produce(ownerExternalId);
        return externalAssetOwnerRepository.findByExternalId(externalId).orElseGet(() -> {
            final Long ownerId = findOrCreateOwnerId(externalId);
            return externalAssetOwnerRepository.getReferenceById(ownerId);
        });
    }

    private ExternalAssetOwnerTransferResponse toTransferResponse(ExternalAssetOwnerTransfer transfer) {
        return ExternalAssetOwnerTransferResponse.builder().resourceId(transfer.getId())
                .resourceExternalId(transfer.getExternalId() != null ? transfer.getExternalId().getValue() : null)
                .subResourceId(transfer.getLoanId())
                .subResourceExternalId(transfer.getExternalLoanId() != null ? transfer.getExternalLoanId().getValue() : null).build();
    }

}
