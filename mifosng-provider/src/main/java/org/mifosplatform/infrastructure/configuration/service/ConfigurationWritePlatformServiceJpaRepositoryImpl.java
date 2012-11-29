package org.mifosplatform.infrastructure.configuration.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.configuration.command.CurrencyCommand;
import org.mifosplatform.infrastructure.configuration.domain.ApplicationCurrency;
import org.mifosplatform.infrastructure.configuration.domain.ApplicationCurrencyRepository;
import org.mifosplatform.infrastructure.office.domain.OrganisationCurrency;
import org.mifosplatform.infrastructure.office.domain.OrganisationCurrencyRepository;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.Permission;
import org.mifosplatform.infrastructure.user.domain.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigurationWritePlatformServiceJpaRepositoryImpl implements ConfigurationWritePlatformService {

    private final PlatformSecurityContext context;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;
    private final OrganisationCurrencyRepository organisationCurrencyRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public ConfigurationWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ApplicationCurrencyRepository applicationCurrencyRepository,
            final OrganisationCurrencyRepository organisationCurrencyRepository,
            final PermissionRepository permissionRepository) {
        this.context = context;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.organisationCurrencyRepository = organisationCurrencyRepository;
        this.permissionRepository = permissionRepository;
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
        
        final Permission thisTask = this.permissionRepository.findOneByCode("UPDATE_CURRENCY");
        if (thisTask.hasMakerCheckerEnabled() && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}