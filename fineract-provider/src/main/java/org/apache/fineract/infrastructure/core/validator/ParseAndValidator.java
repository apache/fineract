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
package org.apache.fineract.infrastructure.core.validator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;

@Slf4j
public class ParseAndValidator {

    protected JsonObject extractJsonObject(final FromJsonHelper jsonHelper, final JsonCommand command) {
        String json = command.json();
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final JsonElement element = jsonHelper.parse(json);
        return element.getAsJsonObject();
    }

    protected void throwExceptionIfValidationWarningsExist(DataValidatorBuilder dataValidator) {
        if (dataValidator.hasError()) {
            log.error("Validation errors: {}", dataValidator.getDataValidationErrors());
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidator.getDataValidationErrors());
        }
    }
}
