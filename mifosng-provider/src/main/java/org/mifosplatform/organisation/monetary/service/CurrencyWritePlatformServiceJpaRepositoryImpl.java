/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.monetary.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.mifosplatform.organisation.monetary.exception.CurrencyInUseException;
import org.mifosplatform.organisation.monetary.serialization.CurrencyCommandFromApiJsonDeserializer;
import org.mifosplatform.organisation.office.domain.OrganisationCurrency;
import org.mifosplatform.organisation.office.domain.OrganisationCurrencyRepository;
import org.mifosplatform.portfolio.charge.service.ChargeReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.mifosplatform.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrencyWritePlatformServiceJpaRepositoryImpl implements CurrencyWritePlatformService {

    private final PlatformSecurityContext context;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final OrganisationCurrencyRepository organisationCurrencyRepository;
    private final CurrencyCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final LoanProductReadPlatformService loanProductService;
    private final SavingsProductReadPlatformService savingsProductService;
    private final ChargeReadPlatformService chargeService;

    @Autowired
    public CurrencyWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final CurrencyCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final OrganisationCurrencyRepository organisationCurrencyRepository, final LoanProductReadPlatformService loanProductService,
            final SavingsProductReadPlatformService savingsProductService, final ChargeReadPlatformService chargeService) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.organisationCurrencyRepository = organisationCurrencyRepository;
        this.loanProductService = loanProductService;
        this.savingsProductService = savingsProductService;
        this.chargeService = chargeService;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateAllowedCurrencies(final JsonCommand command) {

        this.context.authenticatedUser();

        this.fromApiJsonDeserializer.validateForUpdate(command.json());

        final String[] currencies = command.arrayValueOfParameterNamed("currencies");

        final Map<String, Object> changes = new LinkedHashMap<>();
        final List<String> allowedCurrencyCodes = new ArrayList<>();
        final Set<OrganisationCurrency> allowedCurrencies = new HashSet<>();
        for (final String currencyCode : currencies) {

            final ApplicationCurrency currency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currencyCode);

            final OrganisationCurrency allowedCurrency = currency.toOrganisationCurrency();

            allowedCurrencyCodes.add(currencyCode);
            allowedCurrencies.add(allowedCurrency);
        }

        for (OrganisationCurrency priorCurrency : this.organisationCurrencyRepository.findAll()) {
            if (!allowedCurrencyCodes.contains(priorCurrency.getCode())) {
                // Check if it's safe to remove this currency.
                if (!loanProductService.retrieveAllLoanProductsForCurrency(priorCurrency.getCode()).isEmpty()
                        || !savingsProductService.retrieveAllForCurrency(priorCurrency.getCode()).isEmpty()
                        || !chargeService.retrieveAllChargesForCurrency(priorCurrency.getCode()).isEmpty()) { throw new CurrencyInUseException(
                        priorCurrency.getCode()); }
            }
        }

        changes.put("currencies", allowedCurrencyCodes.toArray(new String[allowedCurrencyCodes.size()]));

        this.organisationCurrencyRepository.deleteAll();
        this.organisationCurrencyRepository.save(allowedCurrencies);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .with(changes) //
                .build();
    }
}