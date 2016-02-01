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
package org.apache.fineract.organisation.staff.data;

import java.util.Collection;
import java.util.List;

import org.apache.fineract.portfolio.accountdetails.data.LoanAccountSummaryData;

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