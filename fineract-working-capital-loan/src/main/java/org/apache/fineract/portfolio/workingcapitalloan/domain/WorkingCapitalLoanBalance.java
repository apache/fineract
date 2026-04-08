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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

/**
 * Stores all balances of a working capital loan (one row per loan). Updated from allocations; accounting depends on
 * this.
 */
@Entity
@Table(name = "m_wc_loan_balance", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "wc_loan_id" }, name = "uq_m_wc_loan_balance_loan_id") })
@Getter
public class WorkingCapitalLoanBalance extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wc_loan_id", nullable = false, unique = true)
    private WorkingCapitalLoan wcLoan;

    @Column(name = "principal_outstanding", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal principalOutstanding = BigDecimal.ZERO;

    @Column(name = "total_paid_principal", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal totalPaidPrincipal = BigDecimal.ZERO;

    @Column(name = "total_payment", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal totalPayment = BigDecimal.ZERO;

    @Column(name = "realized_income", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal realizedIncome = BigDecimal.ZERO;

    @Column(name = "unrealized_income", scale = 6, precision = 19, nullable = false)
    @Setter
    private BigDecimal unrealizedIncome = BigDecimal.ZERO;

    @Version
    @Column(name = "version")
    private Integer version;

    protected WorkingCapitalLoanBalance() {}

    public static WorkingCapitalLoanBalance createFor(final WorkingCapitalLoan loan) {
        final WorkingCapitalLoanBalance balance = new WorkingCapitalLoanBalance();
        balance.wcLoan = loan;
        return balance;
    }
}
