/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.charge.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.exception.ChargeDueAtDisbursementCannotBePenaltyException;
import org.mifosplatform.portfolio.charge.service.ChargeEnumerations;
import org.mifosplatform.portfolio.loanaccount.domain.LoanCharge;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_charge", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name") })
public class Charge extends AbstractPersistable<Long> {

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "charge_applies_to_enum", nullable = false)
    private Integer chargeAppliesTo;

    @Column(name = "charge_time_enum", nullable = false)
    private Integer chargeTime;

    @Column(name = "charge_calculation_enum")
    private Integer chargeCalculation;

    @Column(name = "is_penalty", nullable = false)
    private boolean penalty;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    public static Charge fromJson(final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed("name");
        final BigDecimal amount = command.bigDecimalValueOfParameterNamed("amount");
        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");

        final ChargeAppliesTo chargeAppliesTo = ChargeAppliesTo.fromInt(command.integerValueOfParameterNamed("chargeAppliesTo"));
        final ChargeTimeType chargeTimeType = ChargeTimeType.fromInt(command.integerValueOfParameterNamed("chargeTimeType"));
        final ChargeCalculationType chargeCalculationType = ChargeCalculationType.fromInt(command
                .integerValueOfParameterNamed("chargeCalculationType"));

        final boolean penalty = command.booleanPrimitiveValueOfParameterNamed("penalty");
        final boolean active = command.booleanPrimitiveValueOfParameterNamed("active");

        return new Charge(name, amount, currencyCode, chargeAppliesTo, chargeTimeType, chargeCalculationType, penalty, active);
    }

    protected Charge() {
        //
    }

    private Charge(final String name, final BigDecimal amount, final String currencyCode, final ChargeAppliesTo chargeAppliesTo,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculationType, final boolean penalty, final boolean active) {
        this.name = name;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.chargeAppliesTo = chargeAppliesTo.getValue();
        this.chargeTime = chargeTime.getValue();
        this.chargeCalculation = chargeCalculationType.getValue();
        this.penalty = penalty;
        this.active = active;
        if (penalty && chargeTime.isTimeOfDisbursement()) { throw new ChargeDueAtDisbursementCannotBePenaltyException(name); }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        LoanCharge rhs = (LoanCharge) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)) //
                .append(this.getId(), rhs.getId()) //
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 5) //
                .append(this.getId()) //
                .toHashCode();
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Integer getChargeTime() {
        return chargeTime;
    }

    public Integer getChargeCalculation() {
        return chargeCalculation;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPenalty() {
        return penalty;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(7);

        final String localeAsInput = command.locale();

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }

        final String currencyCodeParamName = "currencyCode";
        if (command.isChangeInStringParameterNamed(currencyCodeParamName, this.currencyCode)) {
            final String newValue = command.stringValueOfParameterNamed(currencyCodeParamName);
            actualChanges.put(currencyCodeParamName, newValue);
            this.currencyCode = newValue;
        }

        final String amountParamName = "amount";
        if (command.isChangeInBigDecimalParameterNamed(amountParamName, this.amount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountParamName);
            actualChanges.put(amountParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.amount = newValue;
        }

        final String chargeTimeParamName = "chargeTimeType";
        if (command.isChangeInIntegerParameterNamed(chargeTimeParamName, this.chargeTime)) {
            final Integer newValue = command.integerValueOfParameterNamed(chargeTimeParamName);
            actualChanges.put(chargeTimeParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.chargeTime = ChargeTimeType.fromInt(newValue).getValue();
        }

        final String chargeAppliesToParamName = "chargeAppliesTo";
        if (command.isChangeInIntegerParameterNamed(chargeAppliesToParamName, this.chargeAppliesTo)) {
            final Integer newValue = command.integerValueOfParameterNamed(chargeAppliesToParamName);
            actualChanges.put(chargeAppliesToParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.chargeAppliesTo = ChargeAppliesTo.fromInt(newValue).getValue();
        }

        final String chargeCalculationParamName = "chargeCalculationType";
        if (command.isChangeInIntegerParameterNamed(chargeCalculationParamName, this.chargeCalculation)) {
            final Integer newValue = command.integerValueOfParameterNamed(chargeCalculationParamName);
            actualChanges.put(chargeCalculationParamName, newValue);
            actualChanges.put("locale", localeAsInput);
            this.chargeCalculation = ChargeCalculationType.fromInt(newValue).getValue();
        }

        final String penaltyParamName = "penalty";
        if (command.isChangeInBooleanParameterNamed(penaltyParamName, this.penalty)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(penaltyParamName);
            actualChanges.put(penaltyParamName, newValue);
            this.penalty = newValue;
        }

        final String activeParamName = "active";
        if (command.isChangeInBooleanParameterNamed(activeParamName, this.active)) {
            final boolean newValue = command.booleanPrimitiveValueOfParameterNamed(activeParamName);
            actualChanges.put(activeParamName, newValue);
            this.active = newValue;
        }

        if (this.penalty && ChargeTimeType.fromInt(this.chargeTime).isTimeOfDisbursement()) { throw new ChargeDueAtDisbursementCannotBePenaltyException(
                name); }

        return actualChanges;
    }

    /**
     * Delete is a <i>soft delete</i>. Updates flag on charge so it wont appear
     * in query/report results.
     * 
     * Any fields with unique constraints and prepended with id of record.
     */
    public void delete() {
        this.deleted = true;
        this.name = this.getId() + "_" + this.name;
    }

    public ChargeData toData() {

        EnumOptionData chargeTimeType = ChargeEnumerations.chargeTimeType(this.chargeTime);
        EnumOptionData chargeAppliesTo = ChargeEnumerations.chargeAppliesTo(this.chargeAppliesTo);
        EnumOptionData chargeCalculationType = ChargeEnumerations.chargeCalculationType(this.chargeCalculation);

        CurrencyData currency = new CurrencyData(this.currencyCode, null, 0, 0, null, null);
        return ChargeData.instance(this.getId(), this.name, this.amount, currency, chargeTimeType, chargeAppliesTo, chargeCalculationType,
                penalty, active);
    }
}