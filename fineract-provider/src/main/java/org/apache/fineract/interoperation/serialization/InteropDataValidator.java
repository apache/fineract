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
package org.apache.fineract.interoperation.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.interoperation.data.InteropIdentifierRequestData;
import org.apache.fineract.interoperation.data.InteropQuoteRequestData;
import org.apache.fineract.interoperation.data.InteropTransactionRequestData;
import org.apache.fineract.interoperation.data.InteropTransferRequestData;
import org.apache.fineract.interoperation.domain.InteropIdentifierType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InteropDataValidator {

    private final FromJsonHelper jsonHelper;

    @Autowired
    public InteropDataValidator(final FromJsonHelper fromJsonHelper) {
        this.jsonHelper = fromJsonHelper;
    }

    public InteropIdentifierRequestData validateAndParseCreateIdentifier(@NotNull InteropIdentifierType idType, @NotNull String idValue,
                                                                          String subIdOrType, JsonCommand command) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("interoperation.identifier");
        JsonObject element = extractJsonObject(command);

        InteropIdentifierRequestData result = InteropIdentifierRequestData.validateAndParse(dataValidator, idType, idValue, subIdOrType, element, jsonHelper);
        throwExceptionIfValidationWarningsExist(dataValidator);

        return result;
    }

    public InteropTransactionRequestData validateAndParseCreateRequest(JsonCommand command) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("interoperation.request");
        JsonObject element = extractJsonObject(command);

        InteropTransactionRequestData result = InteropTransactionRequestData.validateAndParse(dataValidator, element, jsonHelper);
        throwExceptionIfValidationWarningsExist(dataValidator);

        return result;
    }

    public InteropQuoteRequestData validateAndParseCreateQuote(JsonCommand command) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("interoperation.quote");
        JsonObject element = extractJsonObject(command);

        InteropQuoteRequestData result = InteropQuoteRequestData.validateAndParse(dataValidator, element, jsonHelper);
        throwExceptionIfValidationWarningsExist(dataValidator);

        return result;
    }

    public InteropTransferRequestData validateAndParsePrepareTransfer(JsonCommand command) {
        return validateAndParseCreateTransfer(command);
    }

    public InteropTransferRequestData validateAndParseCreateTransfer(JsonCommand command) {
        final DataValidatorBuilder dataValidator = new DataValidatorBuilder(new ArrayList<>()).resource("interoperation.transfer");
        JsonObject element = extractJsonObject(command);

        InteropTransferRequestData result = InteropTransferRequestData.validateAndParse(dataValidator, element, jsonHelper);
        throwExceptionIfValidationWarningsExist(dataValidator);

        return result;
    }

    private JsonObject extractJsonObject(JsonCommand command) {
        String json = command.json();
        if (StringUtils.isBlank(json))
            throw new InvalidJsonException();

        final JsonElement element = jsonHelper.parse(json);
        return element.getAsJsonObject();
    }

    private void throwExceptionIfValidationWarningsExist(DataValidatorBuilder dataValidator) {
        if (dataValidator.hasError()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                    "Validation errors exist.", dataValidator.getDataValidationErrors());
        }
    }
}
