package org.mifosplatform.accounting.service;

import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;

public interface ProductToGLAccountMappingReadPlatformService {

    LoanProductData fetchAccountMappingDetailsForLoanProduct(LoanProductData loanProductData);

}
