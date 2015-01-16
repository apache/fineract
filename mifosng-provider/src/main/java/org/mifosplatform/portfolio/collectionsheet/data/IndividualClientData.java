/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.collectionsheet.data;

import java.util.Collection;

/**
 * Immutable data object for clients with loans due for disbursement or
 * collection.
 */
public class IndividualClientData {

    private final Long clientId;
    private final String clientName;
    private Collection<LoanDueData> loans;
    private Collection<SavingsDueData> savings;

    public static IndividualClientData instance(final Long clientId, final String clientName) {
        final Collection<LoanDueData> loans = null;
        final Collection<SavingsDueData> savings = null;
        return new IndividualClientData(clientId, clientName, loans, savings);
    }

    public static IndividualClientData withSavings(final IndividualClientData client, final Collection<SavingsDueData> savings) {

        return new IndividualClientData(client.clientId, client.clientName, client.loans, savings);
    }

    public static IndividualClientData withLoans(final IndividualClientData client, final Collection<LoanDueData> loans) {

        return new IndividualClientData(client.clientId, client.clientName, loans, client.savings);
    }

    /**
     * @param clientId
     * @param clientName
     * @param loans
     * @param savings
     */
    private IndividualClientData(Long clientId, String clientName, Collection<LoanDueData> loans, Collection<SavingsDueData> savings) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.loans = loans;
        this.savings = savings;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public String getClientName() {
        return this.clientName;
    }

    public Collection<LoanDueData> getLoans() {
        return this.loans;
    }

    public void setLoans(final Collection<LoanDueData> loans) {
        this.loans = loans;
    }

    public void addLoans(LoanDueData loans) {
        if (this.loans != null) {
            this.loans.add(loans);
        }
    }

    public Collection<SavingsDueData> getSavings() {
        return this.savings;
    }

    public void setSavings(Collection<SavingsDueData> savings) {
        this.savings = savings;
    }

    public void addSavings(SavingsDueData savings) {
        if (this.savings != null) {
            this.savings.add(savings);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        final IndividualClientData clientData = (IndividualClientData) obj;
        return clientData.clientId.compareTo(this.clientId) == 0;
    }

    @Override
    public int hashCode() {
        return this.clientId.hashCode();
    }
}