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

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.services.BusinessDateManagementApi;
import org.apache.fineract.test.helper.BusinessDateHelper;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class BusinessDateStepDef extends AbstractStepDef {

    @Autowired
    private BusinessDateHelper businessDateHelper;

    @Autowired
    private BusinessDateManagementApi businessDateManagementApi;

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
}
