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

public class LoanTermVariationsData {

    @SuppressWarnings("unused")
    private final Long id;
    private final EnumOptionData termType;
    private final LocalDate termVariationApplicableFrom;
    private final Integer termVariationApplicableFromInstallment;
    private final BigDecimal termValue;

    public LoanTermVariationsData(final Long id, final EnumOptionData termType, final LocalDate termVariationApplicableFrom,
            final BigDecimal termValue) {
        this.id = id;
        this.termType = termType;
        this.termVariationApplicableFrom = termVariationApplicableFrom;
        this.termValue = termValue;
        this.termVariationApplicableFromInstallment = null;
    }
    
    public LoanTermVariationsData(final EnumOptionData termType, final LocalDate termVariationApplicableFrom,
            final BigDecimal termValue) {
        this.id = null;
        this.termType = termType;
        this.termVariationApplicableFrom = termVariationApplicableFrom;
        this.termValue = termValue;
        this.termVariationApplicableFromInstallment = null;
    }

    public EnumOptionData getTermType() {
        return this.termType;
    }
    
    public LoanTermVariationType getTermVariationType(){
        return LoanTermVariationType.fromInt(this.termType.getId().intValue());
    }

    public LocalDate getTermApplicableFrom() {
        return this.termVariationApplicableFrom;
    }

    public BigDecimal getTermValue() {
        return this.termValue;
    }

    public boolean isApplicable(final LocalDate fromDate, final LocalDate dueDate, int installmentNumber) {
        return occursOnDayFromAndUpTo(fromDate, dueDate, this.termVariationApplicableFrom)
                || isIntegerEquals(installmentNumber, this.termVariationApplicableFromInstallment);
    }

    private boolean occursOnDayFromAndUpTo(final LocalDate fromInclusive, final LocalDate upToNotInclusive, final LocalDate target) {
        return target != null && !target.isBefore(fromInclusive) && target.isBefore(upToNotInclusive);
    }

    private boolean isIntegerEquals(final Integer installmentNumber, final Integer target) {
        return target != null && target.equals(installmentNumber);
    }

    public boolean isApplicable(final LocalDate fromDate, int installmentNumber) {
        return occursBefore(fromDate, this.termVariationApplicableFrom)
                || isIntegerGreterThan(installmentNumber, this.termVariationApplicableFromInstallment);
    }

    private boolean occursBefore(final LocalDate date, final LocalDate target) {
        return target != null && target.isBefore(date);
    }

    private boolean isIntegerGreterThan(final Integer installmentNumber, final Integer target) {
        return target != null && target < installmentNumber;
    }
}
