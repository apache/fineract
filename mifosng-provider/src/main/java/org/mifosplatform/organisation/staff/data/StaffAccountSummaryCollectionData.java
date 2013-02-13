/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.staff.data;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.portfolio.client.data.ClientAccountSummaryData;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryData;

/**
 *
 */
public class StaffAccountSummaryCollectionData {

    @SuppressWarnings("unused")
    private final List<ClientSummary> clients;
    @SuppressWarnings("unused")
    private final List<GroupSummary> groups;

    public StaffAccountSummaryCollectionData(final List<ClientSummary> clients, final List<GroupSummary> groups) {
        this.clients = clients;
        this.groups = groups;
    }

    public static final class ClientSummary {

        private final Long id;
        private final String displayName;

        private Collection<ClientAccountSummaryData> loans;

        public ClientSummary(Long id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public Long getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Collection<ClientAccountSummaryData> getLoans() {
            return loans;
        }

        public void setLoans(Collection<ClientAccountSummaryData> loans) {
            this.loans = loans;
        }
    }

    public static final class GroupSummary {

        private final Long id;
        private final String name;

        private Collection<GroupAccountSummaryData> loans;

        public GroupSummary(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Collection<GroupAccountSummaryData> getLoans() {
            return loans;
        }

        public void setLoans(Collection<GroupAccountSummaryData> loans) {
            this.loans = loans;
        }
    }
}