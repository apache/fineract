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
package org.apache.fineract.organisation.teller.data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.teller.domain.TellerStatus;

/**
 * {@code TellerData} represents an immutable data object for teller data.
 *
 * @version 1.0
 *
 * @since 2.0.0
 * @see java.io.Serializable
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public final class TellerData implements Serializable {

    private Long id;
    private Long officeId;
    private Long debitAccountId;
    private Long creditAccountId;
    private String name;

    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private TellerStatus status;
    private Boolean hasTransactions;
    private Boolean hasMappedCashiers;
    private String officeName;
    private Collection<OfficeData> officeOptions;
    private Collection<StaffData> staffOptions;

    /**
     * Creates a new teller data object.
     *
     * @param id
     *            - id of the teller
     * @param officeId
     *            - id of the related office
     * @param debitAccountId
     *            - id of the debit account to use
     * @param creditAccountId
     *            - id of the credit account to use
     * @param name
     *            - name of the teller
     * @param description
     *            - description of the teller
     * @param startDate
     *            - date when the teller becomes available
     * @param endDate
     *            - date when the teller becomes unavailable
     * @param status
     *            - current state of the teller, eg. active, inactive, pending
     * @param hasTransactions
     *            - indicates that this teller already is used to perform postings
     * @param hasMappedCashiers
     *            - indicates that the teller already has @code Cashier}s assigned to it
     * @return the new created {@code TellerData}
     */
    public static TellerData instance(final Long id, final Long officeId, final Long debitAccountId, final Long creditAccountId,
            final String name, final String description, final LocalDate startDate, final LocalDate endDate, final TellerStatus status,
            final String officeName, final Boolean hasTransactions, final Boolean hasMappedCashiers) {
        return new TellerData().setId(id).setOfficeId(officeId).setDebitAccountId(debitAccountId).setCreditAccountId(creditAccountId)
                .setName(name).setDescription(description).setStartDate(startDate).setEndDate(endDate).setStatus(status)
                .setOfficeName(officeName).setHasTransactions(hasTransactions).setHasMappedCashiers(hasMappedCashiers);
    }

    public static TellerData lookup(final Long id, final String name) {
        return new TellerData().setId(id).setName(name);
    }
}
