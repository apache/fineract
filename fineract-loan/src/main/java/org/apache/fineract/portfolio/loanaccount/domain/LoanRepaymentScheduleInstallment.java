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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModelPeriod;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecks;

@Getter
@Setter
@Entity
@Table(name = "m_loan_repayment_schedule")
public class LoanRepaymentScheduleInstallment extends AbstractAuditableWithUTCDateTimeCustom<Long>
        implements Comparable<LoanRepaymentScheduleInstallment> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", referencedColumnName = "id")
    private Loan loan;

    @Column(name = "installment", nullable = false)
    private Integer installmentNumber;

    @Column(name = "fromdate")
    private LocalDate fromDate;

    @Column(name = "duedate", nullable = false)
    private LocalDate dueDate;

    @Column(name = "principal_amount", scale = 6, precision = 19)
    private BigDecimal principal;

    @Column(name = "principal_completed_derived", scale = 6, precision = 19)
    private BigDecimal principalCompleted;

    @Column(name = "principal_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal principalWrittenOff;

    @Column(name = "interest_amount", scale = 6, precision = 19)
    private BigDecimal interestCharged;

    @Column(name = "interest_completed_derived", scale = 6, precision = 19)
    private BigDecimal interestPaid;

    @Column(name = "interest_waived_derived", scale = 6, precision = 19)
    private BigDecimal interestWaived;

    @Column(name = "interest_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal interestWrittenOff;

    @Column(name = "accrual_interest_derived", scale = 6, precision = 19)
    private BigDecimal interestAccrued;

    @Column(name = "reschedule_interest_portion", scale = 6, precision = 19)
    private BigDecimal rescheduleInterestPortion;

    @Column(name = "fee_charges_amount", scale = 6, precision = 19)
    private BigDecimal feeChargesCharged;

    @Column(name = "fee_charges_completed_derived", scale = 6, precision = 19)
    private BigDecimal feeChargesPaid;

    @Column(name = "fee_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal feeChargesWrittenOff;

    @Column(name = "fee_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal feeChargesWaived;

    @Column(name = "accrual_fee_charges_derived", scale = 6, precision = 19)
    private BigDecimal feeAccrued;

    @Column(name = "penalty_charges_amount", scale = 6, precision = 19)
    private BigDecimal penaltyCharges;

    @Column(name = "penalty_charges_completed_derived", scale = 6, precision = 19)
    private BigDecimal penaltyChargesPaid;

    @Column(name = "penalty_charges_writtenoff_derived", scale = 6, precision = 19)
    private BigDecimal penaltyChargesWrittenOff;

    @Column(name = "penalty_charges_waived_derived", scale = 6, precision = 19)
    private BigDecimal penaltyChargesWaived;

    @Column(name = "accrual_penalty_charges_derived", scale = 6, precision = 19)
    private BigDecimal penaltyAccrued;

    @Column(name = "total_paid_in_advance_derived", scale = 6, precision = 19)
    private BigDecimal totalPaidInAdvance;

    @Column(name = "total_paid_late_derived", scale = 6, precision = 19)
    private BigDecimal totalPaidLate;

    @Column(name = "completed_derived", nullable = false)
    private boolean obligationsMet;

    @Column(name = "obligations_met_on_date")
    private LocalDate obligationsMetOnDate;

    @Column(name = "recalculated_interest_component", nullable = false)
    private boolean recalculatedInterestComponent;

    @Column(name = "is_additional", nullable = false)
    private boolean additional;

    // TODO: At some point in time this database column needs to be renamed to credited_principal using the following
    // approach
    // https://blog.thepete.net/blog/2023/12/05/expand/contract-making-a-breaking-change-without-a-big-bang/
    @Column(name = "credits_amount", scale = 6, precision = 19)
    private BigDecimal creditedPrincipal;

    @Column(name = "credited_interest", scale = 6, precision = 19)
    private BigDecimal creditedInterest;

    @Column(name = "credited_fee", scale = 6, precision = 19)
    private BigDecimal creditedFee;

    @Column(name = "credited_penalty", scale = 6, precision = 19)
    private BigDecimal creditedPenalty;

    @Column(name = "is_down_payment", nullable = false)
    private boolean isDownPayment;

    @Column(name = "is_re_aged", nullable = false)
    private boolean isReAged;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "loanRepaymentScheduleInstallment")
    private Set<LoanInterestRecalcualtionAdditionalDetails> loanCompoundingDetails = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "loanRepaymentScheduleInstallment")
    private Set<PostDatedChecks> postDatedChecks = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "installment")
    private Set<LoanInstallmentCharge> installmentCharges = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "installment")
    private Set<LoanTransactionToRepaymentScheduleMapping> loanTransactionToRepaymentScheduleMappings = new HashSet<>();

    public LoanRepaymentScheduleInstallment() {
        this.installmentNumber = null;
        this.fromDate = null;
        this.dueDate = null;
        this.obligationsMet = false;
    }

    public LoanRepaymentScheduleInstallment(final Loan loan, final Integer installmentNumber, final LocalDate fromDate,
            final LocalDate dueDate, final BigDecimal principal, final BigDecimal interest, final BigDecimal feeCharges,
            final BigDecimal penaltyCharges, final boolean recalculatedInterestComponent,
            final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails, final BigDecimal rescheduleInterestPortion) {
        this(loan, installmentNumber, fromDate, dueDate, principal, interest, feeCharges, penaltyCharges, recalculatedInterestComponent,
                compoundingDetails, rescheduleInterestPortion, false);
    }

    public LoanRepaymentScheduleInstallment(final Loan loan, final Integer installmentNumber, final LocalDate fromDate,
            final LocalDate dueDate, final BigDecimal principal, final BigDecimal interest, final BigDecimal feeCharges,
            final BigDecimal penaltyCharges, final boolean recalculatedInterestComponent,
            final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails, final BigDecimal rescheduleInterestPortion,
            final boolean isDownPayment) {
        this.loan = loan;
        this.installmentNumber = installmentNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        setPrincipal(principal);
        setInterestCharged(interest);
        setFeeChargesCharged(feeCharges);
        setPenaltyCharges(penaltyCharges);
        this.obligationsMet = false;
        this.recalculatedInterestComponent = recalculatedInterestComponent;
        if (compoundingDetails != null) {
            compoundingDetails.forEach(cd -> cd.setLoanRepaymentScheduleInstallment(this));
        }
        this.loanCompoundingDetails = compoundingDetails;
        setRescheduleInterestPortion(rescheduleInterestPortion);
        this.isDownPayment = isDownPayment;
    }

    public LoanRepaymentScheduleInstallment(final Loan loan, final Integer installmentNumber, final LocalDate fromDate,
            final LocalDate dueDate, final BigDecimal principal, final BigDecimal interest, final BigDecimal feeCharges,
            final BigDecimal penaltyCharges, final boolean recalculatedInterestComponent,
            final Set<LoanInterestRecalcualtionAdditionalDetails> compoundingDetails) {
        this.loan = loan;
        this.installmentNumber = installmentNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        setPrincipal(principal);
        setInterestCharged(interest);
        setFeeChargesCharged(feeCharges);
        setPenaltyCharges(penaltyCharges);
        this.obligationsMet = false;
        this.recalculatedInterestComponent = recalculatedInterestComponent;
        if (compoundingDetails != null) {
            compoundingDetails.forEach(cd -> cd.setLoanRepaymentScheduleInstallment(this));
        }
        this.loanCompoundingDetails = compoundingDetails;
    }

    public LoanRepaymentScheduleInstallment(final Loan loan) {
        this.loan = loan;
        this.installmentNumber = null;
        this.fromDate = null;
        this.dueDate = null;
        this.obligationsMet = false;
    }

    public LoanRepaymentScheduleInstallment(Loan loan, Integer installmentNumber, LocalDate fromDate, LocalDate dueDate,
            BigDecimal principal, BigDecimal interestCharged, BigDecimal feeChargesCharged, BigDecimal penaltyCharges,
            BigDecimal creditedPrincipal, BigDecimal creditedInterest, BigDecimal creditedFee, BigDecimal creditedPenalty,
            boolean additional, boolean isDownPayment, boolean isReAged) {
        this.loan = loan;
        this.installmentNumber = installmentNumber;
        this.fromDate = fromDate;
        this.dueDate = dueDate;
        setPrincipal(principal);
        setInterestCharged(interestCharged);
        setFeeChargesCharged(feeChargesCharged);
        setPenaltyCharges(penaltyCharges);
        setCreditedPrincipal(creditedPrincipal);
        setCreditedInterest(creditedInterest);
        setCreditedFee(creditedFee);
        setCreditedPenalty(creditedPenalty);
        this.additional = additional;
        this.isDownPayment = isDownPayment;
        this.isReAged = isReAged;
    }

    public static LoanRepaymentScheduleInstallment newReAgedInstallment(final Loan loan, final Integer installmentNumber,
            final LocalDate fromDate, final LocalDate dueDate, final BigDecimal principal) {
        return new LoanRepaymentScheduleInstallment(loan, installmentNumber, fromDate, dueDate, principal, null, null, null, null, null,
                null, null, false, false, true);
    }

    public static LoanRepaymentScheduleInstallment getLastNonDownPaymentInstallment(List<LoanRepaymentScheduleInstallment> installments) {
        return installments.stream().filter(i -> !i.isDownPayment()).reduce((first, second) -> second).orElseThrow();
    }

    public Money getCreditedPrincipal(final MonetaryCurrency currency) {
        return Money.of(currency, this.creditedPrincipal);
    }

    public Money getCreditedFee(final MonetaryCurrency currency) {
        return Money.of(currency, this.creditedFee);
    }

    public Money getCreditedPenalty(final MonetaryCurrency currency) {
        return Money.of(currency, this.creditedPenalty);
    }

    public Money getPrincipal(final MonetaryCurrency currency) {
        return Money.of(currency, this.principal);
    }

    public Money getPrincipalCompleted(final MonetaryCurrency currency) {
        return Money.of(currency, this.principalCompleted);
    }

    public Money getPrincipalWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.principalWrittenOff);
    }

    public Money getPrincipalOutstanding(final MonetaryCurrency currency) {
        final Money principalAccountedFor = getPrincipalCompleted(currency).plus(getPrincipalWrittenOff(currency));
        return getPrincipal(currency).minus(principalAccountedFor);
    }

    public Money getInterestCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestCharged);
    }

    public Money getInterestPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestPaid);
    }

    public Money getInterestWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestWaived);
    }

    public Money getInterestWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestWrittenOff);
    }

    public Money getInterestOutstanding(final MonetaryCurrency currency) {
        final Money interestAccountedFor = getInterestPaid(currency).plus(getInterestWaived(currency))
                .plus(getInterestWrittenOff(currency));
        return getInterestCharged(currency).minus(interestAccountedFor);
    }

    public Money getInterestAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestAccrued);
    }

    public Money getFeeChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesCharged);
    }

    public Money getFeeChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesPaid);
    }

    public Money getFeeChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesWaived);
    }

    public Money getFeeChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesWrittenOff);
    }

    public Money getFeeChargesOutstanding(final MonetaryCurrency currency) {
        final Money feeChargesAccountedFor = getFeeChargesPaid(currency).plus(getFeeChargesWaived(currency))
                .plus(getFeeChargesWrittenOff(currency));
        return getFeeChargesCharged(currency).minus(feeChargesAccountedFor);
    }

    public Money getFeeAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeAccrued);
    }

    public Money getPenaltyChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyCharges);
    }

    public Money getPenaltyChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesPaid);
    }

    public Money getPenaltyChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesWaived);
    }

    public Money getPenaltyChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesWrittenOff);
    }

    public Money getPenaltyChargesOutstanding(final MonetaryCurrency currency) {
        final Money feeChargesAccountedFor = getPenaltyChargesPaid(currency).plus(getPenaltyChargesWaived(currency))
                .plus(getPenaltyChargesWrittenOff(currency));
        return getPenaltyChargesCharged(currency).minus(feeChargesAccountedFor);
    }

    public Money getPenaltyAccrued(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyAccrued);
    }

    public boolean isInterestDue(final MonetaryCurrency currency) {
        return getInterestOutstanding(currency).isGreaterThanZero();
    }

    public Money getTotalOutstanding(final MonetaryCurrency currency) {
        return getPrincipalOutstanding(currency).plus(getInterestOutstanding(currency)).plus(getFeeChargesOutstanding(currency))
                .plus(getPenaltyChargesOutstanding(currency));
    }

    public void setPrincipal(final BigDecimal principal) {
        this.principal = setScaleAndDefaultToNullIfZero(principal);
    }

    public void setPrincipalCompleted(final BigDecimal principalCompleted) {
        this.principalCompleted = setScaleAndDefaultToNullIfZero(principalCompleted);
    }

    public void setPrincipalWrittenOff(final BigDecimal principalWrittenOff) {
        this.principalWrittenOff = setScaleAndDefaultToNullIfZero(principalWrittenOff);
    }

    public void setInterestCharged(final BigDecimal interestCharged) {
        this.interestCharged = setScaleAndDefaultToNullIfZero(interestCharged);
    }

    public void setInterestPaid(final BigDecimal interestPaid) {
        this.interestPaid = setScaleAndDefaultToNullIfZero(interestPaid);
    }

    public void setInterestWaived(final BigDecimal interestWaived) {
        this.interestWaived = setScaleAndDefaultToNullIfZero(interestWaived);
    }

    public void setInterestWrittenOff(final BigDecimal interestWrittenOff) {
        this.interestWrittenOff = setScaleAndDefaultToNullIfZero(interestWrittenOff);
    }

    public void setInterestAccrued(final BigDecimal interestAccrued) {
        this.interestAccrued = setScaleAndDefaultToNullIfZero(interestAccrued);
    }

    public void setRescheduleInterestPortion(final BigDecimal rescheduleInterestPortion) {
        this.rescheduleInterestPortion = setScaleAndDefaultToNullIfZero(rescheduleInterestPortion);
    }

    public void setFeeChargesCharged(final BigDecimal feeChargesCharged) {
        this.feeChargesCharged = setScaleAndDefaultToNullIfZero(feeChargesCharged);
    }

    public void setFeeChargesPaid(final BigDecimal feeChargesPaid) {
        this.feeChargesPaid = setScaleAndDefaultToNullIfZero(feeChargesPaid);
    }

    public void setFeeChargesWrittenOff(final BigDecimal feeChargesWrittenOff) {
        this.feeChargesWrittenOff = setScaleAndDefaultToNullIfZero(feeChargesWrittenOff);
    }

    public void setFeeChargesWaived(final BigDecimal feeChargesWaived) {
        this.feeChargesWaived = setScaleAndDefaultToNullIfZero(feeChargesWaived);
    }

    public void setFeeAccrued(final BigDecimal feeAccrued) {
        this.feeAccrued = setScaleAndDefaultToNullIfZero(feeAccrued);
    }

    public void setPenaltyCharges(final BigDecimal penaltyCharges) {
        this.penaltyCharges = setScaleAndDefaultToNullIfZero(penaltyCharges);
    }

    public void setPenaltyChargesPaid(final BigDecimal penaltyChargesPaid) {
        this.penaltyChargesPaid = setScaleAndDefaultToNullIfZero(penaltyChargesPaid);
    }

    public void setPenaltyChargesWrittenOff(final BigDecimal penaltyChargesWrittenOff) {
        this.penaltyChargesWrittenOff = setScaleAndDefaultToNullIfZero(penaltyChargesWrittenOff);
    }

    public void setPenaltyChargesWaived(final BigDecimal penaltyChargesWaived) {
        this.penaltyChargesWaived = setScaleAndDefaultToNullIfZero(penaltyChargesWaived);
    }

    public void setPenaltyAccrued(final BigDecimal penaltyAccrued) {
        this.penaltyAccrued = setScaleAndDefaultToNullIfZero(penaltyAccrued);
    }

    public void setTotalPaidInAdvance(final BigDecimal totalPaidInAdvance) {
        this.totalPaidInAdvance = setScaleAndDefaultToNullIfZero(totalPaidInAdvance);
    }

    public void setTotalPaidLate(final BigDecimal totalPaidLate) {
        this.totalPaidLate = setScaleAndDefaultToNullIfZero(totalPaidLate);
    }

    public void setCreditedPrincipal(final BigDecimal creditedPrincipal) {
        this.creditedPrincipal = setScaleAndDefaultToNullIfZero(creditedPrincipal);
    }

    public void setCreditedInterest(final BigDecimal creditedInterest) {
        this.creditedInterest = setScaleAndDefaultToNullIfZero(creditedInterest);
    }

    public void setCreditedFee(final BigDecimal creditedFee) {
        this.creditedFee = setScaleAndDefaultToNullIfZero(creditedFee);
    }

    public void setCreditedPenalty(final BigDecimal creditedPenalty) {
        this.creditedPenalty = setScaleAndDefaultToNullIfZero(creditedPenalty);
    }

    void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    public boolean isNotFullyPaidOff() {
        return !this.obligationsMet;
    }

    @Override
    public int compareTo(LoanRepaymentScheduleInstallment o) {
        return this.installmentNumber.compareTo(o.installmentNumber);
    }

    public int compareToByDueDate(LoanRepaymentScheduleInstallment o) {
        return this.dueDate.compareTo(o.dueDate);
    }

    public boolean isPrincipalNotCompleted(final MonetaryCurrency currency) {
        return !isPrincipalCompleted(currency);
    }

    public boolean isPrincipalCompleted(final MonetaryCurrency currency) {
        return getPrincipalOutstanding(currency).isZero();
    }

    public void resetDerivedComponents() {
        this.principalCompleted = null;
        this.principalWrittenOff = null;
        this.interestPaid = null;
        this.interestWaived = null;
        this.interestWrittenOff = null;
        this.feeChargesPaid = null;
        this.feeChargesWaived = null;
        this.feeChargesWrittenOff = null;
        this.penaltyChargesPaid = null;
        this.penaltyChargesWaived = null;
        this.penaltyChargesWrittenOff = null;
        this.totalPaidInAdvance = null;
        this.totalPaidLate = null;

        this.obligationsMet = false;
        this.obligationsMetOnDate = null;
        if (this.creditedPrincipal != null) {
            setPrincipal(this.principal != null ? this.principal.subtract(this.creditedPrincipal) : null);
            this.creditedPrincipal = null;
        }
        if (this.creditedInterest != null) {
            setInterestCharged(this.interestCharged != null ? this.interestCharged.subtract(this.creditedInterest) : null);
            this.creditedInterest = null;
        }
        if (this.creditedFee != null) {
            setFeeChargesCharged(this.feeChargesCharged != null ? this.feeChargesCharged.subtract(this.creditedFee) : null);
            this.creditedFee = null;
        }
        if (this.creditedPenalty != null) {
            setPenaltyCharges(this.penaltyCharges != null ? this.penaltyCharges.subtract(this.creditedPenalty) : null);
            this.creditedPenalty = null;
        }
    }

    public void resetAccrualComponents() {
        this.interestAccrued = null;
        this.feeAccrued = null;
        this.penaltyAccrued = null;
    }

    public void resetChargesCharged() {
        this.feeChargesCharged = null;
        this.penaltyCharges = null;
    }

    public void resetInterestDue() {
        this.interestCharged = null;
    }

    public void resetPrincipalDue() {
        this.principal = null;
    }

    public interface PaymentFunction {

        Money accept(LocalDate transactionDate, Money transactionAmountRemaining);
    }

    public PaymentFunction getPaymentFunction(AllocationType allocationType, PaymentAction action) {
        return switch (allocationType) {
            case PENALTY -> PaymentAction.PAY.equals(action) ? this::payPenaltyChargesComponent
                    : PaymentAction.UNPAY.equals(action) ? this::unpayPenaltyChargesComponent : null;
            case FEE -> PaymentAction.PAY.equals(action) ? this::payFeeChargesComponent
                    : PaymentAction.UNPAY.equals(action) ? this::unpayFeeChargesComponent : null;
            case INTEREST -> PaymentAction.PAY.equals(action) ? this::payInterestComponent
                    : PaymentAction.UNPAY.equals(action) ? this::unpayInterestComponent : null;
            case PRINCIPAL -> PaymentAction.PAY.equals(action) ? this::payPrincipalComponent
                    : PaymentAction.UNPAY.equals(action) ? this::unpayPrincipalComponent : null;
        };
    }

    public Money payPenaltyChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money penaltyPortionOfTransaction = Money.zero(currency);

        if (transactionAmountRemaining.isZero()) {
            return penaltyPortionOfTransaction;
        }

        final Money penaltyChargesDue = getPenaltyChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(penaltyChargesDue)) {
            setPenaltyChargesPaid(getPenaltyChargesPaid(currency).plus(penaltyChargesDue).getAmount());
            penaltyPortionOfTransaction = penaltyPortionOfTransaction.plus(penaltyChargesDue);
        } else {
            setPenaltyChargesPaid(getPenaltyChargesPaid(currency).plus(transactionAmountRemaining).getAmount());
            penaltyPortionOfTransaction = penaltyPortionOfTransaction.plus(transactionAmountRemaining);
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        trackAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, penaltyPortionOfTransaction);

        return penaltyPortionOfTransaction;
    }

    public Money payFeeChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money feePortionOfTransaction = Money.zero(currency);
        if (transactionAmountRemaining.isZero()) {
            return feePortionOfTransaction;
        }
        final Money feeChargesDue = getFeeChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(feeChargesDue)) {
            setFeeChargesPaid(getFeeChargesPaid(currency).plus(feeChargesDue).getAmount());
            feePortionOfTransaction = feePortionOfTransaction.plus(feeChargesDue);
        } else {
            setFeeChargesPaid(getFeeChargesPaid(currency).plus(transactionAmountRemaining).getAmount());
            feePortionOfTransaction = feePortionOfTransaction.plus(transactionAmountRemaining);
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        trackAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, feePortionOfTransaction);

        return feePortionOfTransaction;
    }

    public Money payInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money interestPortionOfTransaction = Money.zero(currency);
        if (transactionAmountRemaining.isZero()) {
            return interestPortionOfTransaction;
        }
        final Money interestDue = getInterestOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
            setInterestPaid(getInterestPaid(currency).plus(interestDue).getAmount());
            interestPortionOfTransaction = interestPortionOfTransaction.plus(interestDue);
        } else {
            setInterestPaid(getInterestPaid(currency).plus(transactionAmountRemaining).getAmount());
            interestPortionOfTransaction = interestPortionOfTransaction.plus(transactionAmountRemaining);
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        trackAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, interestPortionOfTransaction);

        return interestPortionOfTransaction;
    }

    public Money payPrincipalComponent(final LocalDate transactionDate, final Money transactionAmount) {
        final MonetaryCurrency currency = transactionAmount.getCurrency();
        Money principalPortionOfTransaction = Money.zero(currency);
        if (transactionAmount.isZero()) {
            return principalPortionOfTransaction;
        }
        final Money principalDue = getPrincipalOutstanding(currency);
        if (transactionAmount.isGreaterThanOrEqualTo(principalDue)) {
            setPrincipalCompleted(getPrincipalCompleted(currency).plus(principalDue).getAmount());
            principalPortionOfTransaction = principalPortionOfTransaction.plus(principalDue);
        } else {
            setPrincipalCompleted(getPrincipalCompleted(currency).plus(transactionAmount).getAmount());
            principalPortionOfTransaction = principalPortionOfTransaction.plus(transactionAmount);
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        trackAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, principalPortionOfTransaction);

        return principalPortionOfTransaction;
    }

    public Money waiveInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money waivedInterestPortionOfTransaction = Money.zero(currency);
        if (transactionAmountRemaining.isZero()) {
            return waivedInterestPortionOfTransaction;
        }
        final Money interestDue = getInterestOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
            setInterestWaived(getInterestWaived(currency).plus(interestDue).getAmount());
            waivedInterestPortionOfTransaction = waivedInterestPortionOfTransaction.plus(interestDue);
        } else {
            setInterestWaived(getInterestWaived(currency).plus(transactionAmountRemaining).getAmount());
            waivedInterestPortionOfTransaction = waivedInterestPortionOfTransaction.plus(transactionAmountRemaining);
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return waivedInterestPortionOfTransaction;
    }

    public Money waivePenaltyChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money waivedPenaltyChargesPortionOfTransaction = Money.zero(currency);
        if (transactionAmountRemaining.isZero()) {
            return waivedPenaltyChargesPortionOfTransaction;
        }
        final Money penaltiesDue = getPenaltyChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(penaltiesDue)) {
            setPenaltyChargesWaived(getPenaltyChargesWaived(currency).plus(penaltiesDue).getAmount());
            waivedPenaltyChargesPortionOfTransaction = waivedPenaltyChargesPortionOfTransaction.plus(penaltiesDue);
        } else {
            setPenaltyChargesWaived(getPenaltyChargesWaived(currency).plus(transactionAmountRemaining).getAmount());
            waivedPenaltyChargesPortionOfTransaction = waivedPenaltyChargesPortionOfTransaction.plus(transactionAmountRemaining);
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return waivedPenaltyChargesPortionOfTransaction;
    }

    public Money waiveFeeChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money waivedFeeChargesPortionOfTransaction = Money.zero(currency);
        if (transactionAmountRemaining.isZero()) {
            return waivedFeeChargesPortionOfTransaction;
        }
        final Money feesDue = getFeeChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(feesDue)) {
            setFeeChargesWaived(getFeeChargesWaived(currency).plus(feesDue).getAmount());
            waivedFeeChargesPortionOfTransaction = waivedFeeChargesPortionOfTransaction.plus(feesDue);
        } else {
            setFeeChargesWaived(getFeeChargesWaived(currency).plus(transactionAmountRemaining).getAmount());
            waivedFeeChargesPortionOfTransaction = waivedFeeChargesPortionOfTransaction.plus(transactionAmountRemaining);
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return waivedFeeChargesPortionOfTransaction;
    }

    public Money writeOffOutstandingPrincipal(final LocalDate transactionDate, final MonetaryCurrency currency) {
        final Money principalDue = getPrincipalOutstanding(currency);
        setPrincipalWrittenOff(principalDue.getAmount());
        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        return principalDue;
    }

    public Money writeOffOutstandingInterest(final LocalDate transactionDate, final MonetaryCurrency currency) {
        final Money interestDue = getInterestOutstanding(currency);
        setInterestWrittenOff(interestDue.getAmount());
        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        return interestDue;
    }

    public Money writeOffOutstandingFeeCharges(final LocalDate transactionDate, final MonetaryCurrency currency) {
        final Money feeChargesOutstanding = getFeeChargesOutstanding(currency);
        setFeeChargesWrittenOff(feeChargesOutstanding.getAmount());
        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        return feeChargesOutstanding;
    }

    public Money writeOffOutstandingPenaltyCharges(final LocalDate transactionDate, final MonetaryCurrency currency) {
        final Money penaltyChargesOutstanding = getPenaltyChargesOutstanding(currency);
        setPenaltyChargesWrittenOff(penaltyChargesOutstanding.getAmount());
        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        return penaltyChargesOutstanding;
    }

    public boolean isOverdueOn(final LocalDate date) {
        return DateUtils.isAfter(date, getDueDate());
    }

    public void updateChargePortion(final Money feeChargesDue, final Money feeChargesWaived, final Money feeChargesWrittenOff,
            final Money penaltyChargesDue, final Money penaltyChargesWaived, final Money penaltyChargesWrittenOff) {
        setFeeChargesCharged(feeChargesDue.getAmount());
        setFeeChargesWaived(feeChargesWaived.getAmount());
        setFeeChargesWrittenOff(feeChargesWrittenOff.getAmount());
        setPenaltyCharges(penaltyChargesDue.getAmount());
        setPenaltyChargesWaived(penaltyChargesWaived.getAmount());
        setPenaltyChargesWrittenOff(penaltyChargesWrittenOff.getAmount());
    }

    public void addToChargePortion(final Money feeChargesDue, final Money feeChargesWaived, final Money feeChargesWrittenOff,
            final Money penaltyChargesDue, final Money penaltyChargesWaived, final Money penaltyChargesWrittenOff) {
        setFeeChargesCharged(MathUtil.add(feeChargesDue.getAmount(), this.feeChargesCharged));
        setFeeChargesWaived(MathUtil.add(feeChargesWaived.getAmount(), this.feeChargesWaived));
        setFeeChargesWrittenOff(MathUtil.add(feeChargesWrittenOff.getAmount(), this.feeChargesWrittenOff));
        setPenaltyCharges(MathUtil.add(penaltyChargesDue.getAmount(), this.penaltyCharges));
        setPenaltyChargesWaived(MathUtil.add(penaltyChargesWaived.getAmount(), this.penaltyChargesWaived));
        setPenaltyChargesWrittenOff(MathUtil.add(penaltyChargesWrittenOff.getAmount(), this.penaltyChargesWrittenOff));
        checkIfRepaymentPeriodObligationsAreMet(getObligationsMetOnDate(), feeChargesDue.getCurrency());
    }

    public void updateAccrualPortion(final Money interest, final Money feeCharges, final Money penalityCharges) {
        setInterestAccrued(interest.getAmount());
        setFeeAccrued(feeCharges.getAmount());
        setPenaltyAccrued(penalityCharges.getAmount());
    }

    public void updateObligationsMet(final MonetaryCurrency currency, final LocalDate transactionDate) {
        if (!this.obligationsMet && getTotalOutstanding(currency).isZero()) {
            this.obligationsMet = true;
            this.obligationsMetOnDate = transactionDate;
        } else if (this.obligationsMet && !getTotalOutstanding(currency).isZero()) {
            this.obligationsMet = false;
            this.obligationsMetOnDate = null;
        }
    }

    private void trackAdvanceAndLateTotalsForRepaymentPeriod(final LocalDate transactionDate, final MonetaryCurrency currency,
            final Money amountPaidInRepaymentPeriod) {
        if (isInAdvance(transactionDate)) {
            setTotalPaidInAdvance(asMoney(this.totalPaidInAdvance, currency).plus(amountPaidInRepaymentPeriod).getAmount());
        } else if (isLatePayment(transactionDate)) {
            setTotalPaidLate(asMoney(this.totalPaidLate, currency).plus(amountPaidInRepaymentPeriod).getAmount());
        }
    }

    private Money asMoney(final BigDecimal decimal, final MonetaryCurrency currency) {
        return Money.of(currency, decimal);
    }

    private boolean isInAdvance(final LocalDate transactionDate) {
        return DateUtils.isBefore(transactionDate, getDueDate());
    }

    private boolean isLatePayment(final LocalDate transactionDate) {
        return DateUtils.isAfter(transactionDate, getDueDate());
    }

    public void checkIfRepaymentPeriodObligationsAreMet(final LocalDate transactionDate, final MonetaryCurrency currency) {
        this.obligationsMet = getTotalOutstanding(currency).isZero();
        if (this.obligationsMet) {
            this.obligationsMetOnDate = transactionDate;
        } else {
            this.obligationsMetOnDate = null;
        }
    }

    public void updateDueDate(final LocalDate newDueDate) {
        if (newDueDate != null) {
            this.dueDate = newDueDate;
        }
    }

    public void updateFromDate(final LocalDate newFromDate) {
        if (newFromDate != null) {
            this.fromDate = newFromDate;
        }
    }

    public Money getTotalPaidInAdvance(final MonetaryCurrency currency) {
        return Money.of(currency, this.totalPaidInAdvance);
    }

    public void updateInstallmentNumber(final Integer installmentNumber) {
        if (installmentNumber != null) {
            this.installmentNumber = installmentNumber;
        }
    }

    public void updateInterestCharged(final BigDecimal interestCharged) {
        setInterestCharged(interestCharged);
    }

    public void updateObligationMet(final Boolean obligationMet) {
        this.obligationsMet = obligationMet;
    }

    public void updateObligationMetOnDate(final LocalDate obligationsMetOnDate) {
        this.obligationsMetOnDate = obligationsMetOnDate;
    }

    public void updatePrincipal(final BigDecimal principal) {
        setPrincipal(principal);
    }

    public void addToPrincipal(final LocalDate transactionDate, final Money transactionAmount) {
        if (this.principal == null) {
            setPrincipal(transactionAmount.getAmount());
        } else {
            setPrincipal(this.principal.add(transactionAmount.getAmount()));
        }
        checkIfRepaymentPeriodObligationsAreMet(transactionDate, transactionAmount.getCurrency());
    }

    public void addToInterest(final LocalDate transactionDate, final Money transactionAmount) {
        if (this.interestCharged == null) {
            setInterestCharged(transactionAmount.getAmount());
        } else {
            setInterestCharged(this.interestCharged.add(transactionAmount.getAmount()));
        }
        checkIfRepaymentPeriodObligationsAreMet(transactionDate, transactionAmount.getCurrency());
    }

    public void addToCreditedInterest(final BigDecimal amount) {
        if (this.creditedInterest == null) {
            setCreditedInterest(amount);
        } else {
            setCreditedInterest(this.creditedInterest.add(amount));
        }
    }

    public void addToCreditedPrincipal(final BigDecimal amount) {
        if (this.creditedPrincipal == null) {
            setCreditedPrincipal(amount);
        } else {
            setCreditedPrincipal(this.creditedPrincipal.add(amount));
        }
    }

    public void addToCreditedFee(final BigDecimal amount) {
        if (this.creditedFee == null) {
            setCreditedFee(amount);
        } else {
            setCreditedFee(this.creditedFee.add(amount));
        }
    }

    public void addToCreditedPenalty(final BigDecimal amount) {
        if (this.creditedPenalty == null) {
            setCreditedPenalty(amount);
        } else {
            setCreditedPenalty(this.creditedPenalty.add(amount));
        }
    }

    /********** UNPAY COMPONENTS ****/

    public Money unpayPenaltyChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money penaltyPortionOfTransactionDeducted;

        final Money penaltyChargesCompleted = getPenaltyChargesPaid(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(penaltyChargesCompleted)) {
            this.penaltyChargesPaid = null;
            penaltyPortionOfTransactionDeducted = penaltyChargesCompleted;
        } else {
            setPenaltyChargesPaid(penaltyChargesCompleted.minus(transactionAmountRemaining).getAmount());
            penaltyPortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return penaltyPortionOfTransactionDeducted;
    }

    public Money unpayFeeChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money feePortionOfTransactionDeducted;

        final Money feeChargesCompleted = getFeeChargesPaid(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(feeChargesCompleted)) {
            this.feeChargesPaid = null;
            feePortionOfTransactionDeducted = feeChargesCompleted;
        } else {
            setFeeChargesPaid(feeChargesCompleted.minus(transactionAmountRemaining).getAmount());
            feePortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        reduceAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, feePortionOfTransactionDeducted);

        return feePortionOfTransactionDeducted;
    }

    public Money unpayInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money interestPortionOfTransactionDeducted;

        final Money interestCompleted = getInterestPaid(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestCompleted)) {
            this.interestPaid = null;
            interestPortionOfTransactionDeducted = interestCompleted;
        } else {
            setInterestPaid(interestCompleted.minus(transactionAmountRemaining).getAmount());
            interestPortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        reduceAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, interestPortionOfTransactionDeducted);

        return interestPortionOfTransactionDeducted;
    }

    public Money unpayPrincipalComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money principalPortionOfTransactionDeducted;

        final Money principalCompleted = getPrincipalCompleted(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(principalCompleted)) {
            this.principalCompleted = null;
            principalPortionOfTransactionDeducted = principalCompleted;
        } else {
            setPrincipalCompleted(principalCompleted.minus(transactionAmountRemaining).getAmount());
            principalPortionOfTransactionDeducted = transactionAmountRemaining;
        }

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);
        reduceAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, principalPortionOfTransactionDeducted);

        return principalPortionOfTransactionDeducted;
    }

    private void reduceAdvanceAndLateTotalsForRepaymentPeriod(final LocalDate transactionDate, final MonetaryCurrency currency,
            final Money amountDeductedInRepaymentPeriod) {
        if (isInAdvance(transactionDate)) {
            final Money mTotalPaidInAdvance = Money.of(currency, this.totalPaidInAdvance);

            if (mTotalPaidInAdvance.isLessThan(amountDeductedInRepaymentPeriod)
                    || mTotalPaidInAdvance.isEqualTo(amountDeductedInRepaymentPeriod)) {
                this.totalPaidInAdvance = null;
            } else {
                setTotalPaidInAdvance(mTotalPaidInAdvance.minus(amountDeductedInRepaymentPeriod).getAmount());
            }
        } else if (isLatePayment(transactionDate)) {
            final Money mTotalPaidLate = Money.of(currency, this.totalPaidLate);

            if (mTotalPaidLate.isLessThan(amountDeductedInRepaymentPeriod) || mTotalPaidLate.isEqualTo(amountDeductedInRepaymentPeriod)) {
                this.totalPaidLate = null;
            } else {
                setTotalPaidLate(mTotalPaidLate.minus(amountDeductedInRepaymentPeriod).getAmount());
            }
        }
    }

    public void updateCredits(final LocalDate transactionDate, final Money transactionAmount) {
        addToCreditedPrincipal(transactionAmount.getAmount());
        addToPrincipal(transactionDate, transactionAmount);
    }

    public Money getDue(MonetaryCurrency currency) {
        return getPrincipal(currency).plus(getInterestCharged(currency)).plus(getFeeChargesCharged(currency))
                .plus(getPenaltyChargesCharged(currency));
    }

    public Money getTotalPaid(final MonetaryCurrency currency) {
        return getPenaltyChargesPaid(currency).plus(getFeeChargesPaid(currency)).plus(getInterestPaid(currency))
                .plus(getPrincipalCompleted(currency));
    }

    public void markAsAdditional() {
        this.additional = true;
    }

    public void resetBalances() {
        resetDerivedComponents();
        resetPrincipalDue();
        resetChargesCharged();
        resetInterestDue();
    }

    public enum PaymentAction {
        PAY, //
        UNPAY //
    }

    public boolean isTransactionDateWithinPeriod(LocalDate referenceDate) {
        return DateUtils.isAfter(referenceDate, getFromDate()) && !DateUtils.isAfter(referenceDate, getDueDate());
    }

    public boolean isDueBalanceZero() {
        return MathUtil.isZero(
                MathUtil.nullToZero(MathUtil.add(getPrincipal(), getInterestCharged(), getFeeChargesCharged(), getPenaltyCharges())));
    }

    public void copyFrom(final LoanScheduleModelPeriod period) {
        // Reset fields and relations
        resetBalances();
        updateLoanCompoundingDetails(period.getLoanCompoundingDetails());
        getInstallmentCharges().clear();
        getPostDatedChecks().clear();
        getLoanTransactionToRepaymentScheduleMappings().clear();
        // Update fields
        setFromDate(period.periodFromDate());
        setDueDate(period.periodDueDate());
        setPrincipal(period.principalDue());
        setInterestCharged(period.interestDue());
        setFeeChargesCharged(period.feeChargesDue());
        setPenaltyCharges(period.penaltyChargesDue());
        setRecalculatedInterestComponent(period.isRecalculatedInterestComponent());
        setRescheduleInterestPortion(period.rescheduleInterestPortion());
        setDownPayment(period.isDownPaymentPeriod());
        setAdditional(false);
        setReAged(false);
    }

    public void copyFrom(final LoanRepaymentScheduleInstallment installment) {
        if (nonNullAndEqual(getId(), installment.getId())) {
            return;
        }
        // Reset balances
        resetBalances();
        // Dates
        setFromDate(installment.getFromDate());
        setDueDate(installment.getDueDate());
        setObligationsMetOnDate(installment.getObligationsMetOnDate());
        // Flags
        setObligationsMet(installment.isObligationsMet());
        setAdditional(installment.isAdditional());
        setReAged(installment.isReAged());
        setDownPayment(installment.isDownPayment());
        // Principal
        setPrincipal(installment.getPrincipal());
        setPrincipalCompleted(installment.getPrincipalCompleted());
        setPrincipalWrittenOff(installment.getPrincipalWrittenOff());
        // Interest
        setInterestCharged(installment.getInterestCharged());
        setInterestAccrued(installment.getInterestAccrued());
        setInterestPaid(installment.getInterestPaid());
        setInterestWaived(installment.getInterestWaived());
        setInterestWrittenOff(installment.getInterestWrittenOff());
        setRescheduleInterestPortion(installment.getRescheduleInterestPortion());
        setRecalculatedInterestComponent(installment.isRecalculatedInterestComponent());
        // Fee
        setFeeChargesCharged(installment.getFeeChargesCharged());
        setFeeChargesPaid(installment.getFeeChargesPaid());
        setFeeAccrued(installment.getFeeAccrued());
        setFeeChargesWaived(installment.getFeeChargesWaived());
        setFeeChargesWrittenOff(installment.getFeeChargesWrittenOff());
        // Penalty
        setPenaltyCharges(installment.getPenaltyCharges());
        setPenaltyAccrued(installment.getPenaltyAccrued());
        setPenaltyChargesWaived(installment.getPenaltyChargesWaived());
        setPenaltyChargesPaid(installment.getPenaltyChargesPaid());
        setPenaltyChargesWrittenOff(installment.getPenaltyChargesWrittenOff());
        // paid in advance / late
        setTotalPaidInAdvance(installment.getTotalPaidInAdvance());
        setTotalPaidLate(installment.getTotalPaidLate());
        // Credits (Chargeback)
        setCreditedFee(installment.getCreditedFee());
        setCreditedPenalty(installment.getCreditedPenalty());
        setCreditedInterest(installment.getCreditedInterest());
        setCreditedPrincipal(installment.getCreditedPrincipal());
        // Compounding details
        updateLoanCompoundingDetails(installment.getLoanCompoundingDetails());
        // Installment charges
        updateLoaInstallmentCharges(installment.getInstallmentCharges());
        // Post dated checks
        updatePostDatedChecks(installment.getPostDatedChecks());
        // Loan transaction repayment schedule mapping
        updateTransactionRepaymentScheduleMapping(installment.getLoanTransactionToRepaymentScheduleMappings());
    }

    private void updateLoanCompoundingDetails(Set<LoanInterestRecalcualtionAdditionalDetails> loanCompoundingDetails) {
        Set<LoanInterestRecalcualtionAdditionalDetails> retainedLoanCompoundingDetails = new HashSet<>();
        if (loanCompoundingDetails != null) {
            loanCompoundingDetails.forEach(nlcd -> {
                nlcd.setLoanRepaymentScheduleInstallment(this);
                getLoanCompoundingDetails().stream().filter(lcd -> MathUtil.isEqualTo(lcd.getAmount(), nlcd.getAmount()) //
                        && DateUtils.isEqual(lcd.getEffectiveDate(), nlcd.getEffectiveDate())) //
                        .findAny().ifPresentOrElse(retainedLoanCompoundingDetails::add, () -> retainedLoanCompoundingDetails.add(nlcd));
            });
        }
        setLoanCompoundingDetails(retainedLoanCompoundingDetails);
    }

    private void updateLoaInstallmentCharges(Set<LoanInstallmentCharge> installmentCharges) {
        Set<LoanInstallmentCharge> retainedInstallmentCharges = new HashSet<>();
        if (installmentCharges != null) {
            installmentCharges.forEach(nic -> {
                nic.setInstallment(this);
                getInstallmentCharges().stream().filter(ic -> MathUtil.isEqualTo(ic.getAmountOutstanding(), nic.getAmountOutstanding()) //
                        && MathUtil.isEqualTo(ic.getAmountWrittenOff(), nic.getAmountWrittenOff()) //
                        && MathUtil.isEqualTo(ic.getAmountPaid(), nic.getAmountPaid()) //
                        && MathUtil.isEqualTo(ic.getAmountThroughChargePayment(), nic.getAmountThroughChargePayment()) //
                        && MathUtil.isEqualTo(ic.getAmountWaived(), nic.getAmountWaived()) //
                        && MathUtil.isEqualTo(ic.getAmount(), nic.getAmount()) //
                        && Boolean.logicalAnd(ic.isPaid(), nic.isPaid()) //
                        && Boolean.logicalAnd(ic.isWaived(), nic.isWaived()) //
                        && Boolean.logicalAnd(ic.isPending(), nic.isPending())) //
                        .findAny().ifPresentOrElse(retainedInstallmentCharges::add, () -> retainedInstallmentCharges.add(nic));
            });
        }
        setInstallmentCharges(retainedInstallmentCharges);
    }

    private void updatePostDatedChecks(Set<PostDatedChecks> postDatedChecks) {
        Set<PostDatedChecks> retainedPostDatedChecks = new HashSet<>();
        if (postDatedChecks != null) {
            postDatedChecks.forEach(npdc -> {
                npdc.setLoanRepaymentScheduleInstallment(this);
                getPostDatedChecks().stream().filter(pdc -> MathUtil.isEqualTo(pdc.getCheckNo(), npdc.getCheckNo()) //
                        && MathUtil.isEqualTo(pdc.getAccountNo(), npdc.getAccountNo()) //
                        && MathUtil.isEqualTo(pdc.getAmount(), npdc.getAmount()) //
                        && StringUtils.equals(pdc.getBankName(), npdc.getBankName()) //
                        && MathUtil.isEqualTo(pdc.getStatus(), npdc.getStatus()) //
                        && DateUtils.isEqual(pdc.getRepaymentDate(), npdc.getRepaymentDate())) //
                        .findAny().ifPresentOrElse(retainedPostDatedChecks::add, () -> retainedPostDatedChecks.add(npdc));
            });
        }
        setPostDatedChecks(retainedPostDatedChecks);
    }

    private void updateTransactionRepaymentScheduleMapping(
            Set<LoanTransactionToRepaymentScheduleMapping> transactionToRepaymentScheduleMappings) {
        Set<LoanTransactionToRepaymentScheduleMapping> retainedTransactionRepaymentScheduleMapping = new HashSet<>();
        if (transactionToRepaymentScheduleMappings != null) {
            transactionToRepaymentScheduleMappings.forEach(ntrsm -> {
                ntrsm.setInstallment(this);
                getLoanTransactionToRepaymentScheduleMappings().stream()
                        .filter(trsm -> MathUtil.isEqualTo(trsm.getAmount(), ntrsm.getAmount()) //
                                && MathUtil.isEqualTo(trsm.getFeeChargesPortion(), ntrsm.getFeeChargesPortion()) //
                                && MathUtil.isEqualTo(trsm.getInterestPortion(), ntrsm.getInterestPortion()) //
                                && MathUtil.isEqualTo(trsm.getPrincipalPortion(), ntrsm.getPrincipalPortion()) //
                                && MathUtil.isEqualTo(trsm.getPenaltyChargesPortion(), ntrsm.getPenaltyChargesPortion())) //
                        .findAny().ifPresentOrElse(retainedTransactionRepaymentScheduleMapping::add,
                                () -> retainedTransactionRepaymentScheduleMapping.add(ntrsm));
            });
        }
        setLoanTransactionToRepaymentScheduleMappings(retainedTransactionRepaymentScheduleMapping);
    }

    private static boolean nonNullAndEqual(Object a, Object b) {
        return a != null && b != null && Objects.equals(a, b);
    }

    private BigDecimal setScaleAndDefaultToNullIfZero(final BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return value.setScale(6, MoneyHelper.getRoundingMode());
    }
}
