package org.mifosng.platform.accounting.api.infrastructure.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mifosng.platform.accounting.api.data.ChartOfAccountsData;
import org.mifosng.platform.accounting.api.infrastructure.AccountingApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.GoogleGsonSerializerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

/**
 * Implementation of {@link PortfolioApiJsonSerializerService} that uses google-gson to
 * serialize Java object representation into JSON.
 */
@Service
public class GoogleGsonAccountingApiJsonSerializerService implements AccountingApiJsonSerializerService {

	private static final Set<String> CHART_OF_ACCOUNTS_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name"));

	private final GoogleGsonSerializerHelper helper;

	@Autowired
	public GoogleGsonAccountingApiJsonSerializerService(
			final GoogleGsonSerializerHelper helper) {
		this.helper = helper;
	}

	@Override
	public String serializeChartOfAccountDataToJson(final boolean prettyPrint,
			final Set<String> responseParameters,
			final ChartOfAccountsData chartOfAccounts) {
		final Gson gsonDeserializer = helper
				.createGsonBuilderWithParameterExclusionSerializationStrategy(
						CHART_OF_ACCOUNTS_DATA_PARAMETERS, prettyPrint,
						responseParameters);
		return helper.serializedJsonFrom(gsonDeserializer, chartOfAccounts);
	}
}