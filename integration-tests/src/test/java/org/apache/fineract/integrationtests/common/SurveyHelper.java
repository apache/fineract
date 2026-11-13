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
package org.apache.fineract.integrationtests.common;

import static org.apache.fineract.client.feign.util.FeignCalls.executeVoid;
import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.QuestionData;
import org.apache.fineract.client.models.ResponseData;
import org.apache.fineract.client.models.SurveyData;

@Slf4j
public class SurveyHelper {

    private static final int DEFAULT_VALIDITY_YEARS = 100;
    private static final String SURVEY_KEY_PREFIX = "SURVEY_";
    private static final String QUESTION_KEY_PREFIX = "Q";
    private static final String QUESTION_DESC_PREFIX = "Question ";
    private static final String YES_RESPONSE = "Yes";
    private static final String NO_RESPONSE = "No";
    private static final String ACTIVATE_COMMAND = "activate";
    private static final String DEACTIVATE_COMMAND = "deactivate";

    public Long createSurvey(String name, String description, LocalDate validFrom, LocalDate validTo, List<String> questions) {
        validateSurveyInputs(name, questions);
        SurveyData surveyData = buildSurveyData(name, description, validFrom, validTo, questions);
        executeVoid(() -> FineractFeignClientHelper.getFineractFeignClient().spmSurveys().createSurvey(surveyData));
        log.info("Survey created successfully: {}", name);
        return null;
    }

    public Long createSurvey(String name, String description, List<String> questions) {
        LocalDate validFrom = Utils.getLocalDateOfTenant();
        LocalDate validTo = validFrom.plusYears(DEFAULT_VALIDITY_YEARS);
        return createSurvey(name, description, validFrom, validTo, questions);
    }

    public SurveyData retrieveSurvey(Long surveyId) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().spmSurveys().findSurvey(surveyId));
    }

    public List<SurveyData> retrieveAllSurveys() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().spmSurveys().fetchAllSurveys((Boolean) null));
    }

    public List<SurveyData> retrieveActiveSurveys() {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().spmSurveys().fetchAllSurveys(true));
    }

    public String updateSurvey(Long surveyId, SurveyData surveyData) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().spmSurveys().editSurvey(surveyId, surveyData));
    }

    public void deactivateSurvey(Long surveyId) {
        executeVoid(() -> FineractFeignClientHelper.getFineractFeignClient().spmSurveys().activateOrDeactivateSurvey(surveyId,
                DEACTIVATE_COMMAND));
        log.info("Survey deactivated successfully: {}", surveyId);
    }

    public void activateSurvey(Long surveyId) {
        executeVoid(() -> FineractFeignClientHelper.getFineractFeignClient().spmSurveys().activateOrDeactivateSurvey(surveyId,
                ACTIVATE_COMMAND));
        log.info("Survey activated successfully: {}", surveyId);
    }

    public String getSurveyName(SurveyData survey) {
        return survey.getName();
    }

    public String getSurveyDescription(SurveyData survey) {
        return survey.getDescription();
    }

    public LocalDate getSurveyValidFrom(SurveyData survey) {
        return survey.getValidFrom();
    }

    public LocalDate getSurveyValidTo(SurveyData survey) {
        return survey.getValidTo();
    }

    public int getSurveyQuestionsCount(SurveyData survey) {
        return survey.getQuestionDatas() != null ? survey.getQuestionDatas().size() : 0;
    }

    public String getSurveyKey(SurveyData survey) {
        return survey.getKey();
    }

    public String getSurveyCountryCode(SurveyData survey) {
        return survey.getCountryCode();
    }

    private void validateSurveyInputs(String name, List<String> questions) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Survey name cannot be null or empty");
        }
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("Survey must have at least one question");
        }
    }

    private SurveyData buildSurveyData(String name, String description, LocalDate validFrom, LocalDate validTo, List<String> questions) {
        SurveyData surveyData = new SurveyData().name(name).description(description).validFrom(validFrom).validTo(validTo).countryCode("KE")
                .key(SURVEY_KEY_PREFIX + System.currentTimeMillis());
        surveyData.questionDatas(buildQuestionDataList(questions));
        return surveyData;
    }

    private List<QuestionData> buildQuestionDataList(List<String> questions) {
        List<QuestionData> questionDataList = new ArrayList<>(questions.size());
        for (int i = 0; i < questions.size(); i++) {
            QuestionData questionData = new QuestionData().text(questions.get(i)).sequenceNo(i + 1).key(QUESTION_KEY_PREFIX + (i + 1))
                    .description(QUESTION_DESC_PREFIX + (i + 1)).responseDatas(createYesNoResponses());
            questionDataList.add(questionData);
        }
        return questionDataList;
    }

    private List<ResponseData> createYesNoResponses() {
        List<ResponseData> responses = new ArrayList<>(2);
        responses.add(new ResponseData().text(YES_RESPONSE).value(1).sequenceNo(1));
        responses.add(new ResponseData().text(NO_RESPONSE).value(0).sequenceNo(2));
        return responses;
    }
}
