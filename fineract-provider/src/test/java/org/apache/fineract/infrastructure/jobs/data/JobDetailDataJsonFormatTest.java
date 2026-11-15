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
package org.apache.fineract.infrastructure.jobs.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.apache.fineract.infrastructure.core.jersey.JerseyJacksonConverterConfig;
import org.junit.jupiter.api.Test;

/**
 * The job responses used to be written by GSON, which formats an {@link Instant} with ISO_INSTANT. The Jersey mapper
 * writes dates as timestamps, so without the field level format these would turn into decimal epoch seconds.
 */
class JobDetailDataJsonFormatTest {

    private static final Instant TIMESTAMP = Instant.parse("2026-11-28T10:15:30.123Z");

    private final ObjectMapper objectMapper = new JerseyJacksonConverterConfig().objectMapper(List.of(), List.of(), List.of());

    @Test
    void jobDetailWritesInstantsAsIsoStrings() {
        final JobDetailData data = new JobDetailData(1L, "Job", "JOB", TIMESTAMP, null, "0 0 * * * ?", true, false, 2L, TIMESTAMP,
                TIMESTAMP, "success", null, "cron", null);

        final JsonNode json = objectMapper.valueToTree(data);

        assertThat(json.get("nextRunTime").asText()).isEqualTo("2026-11-28T10:15:30.123Z");
        assertThat(json.get("lastRunHistory").get("jobRunStartTime").asText()).isEqualTo("2026-11-28T10:15:30.123Z");
        assertThat(json.get("lastRunHistory").get("jobRunEndTime").asText()).isEqualTo("2026-11-28T10:15:30.123Z");
    }

    @Test
    void jobHistoryWritesInstantsAsIsoStrings() {
        final JobDetailHistoryData data = new JobDetailHistoryData().setId(1L).setJobRunStartTime(TIMESTAMP).setJobRunEndTime(TIMESTAMP);

        final JsonNode json = objectMapper.valueToTree(data);

        assertThat(json.get("jobRunStartTime").isTextual()).isTrue();
        assertThat(json.get("jobRunStartTime").asText()).isEqualTo("2026-11-28T10:15:30.123Z");
        assertThat(json.get("jobRunEndTime").asText()).isEqualTo("2026-11-28T10:15:30.123Z");
    }
}
