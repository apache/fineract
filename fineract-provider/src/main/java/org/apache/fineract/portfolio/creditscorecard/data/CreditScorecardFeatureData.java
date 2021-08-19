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
package org.apache.fineract.portfolio.creditscorecard.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.jetbrains.annotations.NotNull;

public final class CreditScorecardFeatureData implements Comparable<CreditScorecardFeatureData>, Serializable {

    private final Long id;
    private final Long featureId;
    private final String name;
    private final EnumOptionData valueType;
    private final EnumOptionData dataType;
    private final EnumOptionData category;
    private final Boolean active;

    private final BigDecimal weightage;
    private final Integer greenMin;
    private final Integer greenMax;
    private final Integer amberMin;
    private final Integer amberMax;
    private final Integer redMin;
    private final Integer redMax;

    private final Collection<ScorecardFeatureCriteriaData> criteria;

    private final Collection<EnumOptionData> valueTypeOptions;
    private final Collection<EnumOptionData> dataTypeOptions;
    private final Collection<EnumOptionData> categoryOptions;

    public CreditScorecardFeatureData(final Long id, final Long featureId, final String name, final EnumOptionData valueType,
            final EnumOptionData dataType, final EnumOptionData category, final Boolean active, final BigDecimal weightage,
            final Integer greenMin, final Integer greenMax, final Integer amberMin, final Integer amberMax, final Integer redMin,
            final Integer redMax, final Collection<ScorecardFeatureCriteriaData> criteria,
            final Collection<EnumOptionData> valueTypeOptions, final Collection<EnumOptionData> dataTypeOptions,
            final Collection<EnumOptionData> categoryOptions) {
        this.id = id;

        this.featureId = featureId;
        this.name = name;
        this.valueType = valueType;
        this.dataType = dataType;
        this.category = category;
        this.active = active;

        this.weightage = weightage;
        this.greenMin = greenMin;
        this.greenMax = greenMax;
        this.amberMin = amberMin;
        this.amberMax = amberMax;
        this.redMin = redMin;
        this.redMax = redMax;

        this.criteria = criteria;

        this.valueTypeOptions = valueTypeOptions;
        this.dataTypeOptions = dataTypeOptions;
        this.categoryOptions = categoryOptions;
    }

    public static CreditScorecardFeatureData instance(final Long id, final Long featureId, final String name,
            final EnumOptionData valueType, final EnumOptionData dataType, final EnumOptionData category, final Boolean active,
            final BigDecimal weightage, final Integer greenMin, final Integer greenMax, final Integer amberMin, final Integer amberMax,
            final Integer redMin, final Integer redMax) {

        final Collection<ScorecardFeatureCriteriaData> criteria = new ArrayList<>();

        final Collection<EnumOptionData> valueTypeOptions = null;
        final Collection<EnumOptionData> dataTypeOptions = null;
        final Collection<EnumOptionData> categoryOptions = null;

        return new CreditScorecardFeatureData(id, featureId, name, valueType, dataType, category, active, weightage, greenMin, greenMax,
                amberMin, amberMax, redMin, redMax, criteria, valueTypeOptions, dataTypeOptions, categoryOptions);
    }

    public static CreditScorecardFeatureData template(final Collection<EnumOptionData> valueTypeOptions,
            final Collection<EnumOptionData> dataTypeOptions, final Collection<EnumOptionData> categoryOptions) {

        final Long id = null;
        final Long featureId = null;
        final String name = null;
        final EnumOptionData valueType = null;
        final EnumOptionData dataType = null;
        final EnumOptionData category = null;
        final Boolean active = null;

        final BigDecimal weightage = null;
        final Integer greenMin = null;
        final Integer greenMax = null;
        final Integer amberMin = null;
        final Integer amberMax = null;
        final Integer redMin = null;
        final Integer redMax = null;

        final Collection<ScorecardFeatureCriteriaData> criteria = null;

        return new CreditScorecardFeatureData(id, featureId, name, valueType, dataType, category, active, weightage, greenMin, greenMax,
                amberMin, amberMax, redMin, redMax, criteria, valueTypeOptions, dataTypeOptions, categoryOptions);
    }

    public static CreditScorecardFeatureData withTemplate(CreditScorecardFeatureData scf, CreditScorecardFeatureData template) {

        return new CreditScorecardFeatureData(scf.id, scf.featureId, scf.name, scf.valueType, scf.dataType, scf.category, scf.active,
                scf.weightage, scf.greenMin, scf.greenMax, scf.amberMin, scf.amberMax, scf.redMin, scf.redMax, scf.criteria,
                template.valueTypeOptions, template.dataTypeOptions, template.categoryOptions);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EnumOptionData getValueType() {
        return valueType;
    }

    public EnumOptionData getDataType() {
        return dataType;
    }

    public EnumOptionData getCategory() {
        return category;
    }

    public Boolean getActive() {
        return active;
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

    public Collection<ScorecardFeatureCriteriaData> getCriteria() {
        return criteria;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof CreditScorecardFeatureData)) {
            return false;
        }
        final CreditScorecardFeatureData creditScorecardFeatureData = (CreditScorecardFeatureData) obj;
        return this.id.equals(creditScorecardFeatureData.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public int compareTo(@NotNull CreditScorecardFeatureData obj) {
        return obj.id.compareTo(this.id);
    }
}
