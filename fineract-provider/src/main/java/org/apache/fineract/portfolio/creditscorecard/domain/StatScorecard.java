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
@Table(name = "m_stat_scorecard")
public class StatScorecard extends AbstractPersistableCustom {

    @Embedded
    private MLScorecardFields scorecardFields;

    @Column(name = "method")
    private String method;

    @Column(name = "color")
    private String color;

    @Column(name = "prediction")
    private BigDecimal prediction;

    @Column(name = "wilki_s_lambda")
    private BigDecimal wilkisLambda;

    @Column(name = "pillai_s_trace")
    private BigDecimal pillaisTrace;

    @Column(name = "hotelling_lawley_trace")
    private BigDecimal hotellingLawley;

    @Column(name = "roy_s_greatest_roots")
    private BigDecimal roysGreatestRoots;

    public StatScorecard() {
        //
    }

    public StatScorecard(String method, String color, BigDecimal prediction, BigDecimal wilkisLambda, BigDecimal pillaisTrace,
            BigDecimal hotellingLawley, BigDecimal roysGreatestRoots) {
        this.method = method;
        this.color = color;
        this.prediction = prediction;
        this.wilkisLambda = wilkisLambda;
        this.pillaisTrace = pillaisTrace;
        this.hotellingLawley = hotellingLawley;
        this.roysGreatestRoots = roysGreatestRoots;
    }

    public StatScorecard(final MLScorecardFields mlScorecardFields) {
        this.scorecardFields = mlScorecardFields;
    }

    public void setPredictionResponse(String method, String color, BigDecimal prediction, BigDecimal wilkisLambda, BigDecimal pillaisTrace,
            BigDecimal hotellingLawley, BigDecimal roysGreatestRoots) {
        this.method = method;
        this.color = color;
        this.prediction = prediction;
        if (this.method.equalsIgnoreCase("manova")) {
            this.wilkisLambda = wilkisLambda;
            this.pillaisTrace = pillaisTrace;
            this.hotellingLawley = hotellingLawley;
            this.roysGreatestRoots = roysGreatestRoots;
        }
    }

    public MLScorecardFields getScorecardFields() {
        return scorecardFields;
    }

    public String getMethod() {
        return method;
    }

    public String getColor() {
        return color;
    }

    public BigDecimal getPrediction() {
        return prediction;
    }

    public BigDecimal getWilkisLambda() {
        return wilkisLambda;
    }

    public BigDecimal getPillaisTrace() {
        return pillaisTrace;
    }

    public BigDecimal getHotellingLawley() {
        return hotellingLawley;
    }

    public BigDecimal getRoysGreatestRoots() {
        return roysGreatestRoots;
    }

    @Override
    public String toString() {
        return "StatScorecard{" + "scorecardFields=" + scorecardFields + ", method='" + method + '\'' + ", color='" + color + '\''
                + ", prediction=" + prediction + ", wilkisLambda=" + wilkisLambda + ", pillaisTrace=" + pillaisTrace + ", hotellingLawley="
                + hotellingLawley + ", roysGreatestRoots=" + roysGreatestRoots + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StatScorecard)) {
            return false;
        }
        StatScorecard that = (StatScorecard) o;
        return Objects.equals(scorecardFields, that.scorecardFields) && Objects.equals(method, that.method)
                && Objects.equals(color, that.color) && Objects.equals(prediction, that.prediction)
                && Objects.equals(wilkisLambda, that.wilkisLambda) && Objects.equals(pillaisTrace, that.pillaisTrace)
                && Objects.equals(hotellingLawley, that.hotellingLawley) && Objects.equals(roysGreatestRoots, that.roysGreatestRoots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scorecardFields, method, color, prediction, wilkisLambda, pillaisTrace, hotellingLawley, roysGreatestRoots);
    }
}
