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

package org.apache.fineract.portfolio.charge.domain;

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.annualInterestRateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.fromPeriodParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.periodTypeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.toPeriodParamName;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_fee_charge_slab")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ChargeSlab extends AbstractPersistableCustom {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_id")
    private Charge charge;

    @Column(name = "period_type_enum")
    private Integer periodType;

    @Column(name = "from_period")
    private Integer fromPeriod;

    @Column(name = "to_period")
    private Integer toPeriod;

    @Column(name = "value")
    private BigDecimal value;

    public static List<ChargeSlab> assembleFrom(JsonCommand command, Charge charge) {

        final List chartList = new ArrayList<>();

        JsonArray array = command.jsonElement("chart").getAsJsonObject().getAsJsonArray("chartSlabs");
        for (JsonElement jsonElement : array) {
            JsonObject obj = jsonElement.getAsJsonObject();

            Integer fromPeriod = null;
            Integer toPeriod = null;

            if (obj.has("fromPeriod")) {
                fromPeriod = obj.get("fromPeriod").getAsInt();
            }

            if (obj.has("toPeriod")) {
                toPeriod = obj.get("toPeriod").getAsInt();
            }

            final BigDecimal value = obj.get("value").getAsBigDecimal();
            chartList.add(new ChargeSlab(charge, null, fromPeriod, toPeriod, value));
        }

        return chartList;
    }

    public static ChargeSlab assembleFrom(JsonCommand command, Charge charge, Locale locale) {

        Integer fromPeriod = null;
        Integer toPeriod = null;

        if (command.parameterExists("fromPeriod")) {
            fromPeriod = command.integerValueOfParameterNamed("fromPeriod", locale);
        }

        if (command.parameterExists("toPeriod")) {
            toPeriod = command.integerValueOfParameterNamed("toPeriod", locale);
        }

        final BigDecimal value = command.bigDecimalValueOfParameterNamed("value", locale);
        return new ChargeSlab(charge, null, fromPeriod, toPeriod, value);

    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator,
            final Locale locale) {

        if (command.isChangeInIntegerParameterNamed(periodTypeParamName, this.periodType, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(periodTypeParamName, locale);
            actualChanges.put(periodTypeParamName, newValue);
            this.periodType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(fromPeriodParamName, this.fromPeriod, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(fromPeriodParamName, locale);
            actualChanges.put(fromPeriodParamName, newValue);
            this.fromPeriod = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(toPeriodParamName, this.toPeriod, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(toPeriodParamName, locale);
            actualChanges.put(toPeriodParamName, newValue);
            this.toPeriod = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed("value", this.value, locale)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(annualInterestRateParamName, locale);
            actualChanges.put("value", newValue);
            this.value = newValue;
        }

        validateChargeSlabPlatformRules(command, baseDataValidator, locale);
    }

    public void validateChargeSlabPlatformRules(final JsonCommand chartSlabsCommand, final DataValidatorBuilder baseDataValidator,
            Locale locale) {
        if (isFromPeriodGreaterThanToPeriod()) {
            final Integer fromPeriod = chartSlabsCommand.integerValueOfParameterNamed(fromPeriodParamName, locale);
            baseDataValidator.parameter(fromPeriodParamName).value(fromPeriod).failWithCode("from.period.is.greater.than.to.period");
        }
    }

    public boolean isFromPeriodGreaterThanToPeriod() {
        boolean isGreater = false;
        if (this.toPeriod != null && this.fromPeriod.compareTo(this.toPeriod) > 0) {
            isGreater = true;
        }
        return isGreater;
    }

    public boolean isValueSame(final ChargeSlab that) {
        return isBigDecimalSame(this.value, that.value);
    }

    public boolean isPeriodsSame(final ChargeSlab that) {
        return isIntegerSame(this.fromPeriod, that.fromPeriod) && isIntegerSame(this.toPeriod, that.toPeriod);
    }

    public boolean isIntegerSame(final Integer obj1, final Integer obj2) {
        if (obj1 == null || obj2 == null) {
            if (Objects.equals(obj1, obj2)) {
                return true;
            }
            return false;
        }
        return obj1.equals(obj2);
    }

    public boolean isBigDecimalSame(final BigDecimal obj1, final BigDecimal obj2) {
        if (obj1 == null || obj2 == null) {
            if (Objects.compare(obj1, obj2, Comparator.nullsFirst(Comparator.naturalOrder())) == 0 ? Boolean.TRUE : Boolean.FALSE) {
                return true;
            }
            return false;
        }
        return obj1.compareTo(obj2) == 0;
    }

    public boolean isValidChart() {
        return this.fromPeriod != null;
    }

    public boolean isRateChartOverlapping(final ChargeSlab that) {
        boolean isPeriodOverLapping = isPeriodOverlapping(that);
        boolean isPeriodSame = isPeriodsSame(that);
        return (isPeriodOverLapping && !isPeriodSame);

    }

    private boolean isPeriodOverlapping(final ChargeSlab that) {
        if (isIntegerSame(that.toPeriod, this.toPeriod)) {
            return true;
        } else if (isIntegerSame(that.fromPeriod, this.fromPeriod)) {
            return true;
        } else if (this.toPeriod == null) {
            return true;
        } else if (that.toPeriod == null) {
            return that.fromPeriod <= this.toPeriod;
        }
        return this.fromPeriod <= that.toPeriod && that.fromPeriod <= this.toPeriod;
    }

    public boolean isRateChartHasGap(final ChargeSlab that) {
        if (isPeriodsSame(that)) {
            return false;
        } else {
            return isNotProperPeriodStart(that.fromPeriod);
        }
    }

    private boolean isNotProperPeriodStart(final Integer period) {
        return this.toPeriod == null || (period != null && period.compareTo(this.toPeriod + 1) != 0);
    }

    public boolean isNotProperPriodEnd() {
        return !(this.toPeriod == null);

    }

}
