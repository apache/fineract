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
package org.apache.fineract.organisation.office.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object for office transactions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public final class OfficeTransactionData {

    @SuppressWarnings("unused")
    private Long id;
    @SuppressWarnings("unused")
    private LocalDate transactionDate;
    @SuppressWarnings("unused")
    private Long fromOfficeId;
    @SuppressWarnings("unused")
    private String fromOfficeName;
    @SuppressWarnings("unused")
    private Long toOfficeId;
    @SuppressWarnings("unused")
    private String toOfficeName;
    @SuppressWarnings("unused")
    private CurrencyData currency;
    @SuppressWarnings("unused")
    private BigDecimal transactionAmount;
    @SuppressWarnings("unused")
    private String description;
    @SuppressWarnings("unused")
    private Collection<CurrencyData> currencyOptions;
    @SuppressWarnings("unused")
    private Collection<OfficeData> allowedOffices;

    public static OfficeTransactionData instance(final Long id, final LocalDate transactionDate, final Long fromOfficeId,
            final String fromOfficeName, final Long toOfficeId, final String toOfficeName, final CurrencyData currency,
            final BigDecimal transactionAmount, final String description) {
        return new OfficeTransactionData().setId(id).setTransactionDate(transactionDate).setFromOfficeId(fromOfficeId)
                .setFromOfficeName(fromOfficeName).setToOfficeId(toOfficeId).setToOfficeName(toOfficeName).setCurrency(currency)
                .setTransactionAmount(transactionAmount).setDescription(description);
    }

    public static OfficeTransactionData template(final LocalDate transactionDate, final Collection<OfficeData> parentLookups,
            final Collection<CurrencyData> currencyOptions) {
        return new OfficeTransactionData().setTransactionDate(transactionDate).setAllowedOffices(parentLookups)
                .setCurrencyOptions(currencyOptions);
    }
}
