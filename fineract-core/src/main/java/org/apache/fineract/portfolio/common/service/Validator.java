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
package org.apache.fineract.portfolio.common.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;

public final class Validator {

    private Validator() {}

    public static void validateOrThrow(String resource, Consumer<DataValidatorBuilder> baseDataValidator) {
        final List<ApiParameterError> dataValidationErrors = getApiParameterErrors(resource, baseDataValidator);

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }

    public static void validateOrThrowDomainViolation(String resource, Consumer<DataValidatorBuilder> baseDataValidator) {
        final List<ApiParameterError> dataValidationErrors = getApiParameterErrors(resource, baseDataValidator);

        if (!dataValidationErrors.isEmpty()) {
            throw new GeneralPlatformDomainRuleException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors.toArray(new Object[0]));
        }
    }

    private static List<ApiParameterError> getApiParameterErrors(String resource, Consumer<DataValidatorBuilder> baseDataValidator) {
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder dataValidatorBuilder = new DataValidatorBuilder(dataValidationErrors).resource(resource);

        baseDataValidator.accept(dataValidatorBuilder);
        return dataValidationErrors;
    }
}
