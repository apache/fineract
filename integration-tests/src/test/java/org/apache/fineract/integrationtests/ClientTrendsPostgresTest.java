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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.integrationtests.common.FineractClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.report.ReportHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientTrendsPostgresTest {

    private ReportHelper reportHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.reportHelper = new ReportHelper(Utils.getRequestSpecification(), Utils.getResponseSpecification());
    }

    @Test
    public void testClientTrendsReportsPostgresSyntax() throws IOException {
        Map<String, String> reportParameters = new HashMap<>();
        reportParameters.put("officeId", "1");

        var reportsRunApi = FineractClientHelper.getFineractClient().reportsRun;

        assertTrue(reportsRunApi.runReportGetFile("ClientTrendsByWeek", reportParameters, false).execute().isSuccessful(),
                "Postgres Weekly Report SQL syntax is invalid!");

        assertTrue(reportsRunApi.runReportGetFile("ClientTrendsByMonth", reportParameters, false).execute().isSuccessful(),
                "Postgres Monthly Report SQL syntax is invalid!");
    }
}
