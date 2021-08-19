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

import java.math.BigDecimal;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductScorecardFeature;

@Entity
@Table(name = "m_scorecard_feature_criteria_score")
public class FeatureCriteriaScore extends AbstractPersistableCustom {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "product_loan_scorecard_feature_id", referencedColumnName = "id")
    private LoanProductScorecardFeature feature;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "color")
    private String color;

    public FeatureCriteriaScore() {
        //
    }

    public FeatureCriteriaScore(LoanProductScorecardFeature feature, String value) {
        this.feature = feature;
        this.value = value;
    }

    public FeatureCriteriaScore(LoanProductScorecardFeature feature, BigDecimal score, String color) {
        this.feature = feature;
        this.score = score;
        this.color = color;
    }

    public LoanProductScorecardFeature getFeature() {
        return feature;
    }

    public String getValue() {
        return value;
    }

    public BigDecimal getScore() {
        return score;
    }

    public String getColor() {
        return color;
    }

    public void setScore(final BigDecimal score, final String color) {
        this.score = score;
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeatureCriteriaScore)) {
            return false;
        }
        FeatureCriteriaScore that = (FeatureCriteriaScore) o;
        return Objects.equals(feature, that.feature) && Objects.equals(value, that.value) && Objects.equals(score, that.score)
                && Objects.equals(color, that.color);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feature, value, score, color);
    }

    @Override
    public String toString() {
        return "FeatureCriteriaScore{" + "feature=" + feature + ", value='" + value + '\'' + ", score=" + score + ", color='" + color + '\''
                + '}';
    }
}
