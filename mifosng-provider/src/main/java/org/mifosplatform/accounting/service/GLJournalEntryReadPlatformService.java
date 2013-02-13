/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.service;

import java.util.Date;
import java.util.List;

import org.mifosplatform.accounting.api.data.GLJournalEntryData;

public interface GLJournalEntryReadPlatformService {

    List<GLJournalEntryData> retrieveAllGLJournalEntries(Long officeId, Long glAccountId, Boolean portfolioGenerated, Date fromDate,
            Date toDate);

    List<GLJournalEntryData> retrieveRelatedJournalEntries(String transactionId);

    GLJournalEntryData retrieveGLJournalEntryById(long glJournalEntryId);

}
