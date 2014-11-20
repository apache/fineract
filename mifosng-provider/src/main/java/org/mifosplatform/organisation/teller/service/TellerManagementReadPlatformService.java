/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.service;

import org.mifosplatform.organisation.teller.data.CashierData;
import org.mifosplatform.organisation.teller.data.CashierTransactionData;
import org.mifosplatform.organisation.teller.data.CashierTransactionsWithSummaryData;
import org.mifosplatform.organisation.teller.data.TellerData;
import org.mifosplatform.organisation.teller.data.TellerJournalData;
import org.mifosplatform.organisation.teller.data.TellerTransactionData;

import java.util.Collection;
import java.util.Date;

public interface TellerManagementReadPlatformService {

    public Collection<TellerData> getTellers(Long officeId);

    public TellerData findTeller(Long tellerId);

    public CashierData findCashier(Long cashierId);

    public Collection<CashierData> getCashierData(Long officeId, Long tellerId, Long staffId, Date date);

    public Collection<CashierData> getTellerCashiers(Long tellerId, Date date);
    
    public CashierData retrieveCashierTemplate (Long officeId, Long tellerId, boolean staffInSelectedOfficeOnly);
    
    public CashierTransactionData retrieveCashierTxnTemplate(Long cashierId);

    public TellerTransactionData findTellerTransaction(Long transactionId);

    public Collection<TellerTransactionData> fetchTellerTransactionsByTellerId(Long tellerId, Date fromDate, Date toDate);

    public Collection<TellerJournalData> getJournals(Long officeId, Long tellerId, Long cashierId, Date dateFrom, Date dateTo);

    public Collection<TellerJournalData> fetchTellerJournals(Long tellerId, Long cashierId, Date fromDate, Date toDate);

    public Collection<TellerData> retrieveAllTellersForDropdown(Long officeId);

    public Collection<TellerData> retrieveAllTellers(String sqlSearch, Long officeId, String status);

    public Collection<CashierData> getCashiersForTeller(Long tellerId, Date fromDate, Date toDate);

    public Collection<CashierData> retrieveCashiersForTellers(String sqlSearch,
			Long tellerId);

    public Collection<CashierTransactionData> retrieveCashierTransactions(
			Long cashierId, boolean includeAllTellers, Date fromDate,
			Date toDate);
    
    public CashierTransactionsWithSummaryData retrieveCashierTransactionsWithSummary (
			Long cashierId, boolean includeAllTellers, Date fromDate, Date toDate);

}
