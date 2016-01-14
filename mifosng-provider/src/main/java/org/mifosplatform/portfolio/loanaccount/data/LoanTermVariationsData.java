/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.loanaccount.domain.LoanTermVariationType;

public class LoanTermVariationsData implements Comparable<LoanTermVariationsData> {

    @SuppressWarnings("unused")
    private final Long id;
    private final EnumOptionData termType;
    private final LocalDate termVariationApplicableFrom;
    private final BigDecimal decimalValue;
    private final LocalDate dateValue;
    private final boolean isSpecificToInstallment;
    private Boolean isProcessed;

    public LoanTermVariationsData(final Long id, final EnumOptionData termType, final LocalDate termVariationApplicableFrom,
            final BigDecimal decimalValue, final LocalDate dateValue, final boolean isSpecificToInstallment) {
        this.id = id;
        this.termType = termType;
        this.termVariationApplicableFrom = termVariationApplicableFrom;
        this.decimalValue = decimalValue;
        this.dateValue = dateValue;
        this.isSpecificToInstallment = isSpecificToInstallment;
    }

    public LoanTermVariationsData(final EnumOptionData termType, final LocalDate termVariationApplicableFrom,
            final BigDecimal decimalValue, LocalDate dateValue, final boolean isSpecificToInstallment) {
        this.id = null;
        this.termType = termType;
        this.termVariationApplicableFrom = termVariationApplicableFrom;
        this.decimalValue = decimalValue;
        this.dateValue = dateValue;
        this.isSpecificToInstallment = isSpecificToInstallment;
    }

    public EnumOptionData getTermType() {
        return this.termType;
    }

    public LoanTermVariationType getTermVariationType() {
        return LoanTermVariationType.fromInt(this.termType.getId().intValue());
    }

    public LocalDate getTermApplicableFrom() {
        return this.termVariationApplicableFrom;
    }

    public BigDecimal getDecimalValue() {
        return this.decimalValue;
    }

    public boolean isApplicable(final LocalDate fromDate, final LocalDate dueDate) {
        return occursOnDayFromAndUpTo(fromDate, dueDate, this.termVariationApplicableFrom);
    }

    private boolean occursOnDayFromAndUpTo(final LocalDate fromNotInclusive, final LocalDate upToInclusive, final LocalDate target) {
        return target != null && target.isAfter(fromNotInclusive) && !target.isAfter(upToInclusive);
    }

    public boolean isApplicable(final LocalDate fromDate) {
        return occursBefore(fromDate, this.termVariationApplicableFrom);
    }

    private boolean occursBefore(final LocalDate date, final LocalDate target) {
        return target != null && target.isBefore(date);
    }

    public LocalDate getDateValue() {
        return this.dateValue;
    }

    public boolean isSpecificToInstallment() {
        return this.isSpecificToInstallment;
    }

    public Boolean isProcessed() {
        return this.isProcessed == null ? false : this.isProcessed;
    }

    public void setProcessed(Boolean isProcessed) {
        this.isProcessed = isProcessed;
    }

    @Override
    public int compareTo(LoanTermVariationsData o) {
        int comparsion = getTermApplicableFrom().compareTo(o.getTermApplicableFrom());
        if (comparsion == 0) {
            if (o.getTermVariationType().isDueDateVariation() || o.getTermVariationType().isInsertInstallment()) {
                comparsion = 1;
            }
        }
        return comparsion;
    }

}
