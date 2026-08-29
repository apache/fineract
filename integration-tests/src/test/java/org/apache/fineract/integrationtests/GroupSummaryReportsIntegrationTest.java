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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignReportHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroupSummaryReportsIntegrationTest extends FeignIntegrationTest {

    private FeignGroupHelper groupHelper;
    private FeignReportHelper reportHelper;

    @BeforeEach
    public void setup() {
        groupHelper = new FeignGroupHelper(fineractClient());
        reportHelper = new FeignReportHelper(fineractClient());
    }

    @Test
    public void shouldRunGroupSummaryReportsWithNumericGroupIdParameter() {
        final Long groupId = groupHelper.createActiveGroup().getGroupId();
        assertNotNull(groupId);

        assertGroupSummaryReportRuns("GroupSummaryCounts", groupId);
        assertGroupSummaryReportRuns("GroupSummaryAmounts", groupId);
        assertGroupSummaryReportRuns("GroupSavingSummary", groupId);
    }

    /**
     * The reports compare {@code R_groupId} against a {@code bigint} column, so the parameter has to be registered as
     * numeric (FINERACT-2691). Registered as a string, PostgreSQL rejects the comparison with "operator does not exist:
     * bigint = character varying" and the report never produces rows - so running it is the assertion. The error
     * surfaces either as a failure status or as a body that is not the array of rows; neither decodes here.
     */
    private void assertGroupSummaryReportRuns(final String reportName, final Long groupId) {
        final List<Map<String, Object>> rows = reportHelper.runReportRows(reportName, Map.of("R_groupId", groupId.toString()));

        assertNotNull(rows, reportName + " returned no result set");
    }
}
