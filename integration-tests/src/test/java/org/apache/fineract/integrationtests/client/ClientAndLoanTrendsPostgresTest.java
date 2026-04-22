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
package org.apache.fineract.integrationtests.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

public class ClientAndLoanTrendsPostgresTest extends IntegrationTest {

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
    }

    @Test
    void testClientTrendsByWeekReportRunsSuccessfully() {
        Response<RunReportsResponse> response = okR(
                fineractClient().reportsRun.runReportGetData("ClientTrendsByWeek", Map.of("R_officeId", "1")));
        assertEquals(200, response.code());
    }

    @Test
    void testClientTrendsByMonthReportRunsSuccessfully() {
        Response<RunReportsResponse> response = okR(
                fineractClient().reportsRun.runReportGetData("ClientTrendsByMonth", Map.of("R_officeId", "1")));
        assertEquals(200, response.code());
    }

    @Test
    void testLoanTrendsByWeekReportRunsSuccessfully() {
        Response<RunReportsResponse> response = okR(
                fineractClient().reportsRun.runReportGetData("LoanTrendsByWeek", Map.of("R_officeId", "1")));
        assertEquals(200, response.code());
    }

    @Test
    void testLoanTrendsByMonthReportRunsSuccessfully() {
        Response<RunReportsResponse> response = okR(
                fineractClient().reportsRun.runReportGetData("LoanTrendsByMonth", Map.of("R_officeId", "1")));
        assertEquals(200, response.code());
    }

}
