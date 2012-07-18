package org.mifosng.platform.configuration.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mifosng.platform.DataValidatorBuilder;
import org.mifosng.platform.api.commands.OrganisationCurrencyCommand;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.ApplicationCurrencyRepository;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.mifosng.platform.organisation.domain.OrganisationCurrency;
import org.mifosng.platform.organisation.domain.OrganisationCurrencyRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfigurationWritePlatformServiceJpaRepositoryImpl implements ConfigurationWritePlatformService {

	private final PlatformSecurityContext context;
	private final ApplicationCurrencyRepository applicationCurrencyRepository;
	private final OrganisationCurrencyRepository organisationCurrencyRepository;

	@Autowired
	public ConfigurationWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, 
			final ApplicationCurrencyRepository applicationCurrencyRepository, final OrganisationCurrencyRepository organisationCurrencyRepository) {
		this.context = context;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.organisationCurrencyRepository = organisationCurrencyRepository;
	}
	
	@Transactional
	@Override
	public void updateOrganisationCurrencies(final OrganisationCurrencyCommand command) {
		
		context.authenticatedUser();
		
		List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("configuration");
		
		baseDataValidator.reset().parameter("currencies").value(command.getCurrencies()).arrayNotEmpty();
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
		
		Set<OrganisationCurrency> allowedCurrencies = new HashSet<OrganisationCurrency>();

		for (String currencyCode : command.getCurrencies()) {

			ApplicationCurrency currency = this.applicationCurrencyRepository.findOneByCode(currencyCode);
			if (currency == null) {
				baseDataValidator.reset().parameter("currencies").value(command.getCurrencies()).inValidValue("currency.code", currencyCode);
			} else {
				OrganisationCurrency allowedCurrency = new OrganisationCurrency(
						currency.getCode(), currency.getName(),
						currency.getDecimalPlaces(), currency.getNameCode(), currency.getDisplaySymbol());

				allowedCurrencies.add(allowedCurrency);				
			}
		}
		
		this.organisationCurrencyRepository.deleteAll();
		this.organisationCurrencyRepository.save(allowedCurrencies);
		
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.", dataValidationErrors);
		}
	}
}