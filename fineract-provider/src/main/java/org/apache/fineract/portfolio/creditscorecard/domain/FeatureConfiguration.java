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
package org.apache.fineract.portfolio.creditscorecard.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;

@Embeddable
public class FeatureConfiguration implements Serializable {

    @Column(name = "weightage", scale = 6, precision = 5, nullable = false)
    private BigDecimal weightage;

    @Column(name = "green_min", nullable = false)
    private Integer greenMin;

    @Column(name = "green_max", nullable = false)
    private Integer greenMax;

    @Column(name = "amber_min", nullable = false)
    private Integer amberMin;

    @Column(name = "amber_max", nullable = false)
    private Integer amberMax;

    @Column(name = "red_min", nullable = false)
    private Integer redMin;

    @Column(name = "red_max", nullable = false)
    private Integer redMax;

    protected FeatureConfiguration() {
        //
    }

    public FeatureConfiguration(final BigDecimal weightage, final Integer greenMin, final Integer greenMax, final Integer amberMin,
            final Integer amberMax, final Integer redMin, final Integer redMax) {
        this.weightage = weightage;
        this.greenMin = greenMin;
        this.greenMax = greenMax;
        this.amberMin = amberMin;
        this.amberMax = amberMax;
        this.redMin = redMin;
        this.redMax = redMax;
    }

    public static FeatureConfiguration from(final BigDecimal weightage, final Integer greenMin, final Integer greenMax,
            final Integer amberMin, final Integer amberMax, final Integer redMin, final Integer redMax) {
        return new FeatureConfiguration(weightage, greenMin, greenMax, amberMin, amberMax, redMin, redMax);
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator,
            final Locale locale) {

        validateConfiguration(baseDataValidator);

        if (command.isChangeInBigDecimalParameterNamed("weightage", this.weightage, locale)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed("weightage", locale);
            actualChanges.put("weightage", newValue);
            this.weightage = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("greenMin", this.greenMin, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed("greenMin", locale);
            actualChanges.put("greenMin", newValue);
            this.greenMin = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("greenMax", this.greenMax, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed("greenMax", locale);
            actualChanges.put("greenMax", newValue);
            this.greenMax = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("amberMin", this.amberMin, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed("amberMin", locale);
            actualChanges.put("amberMin", newValue);
            this.amberMin = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("amberMax", this.amberMax, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed("amberMax", locale);
            actualChanges.put("amberMax", newValue);
            this.amberMax = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("redMin", this.greenMin, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed("redMin", locale);
            actualChanges.put("redMin", newValue);
            this.redMin = newValue;
        }

        if (command.isChangeInIntegerParameterNamed("redMax", this.greenMin, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed("redMax", locale);
            actualChanges.put("redMax", newValue);
            this.redMax = newValue;
        }

    }

    public void validateConfiguration(final DataValidatorBuilder baseDataValidator) {

        baseDataValidator.reset().parameter("weightage").value(this.weightage).notNull().inMinMaxRange(0, 1);

        baseDataValidator.reset().parameter("greenMin").value(this.greenMin).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("greenMax").value(this.greenMax).notNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("amberMin").value(this.amberMin).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("amberMax").value(this.amberMax).notNull().integerGreaterThanZero();

        baseDataValidator.reset().parameter("redMin").value(this.redMin).notNull().integerGreaterThanZero();
        baseDataValidator.reset().parameter("redMax").value(this.redMax).notNull().integerGreaterThanZero();

    }

    public String getColorFromScore(final BigDecimal score) {
        String color;
        if (score.longValue() >= this.greenMin.longValue() && score.longValue() <= this.greenMax.longValue()) {
            color = "green";

        } else if (score.longValue() >= this.amberMin.longValue() && score.longValue() <= this.amberMax.longValue()) {
            color = "amber";

        } else if (score.longValue() >= this.redMin.longValue() && score.longValue() <= this.redMax.longValue()) {
            color = "red";

        } else {
            color = "orange";

        }

        return color;
    }

    public BigDecimal getWeightage() {
        return weightage;
    }

    public Integer getGreenMin() {
        return greenMin;
    }

    public Integer getGreenMax() {
        return greenMax;
    }

    public Integer getAmberMin() {
        return amberMin;
    }

    public Integer getAmberMax() {
        return amberMax;
    }

    public Integer getRedMin() {
        return redMin;
    }

    public Integer getRedMax() {
        return redMax;
    }
}
