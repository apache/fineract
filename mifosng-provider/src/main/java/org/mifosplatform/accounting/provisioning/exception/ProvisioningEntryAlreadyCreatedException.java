/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.exception;

import java.util.Date;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;


public class ProvisioningEntryAlreadyCreatedException extends AbstractPlatformResourceNotFoundException {

    public ProvisioningEntryAlreadyCreatedException(Long id, Date date) {
        super("error.msg.provisioningentry.already.exists.for.this.date", "ProvisioningEntry with identifier " + id + " exists for given date" + date, id);
    }
}
