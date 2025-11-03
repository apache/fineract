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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.QuestionData;
import org.apache.fineract.client.models.ResponseData;
import org.apache.fineract.client.models.SurveyData;
import org.apache.fineract.client.services.SpmSurveysApi;
import org.apache.fineract.client.util.FineractClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

public class SurveyHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyHelper.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int DEFAULT_VALIDITY_YEARS = 100;
    private static final String SURVEY_KEY_PREFIX = "SURVEY_";
    private static final String QUESTION_KEY_PREFIX = "Q";
    private static final String QUESTION_DESC_PREFIX = "Question ";
    private static final String YES_RESPONSE = "Yes";
    private static final String NO_RESPONSE = "No";
    private static final String ACTIVATE_COMMAND = "activate";
    private static final String DEACTIVATE_COMMAND = "deactivate";

    private final SpmSurveysApi surveysApi;

    public SurveyHelper(final FineractClient fineractClient) {
        this.surveysApi = fineractClient.surveys;
    }

    public Long createSurvey(String name, String description, LocalDate validFrom, LocalDate validTo, List<String> questions) {
        validateSurveyInputs(name, description, questions);

        try {
            SurveyData surveyData = buildSurveyData(name, description, validFrom, validTo, questions);
            return executeSurveyCreation(surveyData, name);
        } catch (Exception e) {
            LOG.error("Exception whilst creating survey: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create survey: " + e.getMessage(), e);
        }
    }

    public Long createSurvey(String name, String description, List<String> questions) {
        LocalDate validFrom = Utils.getLocalDateOfTenant();
        LocalDate validTo = validFrom.plusYears(DEFAULT_VALIDITY_YEARS);
        return createSurvey(name, description, validFrom, validTo, questions);
    }

    public SurveyData retrieveSurvey(Long surveyId) {
        return executeApiCall(() -> surveysApi.findSurvey(surveyId), "Failed to retrieve survey with ID: " + surveyId);
    }

    public List<SurveyData> retrieveAllSurveys() {
        return executeApiCall(() -> surveysApi.fetchAllSurveys1(null), "Failed to retrieve all surveys");
    }

    public List<SurveyData> retrieveActiveSurveys() {
        return executeApiCall(() -> surveysApi.fetchAllSurveys1(true), "Failed to retrieve active surveys");
    }

    public String updateSurvey(Long surveyId, SurveyData surveyData) {
        String result = executeApiCall(() -> surveysApi.editSurvey(surveyId, surveyData), "Failed to update survey with ID: " + surveyId);
        LOG.info("Survey updated successfully: {}", surveyId);
        return result;
    }

    public void deactivateSurvey(Long surveyId) {
        changeSurveyStatus(surveyId, DEACTIVATE_COMMAND, "deactivated");
    }

    public void activateSurvey(Long surveyId) {
        changeSurveyStatus(surveyId, ACTIVATE_COMMAND, "activated");
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

    private void validateSurveyInputs(String name, String description, List<String> questions) {
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

    private Long executeSurveyCreation(SurveyData surveyData, String surveyName) throws Exception {
        Call<Void> call = surveysApi.createSurvey(surveyData);
        Response<Void> response = call.execute();

        if (response.isSuccessful()) {
            LOG.info("Survey created successfully: {}", surveyName);
            return null;
        } else {
            String errorBody = getErrorBody(response);
            LOG.error("Failed to create survey: {}", errorBody);
            throw new RuntimeException("Failed to create survey: " + errorBody);
        }
    }

    private void changeSurveyStatus(Long surveyId, String command, String action) {
        executeVoidApiCall(() -> surveysApi.activateOrDeactivateSurvey(surveyId, command),
                "Failed to " + action.toLowerCase() + " survey with ID: " + surveyId);
        LOG.info("Survey {} successfully: {}", action, surveyId);
    }

    private <T> T executeApiCall(ApiCallSupplier<T> apiCall, String errorMessage) {
        try {
            Response<T> response = apiCall.get().execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                String errorBody = getErrorBody(response);
                throw new RuntimeException(errorMessage + ": " + errorBody);
            }
        } catch (Exception e) {
            throw new RuntimeException(errorMessage + ": " + e.getMessage(), e);
        }
    }

    private void executeVoidApiCall(ApiCallSupplier<Void> apiCall, String errorMessage) {
        try {
            Response<Void> response = apiCall.get().execute();
            if (!response.isSuccessful()) {
                String errorBody = getErrorBody(response);
                throw new RuntimeException(errorMessage + ": " + errorBody);
            }
        } catch (Exception e) {
            throw new RuntimeException(errorMessage + ": " + e.getMessage(), e);
        }
    }

    private String getErrorBody(Response<?> response) {
        try {
            return response.errorBody() != null ? response.errorBody().string() : "Unknown error";
        } catch (Exception e) {
            return "Error reading response body: " + e.getMessage();
        }
    }

    @FunctionalInterface
    private interface ApiCallSupplier<T> {

        Call<T> get() throws Exception;
    }

    @Deprecated(forRemoval = true)
    public static Integer fulfilSurvey(final io.restassured.specification.RequestSpecification requestSpec,
            final io.restassured.specification.ResponseSpecification responseSpec) {
        return fulfilSurvey(requestSpec, responseSpec, "04 March 2011");
    }

    @Deprecated(forRemoval = true)
    public static Integer fulfilSurvey(final io.restassured.specification.RequestSpecification requestSpec,
            final io.restassured.specification.ResponseSpecification responseSpec, final String activationDate) {
        LOG.info("---------------------------------FULFIL PPI ---------------------------------------------");
        final String FULFIL_SURVEY_URL = "/fineract-provider/api/v1/survey/ppi_kenya_2009/clientId?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPost(requestSpec, responseSpec, FULFIL_SURVEY_URL, getTestPPIAsJSON(), "clientId");
    }

    @Deprecated(forRemoval = true)
    public static String getTestPPIAsJSON() {
        final java.util.HashMap<String, String> map = new java.util.HashMap<>();

        map.put("date", "2014-05-19 00:00:00");
        map.put("ppi_household_members_cd_q1_householdmembers", "107");
        map.put("ppi_highestschool_cd_q2_highestschool", "112");
        map.put("ppi_businessoccupation_cd_q3_businessoccupation", "116");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("locale", "en");
        map.put("ppi_habitablerooms_cd_q4_habitablerooms", "120");
        map.put("ppi_floortype_cd_q5_floortype", "124");
        map.put("ppi_lightingsource_cd_q6_lightingsource", "126");
        map.put("ppi_irons_cd_q7_irons", "128");
        map.put("ppi_mosquitonets_cd_q8_mosquitonets", "132");
        map.put("ppi_towels_cd_q9_towels", "134");
        map.put("ppi_fryingpans_cd_q10_fryingpans", "138");

        LOG.info("map :  {}", map);
        return new com.google.gson.Gson().toJson(map);
    }

    @Deprecated(forRemoval = true)
    public static void verifySurveyCreatedOnServer(final io.restassured.specification.RequestSpecification requestSpec,
            final io.restassured.specification.ResponseSpecification responseSpec, final Integer generatedClientID) {
        LOG.info("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        final String SURVEY_URL = "/fineract-provider/api/v1/Survey/ppi_kenya_2009/clientid/entryId" + generatedClientID + "?"
                + Utils.TENANT_IDENTIFIER;
        final Integer responseClientID = Utils.performServerGet(requestSpec, responseSpec, SURVEY_URL, "id");
        org.junit.jupiter.api.Assertions.assertEquals(generatedClientID, responseClientID, "ERROR IN CREATING THE CLIENT");
    }
}
