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

import java.time.LocalDate;
import java.util.Collection;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.office.data.OfficeData;

/**
 * Immutable data object returned for loan-officer bulk transfer screens.
 */

@Data
@NoArgsConstructor
@Accessors(chain = true)
public final class BulkTransferLoanOfficerData {

    @SuppressWarnings("unused")
    private Long officeId;
    @SuppressWarnings("unused")
    private Long fromLoanOfficerId;
    @SuppressWarnings("unused")
    private LocalDate assignmentDate;

    // template
    @SuppressWarnings("unused")
    private Collection<OfficeData> officeOptions;
    @SuppressWarnings("unused")
    private Collection<StaffData> loanOfficerOptions;
    @SuppressWarnings("unused")
    private StaffAccountSummaryCollectionData accountSummaryCollection;

    public static BulkTransferLoanOfficerData templateForBulk(final Long officeId, final Long fromLoanOfficerId,
            final LocalDate assignmentDate, final Collection<OfficeData> officeOptions, final Collection<StaffData> loanOfficerOptions,
            final StaffAccountSummaryCollectionData accountSummaryCollection) {
        return new BulkTransferLoanOfficerData().setOfficeId(officeId).setFromLoanOfficerId(fromLoanOfficerId)
                .setAssignmentDate(assignmentDate).setOfficeOptions(officeOptions).setLoanOfficerOptions(loanOfficerOptions)
                .setAccountSummaryCollection(accountSummaryCollection);
    }

    public static BulkTransferLoanOfficerData template(final Long fromLoanOfficerId, final Collection<StaffData> loanOfficerOptions,
            final LocalDate assignmentDate) {
        return new BulkTransferLoanOfficerData().setFromLoanOfficerId(fromLoanOfficerId).setAssignmentDate(assignmentDate)
                .setLoanOfficerOptions(loanOfficerOptions);
    }
}
