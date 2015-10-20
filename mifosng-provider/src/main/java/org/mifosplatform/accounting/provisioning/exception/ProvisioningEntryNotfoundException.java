/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;


public class ProvisioningEntryNotfoundException extends AbstractPlatformResourceNotFoundException {

    public ProvisioningEntryNotfoundException(Long id) {
        super("error.msg.provisioningentry.id.invalid", "ProvisioningEntry with identifier " + id + " does not exist", id);
    }

}
