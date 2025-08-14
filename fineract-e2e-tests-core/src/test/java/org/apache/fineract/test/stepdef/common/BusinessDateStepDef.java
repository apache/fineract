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
package org.apache.fineract.test.stepdef.common;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.BusinessDateUpdateRequest;
import org.apache.fineract.client.services.BusinessDateManagementApi;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.test.helper.BusinessDateHelper;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.helper.ErrorMessageHelper;
import org.apache.fineract.test.helper.ErrorResponse;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class BusinessDateStepDef extends AbstractStepDef {

    @Autowired
    private BusinessDateHelper businessDateHelper;

    @Autowired
    private BusinessDateManagementApi businessDateManagementApi;

    private static final Gson GSON = new JSON().getGson();

    @When("Admin sets the business date to {string}")
    public void setBusinessDate(String businessDate) throws IOException {
        businessDateHelper.setBusinessDate(businessDate);
    }

    @When("Admin sets the business date to the actual date")
    public void setBusinessDateToday() throws IOException {
        businessDateHelper.setBusinessDateToday();
    }

    @Then("Admin checks that the business date is correctly set to {string}")
    public void checkBusinessDate(String businessDate) throws IOException {
        Response<BusinessDateResponse> businessDateResponse = businessDateManagementApi.getBusinessDate(BusinessDateHelper.BUSINESS_DATE)
                .execute();
        ErrorHelper.checkSuccessfulApiCall(businessDateResponse);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy");
        LocalDate localDate = LocalDate.parse(businessDate, formatter);

        assertThat(businessDateResponse.body().getDate()).isEqualTo(localDate);
    }

    @Then("Set incorrect business date value {string} outcomes with an error")
    public void setIncorrectBusinessDateFailure(String businessDate) throws IOException {
        BusinessDateUpdateRequest businessDateRequest = businessDateHelper.defaultBusinessDateRequest().date(businessDate);

        Response<BusinessDateResponse> businessDateRequestResponse = businessDateManagementApi.updateBusinessDate(null, businessDateRequest)
                .execute();
        final ErrorResponse errorDetails = ErrorResponse.from(businessDateRequestResponse);
        assertThat(errorDetails.getHttpStatusCode()).as(ErrorMessageHelper.setIncorrectBusinessDateFailure()).isEqualTo(400);
        assertThat(errorDetails.getSingleError().getDeveloperMessage()).isEqualTo(ErrorMessageHelper.setIncorrectBusinessDateFailure());
    }

    @Then("Set incorrect business date with empty value {string} outcomes with an error")
    public void setNullOrEmptyBusinessDateFailure(String businessDate) throws IOException {
        BusinessDateUpdateRequest businessDateRequest = businessDateHelper.defaultBusinessDateRequest();
        if (businessDate.equals("null")) {
            businessDateRequest.date(null);
        } else {
            businessDateRequest.date(businessDate);
        }
        Response<BusinessDateResponse> businessDateRequestResponse = businessDateManagementApi.updateBusinessDate(null, businessDateRequest)
                .execute();
        Integer httpStatusCodeExpected = 400;

        String errorBody = businessDateRequestResponse.errorBody().string();
        ErrorResponse errorResponse = GSON.fromJson(errorBody, ErrorResponse.class);
        Integer httpStatusCodeActual = errorResponse.getHttpStatusCode();
        List<String> developerMessagesActual = errorResponse.getErrors().stream().map(ErrorResponse.Error::getDeveloperMessage).toList();

        List<String> developerMessagesExpected = asList(ErrorMessageHelper.setIncorrectBusinessDateMandatoryFailure(),
                ErrorMessageHelper.setIncorrectBusinessDateFailure());

        assertThat(httpStatusCodeActual)
                .as(ErrorMessageHelper.wrongErrorCodeInFailedChargeAdjustment(httpStatusCodeActual, httpStatusCodeExpected))
                .isEqualTo(httpStatusCodeExpected);
        assertThat(developerMessagesActual)
                .as(ErrorMessageHelper.wrongErrorMessage(developerMessagesActual.toString(), developerMessagesExpected.toString()))
                .containsAll(developerMessagesExpected);
    }

}
