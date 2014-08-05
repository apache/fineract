/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class AccountingRuleDataException extends AbstractPlatformDomainRuleException {

    public AccountingRuleDataException(final String debitOrCreditAccount, final String debitOrCreditTags) {
        super("error.msg.accounting.rule." + debitOrCreditAccount + ".or." + debitOrCreditTags + ".required", debitOrCreditAccount + " or "
                + debitOrCreditTags + " required", debitOrCreditAccount, debitOrCreditTags);
    }

}
