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
package org.apache.fineract.organisation.monetary.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.organisation.monetary.data.CurrencyCreateRequest;
import org.apache.fineract.organisation.monetary.data.CurrencyCreateResponse;
import org.apache.fineract.organisation.monetary.data.CurrencyUpdateRequest;
import org.apache.fineract.organisation.monetary.data.CurrencyUpdateResponse;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrency;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepository;
import org.apache.fineract.organisation.monetary.exception.CurrencyInUseException;
import org.apache.fineract.organisation.monetary.exception.InvalidCurrencyException;
import org.apache.fineract.organisation.monetary.mapper.CurrencyMapper;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrencyWritePlatformServiceJpaRepositoryImpl implements CurrencyWritePlatformService {

    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final OrganisationCurrencyRepository organisationCurrencyRepository;
    private final LoanProductReadPlatformService loanProductService;
    private final SavingsProductReadPlatformService savingsProductService;
    private final ChargeReadPlatformService chargeService;
    private final CurrencyMapper currencyMapper;

    @Transactional
    @Override
    public CurrencyUpdateResponse updateAllowedCurrencies(final CurrencyUpdateRequest request) {
        final var currencies = request.getCurrencies();

        final List<String> allowedCurrencyCodes = new ArrayList<>();
        final Set<OrganisationCurrency> allowedCurrencies = new HashSet<>();
        for (final String currencyCode : currencies) {

            final ApplicationCurrency currency = applicationCurrencyRepository.findOneWithNotFoundDetection(currencyCode);

            final OrganisationCurrency allowedCurrency = currency.toOrganisationCurrency();

            allowedCurrencyCodes.add(currencyCode);
            allowedCurrencies.add(allowedCurrency);
        }

        for (OrganisationCurrency priorCurrency : organisationCurrencyRepository.findAll()) {
            if (!allowedCurrencyCodes.contains(priorCurrency.getCode())) {
                // check if it's safe to remove this currency.
                if (!loanProductService.retrieveAllLoanProductsForCurrency(priorCurrency.getCode()).isEmpty()
                        || !savingsProductService.retrieveAllForCurrency(priorCurrency.getCode()).isEmpty()
                        || !chargeService.retrieveAllChargesForCurrency(priorCurrency.getCode()).isEmpty()) {
                    throw new CurrencyInUseException(priorCurrency.getCode());
                }
            }
        }

        organisationCurrencyRepository.deleteAll();
        organisationCurrencyRepository.saveAll(allowedCurrencies);

        return CurrencyUpdateResponse.builder().currencies(allowedCurrencyCodes).build();
    }

    @Transactional
    @Override
    public CurrencyCreateResponse createCurrency(CurrencyCreateRequest request) {
        final ApplicationCurrency currency = currencyMapper.mapToEntity(request);
        currency.setNameCode(getNameCode(currency.getCode()));

        validateIfExistingCurrency(currency.getCode());

        final ApplicationCurrency savedResults = applicationCurrencyRepository.saveCurrency(currency);
        return generateResponse(savedResults);
    }

    private void validateIfExistingCurrency(String code) {
        // Check whether the currency exists
        if (applicationCurrencyRepository.existsByCode(code)) {
            final String errorMessage = "Error: Duplicate Currency. Request cannot be accepted as the"
                    + " currency is already present in the system.";
            throw new InvalidCurrencyException("existing", "currency.code", errorMessage, code);
        }
    }

    private String getNameCode(String code) {
        return "currency." + code;
    }

    private CurrencyCreateResponse generateResponse(ApplicationCurrency savedResults) {
        CurrencyCreateResponse response = currencyMapper.mapToResponse(savedResults);
        response.setDisplayLabel(computeDisplayLabel(savedResults));
        return response;
    }

    private String computeDisplayLabel(ApplicationCurrency entity) {
        StringBuilder builder = new StringBuilder(20);

        if (entity.getName() != null) {
            builder.append(entity.getName()).append(' ');
        }

        if (entity.getDisplaySymbol() != null && !entity.getDisplaySymbol().trim().isEmpty()) {
            builder.append('(').append(entity.getDisplaySymbol()).append(')');
        } else {
            builder.append('[').append(entity.getCode()).append(']');
        }
        return builder.toString();
    }
}
