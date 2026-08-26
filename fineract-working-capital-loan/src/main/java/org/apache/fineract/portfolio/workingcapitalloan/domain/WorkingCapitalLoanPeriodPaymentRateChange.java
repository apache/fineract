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
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "m_wc_loan_period_payment_rate_change")
public class WorkingCapitalLoanPeriodPaymentRateChange extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wc_loan_id", nullable = false)
    private WorkingCapitalLoan workingCapitalLoan;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "previous_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal previousRate;

    @Column(name = "new_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal newRate;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Column(name = "reversed_on_date")
    private LocalDate reversedOnDate;

    @Column(name = "submitted_on_date", nullable = false)
    private LocalDate submittedOnDate;

    @Version
    private int version;

    public static WorkingCapitalLoanPeriodPaymentRateChange create(final WorkingCapitalLoan loan, final LocalDate effectiveDate,
            final BigDecimal previousRate, final BigDecimal newRate) {
        final WorkingCapitalLoanPeriodPaymentRateChange change = new WorkingCapitalLoanPeriodPaymentRateChange();
        change.workingCapitalLoan = loan;
        change.effectiveDate = effectiveDate;
        change.previousRate = previousRate;
        change.newRate = newRate;
        change.reversed = false;
        change.submittedOnDate = DateUtils.getBusinessLocalDate();
        return change;
    }

    public void reverse(final LocalDate reversalDate) {
        this.reversed = true;
        this.reversedOnDate = reversalDate;
    }
}
