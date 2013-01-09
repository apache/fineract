package org.mifosplatform.accounting.service;

import java.util.Map;

import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;

public interface ProductToGLAccountMappingWritePlatformService {

    void createLoanProductToGLAccountMapping(Long loanProductId, LoanProductCommand command);

    Map<String, Object> updateLoanProductToGLAccountMapping(Long loanProductId, LoanProductCommand command, boolean accountingRuleChanged);

    void deleteLoanProductToGLAccountMapping(Long loanProductId);

}
