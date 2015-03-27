/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when role resources are not found.
 */
public class PasswordValidationPolicyNotFoundException extends AbstractPlatformResourceNotFoundException {

    public PasswordValidationPolicyNotFoundException(final Long id) {
        super("error.msg.password.validation.policy.id.invalid", "Password Validation Policy with identifier " + id + " does not exist", id);
    }

    public PasswordValidationPolicyNotFoundException() {
        super("error.msg.password.validation.policy.not.found", "An active password validation policy was not found");
    }
}