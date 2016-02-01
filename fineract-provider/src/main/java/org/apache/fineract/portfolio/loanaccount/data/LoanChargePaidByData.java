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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;

public class LoanChargePaidByData {

    private final Long id;
    private final BigDecimal amount;
    private final Integer installmentNumber;
    private final Long chargeId;
    private final Long transactionId;

    public LoanChargePaidByData(final Long id, final BigDecimal amount, final Integer installmentNumber, final Long chargeId,
            final Long transactionId) {
        this.id = id;
        this.amount = amount;
        this.installmentNumber = installmentNumber;
        this.chargeId = chargeId;
        this.transactionId = transactionId;
    }

    public Long getId() {
        return this.id;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }

    public Long getChargeId() {
        return this.chargeId;
    }

    public Long getTransactionId() {
        return this.transactionId;
    }

}
