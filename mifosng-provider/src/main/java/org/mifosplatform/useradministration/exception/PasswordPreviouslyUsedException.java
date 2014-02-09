/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.exception;

import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;

public class PasswordPreviouslyUsedException extends PlatformApiDataValidationException {

    public PasswordPreviouslyUsedException() {
        super("error.msg.password.already.used", "The submitted password has already been used in the past",null);
    }
}

