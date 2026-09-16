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
import org.apache.fineract.organisation.monetary.data.CurrencyUpdateRequest;
import org.apache.fineract.organisation.monetary.data.CurrencyUpdateResponse;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrency;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepository;
import org.apache.fineract.organisation.monetary.exception.CurrencyInUseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CurrencyWritePlatformServiceJpaRepositoryImpl implements CurrencyWritePlatformService {

    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final OrganisationCurrencyRepository organisationCurrencyRepository;
    private final JdbcTemplate jdbcTemplate;

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
                if (isCurrencyUsedInExistingData(priorCurrency.getCode())) {
                    throw new CurrencyInUseException(priorCurrency.getCode());
                }
            }
        }

        organisationCurrencyRepository.deleteAll();
        organisationCurrencyRepository.saveAll(allowedCurrencies);

        return CurrencyUpdateResponse.builder().currencies(allowedCurrencyCodes).build();
    }

    private boolean isCurrencyUsedInExistingData(final String currencyCode) {
        return hasCurrencyUsage("m_product_loan", currencyCode) || hasCurrencyUsage("m_savings_product", currencyCode)
                || hasCurrencyUsage("m_share_product", currencyCode) || hasCurrencyUsage("m_charge", currencyCode)
                || hasCurrencyUsage("m_loan", currencyCode) || hasCurrencyUsage("m_savings_account", currencyCode)
                || hasCurrencyUsage("m_share_account", currencyCode) || hasCurrencyUsage("m_client_transaction", currencyCode)
                || hasCurrencyUsage("m_account_transfer_transaction", currencyCode)
                || hasCurrencyUsage("m_cashier_transactions", currencyCode) || hasCurrencyUsage("acc_gl_journal_entry", currencyCode);
    }

    private boolean hasCurrencyUsage(final String tableName, final String currencyCode) {
        final Integer usageCount = jdbcTemplate.queryForObject("select count(1) from " + tableName + " where currency_code = ?",
                Integer.class, currencyCode);
        return usageCount != null && usageCount > 0;
    }
}
