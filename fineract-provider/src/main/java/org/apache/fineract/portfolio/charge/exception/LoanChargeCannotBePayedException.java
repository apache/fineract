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
package org.apache.fineract.portfolio.charge.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class LoanChargeCannotBePayedException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons of why Loan Charge cannot be waived **/
    public static enum LOAN_CHARGE_CANNOT_BE_PAYED_REASON {
        ALREADY_PAID, ALREADY_WAIVED, LOAN_INACTIVE, CHARGE_NOT_ACCOUNT_TRANSFER, CHARGE_NOT_PAYABLE;

        public String errorMessage() {
            if (name().toString().equalsIgnoreCase("ALREADY_PAID")) {
                return "This loan charge has been completely paid";
            } else if (name().toString().equalsIgnoreCase("ALREADY_WAIVED")) {
                return "This loan charge has already been waived";
            } else if (name().toString().equalsIgnoreCase("LOAN_INACTIVE")) {
                return "This loan charge can be payed as the loan associated with it is currently inactive";
            } else if (name().toString().equalsIgnoreCase("CHARGE_NOT_ACCOUNT_TRANSFER")) {
                return "This loan charge can be payed as the charge payment mode is not account transfer";
            } else if (name().toString().equalsIgnoreCase("CHARGE_NOT_PAYABLE")) { return "This loan charge is not Payable through account transfer"; }

            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("ALREADY_PAID")) {
                return "error.msg.loan.charge.already.paid";
            } else if (name().toString().equalsIgnoreCase("ALREADY_WAIVED")) {
                return "error.msg.loan.charge.already.waived";
            } else if (name().toString().equalsIgnoreCase("LOAN_INACTIVE")) {
                return "error.msg.loan.charge.associated.loan.inactive";
            } else if (name().toString().equalsIgnoreCase("CHARGE_NOT_ACCOUNT_TRANSFER")) {
                return "error.msg.loan.charge.payment.mode.not.account.transfer";
            } else if (name().toString().equalsIgnoreCase("CHARGE_NOT_PAYABLE")) { return "error.msg.loan.charge.payment.not.allowed.account.transfer"; }
            return name().toString();
        }
    }

    public LoanChargeCannotBePayedException(final LOAN_CHARGE_CANNOT_BE_PAYED_REASON reason, final Long loanChargeId) {
        super(reason.errorCode(), reason.errorMessage(), loanChargeId);
    }
}
