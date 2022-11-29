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
package org.apache.fineract.infrastructure.jobs.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobParameterDataParser {

    private final FromJsonHelper jsonHelper;

    public Set<JobParameterDTO> parseExecution(String requestBody) {
        if (StringUtils.isBlank(requestBody)) {
            return Collections.emptySet();
        }
        JsonObject element = JsonParser.parseString(requestBody).getAsJsonObject();
        if (Objects.isNull(element)) {
            return Collections.emptySet();
        }
        JsonArray jobParametersJsonArray = jsonHelper.extractJsonArrayNamed("jobParameters", element);
        if (Objects.isNull(jobParametersJsonArray)) {
            return Collections.emptySet();
        }
        Set<JobParameterDTO> jobParameters = new HashSet<>();
        for (final JsonElement jobParameterElement : jobParametersJsonArray) {
            jobParameters.add(new JobParameterDTO(jobParameterElement.getAsJsonObject().get("parameterName").getAsString(),
                    jobParameterElement.getAsJsonObject().get("parameterValue").getAsString()));
        }
        return Collections.unmodifiableSet(jobParameters);
    }
}
