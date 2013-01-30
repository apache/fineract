package org.mifosplatform.portfolio.loanproduct.service;

import java.util.Collection;

import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;

public interface LoanProductReadPlatformService {

    Collection<LoanProductData> retrieveAllLoanProducts();

    Collection<LoanProductData> retrieveAllLoanProductsForLookup();

    LoanProductData retrieveLoanProduct(Long productId);

    LoanProductData retrieveNewLoanProductDetails();
}