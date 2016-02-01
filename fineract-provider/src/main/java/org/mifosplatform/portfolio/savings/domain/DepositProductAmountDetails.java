/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.mifosplatform.infrastructure.core.api.JsonCommand;

/**
 * RecurringDepositProductAmountDetails encapsulates all recurring Deposit
 * Amount of a {@link RecurringDepositProduct}.
 */
@Embeddable
public class DepositProductAmountDetails {

    @Column(name = "min_deposit_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal minDepositAmount;

    @Column(name = "max_deposit_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal maxDepositAmount;

    @Column(name = "deposit_amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal depositAmount;

    public static DepositProductAmountDetails createFrom(final BigDecimal minDepositAmount, final BigDecimal depositAmount,
            final BigDecimal maxDepositAmount) {

        return new DepositProductAmountDetails(minDepositAmount, depositAmount, maxDepositAmount);
    }

    protected DepositProductAmountDetails() {
        //
    }

    public DepositProductAmountDetails(final BigDecimal minDepositAmount, final BigDecimal depositAmount, final BigDecimal maxDepositAmount) {
        this.minDepositAmount = minDepositAmount;
        this.depositAmount = depositAmount;
        this.maxDepositAmount = maxDepositAmount;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(20);

        final String localeAsInput = command.locale();

        final String minDepositAmountParamName = "minDepositAmount";
        if (command.isChangeInBigDecimalParameterNamedWithNullCheck(minDepositAmountParamName, this.minDepositAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(minDepositAmountParamName);
            actualChanges.put(minDepositAmountParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.minDepositAmount = newValue;
        }

        final String maxDepositAmountParamName = "maxDepositAmount";
        if (command.isChangeInBigDecimalParameterNamedWithNullCheck(maxDepositAmountParamName, this.maxDepositAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(maxDepositAmountParamName);
            actualChanges.put(maxDepositAmountParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.maxDepositAmount = newValue;
        }

        final String depositAmountParamName = "depositAmount";
        if (command.isChangeInBigDecimalParameterNamedWithNullCheck(depositAmountParamName, this.depositAmount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(depositAmountParamName);
            actualChanges.put(depositAmountParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.depositAmount = newValue;
        }

        return actualChanges;
    }

    public BigDecimal getMinDepositAmount() {
        return this.minDepositAmount;
    }

    public BigDecimal getMaxDepositAmount() {
        return this.maxDepositAmount;
    }

    public BigDecimal getDepositAmount() {
        return this.depositAmount;
    }

}