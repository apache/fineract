
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
package org.apache.fineract.investor.serialization;

import com.google.gson.JsonElement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.validator.ParseAndValidator;
import org.apache.fineract.investor.data.ExternalTransferRequestParameters;
import org.apache.fineract.portfolio.common.service.Validator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExternalAssetOwnerValidator extends ParseAndValidator {

    private final FromJsonHelper fromApiJsonHelper;

    public void validateForCreate(JsonCommand command) {
        final String json = command.json();
        validateRequestBody(json);
        validateForSupportedParameters(json, List.of(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID), fromApiJsonHelper);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        Validator.validateOrThrow("externalAssetOwner", baseDataValidator -> {
            final String ownerExternalId = this.fromApiJsonHelper.extractStringNamed(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID,
                    element);
            baseDataValidator.reset().parameter(ExternalTransferRequestParameters.OWNER_EXTERNAL_ID).value(ownerExternalId).notNull();
        });
    }

}
