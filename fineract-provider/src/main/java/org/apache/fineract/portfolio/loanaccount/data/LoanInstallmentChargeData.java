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

import org.joda.time.LocalDate;

public class LoanInstallmentChargeData {

    private final Integer installmentNumber;
    private final LocalDate dueDate;
    private final BigDecimal amount;
    private final BigDecimal amountOutstanding;
    private final BigDecimal amountWaived;
    private final boolean paid;
    private final boolean waived;

    private BigDecimal amountAccrued;
    private BigDecimal amountUnrecognized;

    public LoanInstallmentChargeData(final Integer installmentNumber, final LocalDate dueDate, final BigDecimal amount,
            final BigDecimal amountOutstanding, BigDecimal amountWaived, final boolean paid, final boolean waived) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.amount = amount;
        this.amountOutstanding = amountOutstanding;
        this.paid = paid;
        this.waived = waived;
        this.amountWaived = amountWaived;
    }

    public LoanInstallmentChargeData(final Integer installmentNumber, final LocalDate dueDate, final BigDecimal amount,
            final BigDecimal amountOutstanding, final BigDecimal amountWaived, final boolean paid, final boolean waived,
            final BigDecimal amountAccrued) {
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.amount = amount;
        this.amountOutstanding = amountOutstanding;
        this.paid = paid;
        this.waived = waived;
        this.amountWaived = amountWaived;
        this.amountAccrued = amountAccrued;
    }

    public LoanInstallmentChargeData(final LoanInstallmentChargeData installmentChargeData, final BigDecimal amountUnrecognized) {
        this.installmentNumber = installmentChargeData.installmentNumber;
        this.dueDate = installmentChargeData.dueDate;
        this.amount = installmentChargeData.amount;
        this.amountOutstanding = installmentChargeData.amountOutstanding;
        this.paid = installmentChargeData.paid;
        this.waived = installmentChargeData.waived;
        this.amountWaived = installmentChargeData.amountWaived;
        this.amountAccrued = installmentChargeData.amountAccrued;
        this.amountUnrecognized = amountUnrecognized;
    }

    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }

    public LocalDate getDueDate() {
        return this.dueDate;
    }

    public boolean isPaymentPending() {
        return !(this.paid || this.waived);
    }

    public BigDecimal getAmountOutstanding() {
        return this.amountOutstanding;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public BigDecimal getAmountWaived() {
        return this.amountWaived;
    }

    public BigDecimal getAmountAccrued() {
        return this.amountAccrued;
    }

    public BigDecimal getAmountUnrecognized() {
        return this.amountUnrecognized;
    }

}
