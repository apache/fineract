/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.service;

import java.util.Date;
import java.util.List;

import org.mifosplatform.accounting.journalentry.data.JournalEntryData;

public interface JournalEntryReadPlatformService {

    List<JournalEntryData> retrieveAllGLJournalEntries(Long officeId, Long glAccountId, Boolean onlyManualEntries, Date fromDate,
            Date toDate);

    List<JournalEntryData> retrieveRelatedJournalEntries(String transactionId);

    JournalEntryData retrieveGLJournalEntryById(long glJournalEntryId);

}
