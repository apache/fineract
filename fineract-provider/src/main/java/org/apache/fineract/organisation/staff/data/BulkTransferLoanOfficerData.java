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

import org.apache.fineract.organisation.office.data.OfficeData;
import org.joda.time.LocalDate;

/**
 * Immutable data object returned for loan-officer bulk transfer screens.
 */
public class BulkTransferLoanOfficerData {

    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final Long fromLoanOfficerId;
    @SuppressWarnings("unused")
    private final LocalDate assignmentDate;

    // template
    @SuppressWarnings("unused")
    private final Collection<OfficeData> officeOptions;
    @SuppressWarnings("unused")
    private final Collection<StaffData> loanOfficerOptions;
    @SuppressWarnings("unused")
    private final StaffAccountSummaryCollectionData accountSummaryCollection;

    public static BulkTransferLoanOfficerData templateForBulk(final Long officeId, final Long fromLoanOfficerId,
            final LocalDate assignmentDate, final Collection<OfficeData> officeOptions, final Collection<StaffData> loanOfficerOptions,
            final StaffAccountSummaryCollectionData accountSummaryCollection) {
        return new BulkTransferLoanOfficerData(officeId, fromLoanOfficerId, assignmentDate, officeOptions, loanOfficerOptions,
                accountSummaryCollection);
    }

    public static BulkTransferLoanOfficerData template(final Long fromLoanOfficerId, final Collection<StaffData> loanOfficerOptions,
            final LocalDate assignmentDate) {
        return new BulkTransferLoanOfficerData(null, fromLoanOfficerId, assignmentDate, null, loanOfficerOptions, null);
    }

    private BulkTransferLoanOfficerData(final Long officeId, final Long fromLoanOfficerId, final LocalDate assignmentDate,
            final Collection<OfficeData> officeOptions, final Collection<StaffData> loanOfficerOptions,
            final StaffAccountSummaryCollectionData accountSummaryCollection) {
        this.officeId = officeId;
        this.fromLoanOfficerId = fromLoanOfficerId;
        this.assignmentDate = assignmentDate;
        this.officeOptions = officeOptions;
        this.loanOfficerOptions = loanOfficerOptions;
        this.accountSummaryCollection = accountSummaryCollection;
    }
}