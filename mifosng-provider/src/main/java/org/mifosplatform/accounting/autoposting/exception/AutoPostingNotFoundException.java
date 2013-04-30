/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.autoposting.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when Auto posting rule is not found.
 */
public class AutoPostingNotFoundException extends AbstractPlatformResourceNotFoundException {

    public AutoPostingNotFoundException(final Long id) {
        super("error.msg.autoposting.id.invalid", "Autoposting rule with identifier " + id + " does not exist", id);
    }
}