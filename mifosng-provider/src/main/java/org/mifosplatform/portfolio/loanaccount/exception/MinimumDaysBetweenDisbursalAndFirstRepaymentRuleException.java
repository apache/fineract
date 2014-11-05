/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when a number of days between disbursal date and firstRepayment is less than 
 * 	minimumDaysBetweenDisbursalAndFirstRepayment
 * 
 */
public class MinimumDaysBetweenDisbursalAndFirstRepaymentRuleException extends AbstractPlatformDomainRuleException {

    public MinimumDaysBetweenDisbursalAndFirstRepaymentRuleException(final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        super("validation.msg.loan.days.between.first.repayment.and.disbursal.are.less than.minimum.number.of.days.required",
        		defaultUserMessage, defaultUserMessageArgs);
    }
}