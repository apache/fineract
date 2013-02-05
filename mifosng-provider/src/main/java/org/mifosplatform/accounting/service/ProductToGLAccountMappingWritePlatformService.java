package org.mifosplatform.accounting.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;

public interface ProductToGLAccountMappingWritePlatformService {

    void createLoanProductToGLAccountMapping(Long loanProductId, JsonCommand command);

    Map<String, Object> updateLoanProductToGLAccountMapping(Long loanProductId, JsonCommand command, boolean accountingRuleChanged,
            int accountingRuleTypeId);

    void deleteLoanProductToGLAccountMapping(Long loanProductId);
}