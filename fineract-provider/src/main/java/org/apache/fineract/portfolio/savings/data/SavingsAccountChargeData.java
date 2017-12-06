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
package org.apache.fineract.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;

/**
 * Immutable data object for Savings Account charge data.
 */
@SuppressWarnings("unused")
public class SavingsAccountChargeData {

    private final Long id;

    private final Long chargeId;

    private final Long accountId;

    private final String name;

    private final EnumOptionData chargeTimeType;

    private final LocalDate dueDate;

    private final MonthDay feeOnMonthDay;

    private final Integer feeInterval;

    private final EnumOptionData chargeCalculationType;

    private final BigDecimal percentage;

    private final BigDecimal amountPercentageAppliedTo;

    private final CurrencyData currency;

    private final BigDecimal amount;

    private final BigDecimal amountPaid;

    private final BigDecimal amountWaived;

    private final BigDecimal amountWrittenOff;

    private final BigDecimal amountOutstanding;

    private final BigDecimal amountOrPercentage;

    private final boolean penalty;

    private final Boolean isActive;

    private final LocalDate inactivationDate;

    private final Collection<ChargeData> chargeOptions;

    public SavingsAccountChargeData(Long chargeId,  BigDecimal amount,LocalDate dueDate) {
        this.chargeId = chargeId;
        this.amount = amount;
        this.dueDate = dueDate;
        this.id=null;
        this.accountId = null;
        this.name = null;
        this.chargeTimeType = null;
        this.feeOnMonthDay = null;
        this.feeInterval = null;
        this.chargeCalculationType = null;
        this.percentage = null;
        this.amountPercentageAppliedTo = null;
        this.currency = null;
        this.amountPaid = null;
        this.amountWaived = null;
        this.amountWrittenOff = null;
        this.amountOutstanding = null;
        this.amountOrPercentage = null;
        this.penalty = false;
        this.isActive = null;
        this.inactivationDate = null;
        this.chargeOptions = null;
    }

    public static SavingsAccountChargeData template(final Collection<ChargeData> chargeOptions) {
        final Long id = null;
        final Long chargeId = null;
        final Long accountId = null;
        final String name = null;
        final CurrencyData currency = null;
        final BigDecimal amount = BigDecimal.ZERO;
        final BigDecimal amountPaid = BigDecimal.ZERO;
        final BigDecimal amountWaived = BigDecimal.ZERO;
        final BigDecimal amountWrittenOff = BigDecimal.ZERO;
        final BigDecimal amountOutstanding = BigDecimal.ZERO;
        final BigDecimal amountPercentageAppliedTo = BigDecimal.ZERO;
        final EnumOptionData chargeTimeType = null;
        final EnumOptionData chargeCalculationType = null;
        final BigDecimal percentage = BigDecimal.ZERO;
        final boolean penalty = false;
        final LocalDate dueAsOfDate = null;
        final MonthDay feeOnMonthDay = null;
        final Integer feeInterval = null;
        final Boolean isActive = null;
        final LocalDate inactivationDate = null;

        return new SavingsAccountChargeData(id, chargeId, accountId, name, chargeTimeType, dueAsOfDate, chargeCalculationType, percentage,
                amountPercentageAppliedTo, currency, amount, amountPaid, amountWaived, amountWrittenOff, amountOutstanding, chargeOptions,
                penalty, feeOnMonthDay, feeInterval, isActive, inactivationDate);
    }

    public static SavingsAccountChargeData instance(final Long id, final Long chargeId, final Long accountId, final String name,
            final CurrencyData currency, final BigDecimal amount, final BigDecimal amountPaid, final BigDecimal amountWaived,
            final BigDecimal amountWrittenOff, final BigDecimal amountOutstanding, final EnumOptionData chargeTimeType,
            final LocalDate dueAsOfDate, final EnumOptionData chargeCalculationType, final BigDecimal percentage,
            final BigDecimal amountPercentageAppliedTo, final Collection<ChargeData> chargeOptions, final boolean penalty,
            final MonthDay feeOnMonthDay, final Integer feeInterval, final Boolean isActive, final LocalDate inactivationDate) {

        return new SavingsAccountChargeData(id, chargeId, accountId, name, chargeTimeType, dueAsOfDate, chargeCalculationType, percentage,
                amountPercentageAppliedTo, currency, amount, amountPaid, amountWaived, amountWrittenOff, amountOutstanding, chargeOptions,
                penalty, feeOnMonthDay, feeInterval, isActive, inactivationDate);
    }

    private SavingsAccountChargeData(final Long id, final Long chargeId, final Long accountId, final String name,
            final EnumOptionData chargeTimeType, final LocalDate dueAsOfDate, final EnumOptionData chargeCalculationType,
            final BigDecimal percentage, final BigDecimal amountPercentageAppliedTo, final CurrencyData currency, final BigDecimal amount,
            final BigDecimal amountPaid, final BigDecimal amountWaived, final BigDecimal amountWrittenOff,
            final BigDecimal amountOutstanding, final Collection<ChargeData> chargeOptions, final boolean penalty,
            final MonthDay feeOnMonthDay, final Integer feeInterval, final Boolean isActive, final LocalDate inactivationDate) {
        this.id = id;
        this.chargeId = chargeId;
        this.accountId = accountId;
        this.name = name;
        this.chargeTimeType = chargeTimeType;
        this.dueDate = dueAsOfDate;
        this.chargeCalculationType = chargeCalculationType;
        this.percentage = percentage;
        this.amountPercentageAppliedTo = amountPercentageAppliedTo;
        this.currency = currency;
        this.amount = amount;
        this.amountPaid = amountPaid;
        this.amountWaived = amountWaived;
        this.amountWrittenOff = amountWrittenOff;
        this.amountOutstanding = amountOutstanding;
        this.amountOrPercentage = getAmountOrPercentage();
        this.chargeOptions = chargeOptions;
        this.penalty = penalty;
        this.feeOnMonthDay = feeOnMonthDay;
        this.feeInterval = feeInterval;
        this.isActive = isActive;
        this.inactivationDate = inactivationDate;
    }

    private BigDecimal getAmountOrPercentage() {
        return (this.chargeCalculationType != null) && (this.chargeCalculationType.getId().intValue() > 1) ? this.percentage : this.amount;
    }

    public boolean isWithdrawalFee() {
        return ChargeTimeType.fromInt(this.chargeTimeType.getId().intValue()).isWithdrawalFee();
    }

    public boolean isAnnualFee() {
        return ChargeTimeType.fromInt(this.chargeTimeType.getId().intValue()).isAnnualFee();
    }
    
    public boolean isSavingsActivation() {
        return ChargeTimeType.fromInt(this.chargeTimeType.getId().intValue()).isSavingsActivation();
    }

    public BigDecimal getAmountOutstanding() {
        return this.amountOutstanding;
    } 
    
    
}