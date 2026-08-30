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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.office.domain.Office;
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

    /** Principal requested on the application. Set at submission, updated on modification, never changed afterwards. */
    @Setter
    @Column(name = "principal_amount_proposed", scale = 6, precision = 19, nullable = false)
    private BigDecimal proposedPrincipal;

    /**
     * Principal granted at approval, defaulting to the proposed one. Zero before approval and after undoing it, which
     * is a deliberate divergence from classic Fineract, where the approved principal mirrors the proposed one until it
     * is actually approved.
     */
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

    @Setter
    @Embedded
    private WorkingCapitalLoanProductRelatedDetails loanProductRelatedDetails;

    @Setter
    @Column(name = "total_payment_volume", scale = 6, precision = 19, nullable = false)
    private BigDecimal totalPaymentVolume;

    /**
     * Charge-off is a pure accounting tag: it does not affect the portfolio (balance, schedule) and the loan stays
     * ACTIVE. Once set, the tag is cleared by an explicit undo, or automatically when reprocessing finds that preceding
     * money-movers have already cleared the outstanding at the charge-off point.
     */
    @Column(name = "is_charged_off", nullable = false)
    private boolean chargedOff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_off_reason_cv_id")
    private CodeValue chargeOffReason;

    @Column(name = "charged_off_on_date")
    private LocalDate chargedOffOnDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charged_off_by_userid")
    private AppUser chargedOffBy;

    /**
     * Flags the loan as fraudulent. When charged off, a fraudulent loan is routed to the charge-off fraud expense
     * account instead of the regular charge-off expense account. Independent of the charge-off state itself.
     */
    @Setter
    @Column(name = "is_fraud", nullable = false)
    private boolean fraud = false;

    /**
     * Write-off is terminal: it zeroes the outstanding balance and moves the loan to {@code CLOSED_WRITTEN_OFF}. These
     * audit fields record when it happened and the optional reason; they are cleared by an undo. All write-off
     * behaviour (status transition, balance zeroing, audit) lives in {@code WorkingCapitalLoanWriteOffDomainService}.
     */
    @Setter
    @Column(name = "written_off_on_date")
    private LocalDate writtenOffOnDate;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "write_off_reason_cv_id")
    private CodeValue writeOffReason;

    public Long getOfficeId() {
        return client != null && client.getOffice() != null ? client.getOffice().getId() : null;
    }

    public Long getClientId() {
        return client != null ? client.getId() : null;
    }

    /**
     * Mirrors {@code Loan.getOffice()} minus the group branch: working capital loans are client-only, so there is no
     * {@code group} to fall back on.
     */
    public Office getOffice() {
        return client != null ? client.getOffice() : null;
    }

    public Long productId() {
        return loanProduct != null ? loanProduct.getId() : null;
    }

    /**
     * The {@code Loan} spelling of the status accessor. Lombok generates {@code getLoanStatus()} from the field; this
     * is the same value under the name the {@code Loan}-shaped call sites use.
     */
    public LoanStatus getStatus() {
        return loanStatus;
    }

    /**
     * Mirrors {@code Loan.getCurrency()}: {@code loanProductRelatedDetails} is WC's counterpart of
     * {@code loanRepaymentScheduleDetail}, the copy of the product terms the loan owns.
     */
    public MonetaryCurrency getCurrency() {
        return loanProductRelatedDetails != null ? loanProductRelatedDetails.getCurrency() : null;
    }

    public String getCurrencyCode() {
        final MonetaryCurrency currency = getCurrency();
        return currency != null ? currency.getCode() : null;
    }

    /**
     * Mirrors {@code Loan.getPrincipal()}: the principal currently in force, which the write service keeps in step with
     * the lifecycle - the proposed amount on submission, the approved amount on approval, the disbursed amount on
     * disbursement. Not the outstanding principal, which lives on {@link WorkingCapitalLoanBalance}.
     */
    public Money getPrincipal() {
        final MonetaryCurrency currency = getCurrency();
        return currency != null ? Money.of(currency, loanProductRelatedDetails.getPrincipal()) : null;
    }

    public boolean isSubmittedAndPendingApproval() {
        return loanStatus != null && loanStatus.isSubmittedAndPendingApproval();
    }

    public boolean isNotSubmittedAndPendingApproval() {
        return !isSubmittedAndPendingApproval();
    }

    public boolean isApproved() {
        return loanStatus != null && loanStatus.isApproved();
    }

    /**
     * Mirrors {@code Loan.isOpen()}: the loan is in the {@code ACTIVE} state.
     */
    public boolean isOpen() {
        return loanStatus != null && loanStatus.isActive();
    }

    public boolean isClosed() {
        return (loanStatus != null && loanStatus.isClosed()) || isCancelled();
    }

    public boolean isClosedObligationsMet() {
        return loanStatus != null && loanStatus.isClosedObligationsMet();
    }

    public boolean isClosedWrittenOff() {
        return loanStatus != null && loanStatus.isClosedWrittenOff();
    }

    public boolean isOverpaid() {
        return loanStatus != null && loanStatus.isOverpaid();
    }

    /**
     * Mirrors {@code Loan.isCancelled()}. Working capital loans have no transition to {@code WITHDRAWN_BY_CLIENT}
     * today, so only the rejected half is reachable; the two-term definition is kept so the predicate stays correct if
     * withdrawal is ever added.
     */
    public boolean isCancelled() {
        return loanStatus != null && (loanStatus.isRejected() || loanStatus.isWithdrawnByClient());
    }

    /**
     * Disbursement is decided by the disbursement detail rows, never by the status: a written-off loan is
     * {@code CLOSED_WRITTEN_OFF} yet is still disbursed.
     */
    public boolean isDisbursed() {
        return disbursementDetails.stream() //
                .map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate) //
                .anyMatch(Objects::nonNull);
    }

    public boolean isNotDisbursed() {
        return !isDisbursed();
    }

    /**
     * Mirrors {@code Loan.getDisbursedLoanDisbursementDetails()}: the detail rows that were actually disbursed, in no
     * guaranteed order - {@code disbursementDetails} is an unordered bag.
     */
    public List<WorkingCapitalLoanDisbursementDetails> getDisbursedLoanDisbursementDetails() {
        return disbursementDetails.stream() //
                .filter(it -> it.getActualDisbursementDate() != null) //
                .toList();
    }

    /**
     * The earliest non-null actual disbursement date, or {@code null} when nothing was disbursed yet.
     */
    public LocalDate getFirstActualDisbursementDate() {
        return disbursementDetails.stream() //
                .map(WorkingCapitalLoanDisbursementDetails::getActualDisbursementDate) //
                .filter(Objects::nonNull) //
                .min(LocalDate::compareTo) //
                .orElse(null);
    }

    /**
     * Marks the account as charged-off. The {@code chargeOffReason} is optional and may be {@code null}.
     */
    public void markAsChargedOff(final LocalDate chargedOffOnDate, final AppUser chargedOffBy, final CodeValue chargeOffReason) {
        this.chargedOff = true;
        this.chargedOffOnDate = chargedOffOnDate;
        this.chargedOffBy = chargedOffBy;
        this.chargeOffReason = chargeOffReason;
    }

    /**
     * Reverses the charge-off tag. Used by explicit undo and by auto-lift when reprocessing finds nothing left to
     * charge off.
     */
    public void liftChargeOff() {
        this.chargedOff = false;
        this.chargedOffOnDate = null;
        this.chargedOffBy = null;
        this.chargeOffReason = null;
    }

    public Long fetchChargeOffReasonId() {
        return this.chargeOffReason != null ? this.chargeOffReason.getId() : null;
    }

    /**
     * Returns the actual amount for the chronologically first disbursement, or zero when no disbursement amount is
     * available. Undisbursed detail rows are ignored, just as they are for {@link #getFirstActualDisbursementDate()}.
     */
    public BigDecimal getFirstActualDisbursementAmount() {
        return Optional.ofNullable(getFirstActualDisbursement()) //
                .map(WorkingCapitalLoanDisbursementDetails::getActualAmount) //
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Finds the disbursement detail with the earliest non-null actual disbursement date. When multiple persisted
     * details share that date, the detail ID is used as a deterministic tie-breaker. The disbursement detail collection
     * is otherwise unordered, so collection position is never used as the ordering rule.
     *
     * @return the earliest actual disbursement detail, or {@code null} when no detail has been disbursed
     */
    public WorkingCapitalLoanDisbursementDetails getFirstActualDisbursement() {
        return disbursementDetails.stream() //
                .filter(detail -> detail.getActualDisbursementDate() != null) //
                .min(WorkingCapitalLoanDisbursementDetailsComparator.ACTUAL_DISBURSEMENT_ORDER) //
                .orElse(null);
    }
}
