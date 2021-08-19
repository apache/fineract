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
import org.apache.fineract.portfolio.creditscorecard.domain.FeatureCriteriaScore;

public final class FeatureCriteriaScoreData implements Serializable {

    private final Long id;
    private final String feature;
    private final String value;
    private final BigDecimal score;
    private final String color;

    private FeatureCriteriaScoreData(final Long id, final String feature, final String value, final BigDecimal score, final String color) {
        this.id = id;
        this.feature = feature;
        this.value = value;
        this.score = score;
        this.color = color;
    }

    public static FeatureCriteriaScoreData instance(final FeatureCriteriaScore ctScore) {
        final String feature = ctScore.getFeature().getScorecardFeature().getName();

        return new FeatureCriteriaScoreData(ctScore.getId(), feature, ctScore.getValue(), ctScore.getScore(), ctScore.getColor());
    }

    public BigDecimal getScore() {
        return score;
    }

    public String getColor() {
        return color;
    }

}
