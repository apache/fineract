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

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.cob.data.LoanDataForExternalTransfer;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.investor.data.ExternalTransferRequestParameters;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.data.ExternalTransferSubStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerInitiateTransferException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAssetOwnersWriteServiceImpl implements ExternalAssetOwnersWriteService {

    private static final LocalDate FUTURE_DATE_9999_12_31 = LocalDate.of(9999, 12, 31);
    private static final List<ExternalTransferStatus> BUYBACK_READY_STATUSES = List.of(ExternalTransferStatus.PENDING,
            ExternalTransferStatus.ACTIVE);
    private static final List<ExternalTransferStatus> BUYBACK_READY_STATUSES_FOR_DELAY_SETTLEMENT = List
            .of(ExternalTransferStatus.ACTIVE_INTERMEDIATE, ExternalTransferStatus.ACTIVE);
    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalAssetOwnerRepository externalAssetOwnerRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepository loanRepository;
    private final DelayedSettlementAttributeService delayedSettlementAttributeService;
    private final ConfigurationDomainService configurationDomainService;

    @Override
    @Transactional
    public CommandProcessingResult intermediarySaleLoanByLoanId(JsonCommand command) {
        final JsonElement json = fromApiJsonHelper.parse(command.json());
        validateIntermediarySaleRequestBody(command.json());
        Long loanId = command.getLoanId();
        LoanDataForExternalTransfer loanDataForExternalTransfer = fetchAndValidateLoanDataForExternalTransfer(loanId);
        if (!delayedSettlementAttributeService.isEnabled(loanDataForExternalTransfer.getLoanProductId())) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    String.format("Delayed Settlement Configuration is not enabled for the loan product: %s",
                            loanDataForExternalTransfer.getLoanProductShortName()));
        }
        ExternalId externalId = getTransferExternalIdFromJson(json);
        validateExternalId(externalId);
        validateLoanStatusIntermediarySale(loanDataForExternalTransfer);
        ExternalAssetOwnerTransfer intermediarySaleTransfer = createIntermediarySaleTransfer(loanId, json,
                loanDataForExternalTransfer.getExternalId());
        validateIntermediarySale(intermediarySaleTransfer);
        externalAssetOwnerTransferRepository.saveAndFlush(intermediarySaleTransfer);
        return buildResponseData(intermediarySaleTransfer);
    }

    @Override
    @Transactional
    public CommandProcessingResult saleLoanByLoanId(JsonCommand command) {
        final JsonElement json = fromApiJsonHelper.parse(command.json());
        final LoanDataForExternalTransfer loanDataForExternalTransfer = fetchAndValidateLoanDataForExternalTransfer(command.getLoanId());
        final boolean isDelayedSettlementEnabled = delayedSettlementAttributeService
                .isEnabled(loanDataForExternalTransfer.getLoanProductId());
        validateSaleRequestBody(command.json());
        ExternalId externalId = getTransferExternalIdFromJson(json);
        validateExternalId(externalId);
        Long loanId = command.getLoanId();
        validateLoanStatus(loanDataForExternalTransfer, isDelayedSettlementEnabled);
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = createSaleTransfer(loanId, json,
                loanDataForExternalTransfer.getExternalId());
        validateSale(externalAssetOwnerTransfer, isDelayedSettlementEnabled);
        externalAssetOwnerTransferRepository.saveAndFlush(externalAssetOwnerTransfer);
        return buildResponseData(externalAssetOwnerTransfer);
    }

    @Override
    @Transactional
    public CommandProcessingResult buybackLoanByLoanId(JsonCommand command) {
        final JsonElement json = fromApiJsonHelper.parse(command.json());
        validateBuybackRequestBody(command.json());
        LoanDataForExternalTransfer loanDataForExternalTransfer = fetchAndValidateLoanDataForExternalTransfer(command.getLoanId());
        LocalDate settlementDate = getSettlementDateFromJson(json);
        ExternalId externalId = getTransferExternalIdFromJson(json);
        validateSettlementDate(settlementDate);
        validateExternalId(externalId);
        ExternalAssetOwnerTransfer effectiveTransfer = fetchAndValidateEffectiveTransferForBuyback(loanDataForExternalTransfer,
                settlementDate);
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = createBuybackTransfer(effectiveTransfer, settlementDate, externalId);
        externalAssetOwnerTransferRepository.saveAndFlush(externalAssetOwnerTransfer);
        return buildResponseData(externalAssetOwnerTransfer);
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
    public CommandProcessingResult cancelTransactionById(JsonCommand command) {
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = fetchAndValidateEffectiveTransferForCancel(command.entityId());
        externalAssetOwnerTransfer.setEffectiveDateTo(DateUtils.getBusinessLocalDate());
        ExternalAssetOwnerTransfer cancelTransfer = createCancelTransfer(externalAssetOwnerTransfer);
        externalAssetOwnerTransferRepository.save(cancelTransfer);
        externalAssetOwnerTransferRepository.save(externalAssetOwnerTransfer);
        return buildResponseData(cancelTransfer);
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

    private CommandProcessingResult buildResponseData(ExternalAssetOwnerTransfer savedExternalAssetOwnerTransfer) {
        return new CommandProcessingResultBuilder().withEntityId(savedExternalAssetOwnerTransfer.getId())
                .withEntityExternalId(savedExternalAssetOwnerTransfer.getExternalId())
                .withSubEntityId(savedExternalAssetOwnerTransfer.getLoanId())
                .withSubEntityExternalId(Objects.isNull(savedExternalAssetOwnerTransfer.getExternalLoanId()) ? null
                        : savedExternalAssetOwnerTransfer.getExternalLoanId())
                .build();
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

    private ExternalAssetOwnerTransfer createSaleTransfer(Long loanId, JsonElement json, ExternalId externalLoanId) {
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();
        LocalDate effectiveFrom = ThreadLocalContextUtil.getBusinessDate();

        ExternalAssetOwner owner = getOwner(json);
        externalAssetOwnerTransfer.setOwner(owner);
        externalAssetOwnerTransfer.setExternalId(getTransferExternalIdFromJson(json));
        externalAssetOwnerTransfer.setStatus(PENDING);
        externalAssetOwnerTransfer.setPurchasePriceRatio(getPurchasePriceRatioFromJson(json));
        externalAssetOwnerTransfer.setSettlementDate(getSettlementDateFromJson(json));
        externalAssetOwnerTransfer.setEffectiveDateFrom(effectiveFrom);
        externalAssetOwnerTransfer.setEffectiveDateTo(FUTURE_DATE_9999_12_31);
        externalAssetOwnerTransfer.setLoanId(loanId);
        externalAssetOwnerTransfer.setExternalLoanId(externalLoanId);
        externalAssetOwnerTransfer.setExternalGroupId(getTransferExternalGroupIdFromJson(json));
        return externalAssetOwnerTransfer;
    }

    private ExternalAssetOwnerTransfer createIntermediarySaleTransfer(Long loanId, JsonElement json, ExternalId externalLoanId) {
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();
        LocalDate effectiveFrom = ThreadLocalContextUtil.getBusinessDate();

        ExternalAssetOwner owner = getOwner(json);
        externalAssetOwnerTransfer.setOwner(owner);
        externalAssetOwnerTransfer.setExternalId(getTransferExternalIdFromJson(json));
        externalAssetOwnerTransfer.setStatus(PENDING_INTERMEDIATE);
        externalAssetOwnerTransfer.setPurchasePriceRatio(getPurchasePriceRatioFromJson(json));
        externalAssetOwnerTransfer.setSettlementDate(getSettlementDateFromJson(json));
        externalAssetOwnerTransfer.setEffectiveDateFrom(effectiveFrom);
        externalAssetOwnerTransfer.setEffectiveDateTo(FUTURE_DATE_9999_12_31);
        externalAssetOwnerTransfer.setLoanId(loanId);
        externalAssetOwnerTransfer.setExternalLoanId(externalLoanId);
        externalAssetOwnerTransfer.setExternalGroupId(getTransferExternalGroupIdFromJson(json));
        return externalAssetOwnerTransfer;
    }

    private void validateSaleRequestBody(String apiRequestBodyAsJson) {
        final Set<String> requestParameters = new HashSet<>(Arrays.asList(ExternalTransferRequestParameters.SETTLEMENT_DATE,
                ExternalTransferRequestParameters.OWNER_EXTERNAL_ID, ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID,
                ExternalTransferRequestParameters.TRANSFER_EXTERNAL_GROUP_ID, ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO,
                ExternalTransferRequestParameters.DATEFORMAT, ExternalTransferRequestParameters.LOCALE));
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, apiRequestBodyAsJson, requestParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loantransfer");
        final JsonElement json = fromApiJsonHelper.parse(apiRequestBodyAsJson);

        String ownerExternalId = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID).value(ownerExternalId).notBlank()
                .notExceedingLengthOf(100);

        String transferExternalId = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID).value(transferExternalId).ignoreIfNull()
                .notExceedingLengthOf(100);

        String purchasePriceRatio = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO).value(purchasePriceRatio).notBlank()
                .notExceedingLengthOf(50);

        LocalDate settlementDate = fromApiJsonHelper.extractLocalDateNamed(ExternalTransferRequestParameters.SETTLEMENT_DATE, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.SETTLEMENT_DATE).value(settlementDate).notNull();

        final String transferExternalGroupId = fromApiJsonHelper
                .extractStringNamed(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_GROUP_ID, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_GROUP_ID).value(transferExternalGroupId)
                .ignoreIfNull().notExceedingLengthOf(100);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private void validateIntermediarySaleRequestBody(String apiRequestBodyAsJson) {
        final Set<String> requestParameters = new HashSet<>(Arrays.asList(ExternalTransferRequestParameters.SETTLEMENT_DATE,
                ExternalTransferRequestParameters.OWNER_EXTERNAL_ID, ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID,
                ExternalTransferRequestParameters.TRANSFER_EXTERNAL_GROUP_ID, ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO,
                ExternalTransferRequestParameters.DATEFORMAT, ExternalTransferRequestParameters.LOCALE));
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, apiRequestBodyAsJson, requestParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loantransfer");
        final JsonElement json = fromApiJsonHelper.parse(apiRequestBodyAsJson);

        String ownerExternalId = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID).value(ownerExternalId).notBlank()
                .notExceedingLengthOf(100);

        String transferExternalId = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID).value(transferExternalId).ignoreIfNull()
                .notExceedingLengthOf(100);

        String purchasePriceRatio = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO).value(purchasePriceRatio).notBlank()
                .notExceedingLengthOf(50);

        LocalDate settlementDate = fromApiJsonHelper.extractLocalDateNamed(ExternalTransferRequestParameters.SETTLEMENT_DATE, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.SETTLEMENT_DATE).value(settlementDate).notNull();

        String transferExternalGroupId = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_GROUP_ID,
                json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_GROUP_ID).value(transferExternalGroupId)
                .ignoreIfNull().notExceedingLengthOf(100);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private void validateBuybackRequestBody(String apiRequestBodyAsJson) {
        final Set<String> requestParameters = new HashSet<>(
                Arrays.asList(ExternalTransferRequestParameters.SETTLEMENT_DATE, ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID,
                        ExternalTransferRequestParameters.DATEFORMAT, ExternalTransferRequestParameters.LOCALE));
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {

        }.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, apiRequestBodyAsJson, requestParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loantransfer");
        final JsonElement json = fromApiJsonHelper.parse(apiRequestBodyAsJson);

        String transferExternalId = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID).value(transferExternalId).ignoreIfNull()
                .notExceedingLengthOf(100);

        LocalDate settlementDate = fromApiJsonHelper.extractLocalDateNamed(ExternalTransferRequestParameters.SETTLEMENT_DATE, json);
        baseDataValidator.reset().parameter(ExternalTransferRequestParameters.SETTLEMENT_DATE).value(settlementDate).notNull();

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    private LocalDate getSettlementDateFromJson(JsonElement json) {
        String dateFormat = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.DATEFORMAT, json);
        String locale = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.LOCALE, json);
        return fromApiJsonHelper.extractLocalDateNamed(ExternalTransferRequestParameters.SETTLEMENT_DATE, json, dateFormat,
                JsonParserHelper.localeFromString(locale));
    }

    private ExternalId getTransferExternalIdFromJson(JsonElement json) {
        String transferExternalId = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID, json);
        return StringUtils.isEmpty(transferExternalId) ? ExternalId.generate() : ExternalIdFactory.produce(transferExternalId);
    }

    private ExternalId getTransferExternalGroupIdFromJson(JsonElement json) {
        String transferExternalGroupId = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_GROUP_ID,
                json);
        return StringUtils.isEmpty(transferExternalGroupId) ? null : ExternalIdFactory.produce(transferExternalGroupId);
    }

    private String getPurchasePriceRatioFromJson(JsonElement json) {
        return fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO, json);
    }

    private ExternalAssetOwner getOwner(JsonElement json) {
        String ownerExternalId = fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID, json);
        Optional<ExternalAssetOwner> byExternalId = externalAssetOwnerRepository
                .findByExternalId(ExternalIdFactory.produce(ownerExternalId));
        return byExternalId.orElseGet(() -> createAndGetAssetOwner(ownerExternalId));
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
}
