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
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.LoanChargeWithoutMandatoryFieldException;
import org.apache.fineract.portfolio.loanaccount.command.LoanChargeCommand;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargePaidDetail;
import org.joda.time.LocalDate;

@Entity
@Table(name = "m_loan_charge")
public class LoanCharge extends AbstractPersistableCustom<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", referencedColumnName = "id", nullable = false)
    private Loan loan;

    @ManyToOne(optional = false)
    @JoinColumn(name = "charge_id", referencedColumnName = "id", nullable = false)
    private Charge charge;

    @Column(name = "charge_time_enum", nullable = false)
    private Integer chargeTime;

    @Temporal(TemporalType.DATE)
    @Column(name = "due_for_collection_as_of_date")
    private Date dueDate;

    @Column(name = "charge_calculation_enum")
    private Integer chargeCalculation;

    @Column(name = "charge_payment_mode_enum")
    private Integer chargePaymentMode;

    @Column(name = "calculation_percentage", scale = 6, precision = 19, nullable = true)
    private BigDecimal percentage;

    @Column(name = "calculation_on_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountPercentageAppliedTo;

    @Column(name = "charge_amount_or_percentage", scale = 6, precision = 19, nullable = false)
    private BigDecimal amountOrPercentage;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "amount_paid_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountPaid;

    @Column(name = "amount_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountWaived;

    @Column(name = "amount_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountWrittenOff;

    @Column(name = "amount_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal amountOutstanding;

    @Column(name = "is_penalty", nullable = false)
    private boolean penaltyCharge = false;

    @Column(name = "is_paid_derived", nullable = false)
    private boolean paid = false;

    @Column(name = "waived", nullable = false)
    private boolean waived = false;

    @Column(name = "min_cap", scale = 6, precision = 19, nullable = true)
    private BigDecimal minCap;

    @Column(name = "max_cap", scale = 6, precision = 19, nullable = true)
    private BigDecimal maxCap;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loancharge", orphanRemoval = true, fetch=FetchType.EAGER)
    private Set<LoanInstallmentCharge> loanInstallmentCharge = new HashSet<>();

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @OneToOne(mappedBy = "loancharge", cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanOverdueInstallmentCharge overdueInstallmentCharge;

    @OneToOne(mappedBy = "loancharge", cascade = CascadeType.ALL, optional = true, orphanRemoval = true, fetch = FetchType.EAGER)
    private LoanTrancheDisbursementCharge loanTrancheDisbursementCharge;

    public static LoanCharge createNewFromJson(final Loan loan, final Charge chargeDefinition, final JsonCommand command) {
        final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
        return createNewFromJson(loan, chargeDefinition, command, dueDate);
    }

    public static LoanCharge createNewFromJson(final Loan loan, final Charge chargeDefinition, final JsonCommand command,
            final LocalDate dueDate) {
        final BigDecimal amount = command.bigDecimalValueOfParameterNamed("amount");

        final ChargeTimeType chargeTime = null;
        final ChargeCalculationType chargeCalculation = null;
        final ChargePaymentMode chargePaymentMode = null;
        BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        switch (ChargeCalculationType.fromInt(chargeDefinition.getChargeCalculation())) {
            case PERCENT_OF_AMOUNT:
                if (command.hasParameter("principal")) {
                    amountPercentageAppliedTo = command.bigDecimalValueOfParameterNamed("principal");
                } else {
                    amountPercentageAppliedTo = loan.getPrincpal().getAmount();
                }
            break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                if (command.hasParameter("principal") && command.hasParameter("interest")) {
                    amountPercentageAppliedTo = command.bigDecimalValueOfParameterNamed("principal").add(
                            command.bigDecimalValueOfParameterNamed("interest"));
                } else {
                    amountPercentageAppliedTo = loan.getPrincpal().getAmount().add(loan.getTotalInterest());
                }
            break;
            case PERCENT_OF_INTEREST:
                if (command.hasParameter("interest")) {
                    amountPercentageAppliedTo = command.bigDecimalValueOfParameterNamed("interest");
                } else {
                    amountPercentageAppliedTo = loan.getTotalInterest();
                }
            break;
            default:
            break;
        }

        BigDecimal loanCharge = BigDecimal.ZERO;
        if (ChargeTimeType.fromInt(chargeDefinition.getChargeTimeType()).equals(ChargeTimeType.INSTALMENT_FEE)) {
            BigDecimal percentage = amount;
            if (percentage == null) {
                percentage = chargeDefinition.getAmount();
            }
            loanCharge = loan.calculatePerInstallmentChargeAmount(ChargeCalculationType.fromInt(chargeDefinition.getChargeCalculation()),
                    percentage);
        }

        return new LoanCharge(loan, chargeDefinition, amountPercentageAppliedTo, amount, chargeTime, chargeCalculation, dueDate,
                chargePaymentMode, null, loanCharge);
    }

    /*
     * loanPrincipal is required for charges that are percentage based
     */
    public static LoanCharge createNewWithoutLoan(final Charge chargeDefinition, final BigDecimal loanPrincipal, final BigDecimal amount,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculation, final LocalDate dueDate,
            final ChargePaymentMode chargePaymentMode, final Integer numberOfRepayments) {
        return new LoanCharge(null, chargeDefinition, loanPrincipal, amount, chargeTime, chargeCalculation, dueDate, chargePaymentMode,
                numberOfRepayments, BigDecimal.ZERO);
    }

    protected LoanCharge() {
        //
    }

    public LoanCharge(final Loan loan, final Charge chargeDefinition, final BigDecimal loanPrincipal, final BigDecimal amount,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculation, final LocalDate dueDate,
            final ChargePaymentMode chargePaymentMode, final Integer numberOfRepayments, final BigDecimal loanCharge) {
        this.loan = loan;
        this.charge = chargeDefinition;
        this.penaltyCharge = chargeDefinition.isPenalty();
        this.minCap = chargeDefinition.getMinCap();
        this.maxCap = chargeDefinition.getMaxCap();

        this.chargeTime = chargeDefinition.getChargeTimeType();
        if (chargeTime != null) {
            this.chargeTime = chargeTime.getValue();
        }

        if (ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.SPECIFIED_DUE_DATE)
                || ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.OVERDUE_INSTALLMENT)) {

            if (dueDate == null) {
                final String defaultUserMessage = "Loan charge is missing due date.";
                throw new LoanChargeWithoutMandatoryFieldException("loanCharge", "dueDate", defaultUserMessage, chargeDefinition.getId(),
                        chargeDefinition.getName());
            }

            this.dueDate = dueDate.toDate();
        } else {
            this.dueDate = null;
        }

        this.chargeCalculation = chargeDefinition.getChargeCalculation();
        if (chargeCalculation != null) {
            this.chargeCalculation = chargeCalculation.getValue();
        }

        BigDecimal chargeAmount = chargeDefinition.getAmount();
        if (amount != null) {
            chargeAmount = amount;
        }

        this.chargePaymentMode = chargeDefinition.getChargePaymentMode();
        if (chargePaymentMode != null) {
            this.chargePaymentMode = chargePaymentMode.getValue();
        }
        populateDerivedFields(loanPrincipal, chargeAmount, numberOfRepayments, loanCharge);
        this.paid = determineIfFullyPaid();
    }

    private void populateDerivedFields(final BigDecimal amountPercentageAppliedTo, final BigDecimal chargeAmount,
            Integer numberOfRepayments, BigDecimal loanCharge) {

        switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
            case INVALID:
                this.percentage = null;
                this.amount = null;
                this.amountPercentageAppliedTo = null;
                this.amountPaid = null;
                this.amountOutstanding = BigDecimal.ZERO;
                this.amountWaived = null;
                this.amountWrittenOff = null;
            break;
            case FLAT:
                this.percentage = null;
                this.amountPercentageAppliedTo = null;
                this.amountPaid = null;
                if (isInstalmentFee()) {
                    if (numberOfRepayments == null) {
                        numberOfRepayments = this.loan.fetchNumberOfInstallmensAfterExceptions();
                    }
                    this.amount = chargeAmount.multiply(BigDecimal.valueOf(numberOfRepayments));
                } else {
                    this.amount = chargeAmount;
                }
                this.amountOutstanding = this.amount;
                this.amountWaived = null;
                this.amountWrittenOff = null;
            break;
            case PERCENT_OF_AMOUNT:
            case PERCENT_OF_AMOUNT_AND_INTEREST:
            case PERCENT_OF_INTEREST:
            case PERCENT_OF_DISBURSEMENT_AMOUNT:
                this.percentage = chargeAmount;
                this.amountPercentageAppliedTo = amountPercentageAppliedTo;
                if (loanCharge.compareTo(BigDecimal.ZERO) == 0) {
                    loanCharge = percentageOf(this.amountPercentageAppliedTo);
                }
                this.amount = minimumAndMaximumCap(loanCharge);
                this.amountPaid = null;
                this.amountOutstanding = calculateOutstanding();
                this.amountWaived = null;
                this.amountWrittenOff = null;
            break;
        }
        this.amountOrPercentage = chargeAmount;
        if (this.loan != null && isInstalmentFee()) {
            updateInstallmentCharges();
        }
    }

    public void markAsFullyPaid() {
        this.amountPaid = this.amount;
        this.amountOutstanding = BigDecimal.ZERO;
        this.paid = true;
    }

    public boolean isFullyPaid() {
        return this.paid;
    }

    public void resetToOriginal(final MonetaryCurrency currency) {
        this.amountPaid = BigDecimal.ZERO;
        this.amountWaived = BigDecimal.ZERO;
        this.amountWrittenOff = BigDecimal.ZERO;
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.paid = false;
        this.waived = false;
        for (final LoanInstallmentCharge installmentCharge : this.loanInstallmentCharge) {
            installmentCharge.resetToOriginal(currency);
        }
    }

    public void resetPaidAmount(final MonetaryCurrency currency) {
        this.amountPaid = BigDecimal.ZERO;
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.paid = false;
        for (final LoanInstallmentCharge installmentCharge : this.loanInstallmentCharge) {
            installmentCharge.resetPaidAmount(currency);
        }
    }

    public Money waive(final MonetaryCurrency currency, final Integer loanInstallmentNumber) {
        if (isInstalmentFee()) {
            final LoanInstallmentCharge chargePerInstallment = getInstallmentLoanCharge(loanInstallmentNumber);
            final Money amountWaived = chargePerInstallment.waive(currency);
            if (this.amountWaived == null) {
                this.amountWaived = BigDecimal.ZERO;
            }
            this.amountWaived = this.amountWaived.add(amountWaived.getAmount());
            this.amountOutstanding = this.amountOutstanding.subtract(amountWaived.getAmount());
            if (determineIfFullyPaid()) {
                this.paid = false;
                this.waived = true;
            }
            return amountWaived;
        }
        this.amountWaived = this.amountOutstanding;
        this.amountOutstanding = BigDecimal.ZERO;
        this.paid = false;
        this.waived = true;
        return getAmountWaived(currency);

    }

    public BigDecimal getAmountPercentageAppliedTo() {
        return this.amountPercentageAppliedTo;
    }

    private BigDecimal calculateAmountOutstanding(final MonetaryCurrency currency) {
        return getAmount(currency).minus(getAmountWaived(currency)).minus(getAmountPaid(currency)).getAmount();
    }

    public void update(final Loan loan) {
        this.loan = loan;
    }

    public void update(final BigDecimal amount, final LocalDate dueDate, final BigDecimal loanPrincipal, Integer numberOfRepayments,
            BigDecimal loanCharge) {
        if (dueDate != null) {
            this.dueDate = dueDate.toDate();
        }

        if (amount != null) {
            switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
                case INVALID:
                break;
                case FLAT:
                    if (isInstalmentFee()) {
                        if (numberOfRepayments == null) {
                            numberOfRepayments = this.loan.fetchNumberOfInstallmensAfterExceptions();
                        }
                        this.amount = amount.multiply(BigDecimal.valueOf(numberOfRepayments));
                    } else {
                        this.amount = amount;
                    }
                break;
                case PERCENT_OF_AMOUNT:
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                case PERCENT_OF_INTEREST:
                case PERCENT_OF_DISBURSEMENT_AMOUNT:
                    this.percentage = amount;
                    this.amountPercentageAppliedTo = loanPrincipal;
                    if (loanCharge.compareTo(BigDecimal.ZERO) == 0) {
                        loanCharge = percentageOf(this.amountPercentageAppliedTo);
                    }
                    this.amount = minimumAndMaximumCap(loanCharge);
                break;
            }
            this.amountOrPercentage = amount;
            this.amountOutstanding = calculateOutstanding();
            if (this.loan != null && isInstalmentFee()) {
                updateInstallmentCharges();
            }
        }
    }

    public void update(final BigDecimal amount, final LocalDate dueDate, final Integer numberOfRepayments) {
        BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        if (this.loan != null) {
            switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
                case PERCENT_OF_AMOUNT:
                    amountPercentageAppliedTo = this.loan.getPrincpal().getAmount();
                break;
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                    amountPercentageAppliedTo = this.loan.getPrincpal().getAmount().add(this.loan.getTotalInterest());
                break;
                case PERCENT_OF_INTEREST:
                    amountPercentageAppliedTo = this.loan.getTotalInterest();
                break;
                case PERCENT_OF_DISBURSEMENT_AMOUNT:
                    LoanTrancheDisbursementCharge loanTrancheDisbursementCharge = this.loanTrancheDisbursementCharge;
                    amountPercentageAppliedTo = loanTrancheDisbursementCharge.getloanDisbursementDetails().principal();
                break;
                default:
                break;
            }
        }
        update(amount, dueDate, amountPercentageAppliedTo, numberOfRepayments, BigDecimal.ZERO);
    }

    public Map<String, Object> update(final JsonCommand command, final BigDecimal amount) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String dueDateParamName = "dueDate";
        if (command.isChangeInLocalDateParameterNamed(dueDateParamName, getDueLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(dueDateParamName);
            actualChanges.put(dueDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(dueDateParamName);
            this.dueDate = newValue.toDate();
        }

        final String amountParamName = "amount";
        if (command.isChangeInBigDecimalParameterNamed(amountParamName, this.amount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountParamName);
            BigDecimal loanCharge = null;
            actualChanges.put(amountParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
                case INVALID:
                break;
                case FLAT:
                    if (isInstalmentFee()) {
                        this.amount = newValue.multiply(BigDecimal.valueOf(this.loan.fetchNumberOfInstallmensAfterExceptions()));
                    } else {
                        this.amount = newValue;
                    }
                    this.amountOutstanding = calculateOutstanding();
                break;
                case PERCENT_OF_AMOUNT:
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                case PERCENT_OF_INTEREST:
                case PERCENT_OF_DISBURSEMENT_AMOUNT:
                    this.percentage = newValue;
                    this.amountPercentageAppliedTo = amount;
                    loanCharge = BigDecimal.ZERO;
                    if (isInstalmentFee()) {
                        loanCharge = this.loan.calculatePerInstallmentChargeAmount(ChargeCalculationType.fromInt(this.chargeCalculation),
                                this.percentage);
                    }
                    if (loanCharge.compareTo(BigDecimal.ZERO) == 0) {
                        loanCharge = percentageOf(this.amountPercentageAppliedTo);
                    }
                    this.amount = minimumAndMaximumCap(loanCharge);
                    this.amountOutstanding = calculateOutstanding();
                break;
            }
            this.amountOrPercentage = newValue;
            if (isInstalmentFee()) {
                updateInstallmentCharges();
            }
        }
        return actualChanges;
    }

    private void updateInstallmentCharges() {
        final Collection<LoanInstallmentCharge> remove = new HashSet<>();
        final List<LoanInstallmentCharge> newChargeInstallments = this.loan.generateInstallmentLoanCharges(this);
        if (this.loanInstallmentCharge.isEmpty()) {
            this.loanInstallmentCharge.addAll(newChargeInstallments);
        } else {
            int index = 0;
            final List<LoanInstallmentCharge> oldChargeInstallments = new ArrayList<>();
            if(this.loanInstallmentCharge != null && !this.loanInstallmentCharge.isEmpty()){
                oldChargeInstallments.addAll(this.loanInstallmentCharge);
            }
            Collections.sort(oldChargeInstallments);
            final LoanInstallmentCharge[] loanChargePerInstallmentArray = newChargeInstallments.toArray(new LoanInstallmentCharge[newChargeInstallments.size()]);
            for (final LoanInstallmentCharge chargePerInstallment : oldChargeInstallments) {
                if (index == loanChargePerInstallmentArray.length) {
                    remove.add(chargePerInstallment);
                    chargePerInstallment.updateInstallment(null);
                } else {
                    chargePerInstallment.copyFrom(loanChargePerInstallmentArray[index++]);
                }
            }
            this.loanInstallmentCharge.removeAll(remove);
            while (index < loanChargePerInstallmentArray.length) {
                this.loanInstallmentCharge.add(loanChargePerInstallmentArray[index++]);
            }
        }
        Money amount = Money.zero(this.loan.getCurrency());
        for(LoanInstallmentCharge charge:this.loanInstallmentCharge){
            amount =amount.plus(charge.getAmount());
        }
        this.amount =amount.getAmount();
    }

    public boolean isDueAtDisbursement() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.DISBURSEMENT)
                || ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.TRANCHE_DISBURSEMENT);
    }

    public boolean isSpecifiedDueDate() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.SPECIFIED_DUE_DATE);
    }

    public boolean isInstalmentFee() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.INSTALMENT_FEE);
    }

    public boolean isOverdueInstallmentCharge() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.OVERDUE_INSTALLMENT);
    }

    private static boolean isGreaterThanZero(final BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 1;
    }

    public LoanChargeCommand toCommand() {
        return new LoanChargeCommand(getId(), this.charge.getId(), this.amount, this.chargeTime, this.chargeCalculation, getDueLocalDate());
    }

    public LocalDate getDueLocalDate() {
        LocalDate dueDate = null;
        if (this.dueDate != null) {
            dueDate = new LocalDate(this.dueDate);
        }
        return dueDate;
    }

    private boolean determineIfFullyPaid() {
        if (this.amount == null) { return true; }
        return BigDecimal.ZERO.compareTo(calculateOutstanding()) == 0;
    }

    private BigDecimal calculateOutstanding() {
        if (this.amount == null) { return null; }
        BigDecimal amountPaidLocal = BigDecimal.ZERO;
        if (this.amountPaid != null) {
            amountPaidLocal = this.amountPaid;
        }

        BigDecimal amountWaivedLocal = BigDecimal.ZERO;
        if (this.amountWaived != null) {
            amountWaivedLocal = this.amountWaived;
        }

        BigDecimal amountWrittenOffLocal = BigDecimal.ZERO;
        if (this.amountWrittenOff != null) {
            amountWrittenOffLocal = this.amountWrittenOff;
        }

        final BigDecimal totalAccountedFor = amountPaidLocal.add(amountWaivedLocal).add(amountWrittenOffLocal);

        return this.amount.subtract(totalAccountedFor);
    }

    public BigDecimal percentageOf(final BigDecimal value) {
        return percentageOf(value, this.percentage);
    }

    public static BigDecimal percentageOf(final BigDecimal value, final BigDecimal percentage) {

        BigDecimal percentageOf = BigDecimal.ZERO;

        if (isGreaterThanZero(value)) {
            final MathContext mc = new MathContext(8, MoneyHelper.getRoundingMode());
            final BigDecimal multiplicand = percentage.divide(BigDecimal.valueOf(100l), mc);
            percentageOf = value.multiply(multiplicand, mc);
        }
        return percentageOf;
    }

    /**
     * @param percentageOf
     * @returns a minimum cap or maximum cap set on charges if the criteria fits
     *          else it returns the percentageOf if the amount is within min and
     *          max cap
     */
    private BigDecimal minimumAndMaximumCap(final BigDecimal percentageOf) {
        BigDecimal minMaxCap = BigDecimal.ZERO;
        if (this.minCap != null) {
            final int minimumCap = percentageOf.compareTo(this.minCap);
            if (minimumCap == -1) {
                minMaxCap = this.minCap;
                return minMaxCap;
            }
        }
        if (this.maxCap != null) {
            final int maximumCap = percentageOf.compareTo(this.maxCap);
            if (maximumCap == 1) {
                minMaxCap = this.maxCap;
                return minMaxCap;
            }
        }
        minMaxCap = percentageOf;
        // this will round the amount value
        if (this.loan != null && minMaxCap != null) {
            minMaxCap = Money.of(this.loan.getCurrency(), minMaxCap).getAmount();
        }
        return minMaxCap;
    }

    public BigDecimal amount() {
        return this.amount;
    }

    public BigDecimal amountOutstanding() {
        return this.amountOutstanding;
    }
    
    public Money getAmountOutstanding(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountOutstanding);
    }

    public boolean hasNotLoanIdentifiedBy(final Long loanId) {
        return !hasLoanIdentifiedBy(loanId);
    }

    public boolean hasLoanIdentifiedBy(final Long loanId) {
        return this.loan.hasIdentifyOf(loanId);
    }

    public boolean isDueForCollectionFromAndUpToAndIncluding(final LocalDate fromNotInclusive, final LocalDate upToAndInclusive) {
        final LocalDate dueDate = getDueLocalDate();
        return occursOnDayFromAndUpToAndIncluding(fromNotInclusive, upToAndInclusive, dueDate);
    }

    private boolean occursOnDayFromAndUpToAndIncluding(final LocalDate fromNotInclusive, final LocalDate upToAndInclusive,
            final LocalDate target) {
        return target != null && target.isAfter(fromNotInclusive) && !target.isAfter(upToAndInclusive);
    }

    public boolean isFeeCharge() {
        return !this.penaltyCharge;
    }

    public boolean isPenaltyCharge() {
        return this.penaltyCharge;
    }

    public boolean isNotFullyPaid() {
        return !isPaid();
    }
    
    public boolean isChargePending(){
        return isNotFullyPaid() && !isWaived();
    }

    public boolean isPaid() {
        return this.paid;
    }

    public boolean isWaived() {
        return this.waived;
    }

    public BigDecimal getMinCap() {
        return this.minCap;
    }

    public BigDecimal getMaxCap() {
        return this.maxCap;
    }

    public boolean isPaidOrPartiallyPaid(final MonetaryCurrency currency) {

        final Money amountWaivedOrWrittenOff = getAmountWaived(currency).plus(getAmountWrittenOff(currency));
        return Money.of(currency, this.amountPaid).plus(amountWaivedOrWrittenOff).isGreaterThanZero();
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public Money getAmountPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountPaid);
    }

    public Money getAmountWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountWaived);
    }

    public Money getAmountWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountWrittenOff);
    }

    /**
     * @param feeAmount
     *            TODO
     * @param processAmount
     *            Amount used to pay off this charge
     * @return Actual amount paid on this charge
     */
    public Money updatePaidAmountBy(final Money incrementBy, final Integer installmentNumber, final Money feeAmount) {
        Money processAmount = Money.zero(incrementBy.getCurrency());
        if (isInstalmentFee()) {
            if (installmentNumber == null) {
                processAmount = getUnpaidInstallmentLoanCharge().updatePaidAmountBy(incrementBy, feeAmount);
            } else {
                processAmount = getInstallmentLoanCharge(installmentNumber).updatePaidAmountBy(incrementBy, feeAmount);
            }
        } else {
            processAmount = incrementBy;
        }
        Money amountPaidToDate = Money.of(processAmount.getCurrency(), this.amountPaid);
        final Money amountOutstanding = Money.of(processAmount.getCurrency(), this.amountOutstanding);

        Money amountPaidOnThisCharge = Money.zero(processAmount.getCurrency());
        if (processAmount.isGreaterThanOrEqualTo(amountOutstanding)) {
            amountPaidOnThisCharge = amountOutstanding;
            amountPaidToDate = amountPaidToDate.plus(amountOutstanding);
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = BigDecimal.ZERO;
            Money waivedAmount = getAmountWaived(processAmount.getCurrency());
            if (waivedAmount.isGreaterThanZero()) {
                this.waived = true;
            } else {
                this.paid = true;
            }

        } else {
            amountPaidOnThisCharge = processAmount;
            amountPaidToDate = amountPaidToDate.plus(processAmount);
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = calculateAmountOutstanding(incrementBy.getCurrency());
        }
        return amountPaidOnThisCharge;
    }

    public String name() {
        return this.charge.getName();
    }

    public String currencyCode() {
        return this.charge.getCurrencyCode();
    }

    public Charge getCharge() {
        return this.charge;
    }

    /*@Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final LoanCharge rhs = (LoanCharge) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)) //
                .append(getId(), rhs.getId()) //
                .append(this.charge.getId(), rhs.charge.getId()) //
                .append(this.amount, rhs.amount) //
                .append(getDueLocalDate(), rhs.getDueLocalDate()) //
                .isEquals();
    }

    @Override
    public int hashCode() {
        return 1;
        
         * return new HashCodeBuilder(3, 5) // .append(getId()) //
         * .append(this.charge.getId()) //
         * .append(this.amount).append(getDueLocalDate()) // .toHashCode();
         
    }*/

    public ChargePaymentMode getChargePaymentMode() {
        return ChargePaymentMode.fromInt(this.chargePaymentMode);
    }

    public ChargeCalculationType getChargeCalculation() {
        return ChargeCalculationType.fromInt(this.chargeCalculation);
    }

    public BigDecimal getPercentage() {
        return this.percentage;
    }

    public void updateAmount(final BigDecimal amount) {
        this.amount = amount;
        calculateOutstanding();
    }

    public LoanInstallmentCharge getUnpaidInstallmentLoanCharge() {
        LoanInstallmentCharge unpaidChargePerInstallment = null;
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            if (loanChargePerInstallment.isPending()
                    && (unpaidChargePerInstallment == null || unpaidChargePerInstallment.getRepaymentInstallment().getDueDate()
                            .isAfter(loanChargePerInstallment.getRepaymentInstallment().getDueDate()))) {
                unpaidChargePerInstallment = loanChargePerInstallment;
            }
        }
        return unpaidChargePerInstallment;
    }

    public LoanInstallmentCharge getInstallmentLoanCharge(final LocalDate periodDueDate) {
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            if (periodDueDate.isEqual(loanChargePerInstallment.getRepaymentInstallment().getDueDate())) { return loanChargePerInstallment; }
        }
        return null;
    }

    public LoanInstallmentCharge getInstallmentLoanCharge(final Integer installmentNumber) {
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            if (installmentNumber.equals(loanChargePerInstallment.getRepaymentInstallment().getInstallmentNumber().intValue())) { return loanChargePerInstallment; }
        }
        return null;
    }

    public void clearLoanInstallmentCharges() {
        this.loanInstallmentCharge.clear();
    }

    public void addLoanInstallmentCharges(final Collection<LoanInstallmentCharge> installmentCharges) {
        this.loanInstallmentCharge.addAll(installmentCharges);
    }

    public boolean hasNoLoanInstallmentCharges() {
        return this.loanInstallmentCharge.isEmpty();
    }

    public Set<LoanInstallmentCharge> installmentCharges() {
        return this.loanInstallmentCharge;
    }

    public List<LoanChargePaidDetail> fetchRepaymentInstallment(final MonetaryCurrency currency) {
        List<LoanChargePaidDetail> chargePaidDetails = new ArrayList<>();
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            if (!loanChargePerInstallment.isChargeAmountpaid(currency)
                    && loanChargePerInstallment.getAmountThroughChargePayment(currency).isGreaterThanZero()) {
                LoanChargePaidDetail chargePaidDetail = new LoanChargePaidDetail(
                        loanChargePerInstallment.getAmountThroughChargePayment(currency),
                        loanChargePerInstallment.getRepaymentInstallment(), isFeeCharge());
                chargePaidDetails.add(chargePaidDetail);
            }
        }
        return chargePaidDetails;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active) {
            this.overdueInstallmentCharge = null;
            this.loanTrancheDisbursementCharge = null;
            this.clearLoanInstallmentCharges();
        }
    }

    public BigDecimal amountOrPercentage() {
        return this.amountOrPercentage;
    }

    public BigDecimal chargeAmount() {
        BigDecimal totalChargeAmount = this.amountOutstanding;
        if (this.amountPaid != null) {
            totalChargeAmount = totalChargeAmount.add(this.amountPaid);
        }
        if (this.amountWaived != null) {
            totalChargeAmount = totalChargeAmount.add(this.amountWaived);
        }
        if (this.amountWrittenOff != null) {
            totalChargeAmount = totalChargeAmount.add(this.amountWrittenOff);
        }
        return totalChargeAmount;
    }

    public void updateOverdueInstallmentCharge(LoanOverdueInstallmentCharge overdueInstallmentCharge) {
        this.overdueInstallmentCharge = overdueInstallmentCharge;
    }

    public void updateLoanTrancheDisbursementCharge(final LoanTrancheDisbursementCharge loanTrancheDisbursementCharge) {
        this.loanTrancheDisbursementCharge = loanTrancheDisbursementCharge;
    }

    public void updateWaivedAmount(MonetaryCurrency currency) {
        if (isInstalmentFee()) {
            this.amountWaived = BigDecimal.ZERO;
            for (final LoanInstallmentCharge chargePerInstallment : this.loanInstallmentCharge) {
                final Money amountWaived = chargePerInstallment.updateWaivedAndAmountPaidThroughChargePaymentAmount(currency);
                this.amountWaived = this.amountWaived.add(amountWaived.getAmount());
                this.amountOutstanding = this.amountOutstanding.subtract(amountWaived.getAmount());
                if (determineIfFullyPaid() && Money.of(currency, this.amountWaived).isGreaterThanZero()) {
                    this.paid = false;
                    this.waived = true;
                }
            }
            return;
        }

        Money waivedAmount = Money.of(currency, this.amountWaived);
        if (waivedAmount.isGreaterThanZero()) {
            if (waivedAmount.isGreaterThan(this.getAmount(currency))) {
                this.amountWaived = this.getAmount(currency).getAmount();
                this.amountOutstanding = BigDecimal.ZERO;
                this.paid = false;
                this.waived = true;
            } else if (waivedAmount.isLessThan(this.getAmount(currency))) {
                this.paid = false;
                this.waived = false;
            }
        }

    }

    public LoanOverdueInstallmentCharge getOverdueInstallmentCharge() {
        return this.overdueInstallmentCharge;
    }

    public LoanTrancheDisbursementCharge getTrancheDisbursementCharge() {
        return this.loanTrancheDisbursementCharge;
    }

    public Money undoPaidOrPartiallyAmountBy(final Money incrementBy, final Integer installmentNumber, final Money feeAmount) {
        Money processAmount = Money.zero(incrementBy.getCurrency());
        if (isInstalmentFee()) {
            if (installmentNumber == null) {
                processAmount = getLastPaidOrPartiallyPaidInstallmentLoanCharge(incrementBy.getCurrency()).undoPaidAmountBy(incrementBy,
                        feeAmount);
            } else {
                processAmount = getInstallmentLoanCharge(installmentNumber).undoPaidAmountBy(incrementBy, feeAmount);
            }
        } else {
            processAmount = incrementBy;
        }
        Money amountPaidToDate = Money.of(processAmount.getCurrency(), this.amountPaid);

        Money amountDeductedOnThisCharge = Money.zero(processAmount.getCurrency());
        if (processAmount.isGreaterThanOrEqualTo(amountPaidToDate)) {
            amountDeductedOnThisCharge = amountPaidToDate;
            amountPaidToDate = Money.zero(processAmount.getCurrency());
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = this.amount;
            this.paid = false;

        } else {
            amountDeductedOnThisCharge = processAmount;
            amountPaidToDate = amountPaidToDate.minus(processAmount);
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = calculateAmountOutstanding(incrementBy.getCurrency());
        }
        return amountDeductedOnThisCharge;
    }

    public LoanInstallmentCharge getLastPaidOrPartiallyPaidInstallmentLoanCharge(MonetaryCurrency currency) {
        LoanInstallmentCharge paidChargePerInstallment = null;
        for (final LoanInstallmentCharge loanChargePerInstallment : this.loanInstallmentCharge) {
            Money outstanding = Money.of(currency, loanChargePerInstallment.getAmountOutstanding());
            final boolean partiallyPaid = outstanding.isGreaterThanZero()
                    && outstanding.isLessThan(loanChargePerInstallment.getAmount(currency));
            if ((partiallyPaid || loanChargePerInstallment.isPaid())
                    && (paidChargePerInstallment == null || paidChargePerInstallment.getRepaymentInstallment().getDueDate()
                            .isBefore(loanChargePerInstallment.getRepaymentInstallment().getDueDate()))) {
                paidChargePerInstallment = loanChargePerInstallment;
            }
        }
        return paidChargePerInstallment;
    }

    public Loan getLoan() {
        return this.loan;
    }
    
    public boolean isTrancheDisbursementCharge() {
        return ChargeTimeType.fromInt(this.chargeTime).equals(ChargeTimeType.TRANCHE_DISBURSEMENT);
    }
    
    public boolean isDueDateCharge() {
        return this.dueDate != null;
    }
}