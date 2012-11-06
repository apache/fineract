package org.mifosng.platform.accounting.api.infrastructure;

import java.util.Set;

import org.mifosng.platform.accounting.api.data.ChartOfAccountsData;

public interface AccountingApiJsonSerializerService {

	String serializeChartOfAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, ChartOfAccountsData chartOfAccounts);

}