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
package org.apache.fineract.commands.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.RecoverableError;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecoverableErrorClassifier {

    private final ConfigurationDomainService configurationDomainService;

    public boolean isRecoverable(RuntimeException exception) {
        if (exception instanceof RecoverableError) {
            return true;
        }
        if (exception instanceof PlatformApiDataValidationException validationException) {
            return allErrorCodesRecoverable(validationException.getErrors());
        }
        if (exception instanceof AbstractPlatformException platformException) {
            return isRecoverableCode(recoverableCodes(), platformException.getGlobalisationMessageCode());
        }
        return false;
    }

    private boolean allErrorCodesRecoverable(List<ApiParameterError> errors) {
        if (errors == null || errors.isEmpty()) {
            return false;
        }
        Set<String> recoverableCodes = recoverableCodes();
        return errors.stream().allMatch(error -> isRecoverableCode(recoverableCodes, error.getUserMessageGlobalisationCode()));
    }

    private static boolean isRecoverableCode(Set<String> recoverableCodes, String code) {
        return code != null && recoverableCodes.contains(code);
    }

    private Set<String> recoverableCodes() {
        return configurationDomainService.getIdempotencyRecoverableErrorCodes();
    }
}
