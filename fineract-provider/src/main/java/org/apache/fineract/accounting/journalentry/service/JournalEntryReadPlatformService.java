/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.accounting.journalentry.service;

import java.time.LocalDate;
import org.apache.fineract.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.apache.fineract.accounting.journalentry.data.JournalEntryData;
import org.apache.fineract.accounting.journalentry.data.OfficeOpeningBalancesData;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;

public interface JournalEntryReadPlatformService {

    JournalEntryData retrieveGLJournalEntryById(long glJournalEntryId, JournalEntryAssociationParametersData associationParametersData);

    Page<JournalEntryData> retrieveAll(SearchParameters searchParameters, Long glAccountId, Boolean onlyManualEntries, LocalDate fromDate,
            LocalDate toDate, LocalDate submittedOnDateFrom, LocalDate submittedOnDateTo, String transactionId, Integer entityType,
            JournalEntryAssociationParametersData associationParametersData);

    OfficeOpeningBalancesData retrieveOfficeOpeningBalances(Long officeId, String currencyCode);

    Page<JournalEntryData> retrieveJournalEntriesByEntityId(String transactionId, Long entityId, Integer entityType);
}
