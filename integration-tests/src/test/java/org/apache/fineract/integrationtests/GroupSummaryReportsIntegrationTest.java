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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Locale;
import org.apache.fineract.integrationtests.common.GroupHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupSummaryReportsIntegrationTest extends BaseLoanIntegrationTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    @BeforeEach
    public void setup() {
        Locale.setDefault(Locale.ENGLISH);
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
    }

    @Test
    public void shouldRunGroupSummaryReportsWithNumericGroupIdParameter() {
        final Integer groupId = GroupHelper.createGroup(this.requestSpec, this.responseSpec, true);
        assertNotNull(groupId);

        assertGroupSummaryReportRuns("GroupSummaryCounts", groupId);
        assertGroupSummaryReportRuns("GroupSummaryAmounts", groupId);
        assertGroupSummaryReportRuns("GroupSavingSummary", groupId);
    }

    private void assertGroupSummaryReportRuns(final String reportName, final Integer groupId) {
        final String response = Utils.performServerGet(this.requestSpec, this.responseSpec, "/fineract-provider/api/v1/runreports/"
                + reportName + "?R_groupId=" + groupId + "&genericResultSet=false&" + Utils.TENANT_IDENTIFIER, null);

        assertNotNull(response);
        assertFalse(response.toLowerCase(Locale.ROOT).contains("operator does not exist"));
    }
}
