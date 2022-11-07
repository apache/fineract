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
package org.apache.fineract.cob.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.cob.data.BusinessStep;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessStepConfigDataParser {

    private final FromJsonHelper jsonHelper;

    public List<BusinessStep> parseUpdate(JsonCommand command) {
        JsonObject element = extractJsonObject(command);
        List<BusinessStep> businessSteps = new ArrayList<>();
        JsonArray jsonArray = jsonHelper.extractJsonArrayNamed("businessSteps", element);
        for (final JsonElement businessStepConfig : jsonArray) {
            final String stepName = jsonHelper.extractStringNamed("stepName", businessStepConfig);
            final Long order = jsonHelper.extractLongNamed("order", businessStepConfig);
            BusinessStep businessStep = new BusinessStep();
            businessStep.setStepName(stepName);
            businessStep.setOrder(order);
            businessSteps.add(businessStep);
        }
        return businessSteps;
    }

    private JsonObject extractJsonObject(JsonCommand command) {
        String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final JsonElement element = jsonHelper.parse(json);
        return element.getAsJsonObject();
    }
}
