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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.service.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public final class CashierTransactionsWithSummaryData implements Serializable {

    private BigDecimal sumCashAllocation;
    private BigDecimal sumInwardCash;
    private BigDecimal sumOutwardCash;
    private BigDecimal sumCashSettlement;
    private BigDecimal netCash;
    private String officeName;
    private long tellerId;
    private String tellerName;
    private long cashierId;
    private String cashierName;

    private Page<CashierTransactionData> cashierTransactions;

    public static CashierTransactionsWithSummaryData instance(final Page<CashierTransactionData> cashierTransactions,
            final BigDecimal sumCashAllocation, final BigDecimal sumInwardCash, final BigDecimal sumOutwardCash,
            final BigDecimal sumCashSettlement, final String officeName, final Long tellerId, final String tellerName, final Long cashierId,
            final String cashierName) {

        final BigDecimal netCash = sumCashAllocation.add(sumInwardCash).subtract(sumOutwardCash).subtract(sumCashSettlement);
        return new CashierTransactionsWithSummaryData().setCashierTransactions(cashierTransactions).setSumCashAllocation(sumCashAllocation)
                .setSumInwardCash(sumInwardCash).setSumOutwardCash(sumOutwardCash).setSumCashSettlement(sumCashSettlement)
                .setNetCash(netCash).setOfficeName(officeName).setTellerId(tellerId).setTellerName(tellerName).setCashierId(cashierId)
                .setCashierName(cashierName);
    }
}
