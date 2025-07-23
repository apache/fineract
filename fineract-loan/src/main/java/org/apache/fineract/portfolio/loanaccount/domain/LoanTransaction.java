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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.loanaccount.domain.reaging.LoanReAgeParameter;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;

/**
 * All monetary transactions against a loan are modelled through this entity. Disbursements, Repayments, Waivers,
 * Write-off etc
 */
@Getter
@Entity
@Table(name = "m_loan_transaction", uniqueConstraints = { @UniqueConstraint(columnNames = { "external_id" }, name = "external_id_UNIQUE") })
public class LoanTransaction extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Version
    private Long version;

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(optional = true)
    @JoinColumn(name = "payment_detail_id")
    private PaymentDetail paymentDetail;

    @Column(name = "transaction_type_enum", nullable = false)
    private LoanTransactionType typeOf;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate dateOf;

    @Getter
    @Column(name = "submitted_on_date", nullable = false)
    private LocalDate submittedOnDate;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "principal_portion_derived", scale = 6, precision = 19)
    private BigDecimal principalPortion;

    @Column(name = "interest_portion_derived", scale = 6, precision = 19)
    private BigDecimal interestPortion;

    @Column(name = "fee_charges_portion_derived", scale = 6, precision = 19)
    private BigDecimal feeChargesPortion;

    @Column(name = "penalty_charges_portion_derived", scale = 6, precision = 19)
    private BigDecimal penaltyChargesPortion;

    @Column(name = "overpayment_portion_derived", scale = 6, precision = 19)
    private BigDecimal overPaymentPortion;

    @Column(name = "unrecognized_income_portion", scale = 6, precision = 19)
    private BigDecimal unrecognizedIncomePortion;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Column(name = "external_id", length = 100, unique = true)
    private ExternalId externalId;

    @Column(name = "reversal_external_id", length = 100, unique = true)
    private ExternalId reversalExternalId;

    @Column(name = "reversed_on_date")
    private LocalDate reversedOnDate;

    @Setter
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanTransaction", orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<LoanChargePaidBy> loanChargesPaid = new HashSet<>();

    @Column(name = "outstanding_loan_balance_derived", scale = 6, precision = 19)
    private BigDecimal outstandingLoanBalance;

    @Column(name = "manually_adjusted_or_reversed", nullable = false)
    private boolean manuallyAdjustedOrReversed;

    @Column(name = "charge_refund_charge_type", length = 1, unique = true)
    private String chargeRefundChargeType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "loanTransaction")
    private Set<LoanCollateralManagement> loanCollateralManagementSet = new HashSet<>();

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "loanTransaction")
    private Set<LoanTransactionToRepaymentScheduleMapping> loanTransactionToRepaymentScheduleMappings = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "fromTransaction")
    private Set<LoanTransactionRelation> loanTransactionRelations = new HashSet<>();

    @Setter
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "loanTransaction")
    private LoanReAgeParameter loanReAgeParameter;

    protected LoanTransaction() {}

    public static LoanTransaction incomePosting(final Loan loan, final Office office, final LocalDate dateOf, final BigDecimal amount,
            final BigDecimal interestPortion, final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion,
            final ExternalId externalId) {
        final BigDecimal principalPortion = BigDecimal.ZERO;
        final BigDecimal overPaymentPortion = BigDecimal.ZERO;
        final boolean reversed = false;
        final PaymentDetail paymentDetail = null;
        return new LoanTransaction(loan, office, LoanTransactionType.INCOME_POSTING, dateOf, amount, principalPortion, interestPortion,
                feeChargesPortion, penaltyChargesPortion, overPaymentPortion, reversed, paymentDetail, externalId);
    }

    public static LoanTransaction disbursement(final Loan loan, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate disbursementDate, final ExternalId externalId, final Money loanTotalOverpaid) {
        // We need to set the overpayment amount because it could happen the transaction got saved before the proper
        // portion calculation and side effect would be reverse-replay
        LoanTransaction disbursement = new LoanTransaction(null, loan.getOffice(), LoanTransactionType.DISBURSEMENT, paymentDetail,
                amount.getAmount(), disbursementDate, externalId);
        if (LoanScheduleType.PROGRESSIVE.equals(loan.getLoanProductRelatedDetail().getLoanScheduleType())) {
            Money overPaymentPortion = amount.isGreaterThan(loanTotalOverpaid) ? loanTotalOverpaid : amount;
            disbursement.setOverPayments(overPaymentPortion);
        }
        return disbursement;
    }

    public static LoanTransaction repayment(final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final ExternalId externalId) {
        return new LoanTransaction(null, office, LoanTransactionType.REPAYMENT, paymentDetail, amount.getAmount(), paymentDate, externalId);
    }

    public static LoanTransaction chargeback(final Loan loan, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final ExternalId externalId) {
        LoanTransaction loanTransaction = new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.CHARGEBACK, paymentDetail,
                amount.getAmount(), paymentDate, externalId);
        loanTransaction.principalPortion = amount.getAmount();
        return loanTransaction;
    }

    public static LoanTransaction repaymentType(final LoanTransactionType repaymentType, final Office office, final Money amount,
            final PaymentDetail paymentDetail, final LocalDate paymentDate, final ExternalId externalId,
            final String chargeRefundChargeType) {
        return new LoanTransaction(null, office, repaymentType, paymentDetail, amount.getAmount(), paymentDate, externalId,
                chargeRefundChargeType);
    }

    public static LoanTransaction interestRefund(final Loan loan, final BigDecimal amount, final LocalDate date,
            final ExternalId externalId) {
        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.INTEREST_REFUND, null, amount, date, externalId);
    }

    public static LoanTransaction interestRefund(final Loan loan, final BigDecimal amount, final LocalDate date,
            final PaymentDetail paymentDetail, final ExternalId externalId) {
        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.INTEREST_REFUND, paymentDetail, amount, date, externalId);
    }

    public static LoanTransaction chargeAdjustment(final Loan loan, final BigDecimal amount, final LocalDate transactionDate,
            final ExternalId externalId, PaymentDetail paymentDetail) {
        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.CHARGE_ADJUSTMENT, paymentDetail, amount, transactionDate,
                externalId);
    }

    public static LoanTransaction downPayment(final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final ExternalId externalId) {
        return new LoanTransaction(null, office, LoanTransactionType.DOWN_PAYMENT, paymentDetail, amount.getAmount(), paymentDate,
                externalId);
    }

    public static LoanTransaction refund(Loan loan, LoanTransactionType loanTransactionType, BigDecimal transactionAmount,
            PaymentDetail paymentDetail, LocalDate transactionDate, ExternalId txnExternalId) {
        return new LoanTransaction(loan, loan.getOffice(), loanTransactionType, paymentDetail, transactionAmount, transactionDate,
                txnExternalId);
    }

    public static LoanTransaction recoveryRepayment(final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final ExternalId externalId) {
        return new LoanTransaction(null, office, LoanTransactionType.RECOVERY_REPAYMENT, paymentDetail, amount.getAmount(), paymentDate,
                externalId);
    }

    public static LoanTransaction loanPayment(final Loan loan, final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final ExternalId externalId, final LoanTransactionType transactionType) {
        return new LoanTransaction(loan, office, transactionType, paymentDetail, amount.getAmount(), paymentDate, externalId);
    }

    public static LoanTransaction repaymentAtDisbursement(final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final ExternalId externalId) {
        return new LoanTransaction(null, office, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT, paymentDetail, amount.getAmount(),
                paymentDate, externalId);
    }

    public static LoanTransaction waiver(final Office office, final Loan loan, final Money amount, final LocalDate waiveDate,
            final Money waived, final Money unrecognizedPortion, final ExternalId externalId) {
        LoanTransaction loanTransaction = new LoanTransaction(loan, office, LoanTransactionType.WAIVE_INTEREST, amount.getAmount(),
                waiveDate, externalId);
        loanTransaction.updateInterestComponent(waived, unrecognizedPortion);
        return loanTransaction;
    }

    public static LoanTransaction accrueInterest(final Office office, final Loan loan, final Money amount,
            final LocalDate interestAppliedDate, final ExternalId externalId) {
        BigDecimal interestPortion = amount.getAmount();
        return accrueTransaction(loan, office, interestAppliedDate, interestPortion, interestPortion, null, null, externalId);
    }

    public static LoanTransaction accrueLoanCharge(final Loan loan, final Office office, final Money amount, final LocalDate applyDate,
            final Money feeCharges, final Money penaltyCharges, final ExternalId externalId) {
        final LoanTransaction applyCharge = new LoanTransaction(loan, office, LoanTransactionType.ACCRUAL, amount.getAmount(), applyDate,
                externalId);
        applyCharge.updateChargesComponents(feeCharges, penaltyCharges);
        return applyCharge;
    }

    public static LoanTransaction accrueTransaction(final Loan loan, final Office office, final LocalDate dateOf, final BigDecimal amount,
            final BigDecimal interestPortion, final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion,
            final ExternalId externalId) {
        return new LoanTransaction(loan, office, LoanTransactionType.ACCRUAL, dateOf, amount, null, interestPortion, feeChargesPortion,
                penaltyChargesPortion, null, false, null, externalId);
    }

    public static LoanTransaction accrualAdjustment(final Loan loan, final Office office, final LocalDate dateOf, final BigDecimal amount,
            final BigDecimal interestPortion, final BigDecimal feePortion, final BigDecimal penaltyPortion, final ExternalId externalId) {
        return new LoanTransaction(loan, office, LoanTransactionType.ACCRUAL_ADJUSTMENT, dateOf, amount, null, interestPortion, feePortion,
                penaltyPortion, null, false, null, externalId);
    }

    public static LoanTransaction initiateTransfer(final Office office, final Loan loan, final LocalDate transferDate,
            final ExternalId externalId) {
        return new LoanTransaction(loan, office, LoanTransactionType.INITIATE_TRANSFER, transferDate,
                loan.getSummary().getTotalOutstanding(), loan.getSummary().getTotalPrincipalOutstanding(),
                loan.getSummary().getTotalInterestOutstanding(), loan.getSummary().getTotalFeeChargesOutstanding(),
                loan.getSummary().getTotalPenaltyChargesOutstanding(), null, false, null, externalId);
    }

    public static LoanTransaction approveTransfer(final Office office, final Loan loan, final LocalDate transferDate,
            final ExternalId externalId) {
        return new LoanTransaction(loan, office, LoanTransactionType.APPROVE_TRANSFER, transferDate,
                loan.getSummary().getTotalOutstanding(), loan.getSummary().getTotalPrincipalOutstanding(),
                loan.getSummary().getTotalInterestOutstanding(), loan.getSummary().getTotalFeeChargesOutstanding(),
                loan.getSummary().getTotalPenaltyChargesOutstanding(), null, false, null, externalId);
    }

    public static LoanTransaction withdrawTransfer(final Office office, final Loan loan, final LocalDate transferDate,
            final ExternalId externalId) {
        return new LoanTransaction(loan, office, LoanTransactionType.WITHDRAW_TRANSFER, transferDate,
                loan.getSummary().getTotalOutstanding(), loan.getSummary().getTotalPrincipalOutstanding(),
                loan.getSummary().getTotalInterestOutstanding(), loan.getSummary().getTotalFeeChargesOutstanding(),
                loan.getSummary().getTotalPenaltyChargesOutstanding(), null, false, null, externalId);
    }

    public static LoanTransaction refund(final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final ExternalId externalId) {
        return new LoanTransaction(null, office, LoanTransactionType.REFUND, paymentDetail, amount.getAmount(), paymentDate, externalId);
    }

    public static LoanTransaction copyTransactionProperties(final LoanTransaction loanTransaction) {
        LoanTransaction newTransaction = new LoanTransaction(loanTransaction.loan, loanTransaction.office, loanTransaction.typeOf,
                loanTransaction.dateOf, loanTransaction.amount, loanTransaction.principalPortion, loanTransaction.interestPortion,
                loanTransaction.feeChargesPortion, loanTransaction.penaltyChargesPortion, loanTransaction.overPaymentPortion,
                loanTransaction.reversed, loanTransaction.paymentDetail, loanTransaction.externalId);

        if (LoanTransactionType.CHARGE_PAYMENT.equals(loanTransaction.getTypeOf())
                || LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.equals(loanTransaction.getTypeOf())) {
            newTransaction.getLoanChargesPaid().addAll(loanTransaction.getLoanChargesPaid());
        }
        if (LoanTransactionType.REAGE.equals(loanTransaction.getTypeOf())) {
            newTransaction.setLoanReAgeParameter(loanTransaction.getLoanReAgeParameter().getCopy(newTransaction));
        }
        return newTransaction;
    }

    public static LoanTransaction capitalizedIncome(final Loan loan, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate transactionDate, final ExternalId externalId) {
        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.CAPITALIZED_INCOME, transactionDate, amount.getAmount(),
                amount.getAmount(), null, null, null, null, false, paymentDetail, externalId);
    }

    public static LoanTransaction capitalizedIncomeAmortization(final Loan loan, final Office office, final LocalDate dateOf,
            final BigDecimal amount, final ExternalId externalId) {
        return switch (loan.getLoanProductRelatedDetail().getCapitalizedIncomeType()) {
            case FEE -> new LoanTransaction(loan, office, LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION, dateOf, amount, null, null,
                    amount, null, null, false, null, externalId);
            case INTEREST -> new LoanTransaction(loan, office, LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION, dateOf, amount, null,
                    amount, null, null, null, false, null, externalId);
        };
    }

    public static LoanTransaction capitalizedIncomeAdjustment(final Loan loan, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate transactionDate, final ExternalId externalId) {
        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.CAPITALIZED_INCOME_ADJUSTMENT, transactionDate,
                amount.getAmount(), amount.getAmount(), null, null, null, null, false, paymentDetail, externalId);
    }

    public static LoanTransaction buyDownFeeAdjustment(final Loan loan, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate transactionDate, final ExternalId externalId) {
        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.BUY_DOWN_FEE_ADJUSTMENT, transactionDate, amount.getAmount(),
                null, null, null, null, null, false, paymentDetail, externalId);
    }

    public static LoanTransaction capitalizedIncomeAmortizationAdjustment(final Loan loan, final Money amount,
            final LocalDate transactionDate, final ExternalId externalId) {
        return switch (loan.getLoanProductRelatedDetail().getCapitalizedIncomeType()) {
            case FEE -> new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT,
                    transactionDate, amount.getAmount(), null, null, amount.getAmount(), null, null, false, null, externalId);
            case INTEREST -> new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT,
                    transactionDate, amount.getAmount(), null, amount.getAmount(), null, null, null, false, null, externalId);
        };
    }

    public static LoanTransaction buyDownFeeAmortization(final Loan loan, final Office office, final LocalDate dateOf,
            final BigDecimal amount, final ExternalId externalId) {
        return switch (loan.getLoanProductRelatedDetail().getBuyDownFeeIncomeType()) {
            case FEE -> new LoanTransaction(loan, office, LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION, dateOf, amount, null, null, amount,
                    null, null, false, null, externalId);
            case INTEREST -> new LoanTransaction(loan, office, LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION, dateOf, amount, null, amount,
                    null, null, null, false, null, externalId);
        };
    }

    public static LoanTransaction buyDownFeeAmortizationAdjustment(final Loan loan, final Money amount, final LocalDate transactionDate,
            final ExternalId externalId) {
        return switch (loan.getLoanProductRelatedDetail().getBuyDownFeeIncomeType()) {
            case FEE -> new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT,
                    transactionDate, amount.getAmount(), null, null, amount.getAmount(), null, null, false, null, externalId);
            case INTEREST -> new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT,
                    transactionDate, amount.getAmount(), null, amount.getAmount(), null, null, null, false, null, externalId);
        };
    }

    public LoanTransaction copyTransactionPropertiesAndMappings() {
        LoanTransaction newTransaction = copyTransactionProperties(this);
        newTransaction.updateLoanTransactionToRepaymentScheduleMappings(loanTransactionToRepaymentScheduleMappings);
        newTransaction.updateLoanChargePaidMappings(loanChargesPaid);
        return newTransaction;
    }

    public static LoanTransaction creditBalanceRefund(final Loan loan, final Office office, final Money amount, final LocalDate paymentDate,
            final ExternalId externalId, PaymentDetail paymentDetail) {
        return new LoanTransaction(loan, office, LoanTransactionType.CREDIT_BALANCE_REFUND, paymentDate, amount.getAmount(), null, null,
                null, null, amount.getAmount(), false, paymentDetail, externalId);
    }

    public static LoanTransaction refundForActiveLoan(final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final ExternalId externalId) {
        return new LoanTransaction(null, office, LoanTransactionType.REFUND_FOR_ACTIVE_LOAN, paymentDetail, amount.getAmount(), paymentDate,
                externalId);
    }

    public static LoanTransaction interestRefund(final Loan loan, final Office office, final BigDecimal amount, final BigDecimal principal,
            final BigDecimal interest, final BigDecimal feeCharges, final BigDecimal penaltyCharges, final PaymentDetail paymentDetail,
            final LocalDate refundDate, final ExternalId externalId) {
        return new LoanTransaction(loan, office, LoanTransactionType.INTEREST_REFUND, refundDate, amount, principal, interest, feeCharges,
                penaltyCharges, amount, false, paymentDetail, externalId);
    }

    public static boolean transactionAmountsMatch(final MonetaryCurrency currency, final LoanTransaction loanTransaction,
            final LoanTransaction newLoanTransaction) {
        return loanTransaction.getAmount(currency).isEqualTo(newLoanTransaction.getAmount(currency))
                && loanTransaction.getPrincipalPortion(currency).isEqualTo(newLoanTransaction.getPrincipalPortion(currency))
                && loanTransaction.getInterestPortion(currency).isEqualTo(newLoanTransaction.getInterestPortion(currency))
                && loanTransaction.getFeeChargesPortion(currency).isEqualTo(newLoanTransaction.getFeeChargesPortion(currency))
                && loanTransaction.getPenaltyChargesPortion(currency).isEqualTo(newLoanTransaction.getPenaltyChargesPortion(currency))
                && loanTransaction.getOverPaymentPortion(currency).isEqualTo(newLoanTransaction.getOverPaymentPortion(currency));
    }

    public LoanTransaction(final Loan loan, final Office office, final LoanTransactionType typeOf, final LocalDate dateOf,
            final BigDecimal amount, final BigDecimal principalPortion, final BigDecimal interestPortion,
            final BigDecimal feeChargesPortion, final BigDecimal penaltyChargesPortion, final BigDecimal overPaymentPortion,
            final boolean reversed, final PaymentDetail paymentDetail, final ExternalId externalId) {

        this.loan = loan;
        this.typeOf = typeOf;
        this.dateOf = dateOf;
        this.amount = amount;
        this.principalPortion = principalPortion;
        this.interestPortion = interestPortion;
        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
        this.overPaymentPortion = overPaymentPortion;
        this.reversed = reversed;
        this.paymentDetail = paymentDetail;
        this.office = office;
        this.externalId = externalId;
        this.submittedOnDate = DateUtils.getBusinessLocalDate();
    }

    public static LoanTransaction waiveLoanCharge(final Loan loan, final Office office, final Money waived, final LocalDate waiveDate,
            final Money feeChargesWaived, final Money penaltyChargesWaived, final Money unrecognizedCharge, final ExternalId externalId) {
        final LoanTransaction waiver = new LoanTransaction(loan, office, LoanTransactionType.WAIVE_CHARGES, waived.getAmount(), waiveDate,
                externalId);
        waiver.updateChargesComponents(feeChargesWaived, penaltyChargesWaived, unrecognizedCharge);

        return waiver;
    }

    public static LoanTransaction writeoff(final Loan loan, final Office office, final LocalDate writeOffDate,
            final ExternalId externalId) {
        return new LoanTransaction(loan, office, LoanTransactionType.WRITEOFF, null, writeOffDate, externalId);
    }

    public static LoanTransaction chargeOff(final Loan loan, final LocalDate chargeOffDate, final ExternalId externalId) {
        return createTerminalTransaction(loan, chargeOffDate, LoanTransactionType.CHARGE_OFF, externalId);
    }

    public static LoanTransaction contractTermination(final Loan loan, final LocalDate transactionDate, final ExternalId externalId) {
        return createTerminalTransaction(loan, transactionDate, LoanTransactionType.CONTRACT_TERMINATION, externalId);
    }

    private static LoanTransaction createTerminalTransaction(final Loan loan, final LocalDate transactionDate,
            final LoanTransactionType transactionType, final ExternalId externalId) {
        BigDecimal principalPortion = loan.getSummary().getTotalPrincipalOutstanding().compareTo(BigDecimal.ZERO) != 0
                ? loan.getSummary().getTotalPrincipalOutstanding()
                : null;
        BigDecimal interestPortion = loan.getSummary().getTotalInterestOutstanding().compareTo(BigDecimal.ZERO) != 0
                ? loan.getSummary().getTotalInterestOutstanding()
                : null;
        BigDecimal feePortion = loan.getSummary().getTotalFeeChargesOutstanding().compareTo(BigDecimal.ZERO) != 0
                ? loan.getSummary().getTotalFeeChargesOutstanding()
                : null;
        BigDecimal penaltyPortion = loan.getSummary().getTotalPenaltyChargesOutstanding().compareTo(BigDecimal.ZERO) != 0
                ? loan.getSummary().getTotalPenaltyChargesOutstanding()
                : null;
        BigDecimal totalOutstanding = loan.getSummary().getTotalOutstanding();

        return new LoanTransaction(loan, loan.getOffice(), transactionType, transactionDate, totalOutstanding, principalPortion,
                interestPortion, feePortion, penaltyPortion, null, false, null, externalId);
    }

    private LoanTransaction(final Loan loan, final Office office, final LoanTransactionType type, final BigDecimal amount,
            final LocalDate date, final ExternalId externalId) {
        this.loan = loan;
        this.typeOf = type;
        this.amount = amount;
        this.dateOf = date;
        this.externalId = externalId;
        this.office = office;
        this.submittedOnDate = DateUtils.getBusinessLocalDate();
    }

    private LoanTransaction(final Loan loan, final Office office, final LoanTransactionType type, final PaymentDetail paymentDetail,
            final BigDecimal amount, final LocalDate date, final ExternalId externalId) {
        this.loan = loan;
        this.typeOf = type;
        this.paymentDetail = paymentDetail;
        this.amount = amount;
        this.dateOf = date;
        this.externalId = externalId;
        this.office = office;
        this.submittedOnDate = DateUtils.getBusinessLocalDate();
    }

    private LoanTransaction(final Loan loan, final Office office, final LoanTransactionType type, final PaymentDetail paymentDetail,
            final BigDecimal amount, final LocalDate date, final ExternalId externalId, final String chargeRefundChargeType) {
        this.loan = loan;
        this.typeOf = type;
        this.paymentDetail = paymentDetail;
        this.amount = amount;
        this.dateOf = date;
        this.externalId = externalId;
        this.office = office;
        this.submittedOnDate = DateUtils.getBusinessLocalDate();
        this.chargeRefundChargeType = chargeRefundChargeType;
    }

    public void reverse() {
        this.reversed = true;
        this.reversedOnDate = DateUtils.getBusinessLocalDate();
        this.loanTransactionToRepaymentScheduleMappings.clear();
    }

    public void reverse(final ExternalId reversalExternalId) {
        this.reverse();
        this.reversalExternalId = reversalExternalId;
    }

    public void resetDerivedComponents() {
        this.principalPortion = null;
        this.interestPortion = null;
        this.feeChargesPortion = null;
        this.penaltyChargesPortion = null;
        this.overPaymentPortion = null;
        this.outstandingLoanBalance = null;
    }

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    /**
     * This updates the derived fields of a loan transaction for the principal, interest and interest waived portions.
     *
     * This accumulates the values passed to the already existent values for each of the portions.
     *
     * @param principal
     *            principal
     * @param interest
     *            interest
     * @param feeCharges
     *            feeCharges
     * @param penaltyCharges
     *            penaltyCharges
     */
    public void updateComponents(final Money principal, final Money interest, final Money feeCharges, final Money penaltyCharges) {
        this.principalPortion = MathUtil.zeroToNull(MathUtil.add(getPrincipalPortion(), MathUtil.toBigDecimal(principal)));
        this.interestPortion = MathUtil.zeroToNull(MathUtil.add(getInterestPortion(), MathUtil.toBigDecimal(interest)));
        updateChargesComponents(feeCharges, penaltyCharges);
    }

    public void updateChargesComponents(final Money feeCharges, final Money penaltyCharges) {
        this.feeChargesPortion = MathUtil.zeroToNull(MathUtil.add(getFeeChargesPortion(), MathUtil.toBigDecimal(feeCharges)));
        this.penaltyChargesPortion = MathUtil.zeroToNull(MathUtil.add(getPenaltyChargesPortion(), MathUtil.toBigDecimal(penaltyCharges)));
    }

    private void updateChargesComponents(final Money feeCharges, final Money penaltyCharges, final Money unrecognizedCharges) {
        this.feeChargesPortion = MathUtil.zeroToNull(MathUtil.add(getFeeChargesPortion(), MathUtil.toBigDecimal(feeCharges)));
        this.penaltyChargesPortion = MathUtil.zeroToNull(MathUtil.add(getPenaltyChargesPortion(), MathUtil.toBigDecimal(penaltyCharges)));
        this.unrecognizedIncomePortion = MathUtil
                .zeroToNull(MathUtil.add(getUnrecognizedIncomePortion(), MathUtil.toBigDecimal(unrecognizedCharges)));
    }

    private void updateInterestComponent(final Money interest, final Money unrecognizedInterest) {
        this.interestPortion = MathUtil.zeroToNull(MathUtil.add(getInterestPortion(), MathUtil.toBigDecimal(interest)));
        this.unrecognizedIncomePortion = MathUtil
                .zeroToNull(MathUtil.add(getUnrecognizedIncomePortion(), MathUtil.toBigDecimal(unrecognizedInterest)));
    }

    public void adjustInterestComponent() {
        this.interestPortion = MathUtil.zeroToNull(MathUtil.subtract(getInterestPortion(), getUnrecognizedIncomePortion()));
    }

    public void updateComponentsAndTotal(final Money principal, final Money interest, final Money feeCharges, final Money penaltyCharges) {
        updateComponents(principal, interest, feeCharges, penaltyCharges);
        this.amount = MathUtil
                .nullToZero(MathUtil.add(getPrincipalPortion(), getInterestPortion(), getFeeChargesPortion(), getPenaltyChargesPortion()));
    }

    public void setOverPayments(final Money overPayment) {
        this.overPaymentPortion = MathUtil.zeroToNull(MathUtil.toBigDecimal(overPayment));
    }

    public Money getPrincipalPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.principalPortion);
    }

    public Money getInterestPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestPortion);
    }

    public Money getFeeChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesPortion);
    }

    public Money getPenaltyChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesPortion);
    }

    public Money getOverPaymentPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.overPaymentPortion);
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public LocalDate getTransactionDate() {
        return this.dateOf;
    }

    public LoanTransactionType getTypeOf() {
        return this.typeOf == null ? LoanTransactionType.INVALID : this.typeOf;
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public boolean isNotReversed() {
        return !isReversed();
    }

    public void setManuallyAdjustedOrReversed() {
        this.manuallyAdjustedOrReversed = true;
    }

    public boolean isRepaymentLikeType() {
        return isRepayment() || isMerchantIssuedRefund() || isPayoutRefund() || isGoodwillCredit() || isChargeRefund()
                || isChargeAdjustment() || isDownPayment() || isInterestPaymentWaiver() || isInterestRefund()
                || isCapitalizedIncomeAdjustment();
    }

    public boolean isTypeAllowedForChargeback() {
        return isRepayment() || isMerchantIssuedRefund() || isPayoutRefund() || isGoodwillCredit() || isDownPayment()
                || isInterestPaymentWaiver();
    }

    public boolean isRepayment() {
        return LoanTransactionType.REPAYMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isMerchantIssuedRefund() {
        return LoanTransactionType.MERCHANT_ISSUED_REFUND.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isDownPayment() {
        return LoanTransactionType.DOWN_PAYMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isPayoutRefund() {
        return LoanTransactionType.PAYOUT_REFUND.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isGoodwillCredit() {
        return LoanTransactionType.GOODWILL_CREDIT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isInterestPaymentWaiver() {
        return LoanTransactionType.INTEREST_PAYMENT_WAIVER.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isChargeRefund() {
        return LoanTransactionType.CHARGE_REFUND.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isChargeAdjustment() {
        return LoanTransactionType.CHARGE_ADJUSTMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isNotRepaymentLikeType() {
        return !isRepaymentLikeType();
    }

    public boolean isIncomePosting() {
        return LoanTransactionType.INCOME_POSTING.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isDisbursement() {
        return LoanTransactionType.DISBURSEMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isRepaymentAtDisbursement() {
        return LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isNotRecoveryRepayment() {
        return !isRecoveryRepayment();
    }

    public boolean isRecoveryRepayment() {
        return LoanTransactionType.RECOVERY_REPAYMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isInterestWaiver() {
        return LoanTransactionType.WAIVE_INTEREST.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isChargesWaiver() {
        return LoanTransactionType.WAIVE_CHARGES.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isCapitalizedIncome() {
        return LoanTransactionType.CAPITALIZED_INCOME.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isDeferredIncome() {
        return isCapitalizedIncome() || isBuyDownFee();
    }

    public boolean isCapitalizedIncomeAmortization() {
        return LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isCapitalizedIncomeAmortizationAdjustment() {
        return LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isCapitalizedIncomeAdjustment() {
        return LoanTransactionType.CAPITALIZED_INCOME_ADJUSTMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isContractTermination() {
        return LoanTransactionType.CONTRACT_TERMINATION.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isWaiver() {
        return isInterestWaiver() || isChargesWaiver();
    }

    public boolean isNotWaiver() {
        return !isInterestWaiver() && !isChargesWaiver();
    }

    public boolean isNotCreditBalanceRefund() {
        return !isCreditBalanceRefund();
    }

    public boolean isChargePayment() {
        return getTypeOf().isChargePayment() && isNotReversed();
    }

    public boolean isChargeback() {
        return getTypeOf().isChargeback() && isNotReversed();
    }

    public boolean isEditable() {
        return !(isChargeback() || isGoodwillCredit() || isPayoutRefund() || isMerchantIssuedRefund());
    }

    public boolean isPenaltyPayment() {
        boolean isPenalty = false;
        if (isChargePayment()) {
            for (final LoanChargePaidBy chargePaidBy : this.loanChargesPaid) {
                isPenalty = chargePaidBy.getLoanCharge().isPenaltyCharge();
                break;
            }
        }
        return isPenalty;
    }

    public boolean isWriteOff() {
        return getTypeOf().isWriteOff() && isNotReversed();
    }

    public boolean isChargeOff() {
        return getTypeOf().isChargeOff() && isNotReversed();
    }

    public boolean isReAge() {
        return getTypeOf().isReAge() && isNotReversed();
    }

    public boolean isReAmortize() {
        return getTypeOf().isReAmortize() && isNotReversed();
    }

    public boolean isGreaterThan(final Money monetaryAmount) {
        return MathUtil.isGreaterThan(amount, MathUtil.toBigDecimal(monetaryAmount));
    }

    public boolean isGreaterThanZero() {
        return MathUtil.isGreaterThanZero(amount);
    }

    public boolean isGreaterThanZeroAndLessThanOrEqualTo(BigDecimal totalOverpaid) {
        return isGreaterThanZero() && !MathUtil.isGreaterThan(amount, totalOverpaid);
    }

    public boolean isNotZero() {
        return !MathUtil.isEmpty(amount);
    }

    public boolean isRefund() {
        return LoanTransactionType.REFUND.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isCreditBalanceRefund() {
        return LoanTransactionType.CREDIT_BALANCE_REFUND.equals(getTypeOf()) && isNotReversed();
    }

    public void updateExternalId(final ExternalId externalId) {
        this.externalId = externalId;
    }

    public boolean isAccrual() {
        return getTypeOf().isAccrual();
    }

    public boolean isAccrualAdjustment() {
        return getTypeOf().isAccrualAdjustment();
    }

    public boolean isAccrualActivity() {
        return getTypeOf().isAccrualActivity();
    }

    public boolean isAccrualRelated() {
        return isAccrual() || isAccrualAdjustment() || isAccrualActivity();
    }

    public boolean isWaiveCharge() {
        return getTypeOf().isWaiveCharges();
    }

    public boolean isWaiveInterest() {
        return getTypeOf().isWaiveInterest();
    }

    public boolean isNonMonetaryTransaction() {
        LoanTransactionType type = getTypeOf();
        return isNotReversed() && (type == LoanTransactionType.CONTRA || type == LoanTransactionType.MARKED_FOR_RESCHEDULING
                || type == LoanTransactionType.ACCRUAL || type == LoanTransactionType.ACCRUAL_ADJUSTMENT
                || type == LoanTransactionType.ACCRUAL_ACTIVITY || type == LoanTransactionType.APPROVE_TRANSFER
                || type == LoanTransactionType.INITIATE_TRANSFER || type == LoanTransactionType.REJECT_TRANSFER
                || type == LoanTransactionType.WITHDRAW_TRANSFER || type == LoanTransactionType.CHARGE_OFF
                || type == LoanTransactionType.REAMORTIZE || type == LoanTransactionType.REAGE
                || type == LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION || type == LoanTransactionType.CONTRACT_TERMINATION
                || type == LoanTransactionType.CAPITALIZED_INCOME_AMORTIZATION_ADJUSTMENT
                || type == LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION
                || type == LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT);
    }

    public void updateOutstandingLoanBalance(BigDecimal outstandingLoanBalance) {
        this.outstandingLoanBalance = outstandingLoanBalance;
    }

    public boolean isNotRefundForActiveLoan() {
        // TODO Auto-generated method stub
        return !isRefundForActiveLoan();
    }

    public boolean isRefundForActiveLoan() {
        return LoanTransactionType.REFUND_FOR_ACTIVE_LOAN.equals(getTypeOf()) && isNotReversed();
    }

    public void manuallyAdjustedOrReversed() {
        this.manuallyAdjustedOrReversed = true;
    }

    public void updateLoanTransactionToRepaymentScheduleMappings(final Collection<LoanTransactionToRepaymentScheduleMapping> mappings) {
        Collection<LoanTransactionToRepaymentScheduleMapping> retainMappings = new ArrayList<>();
        for (LoanTransactionToRepaymentScheduleMapping updatedMapping : mappings) {
            updateMappingDetail(retainMappings, updatedMapping, true);
        }
        this.loanTransactionToRepaymentScheduleMappings.retainAll(retainMappings);
    }

    public void addLoanTransactionToRepaymentScheduleMappings(final Collection<LoanTransactionToRepaymentScheduleMapping> updatedMappings) {
        for (LoanTransactionToRepaymentScheduleMapping updatedMapping : updatedMappings) {
            updateMappingDetail(null, updatedMapping, false);
        }
    }

    private void updateMappingDetail(final Collection<LoanTransactionToRepaymentScheduleMapping> retainMappings,
            final LoanTransactionToRepaymentScheduleMapping updatedMapping, boolean overwrite) {
        boolean isMappingUpdated = false;
        LoanRepaymentScheduleInstallment updatedInstallment = updatedMapping.getLoanRepaymentScheduleInstallment();
        for (LoanTransactionToRepaymentScheduleMapping existingMapping : this.loanTransactionToRepaymentScheduleMappings) {
            LoanRepaymentScheduleInstallment existingInstallment = existingMapping.getLoanRepaymentScheduleInstallment();
            if (DateUtils.isEqual(existingInstallment.getDueDate(), updatedInstallment.getDueDate()) && updatedInstallment.getId() != null
                    && updatedInstallment.getId().equals(existingInstallment.getId())) {
                if (overwrite) {
                    existingMapping.setComponents(updatedMapping.getPrincipalPortion(), updatedMapping.getInterestPortion(),
                            updatedMapping.getFeeChargesPortion(), updatedMapping.getPenaltyChargesPortion());
                } else {
                    existingMapping.updateComponents(updatedMapping.getPrincipalPortion(), updatedMapping.getInterestPortion(),
                            updatedMapping.getFeeChargesPortion(), updatedMapping.getPenaltyChargesPortion());
                }
                isMappingUpdated = true;
                if (retainMappings != null) {
                    retainMappings.add(existingMapping);
                }
                break;
            }
        }
        if (!isMappingUpdated) {
            LoanTransactionToRepaymentScheduleMapping newMapping = LoanTransactionToRepaymentScheduleMapping.createFrom(this,
                    updatedInstallment, null, null, null, null);
            newMapping.setComponents(updatedMapping.getPrincipalPortion(), updatedMapping.getInterestPortion(),
                    updatedMapping.getFeeChargesPortion(), updatedMapping.getPenaltyChargesPortion());
            this.loanTransactionToRepaymentScheduleMappings.add(newMapping);
            if (retainMappings != null) {
                retainMappings.add(newMapping);
            }
        }
    }

    public void updateLoanChargePaidMappings(final Collection<LoanChargePaidBy> updatedMappings) {
        Collection<LoanChargePaidBy> retainMappings = new ArrayList<>();
        for (LoanChargePaidBy updatedMapping : updatedMappings) {
            updateLoanChargePaid(retainMappings, updatedMapping);
        }
        this.loanChargesPaid.retainAll(retainMappings);
    }

    private void updateLoanChargePaid(final Collection<LoanChargePaidBy> retainMappings, final LoanChargePaidBy updatedMapping) {
        boolean updated = false;
        LoanCharge updatedCharge = updatedMapping.getLoanCharge();
        Integer updatedInstallment = updatedMapping.getInstallmentNumber();
        for (LoanChargePaidBy existingMapping : loanChargesPaid) {
            LoanCharge existingCharge = existingMapping.getLoanCharge();
            Integer existingInstallment = existingMapping.getInstallmentNumber();
            if (existingCharge.equals(updatedCharge)
                    && (existingInstallment == null ? updatedInstallment == null : existingInstallment.equals(updatedInstallment))) {
                existingMapping.setAmount(updatedMapping.getAmount());
                updated = true;
                retainMappings.add(existingMapping);
                break;
            }
        }
        if (!updated) {
            LoanChargePaidBy newMapping = new LoanChargePaidBy(this, updatedCharge, updatedMapping.getAmount(), updatedInstallment);
            this.loanChargesPaid.add(newMapping);
            retainMappings.add(newMapping);
        }
    }

    public Boolean isAllowTypeTransactionAtTheTimeOfLastUndo() {
        return isNotReversed() && (isDisbursement() || isAccrualActivity() || isRepaymentAtDisbursement() || isRepayment());
    }

    public Money getOutstandingLoanBalanceMoney(final MonetaryCurrency currency) {
        return Money.of(currency, this.outstandingLoanBalance);
    }

    public boolean isPaymentTransaction() {
        return this.isNotReversed() && !(this.isDisbursement() || this.isRepaymentAtDisbursement() || this.isNonMonetaryTransaction()
                || this.isIncomePosting());
    }

    public List<LoanTransactionRelation> getLoanTransactionRelations(Predicate<LoanTransactionRelation> predicate) {
        return loanTransactionRelations.stream().filter(predicate).toList();
    }

    public boolean hasChargebackLoanTransactionRelations() {
        return !getLoanTransactionRelations(e -> LoanTransactionRelationTypeEnum.CHARGEBACK.equals(e.getRelationType())).isEmpty();
    }

    public void copyLoanTransactionRelations(Set<LoanTransactionRelation> sourceLoanTransactionRelations) {
        for (LoanTransactionRelation existingLoanTransactionRelation : sourceLoanTransactionRelations) {
            loanTransactionRelations.add(new LoanTransactionRelation(this, existingLoanTransactionRelation.getToTransaction(),
                    existingLoanTransactionRelation.getToCharge(), existingLoanTransactionRelation.getRelationType()));
        }
    }

    public boolean isBefore(final LocalDate date) {
        return DateUtils.isBefore(getTransactionDate(), date);
    }

    public boolean isAfter(final LocalDate date) {
        return DateUtils.isAfter(getTransactionDate(), date);
    }

    public boolean isOn(final LocalDate date) {
        return DateUtils.isEqual(getTransactionDate(), date);
    }

    public boolean happenedBefore(LoanTransaction loanTransaction) {
        return LoanTransactionComparator.INSTANCE.compare(this, loanTransaction) < 0;
    }

    public boolean isOverPaid() {
        return MathUtil.isGreaterThanZero(overPaymentPortion);
    }

    public boolean isInterestRefund() {
        return getTypeOf().isInterestRefund();
    }

    public void updateAmount(BigDecimal bigDecimal) {
        this.amount = bigDecimal;
    }

    // TODO missing hashCode(), equals(Object obj), but probably OK as long as
    // this is never stored in a Collection.

    public void updateTransactionDate(final LocalDate transactionDate) {
        this.dateOf = transactionDate;
    }

    public static LoanTransaction buyDownFee(final Loan loan, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate transactionDate, final ExternalId externalId) {
        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.BUY_DOWN_FEE, paymentDetail, amount.getAmount(),
                transactionDate, externalId);
    }

    public boolean isBuyDownFee() {
        return LoanTransactionType.BUY_DOWN_FEE.equals(this.typeOf);
    }

    public boolean isBuyDownFeeAdjustment() {
        return LoanTransactionType.BUY_DOWN_FEE_ADJUSTMENT.equals(this.typeOf);
    }

    public boolean isBuyDownFeeAmortization() {
        return LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION.equals(this.typeOf);
    }

    public boolean isBuyDownFeeAmortizationAdjustment() {
        return LoanTransactionType.BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT.equals(this.typeOf);
    }
}
