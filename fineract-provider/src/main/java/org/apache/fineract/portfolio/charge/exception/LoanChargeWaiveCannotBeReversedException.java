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

public class LoanChargeWaiveCannotBeReversedException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons of why Loan Charge waive cannot undo **/
    public enum LoanChargeWaiveCannotUndoReason {

        ALREADY_PAID, ALREADY_WAIVED, LOAN_INACTIVE, WAIVE_NOT_ALLOWED_FOR_CHARGE, NOT_WAIVED, ALREADY_REVERSED;

        public String errorMessage() {

            if (name().toString().equalsIgnoreCase("ALREADY_PAID")) {
                return "This loan charge has been completely paid";
            } else if (name().toString().equalsIgnoreCase("ALREADY_WAIVED")) {
                return "This loan charge has already been waived";
            } else if (name().toString().equalsIgnoreCase("LOAN_INACTIVE")) {
                return "This loan charge can be waived as the loan associated with it is currently inactive";
            } else if (name().toString().equalsIgnoreCase("WAIVE_NOT_ALLOWED_FOR_CHARGE")) {
                return "This loan charge can be waived";
            } else if (name().toString().equalsIgnoreCase("NOT_WAIVED")) {
                return "This loan charge waive cannot be reversed as this charge is not waived";
            } else if (name().toString().equalsIgnoreCase("ALREADY_REVERSED")) {
                return "This loan charge waive cannot be reversed as this transaction is not reversed";
            }

            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("ALREADY_PAID")) {
                return "error.msg.loan.charge.already.paid";
            } else if (name().toString().equalsIgnoreCase("ALREADY_WAIVED")) {
                return "error.msg.loan.charge.already.waived";
            } else if (name().toString().equalsIgnoreCase("LOAN_INACTIVE")) {
                return "error.msg.loan.charge.associated.loan.inactive";
            } else if (name().toString().equalsIgnoreCase("WAIVE_NOT_ALLOWED_FOR_CHARGE")) {
                return "error.msg.loan.charge.waive.not.allowed";
            } else if (name().toString().equalsIgnoreCase("NOT_WAIVED")) {
                return "error.msg.loan.charge.waive.cannot.undo";
            } else if (name().toString().equalsIgnoreCase("ALREADY_REVERSED")) {
                return "error.msg.transaction.cannot.reverse";
            }
            return name().toString();
        }
    }

    public LoanChargeWaiveCannotBeReversedException(final LoanChargeWaiveCannotUndoReason reason, final Long id) {
        super(reason.errorCode(), reason.errorMessage(), id);
    }

}
