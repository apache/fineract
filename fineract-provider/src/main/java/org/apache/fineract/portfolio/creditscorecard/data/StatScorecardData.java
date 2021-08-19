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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.apache.fineract.portfolio.creditscorecard.domain.MLScorecardFields;
import org.apache.fineract.portfolio.creditscorecard.domain.StatScorecard;

public final class StatScorecardData implements Serializable {

    private final Long id;
    private final Integer age;
    private final String sex;
    private final String job;
    private final String housing;
    private final BigDecimal creditAmount;
    private final Integer duration;
    private final String purpose;

    private final String method;

    private final String color;
    private final BigDecimal prediction;

    private final BigDecimal wilkisLambda;
    private final BigDecimal pillaisTrace;
    private final BigDecimal hotellingLawley;
    private final BigDecimal roysGreatestRoots;

    private final Collection<Map<String, Object>> scoringModels;

    public StatScorecardData(Long id, Integer age, String sex, String job, String housing, BigDecimal creditAmount, Integer duration,
            String purpose, String method, String color, BigDecimal prediction, BigDecimal wilkisLambda, BigDecimal pillaisTrace,
            BigDecimal hotellingLawley, BigDecimal roysGreatestRoots, Collection<Map<String, Object>> scoringModels) {
        this.id = id;
        this.age = age;
        this.sex = sex;
        this.job = job;
        this.housing = housing;
        this.creditAmount = creditAmount;
        this.duration = duration;
        this.purpose = purpose;

        this.method = method;

        this.color = color;
        this.prediction = prediction;

        this.wilkisLambda = wilkisLambda;
        this.pillaisTrace = pillaisTrace;
        this.hotellingLawley = hotellingLawley;
        this.roysGreatestRoots = roysGreatestRoots;

        this.scoringModels = scoringModels;
    }

    public static StatScorecardData instance(final StatScorecard sc) {
        final MLScorecardFields scf = sc.getScorecardFields();

        final Collection<Map<String, Object>> scoringModels = null;

        return new StatScorecardData(sc.getId(), scf.getAge(), scf.getSex(), scf.getJob(), scf.getHousing(), scf.getCreditAmount(),
                scf.getDuration(), scf.getPurpose(), sc.getMethod(), sc.getColor(), sc.getPrediction(), sc.getWilkisLambda(),
                sc.getPillaisTrace(), sc.getHotellingLawley(), sc.getRoysGreatestRoots(), scoringModels);
    }

    public static StatScorecardData template() {

        final Long id = null;
        final Integer age = null;
        final String sex = null;
        final String job = null;
        final String housing = null;

        final BigDecimal creditAmount = null;
        final Integer duration = null;
        final String purpose = null;

        final String method = null;

        final String color = null;
        final BigDecimal prediction = null;

        final BigDecimal wilkisLambda = null;
        final BigDecimal pillaisTrace = null;
        final BigDecimal hotellingLawley = null;
        final BigDecimal roysGreatestRoots = null;

        final Collection<Map<String, Object>> scoringModels = new ArrayList<>(
                Arrays.asList(Map.of("code", "manova", "value", "Manova"), Map.of("code", "linearRegression", "value", "Linear Regression"),
                        Map.of("code", "polynomialRegression", "value", "Polynomial Regression")));

        return new StatScorecardData(id, age, sex, job, housing, creditAmount, duration, purpose, method, color, prediction, wilkisLambda,
                pillaisTrace, hotellingLawley, roysGreatestRoots, scoringModels);
    }
}
