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

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.data.LoanIdAndExternalIdData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.JsonParserHelper;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.investor.data.ExternalTransferRequestParameters;
import org.apache.fineract.investor.data.ExternalTransferStatus;
import org.apache.fineract.investor.domain.ExternalAssetOwner;
import org.apache.fineract.investor.domain.ExternalAssetOwnerRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransfer;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferLoanMappingRepository;
import org.apache.fineract.investor.domain.ExternalAssetOwnerTransferRepository;
import org.apache.fineract.investor.exception.ExternalAssetOwnerInitiateTransferException;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformServiceCommon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExternalAssetOwnersWriteServiceImpl implements ExternalAssetOwnersWriteService {

    private final ExternalAssetOwnerTransferRepository externalAssetOwnerTransferRepository;
    private final ExternalAssetOwnerTransferLoanMappingRepository externalAssetOwnerTransferLoanMappingRepository;
    private final ExternalAssetOwnerRepository externalAssetOwnerRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final LoanReadPlatformServiceCommon loanReadPlatformService;

    @Override
    @Transactional
    public CommandProcessingResult saleLoanByLoanId(JsonCommand command) {
        Long loanId = command.getLoanId();
        LoanIdAndExternalIdData loanIdAndExternalId = loanReadPlatformService.getTransferableLoanIdAndExternalId(loanId);
        validateLoanStatus(loanIdAndExternalId);
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = parseJson(loanId, command.json(), loanIdAndExternalId.getLoanExternalId(),
                ExternalTransferStatus.PENDING);
        validateSale(externalAssetOwnerTransfer);
        ExternalAssetOwnerTransfer savedExternalAssetOwnerTransfer = externalAssetOwnerTransferRepository.save(externalAssetOwnerTransfer);
        return buildResponseData(savedExternalAssetOwnerTransfer);
    }

    @Override
    @Transactional
    public CommandProcessingResult buybackLoanByLoanId(JsonCommand command) {
        Long loanId = command.getLoanId();
        LoanIdAndExternalIdData loanIdAndExternalId = loanReadPlatformService.getTransferableLoanIdAndExternalId(loanId);
        validateLoanStatus(loanIdAndExternalId);
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = parseJson(loanId, command.json(), loanIdAndExternalId.getLoanExternalId(),
                ExternalTransferStatus.BUYBACK);
        validateBuyBack(externalAssetOwnerTransfer);
        ExternalAssetOwnerTransfer savedExternalAssetOwnerTransfer = externalAssetOwnerTransferRepository.save(externalAssetOwnerTransfer);
        return buildResponseData(savedExternalAssetOwnerTransfer);
    }

    private CommandProcessingResult buildResponseData(ExternalAssetOwnerTransfer savedExternalAssetOwnerTransfer) {
        Map<String, Object> changes = new HashMap<>();
        changes.put(ExternalTransferRequestParameters.SETTLEMENT_DATE, savedExternalAssetOwnerTransfer.getSettlementDate());
        changes.put(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID,
                savedExternalAssetOwnerTransfer.getOwner().getExternalId().getValue());
        changes.put(ExternalTransferRequestParameters.TRANSFER_EXTERNAL_ID,
                savedExternalAssetOwnerTransfer.getOwner().getExternalId().getValue());
        changes.put(ExternalTransferRequestParameters.PURCHASE_PRICE_RATIO, savedExternalAssetOwnerTransfer.getPurchasePriceRatio());
        return new CommandProcessingResultBuilder().withEntityId(savedExternalAssetOwnerTransfer.getId())
                .withEntityExternalId(savedExternalAssetOwnerTransfer.getExternalId())
                .withSubEntityId(savedExternalAssetOwnerTransfer.getLoanId())
                .withSubEntityExternalId(Objects.isNull(savedExternalAssetOwnerTransfer.getExternalLoanId()) ? null
                        : savedExternalAssetOwnerTransfer.getExternalLoanId())
                .with(changes).build();
    }

    private void validateSale(ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        validateSettlementDate(externalAssetOwnerTransfer);
        validateTransferStatusForSale(externalAssetOwnerTransfer);
    }

    private void validateBuyBack(ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        validateSettlementDate(externalAssetOwnerTransfer);
        validateTransferStatusForBuyBack(externalAssetOwnerTransfer);
    }

    private void validateSettlementDate(ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        if (externalAssetOwnerTransfer.getSettlementDate().isBefore(ThreadLocalContextUtil.getBusinessDate())) {
            throw new ExternalAssetOwnerInitiateTransferException("Settlement date cannot be in the past");
        }
    }

    private void validateLoanStatus(LoanIdAndExternalIdData loanIdAndExternalIdAndExternalId) {
        if (Objects.isNull(loanIdAndExternalIdAndExternalId.getLoanId())
                && Objects.isNull(loanIdAndExternalIdAndExternalId.getLoanExternalId())) {
            throw new ExternalAssetOwnerInitiateTransferException("Loan is not in active status");
        }
    }

    private void validateTransferStatusForSale(ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        Optional<ExternalAssetOwnerTransfer> latestTransferOptional = externalAssetOwnerTransferRepository
                .findLatestByLoanId(externalAssetOwnerTransfer.getLoanId());
        if (latestTransferOptional.isPresent()) {
            ExternalAssetOwnerTransfer latestTransfer = latestTransferOptional.get();
            ExternalTransferStatus latestTransferStatus = latestTransfer.getStatus();
            if (latestTransferStatus.equals(ExternalTransferStatus.PENDING)) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        "External asset owner transfer is already in PENDING state for this loan.");
            } else if (latestTransferStatus.equals(ExternalTransferStatus.ACTIVE)) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        "This loan cannot be sold, because it is owned by an external asset owner.");
            }
        }
    }

    private void validateTransferStatusForBuyBack(ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        Optional<ExternalAssetOwnerTransfer> latestTransferOptional = externalAssetOwnerTransferRepository
                .findLatestByLoanId(externalAssetOwnerTransfer.getLoanId());
        if (latestTransferOptional.isEmpty()) {
            throw new ExternalAssetOwnerInitiateTransferException(
                    "This loan cannot be bought back, because it is not owned by an external asset owner");
        } else {
            ExternalAssetOwnerTransfer latestTransfer = latestTransferOptional.get();
            ExternalTransferStatus latestTransferStatus = latestTransfer.getStatus();
            if (latestTransferStatus.equals(ExternalTransferStatus.BUYBACK)) {
                throw new ExternalAssetOwnerInitiateTransferException(
                        "External asset owner transfer is already in BUYBACK state for this loan.");
            }
        }
    }

    private ExternalAssetOwnerTransfer parseJson(Long loanId, String apiRequestBodyAsJson, ExternalId externalLoanId,
            ExternalTransferStatus status) {
        ExternalAssetOwnerTransfer externalAssetOwnerTransfer = new ExternalAssetOwnerTransfer();

        validateRequestBody(apiRequestBodyAsJson);
        final JsonElement json = fromApiJsonHelper.parse(apiRequestBodyAsJson);

        ExternalAssetOwner owner = getOwner(json);
        externalAssetOwnerTransfer.setOwnerId(owner.getId());
        externalAssetOwnerTransfer.setOwner(owner);
        externalAssetOwnerTransfer.setExternalId(getTransferExternalIdFromJson(json));
        externalAssetOwnerTransfer.setStatus(status);
        externalAssetOwnerTransfer.setPurchasePriceRatio(getPurchasePriceRatioFromJson(json));
        externalAssetOwnerTransfer.setSettlementDate(getSettlementDateFromJson(json));
        externalAssetOwnerTransfer.setEffectiveDateFrom(ThreadLocalContextUtil.getBusinessDate());
        externalAssetOwnerTransfer.setEffectiveDateTo(LocalDate.of(9999, 12, 31));
        externalAssetOwnerTransfer.setLoanId(loanId);
        externalAssetOwnerTransfer.setExternalLoanId(externalLoanId);
        return externalAssetOwnerTransfer;
    }

    private void validateRequestBody(String apiRequestBodyAsJson) {
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
}
