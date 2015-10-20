/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.provisioning.exception;

import java.util.Date;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class ProvisioningJournalEntriesCannotbeCreatedException extends AbstractPlatformResourceNotFoundException {

    public ProvisioningJournalEntriesCannotbeCreatedException(Date existingEntriesDate, Date requestedDate) {
        super("error.msg.provisioning.journalentries.cannot.be.created", "Provisioning Journal Entries already created on later date "
                + existingEntriesDate + " than requested date " + requestedDate);
    }

}
