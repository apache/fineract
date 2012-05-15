package org.mifosng.platform;

import java.util.Collection;

import org.mifosng.data.ClientData;
import org.mifosng.data.ClientDataWithAccountsData;
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.NoteData;
import org.mifosng.data.OrganisationReadModel;

public interface ReadPlatformService {

	Collection<OrganisationReadModel> retrieveAll();

	ClientDataWithAccountsData retrieveClientAccountDetails(Long clientId);

	NewLoanWorkflowStepOneData retrieveClientAndProductDetails(Long clientId, Long productId);

	LoanAccountData retrieveLoanAccountDetails(Long loanId);

	LoanRepaymentData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanRepaymentData retrieveNewLoanWaiverDetails(Long loanId);

	LoanRepaymentData retrieveLoanRepaymentDetails(Long loanId, Long repaymentId);

	ClientData retrieveNewClientDetails();

	Collection<NoteData> retrieveAllClientNotes(Long clientId);

	NoteData retrieveClientNote(Long clientId, Long noteId);

}