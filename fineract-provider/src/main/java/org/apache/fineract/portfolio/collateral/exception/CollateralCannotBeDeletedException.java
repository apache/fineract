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
package org.apache.fineract.portfolio.collateral.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class CollateralCannotBeDeletedException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons of why Loan Charge cannot be waived **/
    public static enum LOAN_COLLATERAL_CANNOT_BE_DELETED_REASON {
        LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE;

        public String errorMessage() {
            if (name().toString().equalsIgnoreCase("LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE")) { return "This collateral cannot be updated as the loan it is associated with is not in submitted and pending approval stage"; }
            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("LOAN_NOT_IN_SUBMITTED_AND_PENDING_APPROVAL_STAGE")) { return "error.msg.loan.collateral.associated.loan.not.in.submitted.and.pending.approval.stage"; }
            return name().toString();
        }
    }

    public CollateralCannotBeDeletedException(final LOAN_COLLATERAL_CANNOT_BE_DELETED_REASON reason, final Long loanId,
            final Long loanCollateralId) {
        super(reason.errorCode(), reason.errorMessage(), loanId, loanCollateralId);
    }
}
