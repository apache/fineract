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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionTypeConverter;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;

@Entity
@Table(name = "m_wc_loan_transaction", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "external_id" }, name = "wc_loan_transaction_external_id_UNIQUE") })
@Getter
public class WorkingCapitalLoanTransaction extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "wc_loan_id", nullable = false)
    private WorkingCapitalLoan wcLoan;

    @Column(name = "transaction_type_id", nullable = false)
    @Convert(converter = LoanTransactionTypeConverter.class)
    private LoanTransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "submitted_on_date", nullable = false)
    private LocalDate submittedOnDate;

    @Column(name = "transaction_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal transactionAmount;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_detail_id")
    private PaymentDetail paymentDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classification_cv_id")
    private CodeValue classification;

    @Column(name = "external_id", length = 100, unique = true)
    @Setter
    private ExternalId externalId;

    @Column(name = "is_reversed", nullable = false)
    @Setter
    private boolean reversed;

    @Column(name = "reversal_external_id", length = 100, unique = true)
    @Setter
    private ExternalId reversalExternalId;

    @Column(name = "reversed_on_date")
    @Setter
    private LocalDate reversedOnDate;

    @Version
    @Column(name = "version")
    private Integer version;

    @OneToOne(mappedBy = "wcLoanTransaction", cascade = CascadeType.ALL, orphanRemoval = true)
    private WorkingCapitalLoanTransactionAllocation allocation;

    protected WorkingCapitalLoanTransaction() {}

    public LoanTransactionType getTypeOf() {
        return transactionType;
    }

    public static WorkingCapitalLoanTransaction disbursement(final WorkingCapitalLoan loan, final BigDecimal amount,
            final PaymentDetail paymentDetail, final LocalDate disbursementDate, final ExternalId externalId) {
        final WorkingCapitalLoanTransaction txn = new WorkingCapitalLoanTransaction();
        txn.wcLoan = loan;
        txn.transactionType = LoanTransactionType.DISBURSEMENT;
        txn.transactionDate = disbursementDate;
        txn.submittedOnDate = disbursementDate;
        txn.transactionAmount = amount;
        txn.paymentDetail = paymentDetail;
        txn.externalId = externalId != null ? externalId : ExternalId.empty();
        txn.reversed = false;
        txn.reversalExternalId = null;
        txn.reversedOnDate = null;
        return txn;
    }
}
