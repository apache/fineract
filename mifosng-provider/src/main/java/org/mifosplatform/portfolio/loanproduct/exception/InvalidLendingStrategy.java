/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when lending strategy
 * mismatch occurs
 */
public class InvalidLendingStrategy extends AbstractPlatformDomainRuleException {

    public InvalidLendingStrategy(final Integer strategyId) {
        super("error.msg.unsupported.lending.strategy", "Stratagy code [" + strategyId + "] passed is not valid.");
    }
}