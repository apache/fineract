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
package org.apache.fineract.portfolio.loanaccount.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationIncomeType;
import org.apache.fineract.portfolio.charge.domain.ChargeAmortizationPeriodType;

@Entity
@Table(name = "m_loan_deferred_charge_balance")
@Getter
@Setter
public class LoanDeferredChargeBalance extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_charge_id", nullable = false)
    private LoanCharge loanCharge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_transaction_id", nullable = false)
    private LoanTransaction loanTransaction;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "window_end_date")
    private LocalDate windowEndDate;

    @Column(name = "unrecognized_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal unrecognizedAmount;

    @Column(name = "charged_off_amount", scale = 6, precision = 19)
    private BigDecimal chargedOffAmount;

    @Column(name = "amount_adjustment", scale = 6, precision = 19)
    private BigDecimal amountAdjustment;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", length = 32, nullable = false)
    private ChargeAmortizationPeriodType periodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", length = 32)
    private ChargeAmortizationCalculationType calculationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_type", length = 32)
    private ChargeAmortizationIncomeType incomeType;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "is_closed", nullable = false)
    private boolean closed = false;
}
