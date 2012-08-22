package org.mifosng.platform.api.infrastructure;

import java.util.Set;

import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.commands.ChargeCommand;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.api.commands.GroupCommand;
import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.NoteCommand;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.api.commands.OrganisationCurrencyCommand;
import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.commands.SavingProductCommand;
import org.mifosng.platform.api.commands.SubmitLoanApplicationCommand;
import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.api.data.LoanAccountData;
import org.mifosng.platform.api.data.LoanTransactionData;
import org.mifosng.platform.api.data.NewLoanData;

public interface ApiDataConversionService {
	
	//
	String convertNewLoanDataToJson(boolean prettyPrint, Set<String> responseParameters, NewLoanData newLoanData);
	
	String convertLoanAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, LoanAccountData loanAccount);
	
	String convertLoanTransactionDataToJson(boolean prettyPrint, Set<String> responseParameters, LoanTransactionData... transactions);
	//

    ChargeCommand convertJsonToChargeCommand(Long resourceIdentifier, String json);

    String convertChargeDataToJson(boolean prettyPrint, Set<String> responseParameters, ChargeData... charges);

	FundCommand convertJsonToFundCommand(Long resourceIdentifier, String json);
	
	OfficeCommand convertJsonToOfficeCommand(Long resourceIdentifier, String json);
	
	RoleCommand convertJsonToRoleCommand(Long resourceIdentifier, String json);

	UserCommand convertJsonToUserCommand(Long resourceIdentifier, String json);

	BranchMoneyTransferCommand convertJsonToBranchMoneyTransferCommand(String jsonRequestBody);
	
	LoanProductCommand convertJsonToLoanProductCommand(Long resourceIdentifier, String json);
	
	SavingProductCommand convertJsonToSavingProductCommand(Long resourceIdentifier, String json);
	
	DepositProductCommand convertJsonToDepositProductCommand(Long resourceIdentifier, String json);

	ClientCommand convertJsonToClientCommand(Long resourceIdentifier, String jsonRequestBody);

	GroupCommand convertJsonToGroupCommand(Long resourceIdentifier, String jsonRequestBody);
	
	SubmitLoanApplicationCommand convertJsonToSubmitLoanApplicationCommand(String jsonRequestBody);

	LoanStateTransitionCommand convertJsonToLoanStateTransitionCommand(Long resourceIdentifier, String jsonRequestBody);

	LoanTransactionCommand convertJsonToLoanTransactionCommand(Long resourceIdentifier, String jsonRequestBody);

	AdjustLoanTransactionCommand convertJsonToAdjustLoanTransactionCommand(
			Long loanId, Long transactionId, String jsonRequestBody);

	OrganisationCurrencyCommand convertJsonToOrganisationCurrencyCommand(String jsonRequestBody);

	NoteCommand convertJsonToNoteCommand(Long resourceIdentifier, Long clientId, String jsonRequestBody);

	DepositAccountCommand convertJsonToDepositAccountCommand(Long resourceIdentifier, String jsonRequestBody);
}