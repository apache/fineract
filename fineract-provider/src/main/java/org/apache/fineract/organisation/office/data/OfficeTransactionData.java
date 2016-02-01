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
import java.util.Collection;

import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.joda.time.LocalDate;

/**
 * Immutable data object for office transactions.
 */
public class OfficeTransactionData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final LocalDate transactionDate;
    @SuppressWarnings("unused")
    private final Long fromOfficeId;
    @SuppressWarnings("unused")
    private final String fromOfficeName;
    @SuppressWarnings("unused")
    private final Long toOfficeId;
    @SuppressWarnings("unused")
    private final String toOfficeName;
    @SuppressWarnings("unused")
    private final CurrencyData currency;
    @SuppressWarnings("unused")
    private final BigDecimal transactionAmount;
    @SuppressWarnings("unused")
    private final String description;
    @SuppressWarnings("unused")
    private final Collection<CurrencyData> currencyOptions;
    @SuppressWarnings("unused")
    private final Collection<OfficeData> allowedOffices;

    public static OfficeTransactionData instance(final Long id, final LocalDate transactionDate, final Long fromOfficeId,
            final String fromOfficeName, final Long toOfficeId, final String toOfficeName, final CurrencyData currency,
            final BigDecimal transactionAmount, final String description) {
        return new OfficeTransactionData(id, transactionDate, fromOfficeId, fromOfficeName, toOfficeId, toOfficeName, currency,
                transactionAmount, description, null, null);
    }

    public static OfficeTransactionData template(final LocalDate transactionDate, final Collection<OfficeData> parentLookups,
            final Collection<CurrencyData> currencyOptions) {
        return new OfficeTransactionData(null, transactionDate, null, null, null, null, null, null, null, parentLookups, currencyOptions);
    }

    private OfficeTransactionData(final Long id, final LocalDate transactionDate, final Long fromOfficeId, final String fromOfficeName,
            final Long toOfficeId, final String toOfficeName, final CurrencyData currency, final BigDecimal transactionAmount,
            final String description, final Collection<OfficeData> allowedOffices, final Collection<CurrencyData> currencyOptions) {
        this.id = id;
        this.fromOfficeId = fromOfficeId;
        this.fromOfficeName = fromOfficeName;
        this.toOfficeId = toOfficeId;
        this.toOfficeName = toOfficeName;
        this.currency = currency;
        this.transactionAmount = transactionAmount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.allowedOffices = allowedOffices;
        this.currencyOptions = currencyOptions;
    }
}