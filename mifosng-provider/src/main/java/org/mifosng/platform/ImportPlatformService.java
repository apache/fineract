package org.mifosng.platform;

import org.mifosng.platform.api.commands.ImportClientCommand;
import org.mifosng.platform.api.commands.ImportLoanCommand;
import org.mifosng.platform.api.commands.ImportLoanRepaymentsCommand;

public interface ImportPlatformService {

	void importClients(ImportClientCommand command);

	ImportClientCommand populateClientImportFromCsv();

	void importLoans(ImportLoanCommand command);
	
	ImportLoanCommand populateLoanImportFromCsv();

	void importLoanRepayments(ImportLoanRepaymentsCommand command);
	
	ImportLoanRepaymentsCommand populateLoanRepaymentsImportFromCsv();

}