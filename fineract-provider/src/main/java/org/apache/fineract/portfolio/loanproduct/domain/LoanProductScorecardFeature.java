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
package org.apache.fineract.portfolio.loanproduct.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.creditscorecard.domain.CreditScorecardFeature;
import org.apache.fineract.portfolio.creditscorecard.domain.FeatureConfiguration;
import org.apache.fineract.portfolio.creditscorecard.domain.FeatureCriteria;

@Entity
@Table(name = "m_product_loan_scorecard_feature")
public class LoanProductScorecardFeature extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "scorecard_feature_id", referencedColumnName = "id", nullable = false)
    private CreditScorecardFeature scorecardFeature;

    @Embedded
    private FeatureConfiguration configuration;

    @OneToMany(cascade = { CascadeType.PERSIST, CascadeType.DETACH }, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_loan_scorecard_feature_id", referencedColumnName = "id")
    private List<FeatureCriteria> featureCriteria;

    public LoanProductScorecardFeature() {
        //
    }

    public LoanProductScorecardFeature(final CreditScorecardFeature scorecardFeature, final BigDecimal weightage, final Integer greenMin,
            final Integer greenMax, final Integer amberMin, final Integer amberMax, final Integer redMin, final Integer redMax) {
        this.scorecardFeature = scorecardFeature;
        this.configuration = FeatureConfiguration.from(weightage, greenMin, greenMax, amberMin, amberMax, redMin, redMax);
    }

    public FeatureConfiguration getFeatureConfiguration() {
        return configuration;
    }

    public CreditScorecardFeature getScorecardFeature() {
        return scorecardFeature;
    }

    public List<FeatureCriteria> getFeatureCriteria() {
        return featureCriteria;
    }

    public void setFeatureCriteria(List<FeatureCriteria> featureCriteria) {
        this.featureCriteria = featureCriteria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoanProductScorecardFeature)) {
            return false;
        }
        LoanProductScorecardFeature that = (LoanProductScorecardFeature) o;
        return Objects.equals(configuration, that.configuration) && Objects.equals(scorecardFeature, that.scorecardFeature)
                && Objects.equals(featureCriteria, that.featureCriteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration, scorecardFeature, featureCriteria);
    }
}
