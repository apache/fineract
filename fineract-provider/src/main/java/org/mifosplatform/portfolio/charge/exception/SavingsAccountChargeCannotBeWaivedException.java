/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SavingsAccountChargeCannotBeWaivedException extends AbstractPlatformDomainRuleException {

    /*** enum of reasons of why Savings Account Charge cannot be waived **/
    public static enum SAVINGS_ACCOUNT_CHARGE_CANNOT_BE_WAIVED_REASON {
        ALREADY_PAID, ALREADY_WAIVED, SAVINGS_ACCOUNT_NOT_ACTIVE, SAVINGS_ACCOUNT_CLOSED;

        public String errorMessage() {
            if (name().toString().equalsIgnoreCase("ALREADY_PAID")) {
                return "This savings account charge has been completely paid";
            } else if (name().toString().equalsIgnoreCase("ALREADY_WAIVED")) {
                return "This savings account charge has already been waived";
            } else if (name().toString().equalsIgnoreCase("SAVINGS_ACCOUNT_NOT_ACTIVE")) {
                return "This savings account charge cannot be waived as the Savings account associated with it is not active";
            } else if (name().toString().equalsIgnoreCase("SAVINGS_ACCOUNT_CLOSED")) { return "This savings account charge cannot be waived as the Savings account associated with it is closed"; }
            return name().toString();
        }

        public String errorCode() {
            if (name().toString().equalsIgnoreCase("ALREADY_PAID")) {
                return "error.msg.savings.account.charge.already.paid";
            } else if (name().toString().equalsIgnoreCase("ALREADY_WAIVED")) {
                return "error.msg.savings.account.charge.already.waived";
            } else if (name().toString().equalsIgnoreCase("SAVINGS_ACCOUNT_NOT_ACTIVE")) {
                return "error.msg.savings.account.charge.associated.savings.account.not.active";
            } else if (name().toString().equalsIgnoreCase("SAVINGS_ACCOUNT_CLOSED")) { return "error.msg.savings.account.charge.associated.savings.account.closed"; }
            return name().toString();
        }
    }

    public SavingsAccountChargeCannotBeWaivedException(final SAVINGS_ACCOUNT_CHARGE_CANNOT_BE_WAIVED_REASON reason,
            final Long savingsAccountChargeId) {
        super(reason.errorCode(), reason.errorMessage(), savingsAccountChargeId);
    }
}
