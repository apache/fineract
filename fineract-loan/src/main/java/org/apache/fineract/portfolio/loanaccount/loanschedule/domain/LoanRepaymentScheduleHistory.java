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
package org.apache.fineract.portfolio.loanaccount.loanschedule.domain;

import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.CREATED_DATE_DB_FIELD;
import static org.apache.fineract.infrastructure.core.domain.AuditableFieldsConstants.LAST_MODIFIED_DATE_DB_FIELD;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;

@Entity
@Table(name = "m_loan_repayment_schedule_history")
public class LoanRepaymentScheduleHistory extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @OneToOne(optional = true)
    @JoinColumn(name = "loan_reschedule_request_id")
    private LoanRescheduleRequest loanRescheduleRequest;

    @Column(name = "installment", nullable = false)
    private Integer installmentNumber;

    @Column(name = "fromdate", nullable = true)
    private LocalDate fromDate;

    @Column(name = "duedate", nullable = false)
    private LocalDate dueDate;

    @Column(name = "principal_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal principal;

    @Column(name = "interest_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestCharged;

    @Column(name = "fee_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesCharged;

    @Column(name = "penalty_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyCharges;

    @Column(name = "created_date")
    private LocalDateTime oldCreatedOnDate;

    @Column(name = "createdby_id")
    private Long createdByUser;

    @Column(name = "lastmodifiedby_id")
    private Long lastModifiedByUser;

    @Column(name = "lastmodified_date")
    private LocalDateTime oldLastModifiedOnDate;

    @Column(name = CREATED_DATE_DB_FIELD)
    private OffsetDateTime createdDate;

    @Column(name = LAST_MODIFIED_DATE_DB_FIELD)
    private OffsetDateTime lastModifiedDate;

    @Column(name = "version")
    private Integer version;

    /**
     * LoanRepaymentScheduleHistory constructor
     **/
    protected LoanRepaymentScheduleHistory() {}

    /**
     * LoanRepaymentScheduleHistory constructor
     **/
    private LoanRepaymentScheduleHistory(final Loan loan, final LoanRescheduleRequest loanRescheduleRequest,
            final Integer installmentNumber, final LocalDate fromDate, final LocalDate dueDate, final BigDecimal principal,
            final BigDecimal interestCharged, final BigDecimal feeChargesCharged, final BigDecimal penaltyCharges,
            final LocalDateTime oldCreatedOnDate, final Long createdByUser, final Long lastModifiedByUser,
            final LocalDateTime oldLastModifiedOnDate, final Integer version, final OffsetDateTime createdDate,
            final OffsetDateTime lastModifiedDate) {

        this.loan = loan;
        this.loanRescheduleRequest = loanRescheduleRequest;
        this.installmentNumber = installmentNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        this.principal = principal;
        this.interestCharged = interestCharged;
        this.feeChargesCharged = feeChargesCharged;
        this.penaltyCharges = penaltyCharges;
        this.oldCreatedOnDate = oldCreatedOnDate;
        this.createdByUser = createdByUser;
        this.lastModifiedByUser = lastModifiedByUser;
        this.oldLastModifiedOnDate = oldLastModifiedOnDate;
        this.version = version;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * @return an instance of the LoanRepaymentScheduleHistory class
     **/
    public static LoanRepaymentScheduleHistory instance(final Loan loan, final LoanRescheduleRequest loanRescheduleRequest,
            final Integer installmentNumber, final LocalDate fromDate, final LocalDate dueDate, final BigDecimal principal,
            final BigDecimal interestCharged, final BigDecimal feeChargesCharged, final BigDecimal penaltyCharges,
            final LocalDateTime oldCreatedOnDate, final Long createdByUser, final Long lastModifiedByUser,
            final LocalDateTime oldLastModifiedOnDate, final Integer version, final OffsetDateTime createdDate,
            final OffsetDateTime lastModifiedDate) {

        return new LoanRepaymentScheduleHistory(loan, loanRescheduleRequest, installmentNumber, fromDate, dueDate, principal,
                interestCharged, feeChargesCharged, penaltyCharges, oldCreatedOnDate, createdByUser, lastModifiedByUser,
                oldLastModifiedOnDate, version, createdDate, lastModifiedDate);

    }

}
