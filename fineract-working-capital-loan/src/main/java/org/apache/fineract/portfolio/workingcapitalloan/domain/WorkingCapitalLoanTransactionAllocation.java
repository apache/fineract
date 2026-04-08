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

@Entity
@Table(name = "m_wc_loan_transaction_allocation", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "wc_loan_transaction_id" }, name = "uq_m_wc_loan_transaction_allocation_transaction_id") })
@Getter
public class WorkingCapitalLoanTransactionAllocation extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wc_loan_transaction_id", nullable = false, unique = true)
    private WorkingCapitalLoanTransaction wcLoanTransaction;

    @Column(name = "principal_portion", scale = 6, precision = 19)
    @Setter
    private BigDecimal principalPortion;

    @Column(name = "fee_charges_portion", scale = 6, precision = 19)
    @Setter
    private BigDecimal feeChargesPortion;

    @Column(name = "penalty_charges_portion", scale = 6, precision = 19)
    @Setter
    private BigDecimal penaltyChargesPortion;

    @Version
    @Column(name = "version")
    private Integer version;

    protected WorkingCapitalLoanTransactionAllocation() {}

    public static WorkingCapitalLoanTransactionAllocation forDisbursement(final WorkingCapitalLoanTransaction transaction,
            final BigDecimal principalAmount) {
        final WorkingCapitalLoanTransactionAllocation allocation = new WorkingCapitalLoanTransactionAllocation();
        allocation.wcLoanTransaction = transaction;
        allocation.principalPortion = principalAmount != null ? principalAmount : BigDecimal.ZERO;
        allocation.feeChargesPortion = BigDecimal.ZERO;
        allocation.penaltyChargesPortion = BigDecimal.ZERO;
        return allocation;
    }
}
