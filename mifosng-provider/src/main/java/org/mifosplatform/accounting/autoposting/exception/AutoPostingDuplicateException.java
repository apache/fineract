/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.autoposting.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when a Auto posting rule is already present
 */
public class AutoPostingDuplicateException extends AbstractPlatformDomainRuleException {

    public AutoPostingDuplicateException(final String autoPostingName) {
        super("error.msg.auto.posting.duplicate", "An autoposting rule with the name " + autoPostingName + " already exists",
                autoPostingName);
    }
}