package org.mifosng.platform;

import org.mifosng.data.command.ImportClientCommand;
import org.mifosng.data.command.ImportLoanCommand;
import org.mifosng.data.command.ImportLoanRepaymentsCommand;

public interface ImportPlatformService {

	void importClients(ImportClientCommand command);

	ImportClientCommand populateClientImportFromCsv();

	void importLoans(ImportLoanCommand command);
	
	ImportLoanCommand populateLoanImportFromCsv();

	void importLoanRepayments(ImportLoanRepaymentsCommand command);
	
	ImportLoanRepaymentsCommand populateLoanRepaymentsImportFromCsv();

}