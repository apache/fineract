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
package org.apache.fineract.spm.data;

import java.util.ArrayList;
import java.util.List;

public class ScorecardData {

    private Long id;
    private Long userId;
    private String username;
    private Long clientId;
    private Long surveyId;
    private String surveyName;
    private List<ScorecardValue> scorecardValues;

    public ScorecardData() {
        super();
    }

    private ScorecardData(final Long id, final Long userId, final String username, final Long surveyId, final String surveyName,
            final Long clientId) {
        this.id = id;
        this.userId = userId;
        this.clientId = clientId;
        this.scorecardValues = new ArrayList<>();
        this.surveyId = surveyId;
        this.surveyName = surveyName;
        this.username = username;
    }

    public static ScorecardData instance(final Long id, final Long userId, final String username, final Long surveyId,
            final String surveyName, final Long clientId) {
        return new ScorecardData(id, userId, username, surveyId, surveyName, clientId);
    }

    public Long getUserId() {
        return userId;
    }

    public Long getClientId() {
        return clientId;
    }

    public List<ScorecardValue> getScorecardValues() {
        return scorecardValues;
    }

    public void setScorecardValues(List<ScorecardValue> scorecardValues) {
        if (this.scorecardValues == null) {
            this.scorecardValues = new ArrayList<>();
        }
        this.scorecardValues.addAll(scorecardValues);
    }

    public String getUsername() {
        return this.username;
    }

    public Long getSurveyId() {
        return this.surveyId;
    }

    public String getSurveyName() {
        return this.surveyName;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public void setSurveyId(Long surveyId) {
        this.surveyId = surveyId;
    }

    public void setSurveyName(String surveyName) {
        this.surveyName = surveyName;
    }

}
