/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.staff.data;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.portfolio.accountdetails.data.LoanAccountSummaryData;

/**
 *
 */
public class StaffAccountSummaryCollectionData {

    @SuppressWarnings("unused")
    private final List<LoanAccountSummary> clients;
    @SuppressWarnings("unused")
    private final List<LoanAccountSummary> groups;

    public StaffAccountSummaryCollectionData(final List<LoanAccountSummary> clients, final List<LoanAccountSummary> groups) {
        this.clients = clients;
        this.groups = groups;
    }

    public static final class LoanAccountSummary {

        private final Long id;
        private final String displayName;

        private Collection<LoanAccountSummaryData> loans;

        public LoanAccountSummary(final Long id, final String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public Long getId() {
            return this.id;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public Collection<LoanAccountSummaryData> getLoans() {
            return this.loans;
        }

        public void setLoans(final Collection<LoanAccountSummaryData> loans) {
            this.loans = loans;
        }
    }

}