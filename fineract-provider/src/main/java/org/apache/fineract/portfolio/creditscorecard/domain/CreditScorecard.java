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

import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_credit_scorecard")
public class CreditScorecard extends AbstractPersistableCustom {

    @Column(name = "scorecard_scoring_method")
    private String scoringMethod;

    @Column(name = "scorecard_scoring_model")
    private String scoringModel;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "rule_based_scorecard_id", referencedColumnName = "id")
    private RuleBasedScorecard ruleBasedScorecard;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stat_scorecard_id", referencedColumnName = "id")
    private StatScorecard statScorecard;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ml_scorecard_id", referencedColumnName = "id")
    private MLScorecard mlScorecard;

    public CreditScorecard() {
        //
    }

    public CreditScorecard(String scoringMethod, String scoringModel) {
        this.scoringMethod = scoringMethod;
        this.scoringModel = scoringModel;
    }

    public CreditScorecard(String scoringMethod, String scoringModel, RuleBasedScorecard ruleBasedScorecard, StatScorecard statScorecard,
            MLScorecard mlScorecard) {
        this.scoringMethod = scoringMethod;
        this.scoringModel = scoringModel;
        this.ruleBasedScorecard = ruleBasedScorecard;
        this.statScorecard = statScorecard;
        this.mlScorecard = mlScorecard;
    }

    public String getScoringMethod() {
        return scoringMethod;
    }

    public String getScoringModel() {
        return scoringModel;
    }

    public RuleBasedScorecard getRuleBasedScorecard() {
        return ruleBasedScorecard;
    }

    public StatScorecard getStatScorecard() {
        return statScorecard;
    }

    public MLScorecard getMlScorecard() {
        return mlScorecard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CreditScorecard)) {
            return false;
        }
        CreditScorecard that = (CreditScorecard) o;
        return Objects.equals(scoringMethod, that.scoringMethod) && Objects.equals(scoringModel, that.scoringModel)
                && Objects.equals(ruleBasedScorecard, that.ruleBasedScorecard) && Objects.equals(statScorecard, that.statScorecard)
                && Objects.equals(mlScorecard, that.mlScorecard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scoringMethod, scoringModel, ruleBasedScorecard, statScorecard, mlScorecard);
    }

    @Override
    public String toString() {
        return "CreditScorecard{" + "scoringMethod='" + scoringMethod + '\'' + ", scoringModel='" + scoringModel + '\''
                + ", ruleBasedScorecard=" + ruleBasedScorecard + ", statScorecard=" + statScorecard + ", mlScorecard=" + mlScorecard + '}';
    }
}
