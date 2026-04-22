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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.SurveyData;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.SurveyHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurveyIntegrationTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SurveyIntegrationTest.class);
    private static final int SURVEY_VALIDITY_YEARS = 100;
    private static final String TEST_SURVEY_PREFIX = "Test Survey ";
    private static final String KENYAN_COUNTRY_CODE = "KE";

    private static final String DEFAULT_DESCRIPTION = "Test Survey Description";
    private static final List<String> BASIC_QUESTIONS = List.of("Question 1", "Question 2");
    private static final List<String> DEFAULT_VALIDITY_QUESTIONS = List.of("Default Question 1", "Default Question 2",
            "Default Question 3");
    private static final List<String> MULTIPLE_QUESTIONS = List.of("What is your age?", "What is your occupation?",
            "What is your income level?", "Do you own a house?", "How many children do you have?", "What is your education level?",
            "Do you have a bank account?", "What is your marital status?");

    private SurveyHelper surveyHelper;

    @BeforeEach
    void setup() {
        Utils.initializeRESTAssured();
        this.surveyHelper = new SurveyHelper(fineractClient());
    }

    @Test
    @Order(1)
    void testCreateSurvey() {
        LOG.info("Creating Survey");

        final String surveyName = generateSurveyName();
        final LocalDate validFrom = Utils.getLocalDateOfTenant();
        final LocalDate validTo = validFrom.plusYears(SURVEY_VALIDITY_YEARS);

        surveyHelper.createSurvey(surveyName, DEFAULT_DESCRIPTION, validFrom, validTo, BASIC_QUESTIONS);
        LOG.info("Survey created: {}", surveyName);
    }

    @Test
    @Order(2)
    void testCreateSurveyWithDefaultValidity() {
        LOG.info("Creating Survey with Default Validity");

        final String surveyName = generateSurveyName();
        final String description = "Test Survey with Default Validity";

        surveyHelper.createSurvey(surveyName, description, DEFAULT_VALIDITY_QUESTIONS);
        LOG.info("Survey created with default validity: {}", surveyName);
    }

    @Test
    @Order(3)
    void testCreateSurveyWithInvalidData() {
        LOG.info("Testing Survey Creation with Invalid Data");

        final String surveyName = generateSurveyName();
        final LocalDate validFrom = Utils.getLocalDateOfTenant();
        final LocalDate validTo = validFrom.plusYears(SURVEY_VALIDITY_YEARS);
        final List<String> emptyQuestions = List.of();

        final Exception exception = assertThrows(RuntimeException.class,
                () -> surveyHelper.createSurvey(surveyName, DEFAULT_DESCRIPTION, validFrom, validTo, emptyQuestions));

        assertThat(exception.getMessage()).containsIgnoringCase("question");
    }

    @Test
    @Order(4)
    void testRetrieveActiveSurveys() {
        LOG.info("Testing Retrieve Active Surveys");

        final String surveyName = createTestSurvey("Test Survey for Retrieval", List.of("Retrieval Question 1", "Retrieval Question 2"));

        List<SurveyData> activeSurveys = surveyHelper.retrieveActiveSurveys();

        assertThat(activeSurveys).isNotNull();
        assertThat(activeSurveys).hasSizeGreaterThanOrEqualTo(1);

        verifyCreatedSurveyExists(activeSurveys, surveyName);

        LOG.info("Retrieved {} active surveys", activeSurveys.size());
    }

    @Test
    @Order(5)
    void testSurveyProperties() {
        LOG.info("Testing Survey Properties");

        final String surveyName = generateSurveyName();
        final String description = "Test Survey for Properties";
        final LocalDate validFrom = Utils.getLocalDateOfTenant();
        final LocalDate validTo = validFrom.plusYears(SURVEY_VALIDITY_YEARS);
        final List<String> questions = List.of("Property Question 1", "Property Question 2", "Property Question 3");

        surveyHelper.createSurvey(surveyName, description, validFrom, validTo, questions);

        SurveyData createdSurvey = findSurveyByName(surveyName);

        verifySurveyProperties(createdSurvey, surveyName, description, validFrom, validTo, 3);

        LOG.info("Survey properties verified successfully");
    }

    @Test
    @Order(6)
    void testCreateSurveyWithMultipleQuestions() {
        LOG.info("Testing Survey Creation with Multiple Questions");

        final String surveyName = createTestSurvey("Test Survey with Multiple Questions", MULTIPLE_QUESTIONS);

        SurveyData createdSurvey = findSurveyByName(surveyName);
        assertThat(surveyHelper.getSurveyQuestionsCount(createdSurvey)).isEqualTo(MULTIPLE_QUESTIONS.size());

        LOG.info("Survey created with {} questions successfully", MULTIPLE_QUESTIONS.size());
    }

    @Test
    @Order(7)
    void testCreateSurveyWithSpecialCharacters() {
        LOG.info("Testing Survey Creation with Special Characters");

        final String surveyName = "Test Survey with Special Chars: @#$%^&*()_+-=[]{}|;':\",./<>?";
        final String description = "Test Survey Description with special characters: £€¥¢₦₹₿";
        final List<String> questions = List.of("Question with special chars: @#$%^&*()", "Another question with £€¥ symbols");

        surveyHelper.createSurvey(surveyName, description, questions);
        LOG.info("Survey created with special characters successfully");
    }

    @Test
    @Order(8)
    void testCreateSurveyWithLongText() {
        LOG.info("Testing Survey Creation with Long Text");

        final String surveyName = generateSurveyName();
        final String description = buildLongDescription();
        final List<String> questions = List.of(buildLongQuestion("first test case"), buildLongQuestion("second test scenario"));

        surveyHelper.createSurvey(surveyName, description, questions);
        LOG.info("Survey created with long text successfully");
    }

    private String generateSurveyName() {
        return TEST_SURVEY_PREFIX + System.currentTimeMillis();
    }

    private String createTestSurvey(String description, List<String> questions) {
        final String surveyName = generateSurveyName();
        surveyHelper.createSurvey(surveyName, description, questions);
        return surveyName;
    }

    private SurveyData findSurveyByName(String surveyName) {
        List<SurveyData> activeSurveys = surveyHelper.retrieveActiveSurveys();
        return activeSurveys.stream().filter(survey -> surveyName.equals(survey.getName())).findFirst()
                .orElseThrow(() -> new RuntimeException("Created survey not found: " + surveyName));
    }

    private void verifyCreatedSurveyExists(List<SurveyData> surveys, String surveyName) {
        boolean foundSurvey = surveys.stream().anyMatch(survey -> surveyName.equals(survey.getName()));
        assertThat(foundSurvey).isTrue();
    }

    private void verifySurveyProperties(SurveyData survey, String expectedName, String expectedDescription, LocalDate expectedValidFrom,
            LocalDate expectedValidTo, int expectedQuestionCount) {
        assertThat(surveyHelper.getSurveyName(survey)).isEqualTo(expectedName);
        assertThat(surveyHelper.getSurveyDescription(survey)).isEqualTo(expectedDescription);
        assertThat(surveyHelper.getSurveyValidFrom(survey)).isEqualTo(expectedValidFrom);
        assertThat(surveyHelper.getSurveyValidTo(survey)).isEqualTo(expectedValidTo);
        assertThat(surveyHelper.getSurveyQuestionsCount(survey)).isEqualTo(expectedQuestionCount);
        assertThat(surveyHelper.getSurveyCountryCode(survey)).isEqualTo(KENYAN_COUNTRY_CODE);
        assertThat(surveyHelper.getSurveyKey(survey)).isNotNull();
    }

    private String buildLongDescription() {
        return "This is a very long description for testing purposes. "
                + "It contains multiple sentences to ensure that the system can handle longer text inputs "
                + "without any issues. The description should be properly stored and retrieved without "
                + "any truncation or encoding problems.";
    }

    private String buildLongQuestion(String testCaseDescription) {
        return "This is a very long question that tests the system's ability to handle longer text inputs "
                + "without any issues. The question should be properly stored and retrieved for " + testCaseDescription + ".";
    }
}
