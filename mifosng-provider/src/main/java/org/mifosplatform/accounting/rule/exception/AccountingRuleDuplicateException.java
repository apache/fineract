/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when an Accounting rule with a given name
 * already exists
 */
public class AccountingRuleDuplicateException extends AbstractPlatformDomainRuleException {

    public AccountingRuleDuplicateException(final String name) {
        super("error.msg.accounting.rule.duplicate", "An accounting rule with the name " + name + " already exists" + name);
    }

    public AccountingRuleDuplicateException() {
        super("error.msg.accounting.rule.tag.duplicate", "The accounting rule already have the tags which you defined");
    }

}