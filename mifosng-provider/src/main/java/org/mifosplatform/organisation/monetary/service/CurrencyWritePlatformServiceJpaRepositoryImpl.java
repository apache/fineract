package org.mifosplatform.organisation.monetary.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.command.CurrencyCommand;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.mifosplatform.organisation.office.domain.OrganisationCurrency;
import org.mifosplatform.organisation.office.domain.OrganisationCurrencyRepository;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrencyWritePlatformServiceJpaRepositoryImpl implements CurrencyWritePlatformService {

    private final PlatformSecurityContext context;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;
    private final OrganisationCurrencyRepository organisationCurrencyRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public CurrencyWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ApplicationCurrencyRepository applicationCurrencyRepository,
            final OrganisationCurrencyRepository organisationCurrencyRepository, final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.organisationCurrencyRepository = organisationCurrencyRepository;
        this.configurationDomainService = configurationDomainService;
    }

    @Transactional
    @Override
    public void updateAllowedCurrencies(final CurrencyCommand command) {

        context.authenticatedUser();

        List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("configuration");

        baseDataValidator.reset().parameter("currencies").value(command.getCurrencies()).arrayNotEmpty();

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }

        Set<OrganisationCurrency> allowedCurrencies = new HashSet<OrganisationCurrency>();

        for (String currencyCode : command.getCurrencies()) {

            ApplicationCurrency currency = this.applicationCurrencyRepository.findOneByCode(currencyCode);
            if (currency == null) {
                baseDataValidator.reset().parameter("currencies").value(command.getCurrencies())
                        .inValidValue("currency.code", currencyCode);
            } else {
                OrganisationCurrency allowedCurrency = new OrganisationCurrency(currency.getCode(), currency.getName(),
                        currency.getDecimalPlaces(), currency.getNameCode(), currency.getDisplaySymbol());

                allowedCurrencies.add(allowedCurrency);
            }
        }

        this.organisationCurrencyRepository.deleteAll();
        this.organisationCurrencyRepository.save(allowedCurrencies);

        if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_CURRENCY") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}