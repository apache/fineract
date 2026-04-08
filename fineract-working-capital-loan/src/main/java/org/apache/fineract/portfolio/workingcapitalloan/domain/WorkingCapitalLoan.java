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
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatusConverter;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_wc_loan", uniqueConstraints = { @UniqueConstraint(columnNames = { "account_no" }, name = "wc_loan_account_no_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "wc_loan_externalid_UNIQUE") })
@Getter
public class WorkingCapitalLoan extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Version
    int version;

    @Setter
    @Column(name = "last_closed_business_date")
    private LocalDate lastClosedBusinessDate;

    @Setter
    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNumber;

    @Setter
    @Column(name = "external_id")
    private ExternalId externalId;

    @Setter
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Setter
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private WorkingCapitalLoanProduct loanProduct;

    @Setter
    @Column(name = "loan_status_id", nullable = false)
    @Convert(converter = LoanStatusConverter.class)
    private LoanStatus loanStatus;

    /**
     * Sequential counter of all WC loans for this client
     */
    @Setter
    @Column(name = "loan_counter")
    private Integer loanCounter;

    /**
     * Sequential counter of WC loans per client+product, used as loan cycle in summaries.
     */
    @Setter
    @Column(name = "loan_product_counter")
    private Integer loanProductCounter;

    @Setter
    @Column(name = "submittedon_date")
    private LocalDate submittedOnDate;

    @Setter
    @Column(name = "rejectedon_date")
    private LocalDate rejectedOnDate;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejectedon_userid")
    private AppUser rejectedBy;

    @Setter
    @Column(name = "approvedon_date")
    private LocalDate approvedOnDate;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvedon_userid")
    private AppUser approvedBy;

    @Setter
    @Column(name = "closedon_date")
    private LocalDate closedOnDate;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closedon_userid")
    private AppUser closedBy;

    @Setter
    @Column(name = "expected_maturedon_date")
    private LocalDate expectedMaturityDate;

    /**
     * Date when the loan was fully paid (matured). Update only when loan is fully paid.
     */
    @Setter
    @Column(name = "maturedon_date")
    private LocalDate maturedOnDate;

    @Setter
    @Column(name = "principal_amount_proposed", scale = 6, precision = 19, nullable = false)
    private BigDecimal proposedPrincipal;

    @Setter
    @Column(name = "approved_principal", scale = 6, precision = 19, nullable = false)
    private BigDecimal approvedPrincipal;

    @Setter
    @OneToOne(mappedBy = "wcLoan", cascade = CascadeType.ALL, orphanRemoval = true)
    private WorkingCapitalLoanBalance balance;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "wcLoan", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkingCapitalLoanPaymentAllocationRule> paymentAllocationRules = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "wcLoan", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkingCapitalLoanDisbursementDetails> disbursementDetails = new ArrayList<>();

    @OrderBy(value = "transactionDate, createdDate, id")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "wcLoan", orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WorkingCapitalLoanTransaction> transactions = new ArrayList<>();

    @Setter
    @Embedded
    private WorkingCapitalLoanProductRelatedDetails loanProductRelatedDetails;

    public Long getOfficeId() {
        return client != null && client.getOffice() != null ? client.getOffice().getId() : null;
    }

    public Long getClientId() {
        return client != null ? client.getId() : null;
    }
}
