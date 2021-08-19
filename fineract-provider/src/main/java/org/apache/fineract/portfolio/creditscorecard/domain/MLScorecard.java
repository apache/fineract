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
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_ml_scorecard")
public class MLScorecard extends AbstractPersistableCustom {

    @Embedded
    private MLScorecardFields scorecardFields;

    @Column(name = "predicted_risk")
    private String predictedRisk;

    @Column(name = "accuracy")
    private BigDecimal accuracy;

    @Column(name = "actual_risk")
    private String actualRisk;

    @Column(name = "prediction_request_id")
    private Integer predictionRequestId;

    public MLScorecard() {

    }

    public MLScorecard(final MLScorecardFields scorecardFields, final String predictedRisk, final String actualRisk,
            final Integer predictionRequestId) {
        this.scorecardFields = scorecardFields;
        this.predictedRisk = predictedRisk;
        this.actualRisk = actualRisk;
        this.predictionRequestId = predictionRequestId;
    }

    public MLScorecard(final MLScorecardFields mlScorecardFields) {
        this.scorecardFields = mlScorecardFields;
    }

    public MLScorecard scorecardFields(final MLScorecardFields scorecardFields) {
        this.scorecardFields = scorecardFields;
        return this;
    }

    public MLScorecardFields getScorecardFields() {
        return scorecardFields;
    }

    public void setScorecardFields(MLScorecardFields scorecardFields) {
        this.scorecardFields = scorecardFields;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public String getPredictedRisk() {
        return predictedRisk;
    }

    public void setPredictedRisk(final String predictedRisk) {
        this.predictedRisk = predictedRisk;
    }

    public String getActualRisk() {
        return actualRisk;
    }

    public void setActualRisk(final String actualRisk) {
        this.actualRisk = actualRisk;
    }

    public Integer getPredictionRequestId() {
        return predictionRequestId;
    }

    public void setPredictionRequestId(final Integer predictionRequestId) {
        this.predictionRequestId = predictionRequestId;
    }

    public MLScorecard setPredictionResponse(BigDecimal accuracy, String predictedRisk, Integer predictionRequestId) {
        this.accuracy = accuracy;
        this.predictedRisk = predictedRisk;
        this.predictionRequestId = predictionRequestId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MLScorecard)) {
            return false;
        }
        MLScorecard loanScorecard = (MLScorecard) o;
        return Objects.equals(scorecardFields, loanScorecard.scorecardFields) && Objects.equals(predictedRisk, loanScorecard.predictedRisk)
                && Objects.equals(actualRisk, loanScorecard.actualRisk)
                && Objects.equals(predictionRequestId, loanScorecard.predictionRequestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scorecardFields, predictedRisk, actualRisk, predictionRequestId);
    }

    @Override
    public String toString() {
        return "MLScorecard{" + "scorecardFields=" + scorecardFields + ", predictedRisk='" + predictedRisk + '\'' + ", actualRisk='"
                + actualRisk + '\'' + ", predictionRequestId=" + predictionRequestId + '}';
    }
}
