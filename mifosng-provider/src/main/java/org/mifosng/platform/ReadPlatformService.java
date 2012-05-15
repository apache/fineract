package org.mifosng.platform;

import java.util.Collection;

import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.NewLoanWorkflowStepOneData;
import org.mifosng.data.OrganisationReadModel;

public interface ReadPlatformService {

	Collection<OrganisationReadModel> retrieveAll();

	NewLoanWorkflowStepOneData retrieveClientAndProductDetails(Long clientId, Long productId);

	LoanRepaymentData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanRepaymentData retrieveNewLoanWaiverDetails(Long loanId);

	LoanRepaymentData retrieveLoanRepaymentDetails(Long loanId, Long repaymentId);
}