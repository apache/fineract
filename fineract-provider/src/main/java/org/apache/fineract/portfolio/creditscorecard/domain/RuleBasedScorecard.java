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
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_rule_based_scorecard")
public class RuleBasedScorecard extends AbstractPersistableCustom {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "rule_based_scorecard_id", referencedColumnName = "id")
    private List<FeatureCriteriaScore> criteriaScores;

    @Column(name = "overall_score")
    private BigDecimal overallScore;

    @Column(name = "overall_color")
    private String overallColor;

    public RuleBasedScorecard() {
        //
    }

    public BigDecimal getOverallScore() {
        return overallScore;
    }

    public String getOverallColor() {
        return overallColor;
    }

    public void setScore(final BigDecimal overallScore, final String overallColor) {
        this.overallScore = overallScore;
        this.overallColor = overallColor;
    }

    public void setCriteriaScores(final List<FeatureCriteriaScore> criteriaScores) {
        this.criteriaScores = criteriaScores;
    }

    public List<FeatureCriteriaScore> getCriteriaScores() {
        return criteriaScores;
    }
}
