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
package org.apache.fineract.infrastructure.survey.data;

import org.joda.time.LocalDate;

/**
 * Created by Cieyou on 3/18/14.
 */
public class ClientScoresOverview {

    @SuppressWarnings("unused")
    final private String surveyName;
    @SuppressWarnings("unused")
    final private long id;
    @SuppressWarnings("unused")
    final private String likelihoodCode;
    @SuppressWarnings("unused")
    final private String likelihoodName;
    @SuppressWarnings("unused")
    final private long score;
    @SuppressWarnings("unused")
    final private Double povertyLine;
    @SuppressWarnings("unused")
    final private LocalDate date;

    public ClientScoresOverview(final String likelihoodCode, final String likelihoodName, final long score, final Double povertyLine,
            final LocalDate date, final long resourceId, final String surveyName) {

        this.likelihoodCode = likelihoodCode;
        this.likelihoodName = likelihoodName;
        this.score = score;
        this.povertyLine = povertyLine;
        this.date = date;
        this.id = resourceId;
        this.surveyName = surveyName;

    }
}
