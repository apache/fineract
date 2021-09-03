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
package org.apache.fineract.portfolio.collateralmanagement.data;

import java.math.BigDecimal;
import java.util.List;

public final class ClientCollateralManagementData {

    private final String name;

    private final BigDecimal quantity;

    private final BigDecimal total;

    private final BigDecimal totalCollateral;

    private final Long clientId;

    private final Long id;

    private final List<LoanTransactionData> loanTransactionData;

    private ClientCollateralManagementData(final String name, final BigDecimal quantity, final BigDecimal total,
            final BigDecimal totalCollateral, final Long clientId, final List<LoanTransactionData> loanTransactionData, final Long id) {
        this.clientId = clientId;
        this.totalCollateral = totalCollateral;
        this.total = total;
        this.name = name;
        this.quantity = quantity;
        this.loanTransactionData = loanTransactionData;
        this.id = id;
    }

    public static ClientCollateralManagementData instance(final String name, final BigDecimal quantity, final BigDecimal total,
            final BigDecimal totalCollateral, final Long clientId, final List<LoanTransactionData> loanTransactionData, final Long id) {
        return new ClientCollateralManagementData(name, quantity, total, totalCollateral, clientId, loanTransactionData, id);
    }

    public String getName() {
        return this.name;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public BigDecimal getQuantity() {
        return this.quantity;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public BigDecimal getTotalCollateral() {
        return this.totalCollateral;
    }

    public Long getId() {
        return this.id;
    }

    public List<LoanTransactionData> getLoanTransactionData() {
        return this.loanTransactionData;
    }

}
