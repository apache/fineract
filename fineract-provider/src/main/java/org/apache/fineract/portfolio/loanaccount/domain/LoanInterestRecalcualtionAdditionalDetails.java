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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Entity
@Table(name = "m_loan_interest_recalculation_additional_details")
public class LoanInterestRecalcualtionAdditionalDetails extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_repayment_schedule_id", nullable = false)
    private LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment;

    @Temporal(TemporalType.DATE)
    @Column(name = "effective_date")
    private Date effectiveDate;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    protected LoanInterestRecalcualtionAdditionalDetails() {

    }

    public LoanInterestRecalcualtionAdditionalDetails(final LocalDate effectiveDate, final BigDecimal amount) {
        if (effectiveDate != null) {
            this.effectiveDate = Date.from(effectiveDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        this.amount = amount;
    }

    public LocalDate getEffectiveDate() {
        return LocalDate.ofInstant(this.effectiveDate.toInstant(), DateUtils.getDateTimeZoneOfTenant());
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public LoanRepaymentScheduleInstallment getLoanRepaymentScheduleInstallment() {
        return loanRepaymentScheduleInstallment;
    }

    public void setLoanRepaymentScheduleInstallment(LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment) {
        this.loanRepaymentScheduleInstallment = loanRepaymentScheduleInstallment;
    }
}
