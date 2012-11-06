package org.mifosng.platform.accounting.api.infrastructure;

import org.mifosng.platform.accounting.api.commands.ChartOfAccountCommand;

public interface AccountingApiDataConversionService {
	
	ChartOfAccountCommand convertJsonToChartOfAccountCommand(Long resourceIdentifier, String json);

}