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
package org.apache.fineract.organisation.teller.service;

import java.time.LocalDate;
import java.util.Collection;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.teller.data.CashierData;
import org.apache.fineract.organisation.teller.data.CashierTransactionData;
import org.apache.fineract.organisation.teller.data.CashierTransactionsWithSummaryData;
import org.apache.fineract.organisation.teller.data.TellerData;
import org.apache.fineract.organisation.teller.data.TellerJournalData;
import org.apache.fineract.organisation.teller.data.TellerTransactionData;

public interface TellerManagementReadPlatformService {

    Collection<TellerData> getTellers(Long officeId);

    TellerData findTeller(Long tellerId);

    CashierData findCashier(Long cashierId);

    Collection<CashierData> getCashierData(Long officeId, Long tellerId, Long staffId, LocalDate date);

    Collection<CashierData> getTellerCashiers(Long tellerId, LocalDate date);

    CashierData retrieveCashierTemplate(Long officeId, Long tellerId, boolean staffInSelectedOfficeOnly);

    CashierTransactionData retrieveCashierTxnTemplate(Long cashierId);

    TellerTransactionData findTellerTransaction(Long transactionId);

    Collection<TellerTransactionData> fetchTellerTransactionsByTellerId(Long tellerId, LocalDate fromDate, LocalDate toDate);

    Collection<TellerJournalData> getJournals(Long officeId, Long tellerId, Long cashierId, LocalDate dateFrom, LocalDate dateTo);

    Collection<TellerJournalData> fetchTellerJournals(Long tellerId, Long cashierId, LocalDate fromDate, LocalDate toDate);

    Collection<TellerData> retrieveAllTellersForDropdown(Long officeId);

    Collection<TellerData> retrieveAllTellers(String sqlSearch, Long officeId, String status);

    Collection<CashierData> getCashiersForTeller(Long tellerId, LocalDate fromDate, LocalDate toDate);

    Collection<CashierData> retrieveCashiersForTellers(String sqlSearch, Long tellerId);

    Page<CashierTransactionData> retrieveCashierTransactions(Long cashierId, boolean includeAllTellers, LocalDate fromDate,
            LocalDate toDate, String currencyCode, SearchParameters searchParameters);

    CashierTransactionsWithSummaryData retrieveCashierTransactionsWithSummary(Long cashierId, boolean includeAllTellers, LocalDate fromDate,
            LocalDate toDate, String currencyCode, SearchParameters searchParameters);

}
