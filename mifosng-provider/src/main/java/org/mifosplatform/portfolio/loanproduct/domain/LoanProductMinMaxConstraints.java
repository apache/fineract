/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.mifosplatform.infrastructure.core.api.JsonCommand;

/**
 * LoanProductMinMaxConstraints encapsulates all the Min and Max details of a
 * {@link LoanProduct}.
 */
@Embeddable
public class LoanProductMinMaxConstraints {

    @Column(name = "min_principal_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal minPrincipal;

    @Column(name = "max_principal_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal maxPrincipal;

    @Column(name = "min_nominal_interest_rate_per_period", scale = 6, precision = 19, nullable = true)
    private BigDecimal minNominalInterestRatePerPeriod;

    @Column(name = "max_nominal_interest_rate_per_period", scale = 6, precision = 19, nullable = true)
    private BigDecimal maxNominalInterestRatePerPeriod;

    @Column(name = "min_number_of_repayments", nullable = true)
    private Integer minNumberOfRepayments;

    @Column(name = "max_number_of_repayments", nullable = true)
    private Integer maxNumberOfRepayments;

    public static LoanProductMinMaxConstraints createFrom(final BigDecimal minPrincipal, final BigDecimal maxPrincipal,
            final BigDecimal minNominalInterestRatePerPeriod, final BigDecimal maxNominalInterestRatePerPeriod,
            final Integer minNumberOfRepayments, final Integer maxNumberOfRepayments) {

        return new LoanProductMinMaxConstraints(minPrincipal, maxPrincipal, minNominalInterestRatePerPeriod,
                maxNominalInterestRatePerPeriod, minNumberOfRepayments, maxNumberOfRepayments);
    }

    protected LoanProductMinMaxConstraints() {
        //
    }

    public LoanProductMinMaxConstraints(final BigDecimal defaultMinPrincipal, final BigDecimal defaultMaxPrincipal,
            final BigDecimal defaultMinNominalInterestRatePerPeriod, final BigDecimal defaultMaxNominalInterestRatePerPeriod,
            final Integer defaultMinNumberOfRepayments, final Integer defaultMaxNumberOfRepayments) {
        this.minPrincipal = defaultMinPrincipal;
        this.maxPrincipal = defaultMaxPrincipal;
        this.minNominalInterestRatePerPeriod = defaultMinNominalInterestRatePerPeriod;
        this.maxNominalInterestRatePerPeriod = defaultMaxNominalInterestRatePerPeriod;
        this.minNumberOfRepayments = defaultMinNumberOfRepayments;
        this.maxNumberOfRepayments = defaultMaxNumberOfRepayments;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);

        final String localeAsInput = command.locale();

        final String minPrincipalParamName = "minPrincipal";
        if (command.isChangeInBigDecimalParameterNamedWithNullCheck(minPrincipalParamName, this.minPrincipal)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minPrincipalParamName);
            actualChanges.put(minPrincipalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minPrincipal = newValue;
        }

        final String maxPrincipalParamName = "maxPrincipal";
        if (command.isChangeInBigDecimalParameterNamedWithNullCheck(maxPrincipalParamName, this.maxPrincipal)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maxPrincipalParamName);
            actualChanges.put(maxPrincipalParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.maxPrincipal = newValue;
        }

        final String minNumberOfRepaymentsParamName = "minNumberOfRepayments";
        if (command.isChangeInIntegerParameterNamed(minNumberOfRepaymentsParamName, this.minNumberOfRepayments)) {
            final Integer newValue = command.integerValueOfParameterNamed(minNumberOfRepaymentsParamName);
            actualChanges.put(minNumberOfRepaymentsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minNumberOfRepayments = newValue;
        }

        final String maxNumberOfRepaymentsParamName = "maxNumberOfRepayments";
        if (command.isChangeInIntegerParameterNamed(maxNumberOfRepaymentsParamName, this.maxNumberOfRepayments)) {
            final Integer newValue = command.integerValueOfParameterNamed(maxNumberOfRepaymentsParamName);
            actualChanges.put(maxNumberOfRepaymentsParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.maxNumberOfRepayments = newValue;
        }

        final String minInterestRatePerPeriodParamName = "minInterestRatePerPeriod";
        if (command
                .isChangeInBigDecimalParameterNamedWithNullCheck(minInterestRatePerPeriodParamName, this.minNominalInterestRatePerPeriod)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minInterestRatePerPeriodParamName);
            actualChanges.put(minInterestRatePerPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minNominalInterestRatePerPeriod = newValue;
        }

        final String maxInterestRatePerPeriodParamName = "maxInterestRatePerPeriod";
        if (command
                .isChangeInBigDecimalParameterNamedWithNullCheck(maxInterestRatePerPeriodParamName, this.maxNominalInterestRatePerPeriod)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maxInterestRatePerPeriodParamName);
            actualChanges.put(maxInterestRatePerPeriodParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.maxNominalInterestRatePerPeriod = newValue;
        }

        return actualChanges;
    }

    public BigDecimal getMinPrincipal() {
        return this.minPrincipal;
    }

    public BigDecimal getMaxPrincipal() {
        return this.maxPrincipal;
    }

    public BigDecimal getMinNominalInterestRatePerPeriod() {
        return this.minNominalInterestRatePerPeriod == null ? null : BigDecimal.valueOf(Double.valueOf(this.minNominalInterestRatePerPeriod
                .stripTrailingZeros().toString()));
    }

    public BigDecimal getMaxNominalInterestRatePerPeriod() {
        return this.maxNominalInterestRatePerPeriod == null ? null : BigDecimal.valueOf(Double.valueOf(this.maxNominalInterestRatePerPeriod
                .stripTrailingZeros().toString()));
    }

    public Integer getMinNumberOfRepayments() {
        return this.minNumberOfRepayments;
    }

    public Integer getMaxNumberOfRepayments() {
        return this.maxNumberOfRepayments;
    }

}