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
package org.apache.fineract.portfolio.collateral.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.collateral.api.CollateralApiConstants.COLLATERAL_JSON_INPUT_PARAMS;
import org.apache.fineract.portfolio.collateral.command.CollateralCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * Implementation of {@link FromApiJsonDeserializer} for
 * {@link CollateralCommand}'s.
 */
@Component
public final class CollateralCommandFromApiJsonDeserializer extends AbstractFromApiJsonDeserializer<CollateralCommand> {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public CollateralCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CollateralCommand commandFromApiJson(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final Set<String> supportedParameters = COLLATERAL_JSON_INPUT_PARAMS.getAllValues();
        supportedParameters.add("locale");
        supportedParameters.add("dateFormat");
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

        final Long collateralTypeId = this.fromApiJsonHelper.extractLongNamed(COLLATERAL_JSON_INPUT_PARAMS.COLLATERAL_TYPE_ID.getValue(),
                element);
        final String description = this.fromApiJsonHelper.extractStringNamed(COLLATERAL_JSON_INPUT_PARAMS.DESCRIPTION.getValue(), element);
        final BigDecimal value = this.fromApiJsonHelper.extractBigDecimalNamed(COLLATERAL_JSON_INPUT_PARAMS.VALUE.getValue(), element,
                locale);

        return new CollateralCommand(collateralTypeId, value, description);
    }
}