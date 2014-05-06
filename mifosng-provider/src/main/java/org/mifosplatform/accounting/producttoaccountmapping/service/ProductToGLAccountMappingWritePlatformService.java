/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.service;

import java.util.Map;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.savings.DepositAccountType;

public interface ProductToGLAccountMappingWritePlatformService {

    void createLoanProductToGLAccountMapping(Long loanProductId, JsonCommand command);

    void createSavingProductToGLAccountMapping(Long savingProductId, JsonCommand command, DepositAccountType accountType);

    Map<String, Object> updateLoanProductToGLAccountMapping(Long loanProductId, JsonCommand command, boolean accountingRuleChanged,
            int accountingRuleTypeId);

    Map<String, Object> updateSavingsProductToGLAccountMapping(Long savingsProductId, JsonCommand command, boolean accountingRuleChanged,
            int accountingRuleTypeId, DepositAccountType accountType);

}