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

import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.organisation.teller.data.CashierData;
import org.apache.fineract.organisation.teller.data.CashierTransactionData;
import org.apache.fineract.organisation.teller.data.CashierTransactionsWithSummaryData;
import org.apache.fineract.organisation.teller.data.TellerData;
import org.apache.fineract.organisation.teller.data.TellerJournalData;
import org.apache.fineract.organisation.teller.data.TellerTransactionData;

import java.util.Collection;
import java.util.Date;

public interface TellerManagementReadPlatformService {

    public Collection<TellerData> getTellers(Long officeId);

    public TellerData findTeller(Long tellerId);

    public CashierData findCashier(Long cashierId);

    public Collection<CashierData> getCashierData(Long officeId, Long tellerId, Long staffId, Date date);

    public Collection<CashierData> getTellerCashiers(Long tellerId, Date date);

    public CashierData retrieveCashierTemplate(Long officeId, Long tellerId, boolean staffInSelectedOfficeOnly);

    public CashierTransactionData retrieveCashierTxnTemplate(Long cashierId);

    public TellerTransactionData findTellerTransaction(Long transactionId);

    public Collection<TellerTransactionData> fetchTellerTransactionsByTellerId(Long tellerId, Date fromDate, Date toDate);

    public Collection<TellerJournalData> getJournals(Long officeId, Long tellerId, Long cashierId, Date dateFrom, Date dateTo);

    public Collection<TellerJournalData> fetchTellerJournals(Long tellerId, Long cashierId, Date fromDate, Date toDate);

    public Collection<TellerData> retrieveAllTellersForDropdown(Long officeId);

    public Collection<TellerData> retrieveAllTellers(String sqlSearch, Long officeId, String status);

    public Collection<CashierData> getCashiersForTeller(Long tellerId, Date fromDate, Date toDate);

    public Collection<CashierData> retrieveCashiersForTellers(String sqlSearch, Long tellerId);

    public Page<CashierTransactionData> retrieveCashierTransactions(Long cashierId, boolean includeAllTellers, Date fromDate,
            Date toDate, String currencyCode, final SearchParameters searchParameters);

    public CashierTransactionsWithSummaryData retrieveCashierTransactionsWithSummary(Long cashierId, boolean includeAllTellers,
            Date fromDate, Date toDate, String currencyCode, final SearchParameters searchParameters);

}
