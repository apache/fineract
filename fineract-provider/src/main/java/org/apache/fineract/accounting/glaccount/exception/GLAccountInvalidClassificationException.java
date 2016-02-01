/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.glaccount.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when trying to fetch accounts belonging to
 * an Invalid Usage Type
 */
public class GLAccountInvalidClassificationException extends AbstractPlatformDomainRuleException {

    public GLAccountInvalidClassificationException(final Integer usage) {
        super("error.msg.glaccount.usage.invalid", "The following COA usage is invalid: " + usage);
    }
}