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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.creditscorecard.data.ScorecardFeatureCriteriaData;

@Entity
@Table(name = "m_scorecard_feature_criteria")
public class FeatureCriteria extends AbstractPersistableCustom {

    @Column(name = "criteria", nullable = false)
    private String criteria;

    @Column(name = "score", nullable = false)
    private BigDecimal score;

    public FeatureCriteria() {
        //
    }

    public FeatureCriteria(String criteria, BigDecimal score) {
        this.criteria = criteria;
        this.score = score;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public ScorecardFeatureCriteriaData toData() {
        return ScorecardFeatureCriteriaData.instance(this.getId(), this.criteria, this.score);
    }

    @Override
    public String toString() {
        return "ScorecardFeatureCriteria{" + "criteria='" + criteria + '\'' + ", score=" + score + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FeatureCriteria)) {
            return false;
        }
        FeatureCriteria that = (FeatureCriteria) o;
        return Objects.equals(criteria, that.criteria) && Objects.equals(score, that.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(criteria, score);
    }
}
