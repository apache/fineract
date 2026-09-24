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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PostGlClosuresRequest;
import org.apache.fineract.client.models.PostOfficesRequest;
import org.apache.fineract.client.models.PostOfficesResponse;
import org.apache.fineract.test.helper.Utils;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;

@RequiredArgsConstructor
public class OfficeStepDef extends AbstractStepDef {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);

    private final FineractFeignClient fineractClient;

    @When("Admin creates a new office")
    public void createNewOffice() {
        final PostOfficesRequest request = new PostOfficesRequest()//
                .name(Utils.randomStringGenerator("Office_", 5))//
                .parentId(1L)//
                .openingDate(LocalDate.of(2000, 1, 1))//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");//

        final PostOfficesResponse response = ok(() -> fineractClient.offices().createOffice(request));
        testContext().set(TestContextKey.OFFICE_CREATE_RESPONSE, response);
    }

    @When("Admin closes accounting for the last created office on {string}")
    public void closeAccountingForLastCreatedOffice(final String closingDate) {
        final PostOfficesResponse office = testContext().get(TestContextKey.OFFICE_CREATE_RESPONSE);
        assertThat(office).as("No office was created. Use 'Admin creates a new office' step first.").isNotNull();
        final PostGlClosuresRequest request = new PostGlClosuresRequest()//
                .officeId(office.getOfficeId())//
                .closingDate(LocalDate.parse(closingDate, FORMATTER))//
                .comments("Accounting closure")//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");//

        ok(() -> fineractClient.accountingClosure().createGLClosure(request));
    }
}
