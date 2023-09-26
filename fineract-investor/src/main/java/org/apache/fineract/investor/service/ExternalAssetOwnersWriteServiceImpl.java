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

import static org.apache.fineract.investor.data.ExternalTransferStatus.PENDING;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanStatus.ACTIVE;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanStatus.TRANSFER_IN_PROGRESS;
import static org.apache.fineract.portfolio.loanaccount.domain.LoanStatus.TRANSFER_ON_HOLD;

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
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.cob.data.LoanIdAndExternalIdAndStatus;
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
    private static final List<LoanStatus> ACTIVE_LOAN_STATUSES = List.of(ACTIVE, TRANSFER_IN_PROGRESS, TRANSFER_ON_HOLD);
    private static final List<ExternalTransferStatus> BUYBACK_READY_STATUSES = List.of(ExternalTransferStatus.PENDING,
            ExternalTransferStatus.ACTIVE);
    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalAssetOwnerRepository externalAssetOwnerRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final LoanRepository loanRepository;

    @Override
    @Transactional
    public CommandProcessingResult saleLoanByLoanId(JsonCommand command) {
        final JsonElement json = fromApiJsonHelper.parse(command.json());
        validateSaleRequestBody(command.json());
        ExternalId externalId = getTransferExternalIdFromJson(json);
        validateExternalId(externalId);
        Long loanId = command.getLoanId();
        LoanIdAndExternalIdAndStatus loanIdAndExternalIdAndStatus = fetchLoanDetails(loanId);
        validateLoanStatus(loanIdAndExternalIdAndStatus);
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = createSaleTransfer(loanId, json,
                loanIdAndExternalIdAndStatus.getExternalId());
        validateSale(externalAssetOwnerTransfer);
        externalAssetOwnerTransferRepository.saveAndFlush(externalAssetOwnerTransfer);
        return buildResponseData(externalAssetOwnerTransfer);
    }

    @Override
    @Transactional
    public CommandProcessingResult buybackLoanByLoanId(JsonCommand command) {
        final JsonElement json = fromApiJsonHelper.parse(command.json());
        validateBuybackRequestBody(command.json());
        Long loanId = command.getLoanId();
        validateLoan(loanId);
        LocalDate settlementDate = getSettlementDateFromJson(json);
        ExternalId externalId = getTransferExternalIdFromJson(json);
        validateSettlementDate(settlementDate);
        validateExternalId(externalId);
        ExternalAssetOwnerTransfer effectiveTransfer = fetchAndValidateEffectiveTransferForBuyback(loanId, settlementDate);
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

    private void validateLoan(Long loanId) {
        if (!loanRepository.existsById(loanId)) {
            throw new LoanNotFoundException(loanId);
        }
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

    private void validateEffectiveTransferForSale(final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        List<ExternalAssetOwnerTransfer> effectiveTransfers = externalAssetOwnerTransferRepository
                .findEffectiveTransfersOrderByIdDesc(externalAssetOwnerTransfer.getLoanId(), DateUtils.getBusinessLocalDate());

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

    private ExternalAssetOwnerTransfer fetchAndValidateEffectiveTransferForBuyback(final Long loanId, final LocalDate settlementDate) {
        List<ExternalAssetOwnerTransfer> effectiveTransfers = externalAssetOwnerTransferRepository
                .findEffectiveTransfersOrderByIdDesc(loanId, DateUtils.getBusinessLocalDate());

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
        externalAssetOwnerTransfer.setStatus(ExternalTransferStatus.BUYBACK);
        externalAssetOwnerTransfer.setLoanId(effectiveTransfer.getLoanId());
        externalAssetOwnerTransfer.setExternalLoanId(effectiveTransfer.getExternalLoanId());
        externalAssetOwnerTransfer.setOwner(effectiveTransfer.getOwner());
        externalAssetOwnerTransfer.setSettlementDate(settlementDate);
        externalAssetOwnerTransfer.setEffectiveDateFrom(effectiveDateFrom);
        externalAssetOwnerTransfer.setEffectiveDateTo(FUTURE_DATE_9999_12_31);
        externalAssetOwnerTransfer.setPurchasePriceRatio(effectiveTransfer.getPurchasePriceRatio());
        return externalAssetOwnerTransfer;
    }

    private ExternalAssetOwnerTransfer createCancelTransfer(ExternalAssetOwnerTransfer effectiveTransfer) {
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();
        externalAssetOwnerTransfer.setExternalId(effectiveTransfer.getExternalId());
        externalAssetOwnerTransfer.setStatus(ExternalTransferStatus.CANCELLED);
        externalAssetOwnerTransfer.setSubStatus(ExternalTransferSubStatus.USER_REQUESTED);
        externalAssetOwnerTransfer.setLoanId(effectiveTransfer.getLoanId());
        externalAssetOwnerTransfer.setExternalLoanId(effectiveTransfer.getExternalLoanId());
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

    private void validateSale(ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        validateSettlementDate(externalAssetOwnerTransfer);
        validateEffectiveTransferForSale(externalAssetOwnerTransfer);
    }

    private void validateSettlementDate(ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        validateSettlementDate(externalAssetOwnerTransfer.getSettlementDate());
    }

    private void validateSettlementDate(LocalDate settlementDate) {
        if (DateUtils.isBeforeBusinessDate(settlementDate)) {
            throw new ExternalAssetOwnerInitiateTransferException("Settlement date cannot be in the past");
        }
    }

    private void validateLoanStatus(LoanIdAndExternalIdAndStatus entity) {
        if (!ACTIVE_LOAN_STATUSES.contains(LoanStatus.fromInt(entity.getLoanStatus()))) {
            throw new ExternalAssetOwnerInitiateTransferException("Loan is not in active status");
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
        return externalAssetOwnerTransfer;
    }

    private void validateSaleRequestBody(String apiRequestBodyAsJson) {
        final Set<String> requestParameters = new HashSet<>(
                Arrays.asList(ExternalTransferRequestParameters.SETTLEMENT_DATE, ExternalTransferRequestParameters.OWNER_EXTERNAL_ID,
                        ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID, ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO,
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

    private LoanIdAndExternalIdAndStatus fetchLoanDetails(Long loanId) {
        Optional<LoanIdAndExternalIdAndStatus> loanIdAndExternalIdAndStatusResult = loanRepository
                .findLoanIdAndExternalIdAndStatusByLoanId(loanId);
        return loanIdAndExternalIdAndStatusResult.orElseThrow(() -> new LoanNotFoundException(loanId));
    }
}
