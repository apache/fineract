package org.mifosng.platform.loanproduct.service;

import java.util.Collection;

import org.mifosng.data.LoanProductData;

public interface LoanProductReadPlatformService {

	Collection<LoanProductData> retrieveAllLoanProducts();

	LoanProductData retrieveLoanProduct(Long productId);

	LoanProductData retrieveNewLoanProductDetails();
}