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
package org.apache.fineract.portfolio.collateralmanagement.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.apache.fineract.portfolio.collateralmanagement.api.CollateralManagementJsonInputParams;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductCreateRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductResponse;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductUpdateRequest;
import org.apache.fineract.portfolio.collateralmanagement.data.CollateralManagementProductUpdateResponse;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementDomain;
import org.apache.fineract.portfolio.collateralmanagement.domain.CollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.exception.CollateralCannotBeDeletedException;
import org.apache.fineract.portfolio.collateralmanagement.exception.CollateralNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CollateralManagementWritePlatformServiceImpl implements CollateralManagementWritePlatformService {

    private final CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;
    private final FromJsonHelper fromApiJsonHelper;

    @Transactional
    @Override
    public CollateralManagementProductResponse createCollateral(CollateralManagementProductCreateRequest request) {
        String currencyCode = request.getCurrency(); // example = "USD"
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(currencyCode);

        CollateralManagementDomain collateral = new CollateralManagementDomain(request.getQuality(), request.getBasePrice(),
                request.getUnitType(), request.getPctToBase(), applicationCurrency, request.getName());

        this.collateralManagementRepositoryWrapper.create(collateral);
        return CollateralManagementProductResponse.builder().resourceId(collateral.getId()).entityId(collateral.getId()).build();
    }

    @Transactional
    @Override
    public CollateralManagementProductUpdateResponse updateCollateral(final Long collateralId,
            CollateralManagementProductUpdateRequest request) {

        final CollateralManagementDomain collateral = this.collateralManagementRepositoryWrapper.getCollateral(collateralId);

        String currencyCode = request.getCurrency(); // example = "USD"
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneByCode(currencyCode);

        if (!currencyCode.equals(applicationCurrency.getCode())) {
            final String newValue = currencyCode;
            applicationCurrency.setCode(newValue);
        }

        final Map<String, Object> changes = update(collateral, request, applicationCurrency);
        this.collateralManagementRepositoryWrapper.update(collateral);

        return CollateralManagementProductUpdateResponse.builder().resourceId(collateral.getId()).entityId(collateral.getId())
                .changes(changes).build();
    }

    @Transactional
    @Override
    public CollateralManagementProductResponse deleteCollateral(final Long collateralId) {
        final CollateralManagementDomain collateralManagementDomain = this.collateralManagementRepositoryWrapper
                .getCollateral(collateralId);
        validateForDeletion(collateralManagementDomain, collateralId);

        this.collateralManagementRepositoryWrapper.delete(collateralId);

        return CollateralManagementProductResponse.builder().resourceId(collateralId).entityId(collateralId).build();
    }

    private void validateForDeletion(final CollateralManagementDomain collateralManagementDomain, final Long collateralId) {
        if (collateralManagementDomain == null) {
            throw new CollateralNotFoundException(collateralId);
        }

        if (collateralManagementDomain.getClientCollateralManagements().size() > 0) {
            for (ClientCollateralManagement clientCollateralManagement : collateralManagementDomain.getClientCollateralManagements()) {
                if (clientCollateralManagement.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                    throw new CollateralCannotBeDeletedException(
                            CollateralCannotBeDeletedException.CollateralCannotBeDeletedReason.COLLATERAL_IS_ALREADY_ATTACHED,
                            collateralId);
                }
            }
        }
    }

    private Map<String, Object> update(final CollateralManagementDomain collateral,
            final CollateralManagementProductUpdateRequest requestDto, final ApplicationCurrency applicationCurrency) {

        final Map<String, Object> changes = new LinkedHashMap<>(6);

        final String nameParamName = CollateralManagementJsonInputParams.NAME.getValue();
        String name = requestDto.getName();
        if (!name.equals(collateral.getName())) {
            collateral.setName(StringUtils.defaultIfEmpty(name, null));
            changes.put(nameParamName, collateral.getName());
        }

        final String qualityParamName = CollateralManagementJsonInputParams.QUALITY.getValue();
        String quality = requestDto.getQuality();
        if (!quality.equals(collateral.getQuality())) {
            collateral.setQuality(StringUtils.defaultIfEmpty(quality, null));
            changes.put(qualityParamName, collateral.getQuality());
        }

        final String unitTypeParamName = CollateralManagementJsonInputParams.UNIT_TYPE.getValue();
        String unitType = requestDto.getUnitType();
        if (!unitType.equals(collateral.getUnitType())) {
            collateral.setUnitType(StringUtils.defaultIfEmpty(unitType, null));
            changes.put(unitTypeParamName, collateral.getUnitType());
        }

        collateral.setCurrency(applicationCurrency);

        final String basePriceParamName = CollateralManagementJsonInputParams.BASE_PRICE.getValue();
        BigDecimal basePrice = requestDto.getBasePrice();
        if (basePrice.compareTo(collateral.getBasePrice()) != 0) {
            collateral.setBasePrice(basePrice);
            changes.put(basePriceParamName, collateral.getBasePrice());
        }

        final String pctToBaseParamName = CollateralManagementJsonInputParams.PCT_TO_BASE.getValue();
        BigDecimal pctToBase = requestDto.getPctToBase();
        if (pctToBase.compareTo(collateral.getPctToBase()) != 0) {
            collateral.setPctToBase(pctToBase);
            changes.put(pctToBaseParamName, collateral.getPctToBase());
        }

        return changes;
    }
}
