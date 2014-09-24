/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class SavingsOfficerUnassignmentException extends AbstractPlatformDomainRuleException {

    public SavingsOfficerUnassignmentException(final Long accountId) {
        super("error.msg.savings.account.not.assigned.to.savings.officer", "Savings Account Identifier" + accountId + " is not assigned to any savings officer.");
    }
}
