/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import static org.mifosplatform.portfolio.savings.DepositsApiConstants.minDepositTermTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.maxDepositTermTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.inMultiplesOfDepositTermParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.maxDepositTermParamName;
import static org.mifosplatform.portfolio.savings.DepositsApiConstants.minDepositTermParamName;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;

/**
 * DepositTermDetail encapsulates all the details of a
 * {@link FixedDepositProduct} that are also used and persisted by a
 * {@link FixedDepositAccount}.
 */
@Embeddable
public class DepositTermDetail {

    @Column(name = "min_deposit_term", nullable = true)
    private Integer minDepositTerm;

    @Column(name = "max_deposit_term", nullable = true)
    private Integer maxDepositTerm;

    @Column(name = "min_deposit_term_type_enum", nullable = true)
    private Integer minDepositTermType;

    @Column(name = "max_deposit_term_type_enum", nullable = true)
    private Integer maxDepositTermType;

    @Column(name = "in_multiples_of_deposit_term", nullable = true)
    private Integer inMultiplesOfDepositTerm;

    @Column(name = "in_multiples_of_deposit_term_type_enum", nullable = true)
    private Integer inMultiplesOfDepositTermType;

    public static DepositTermDetail createFrom(final Integer minDepositTerm, final Integer maxDepositTerm,
            final SavingsPeriodFrequencyType minDepositTermType, final SavingsPeriodFrequencyType maxDepositTermType,
            final Integer inMultiplesOfDepositTerm, final SavingsPeriodFrequencyType inMultiplesOfDepositTermType) {

        return new DepositTermDetail(minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType, inMultiplesOfDepositTerm,
                inMultiplesOfDepositTermType);
    }

    protected DepositTermDetail() {
        //
    }

    private DepositTermDetail(final Integer minDepositTerm, final Integer maxDepositTerm,
            final SavingsPeriodFrequencyType minDepositTermType, final SavingsPeriodFrequencyType maxDepositTermType,
            final Integer inMultiplesOfDepositTerm, final SavingsPeriodFrequencyType inMultiplesOfDepositTermType) {
        this.minDepositTerm = minDepositTerm;
        this.maxDepositTerm = maxDepositTerm;
        this.minDepositTermType = (minDepositTermType == null) ? null : minDepositTermType.getValue();
        this.maxDepositTermType = (maxDepositTermType == null) ? null : maxDepositTermType.getValue();
        this.inMultiplesOfDepositTerm = inMultiplesOfDepositTerm;
        this.inMultiplesOfDepositTermType = (inMultiplesOfDepositTermType == null) ? null : inMultiplesOfDepositTermType.getValue();
    }

    public Map<String, Object> update(final JsonCommand command, final DataValidatorBuilder baseDataValidator) {
        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);

        if (command.isChangeInIntegerParameterNamed(minDepositTermParamName, this.minDepositTerm)) {
            final Integer newValue = command.integerValueOfParameterNamed(minDepositTermParamName);
            actualChanges.put(minDepositTermParamName, newValue);
            this.minDepositTerm = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(maxDepositTermParamName, this.maxDepositTerm)) {
            final Integer newValue = command.integerValueOfParameterNamed(maxDepositTermParamName);
            actualChanges.put(maxDepositTermParamName, newValue);
            this.maxDepositTerm = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(minDepositTermTypeIdParamName, this.minDepositTermType)) {
            final Integer newValue = command.integerValueOfParameterNamed(minDepositTermTypeIdParamName);
            actualChanges.put(minDepositTermTypeIdParamName, SavingsEnumerations.depositTermFrequencyType(newValue));
            this.minDepositTermType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(maxDepositTermTypeIdParamName, this.maxDepositTermType)) {
            final Integer newValue = command.integerValueOfParameterNamed(maxDepositTermTypeIdParamName);
            actualChanges.put(maxDepositTermTypeIdParamName, SavingsEnumerations.depositTermFrequencyType(newValue));
            this.maxDepositTermType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(inMultiplesOfDepositTermParamName, this.inMultiplesOfDepositTerm)) {
            final Integer newValue = command.integerValueOfParameterNamed(inMultiplesOfDepositTermParamName);
            actualChanges.put(inMultiplesOfDepositTermParamName, newValue);
            this.inMultiplesOfDepositTerm = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(inMultiplesOfDepositTermTypeIdParamName, this.inMultiplesOfDepositTermType)) {
            final Integer newValue = command.integerValueOfParameterNamed(inMultiplesOfDepositTermTypeIdParamName);
            actualChanges.put(inMultiplesOfDepositTermTypeIdParamName, SavingsEnumerations.inMultiplesOfDepositTermFrequencyType(newValue));
            this.inMultiplesOfDepositTermType = newValue;
        }

        if (isMinDepositTermGreaterThanMaxDepositTerm()) {
            baseDataValidator.parameter(minDepositTermParamName).value(this.minDepositTerm)
                    .failWithCode(".greater.than.maxDepositTerm", this.minDepositTerm, this.maxDepositTerm);
        }

        return actualChanges;
    }

    public boolean isMinDepositTermGreaterThanMaxDepositTerm() {
        if (this.minDepositTerm != null && this.maxDepositTerm != null) {
            final Integer minDepositInDays = this.convertToSafeDays(minDepositTerm, SavingsPeriodFrequencyType.fromInt(minDepositTermType));
            final Integer maxDepositInDays = this.convertToSafeDays(maxDepositTerm, SavingsPeriodFrequencyType.fromInt(maxDepositTermType));
            if (minDepositInDays.compareTo(maxDepositInDays) > 0) { return true; }
        }
        return false;
    }

    public Integer minDepositTerm() {
        return this.minDepositTerm;
    }

    public Integer maxDepositTerm() {
        return this.maxDepositTerm;
    }

    public Integer minDepositTermType() {
        return this.minDepositTermType;
    }

    public Integer maxDepositTermType() {
        return this.maxDepositTermType;
    }

    public Integer inMultiplesOfDepositTerm() {
        return this.inMultiplesOfDepositTerm;
    }

    public Integer inMultiplesOfDepositTermType() {
        return this.inMultiplesOfDepositTermType;
    }

    public boolean isDepositBetweenMinAndMax(LocalDate depositStartDate, LocalDate depositEndDate) {
        return isEqualOrGreaterThanMin(depositStartDate, depositEndDate) && isEqualOrLessThanMax(depositStartDate, depositEndDate);
    }

    public boolean isValidInMultiplesOfPeriod(final Integer depositPeriod, final SavingsPeriodFrequencyType depositPeriodFrequencyType) {

        boolean isValidInMultiplesOfPeriod = true;
        final Integer depositPeriodInDays = this.convertToSafeDays(depositPeriod, depositPeriodFrequencyType);
        if (this.inMultiplesOfDepositTerm() != null) {
            final Integer inMultiplesOfInDays = this.convertToSafeDays(this.inMultiplesOfDepositTerm(),
                    SavingsPeriodFrequencyType.fromInt(this.inMultiplesOfDepositTermType()));
            final Integer minDepositInDays = this.convertToSafeDays(minDepositTerm, SavingsPeriodFrequencyType.fromInt(minDepositTermType));
            isValidInMultiplesOfPeriod = ((depositPeriodInDays - minDepositInDays) % inMultiplesOfInDays == 0);
        }

        return isValidInMultiplesOfPeriod;
    }

    private boolean isEqualOrGreaterThanMin(LocalDate depositStartDate, LocalDate depositEndDate) {
        if (minDepositTerm() == null) return true;
        final SavingsPeriodFrequencyType periodFrequencyType = SavingsPeriodFrequencyType.fromInt(this.minDepositTermType());
        final Integer depositPeriod = depositPeriod(depositStartDate, depositEndDate, periodFrequencyType);
        return minDepositTerm() == null || depositPeriod.compareTo(minDepositTerm()) >= 0;
    }

    private boolean isEqualOrLessThanMax(LocalDate depositStartDate, LocalDate depositEndDate) {
        if (maxDepositTerm() == null) return true;
        final SavingsPeriodFrequencyType periodFrequencyType = SavingsPeriodFrequencyType.fromInt(this.maxDepositTermType());
        final Integer depositPeriod = depositPeriod(depositStartDate, depositEndDate, periodFrequencyType);
        return maxDepositTerm() == null || depositPeriod.compareTo(maxDepositTerm()) <= 0;
    }

    public Integer depositPeriod(final LocalDate periodStartDate, final LocalDate periodEndDate,
            final SavingsPeriodFrequencyType periodFrequencyType) {
        Integer actualDepositPeriod = 0;

        switch (periodFrequencyType) {
            case DAYS:
                actualDepositPeriod = Days.daysBetween(periodStartDate, periodEndDate).getDays();
            break;
            case WEEKS:
                actualDepositPeriod = Weeks.weeksBetween(periodStartDate, periodEndDate).getWeeks();
            break;
            case MONTHS:
                actualDepositPeriod = Months.monthsBetween(periodStartDate, periodEndDate).getMonths();
            break;
            case YEARS:
                actualDepositPeriod = Years.yearsBetween(periodStartDate, periodEndDate).getYears();
            break;
            case INVALID:
                actualDepositPeriod = 0;// default value
            break;
        }
        return actualDepositPeriod;
    }

    private Integer convertToSafeDays(final Integer period, final SavingsPeriodFrequencyType periodFrequencyType) {
        Integer toDays = 0;
        switch (periodFrequencyType) {
            case DAYS:
                toDays = period;
            break;
            case WEEKS:
                toDays = period * DateTimeConstants.DAYS_PER_WEEK;
            break;
            case MONTHS:
                toDays = period * 30;// converting to stard 30 days
            break;
            case YEARS:
                toDays = period * 365;
            break;
            case INVALID:
                toDays = 0;// default value
            break;
        }
        return toDays;
    }

    public DepositTermDetail copy() {

        final Integer minDepositTerm = this.minDepositTerm;
        final Integer maxDepositTerm = this.maxDepositTerm;
        final SavingsPeriodFrequencyType minDepositTermType = SavingsPeriodFrequencyType.fromInt(this.minDepositTermType);
        final SavingsPeriodFrequencyType maxDepositTermType = SavingsPeriodFrequencyType.fromInt(this.maxDepositTermType);
        final Integer inMultiplesOfDepositTerm = this.inMultiplesOfDepositTerm;
        final SavingsPeriodFrequencyType inMultiplesOfDepositTermType = SavingsPeriodFrequencyType.fromInt(this
                .inMultiplesOfDepositTermType());

        return DepositTermDetail.createFrom(minDepositTerm, maxDepositTerm, minDepositTermType, maxDepositTermType,
                inMultiplesOfDepositTerm, inMultiplesOfDepositTermType);
    }
}