/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.accounting.financialactivityaccount.exception;

import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not found.
 */
public class FinancialActivityAccountInvalidException extends AbstractPlatformDomainRuleException {

    private static final String errorCode = "error.msg.financialActivityAccount.invalid";

    public FinancialActivityAccountInvalidException(final FinancialActivity financialActivity, final GLAccount glAccount) {
        super(errorCode,
                "Financial Activity '" + financialActivity.getCode() + "' with Id :" + financialActivity.getValue()
                        + "' can only be associated with a Ledger Account of Type " + financialActivity.getMappedGLAccountType().getCode()
                        + " the provided Ledger Account '" + glAccount.getName() + "(" + glAccount.getGlCode()
                        + ")'  does not of the required type",
                financialActivity.getCode(), financialActivity.getValue(), financialActivity.getMappedGLAccountType().getCode(),
                glAccount.getName(), glAccount.getGlCode());
    }

    public static String getErrorcode() {
        return errorCode;
    }
}
