package org.mifosng.platform.loanproduct.service;

import java.util.Collection;

import org.mifosng.platform.api.data.LoanProductData;

public interface LoanProductReadPlatformService {

	Collection<LoanProductData> retrieveAllLoanProducts();

	Collection<LoanProductData> retrieveAllLoanProductsForLookup();

	LoanProductData retrieveLoanProduct(Long productId);

	LoanProductData retrieveNewLoanProductDetails();
}