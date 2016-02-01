/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class ProvisioningCategoryNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ProvisioningCategoryNotFoundException(final Long id) {
        super("error.msg.provisioning.id.invalid", "Provisioning Category with identifier " + id + " does not exist", id);
    }
}
