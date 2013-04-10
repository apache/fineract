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
import org.mifosplatform.organisation.monetary.serialization.CurrencyCommandFromApiJsonDeserializer;
import org.mifosplatform.organisation.office.domain.OrganisationCurrency;
import org.mifosplatform.organisation.office.domain.OrganisationCurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrencyWritePlatformServiceJpaRepositoryImpl implements CurrencyWritePlatformService {

    private final PlatformSecurityContext context;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final OrganisationCurrencyRepository organisationCurrencyRepository;
    private final CurrencyCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public CurrencyWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final CurrencyCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final OrganisationCurrencyRepository organisationCurrencyRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.organisationCurrencyRepository = organisationCurrencyRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateAllowedCurrencies(final JsonCommand command) {

        context.authenticatedUser();

        this.fromApiJsonDeserializer.validateForUpdate(command.json());

        final String[] currencies = command.arrayValueOfParameterNamed("currencies");

        final Map<String, Object> changes = new LinkedHashMap<String, Object>();
        final List<String> allowedCurrencyCodes = new ArrayList<String>();
        final Set<OrganisationCurrency> allowedCurrencies = new HashSet<OrganisationCurrency>();
        for (final String currencyCode : currencies) {

            final ApplicationCurrency currency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currencyCode);

            final OrganisationCurrency allowedCurrency = currency.toOrganisationCurrency();

            allowedCurrencyCodes.add(currencyCode);
            allowedCurrencies.add(allowedCurrency);
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