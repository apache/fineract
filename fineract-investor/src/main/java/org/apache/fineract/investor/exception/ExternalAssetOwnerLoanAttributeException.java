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
package org.apache.fineract.investor.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public final class ExternalAssetOwnerLoanAttributeException extends AbstractPlatformDomainRuleException {

    private ExternalAssetOwnerLoanAttributeException(final String errorCode, final String errorMessage, final Long loanId) {
        super(errorCode, errorMessage, loanId);
    }

    public static ExternalAssetOwnerLoanAttributeException noLoanLevelCopy(final Long loanId) {
        return new ExternalAssetOwnerLoanAttributeException("error.msg.externalAssetOwnerLoanAttribute.noLoanLevelCopy",
                "Loan " + loanId + " has no loan-level copy of the external asset owner attributes; it follows its loan product.", loanId);
    }

    public static ExternalAssetOwnerLoanAttributeException settlementModelTransferInProgress(final Long loanId) {
        return new ExternalAssetOwnerLoanAttributeException("error.msg.externalAssetOwnerLoanAttribute.settlementModelTransferInProgress",
                "The settlement model of loan " + loanId + " cannot be changed while a transfer of the loan is in progress.", loanId);
    }
}
