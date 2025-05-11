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
package org.apache.fineract.test.testrail;

import io.cucumber.java.Scenario;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.HttpStatus;
import org.junit.runner.Result;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@Slf4j
@Component
@RequiredArgsConstructor
@Conditional(TestRailEnabledCondition.class)
public class TestRailClient implements InitializingBean {

    public static final Pattern TESTRAIL_TAG_PATTERN = Pattern.compile("@TestRailId:C([0-9]+)");
    private static final String SUCCESS_COMMENT = "Test passed";
    private static final String FAILED_COMMENT = "Test failed";

    private final TestRailProperties testRailProperties;
    private final TestRailApiClient apiClient;

    @Override
    public void afterPropertiesSet() {
        log.warn("Configured TestRail Run ID: '{}'", testRailProperties.getRunId());
    }

    public void saveScenarioResult(Scenario scenario) {
        Integer caseId = getScenarioCaseId(scenario);

        AddResultForCaseRequest request;
        if (scenario.isFailed()) {
            request = createFailedRequest(scenario);
        } else {
            request = createSuccessRequest(scenario);
        }

        if (caseId != null) {
            try {
                Response<Void> response = apiClient.addResultForCase(testRailProperties.getRunId(), caseId, request).execute();
                if (response.code() != HttpStatus.SC_OK) {
                    handleApiError(response);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error while reporting to TestRail", e);
            }
        }
    }

    private Integer getScenarioCaseId(Scenario scenario) {
        Integer caseId = null;
        for (String s : scenario.getSourceTagNames()) {
            if (s.contains("TestRail")) {
                Matcher matcher = TESTRAIL_TAG_PATTERN.matcher(s);
                if (matcher.matches()) {
                    caseId = Integer.parseInt(matcher.group(1));
                }
            }
        }
        return caseId;
    }

    private void handleApiError(Response<Void> response) throws IOException {
        String exceptionMsg = "Error while reporting to TestRail";
        ResponseBody errorBody = response.errorBody();
        if (errorBody != null) {
            exceptionMsg += " " + errorBody.string();
        }
        throw new RuntimeException(exceptionMsg);
    }

    private AddResultForCaseRequest createFailedRequest(Scenario scenario) {
        return new AddResultForCaseRequest.AddResultForCaseRequestBuilder().statusId(TestRailStatus.FAILED)
                .comment(createFailedComment(scenario)).build();
    }

    private AddResultForCaseRequest createSuccessRequest(Scenario scenario) {
        return new AddResultForCaseRequest.AddResultForCaseRequestBuilder().statusId(TestRailStatus.PASSED)
                .comment(createSuccessComment(scenario)).build();
    }

    private String createSuccessComment(Scenario scenario) {
        return SUCCESS_COMMENT;
    }

    private String createFailedComment(Scenario scenario) {
        try {
            Class c = ClassUtils.getClass("cucumber.runtime.java.JavaHookDefinition$ScenarioAdaptor");
            Field fieldScenario = FieldUtils.getField(c, "scenario", true);
            if (fieldScenario != null) {

                fieldScenario.setAccessible(true);
                Object objectScenario = fieldScenario.get(scenario);

                Field fieldStepResults = objectScenario.getClass().getDeclaredField("stepResults");
                fieldStepResults.setAccessible(true);

                ArrayList<Result> results = (ArrayList<Result>) fieldStepResults.get(objectScenario);
                for (Result result : results) {
                    if (result.getFailures() != null) {
                        return FAILED_COMMENT + "%n" + result.getFailures();
                    }
                }
            }

            return FAILED_COMMENT;

        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            return FAILED_COMMENT;
        }
    }
}
