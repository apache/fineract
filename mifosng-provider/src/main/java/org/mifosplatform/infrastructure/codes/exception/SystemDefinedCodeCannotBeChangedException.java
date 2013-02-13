/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link AbstractPlatformDomainRuleException} thrown when someone attempts to update or delete a system defined code.
 */
public class SystemDefinedCodeCannotBeChangedException extends AbstractPlatformDomainRuleException {

    public SystemDefinedCodeCannotBeChangedException() {
        super("error.msg.code.systemdefined", "This code is system defined and cannot be modified or deleted.");
    }
}
