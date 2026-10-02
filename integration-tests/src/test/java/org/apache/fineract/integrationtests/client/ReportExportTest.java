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

import feign.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.fineract.integrationtests.CIOnly;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for /runreports/ API.
 *
 * @author Michael Vorburger.ch
 */
public class ReportExportTest extends FeignIntegrationTest {

    @Test
    void runClientListingTableReportCSV() throws IOException {
        try (Response result = fineractClient().runReports().runReportGetFile("Client Listing",
                Map.of("R_officeId", "1", "exportCSV", "true")); InputStream body = result.body().asInputStream()) {
            assertThat(String.join(",", result.headers().get("Content-Type"))).isEqualTo("text/csv");
            assertThat(new String(body.readAllBytes(), StandardCharsets.UTF_8)).contains("Office/Branch");
        }
    }

    @Test
    @CIOnly
    void runClientListingTableReportS3() {
        try (Response result = fineractClient().runReports().runReportGetFile("Client Listing",
                Map.of("R_officeId", "1", "exportS3", "true"))) {
            assertThat(result.status()).isEqualTo(204);
        }
    }

}
