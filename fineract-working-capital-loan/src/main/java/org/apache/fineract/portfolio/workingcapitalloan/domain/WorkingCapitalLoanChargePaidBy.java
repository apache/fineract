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
package org.apache.fineract.portfolio.workingcapitalloan.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

/**
 * Records that a specific working capital loan {@link WorkingCapitalLoanTransaction} settled a specific
 * {@link WorkingCapitalLoanCharge} for a specific amount - the transaction&times;charge cross-tabulation that the
 * aggregate {@link WorkingCapitalLoanTransactionAllocation} portions and per-charge {@code amountPaid} cannot express
 * on their own.
 *
 * <p>
 * A transaction settles a given charge at most once, so the pair is unique: what one transaction put towards one charge
 * belongs in a single row's amount rather than spread over several. Reprocessing deletes a loan's rows before replaying
 * them, so nothing reachable today writes a duplicate - the constraint is there to fail a future path that tries.
 */
@Entity
@Table(name = "m_wc_loan_charge_paid_by", uniqueConstraints = { @UniqueConstraint(columnNames = { "wc_loan_transaction_id",
        "wc_loan_charge_id" }, name = "uq_m_wc_loan_charge_paid_by_transaction_charge") })
@Getter
public class WorkingCapitalLoanChargePaidBy extends AbstractPersistableCustom<Long> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wc_loan_transaction_id", nullable = false)
    private WorkingCapitalLoanTransaction wcLoanTransaction;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wc_loan_charge_id", nullable = false)
    private WorkingCapitalLoanCharge wcLoanCharge;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal amount;

    protected WorkingCapitalLoanChargePaidBy() {}

    public WorkingCapitalLoanChargePaidBy(final WorkingCapitalLoanTransaction wcLoanTransaction,
            final WorkingCapitalLoanCharge wcLoanCharge, final BigDecimal amount) {
        this.wcLoanTransaction = wcLoanTransaction;
        this.wcLoanCharge = wcLoanCharge;
        this.amount = amount;
    }
}
