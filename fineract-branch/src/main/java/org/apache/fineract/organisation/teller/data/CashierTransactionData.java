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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.teller.domain.CashierTxnType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public final class CashierTransactionData implements Serializable {

    private Long id;
    private Long cashierId;
    private CashierTxnType txnType;
    private BigDecimal txnAmount;
    private LocalDate txnDate;
    private Long entityId;
    private String entityType;
    private String txnNote;
    private OffsetDateTime createdDate;

    // Template fields
    private Long officeId;
    private String officeName;
    private Long tellerId;
    private String tellerName;
    private String cashierName;
    private CashierData cashierData;
    private LocalDate startDate;
    private LocalDate endDate;
    private Collection<CurrencyData> currencyOptions;

    public static CashierTransactionData instance(final Long id, final Long cashierId, CashierTxnType txnType, final BigDecimal txnAmount,
            final LocalDate txnDate, final String txnNote, final String entityType, final Long entityId, final OffsetDateTime createdDate,
            final Long officeId, final String officeName, final Long tellerId, final String tellerName, final String cashierName,
            final CashierData cashierData, LocalDate startDate, LocalDate endDate) {
        return new CashierTransactionData().setId(id).setCashierId(cashierId).setTxnType(txnType).setTxnAmount(txnAmount)
                .setTxnDate(txnDate).setTxnNote(txnNote).setEntityType(entityType).setEntityId(entityId).setCreatedDate(createdDate)
                .setOfficeId(officeId).setOfficeName(officeName).setTellerId(tellerId).setTellerName(tellerName).setCashierName(cashierName)
                .setCashierData(cashierData).setStartDate(startDate).setEndDate(endDate);
    }

    public static CashierTransactionData template(final Long cashierId, final Long tellerId, final String tellerName, final Long officeId,
            final String officeName, final String cashierName, final CashierData cashierData, LocalDate startDate, LocalDate endDate,
            final Collection<CurrencyData> currencyOptions) {
        return new CashierTransactionData().setCashierId(cashierId).setOfficeId(officeId).setOfficeName(officeName).setTellerId(tellerId)
                .setTellerName(tellerName).setCashierName(cashierName).setCashierData(cashierData).setStartDate(startDate)
                .setEndDate(endDate).setCurrencyOptions(currencyOptions);
    }
}
