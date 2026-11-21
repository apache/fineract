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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.RecoverableError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecoverableErrorClassifierTest {

    private static final String CLIENT_ACTIVATION_IN_FUTURE = "error.msg.clients.activationDate.in.the.future";
    private static final String GROUP_ACTIVATION_IN_FUTURE = "error.msg.group.activationDate.in.the.future";
    private static final String CLIENT_SUBMITTED_IN_FUTURE = "error.msg.clients.submittedOnDate.in.the.future";
    private static final String VALIDATION_WRAPPER = "validation.msg.validation.errors.exist";
    private static final Set<String> DEFAULT_CODES = Set.of(CLIENT_ACTIVATION_IN_FUTURE, GROUP_ACTIVATION_IN_FUTURE);

    @Mock
    private ConfigurationDomainService configurationDomainService;

    private RecoverableErrorClassifier underTest;

    @BeforeEach
    void setUp() {
        underTest = new RecoverableErrorClassifier(configurationDomainService);
    }

    @Test
    void markerInterface_isRecoverableEvenWithoutConfiguredCodes() {
        assertTrue(underTest.isRecoverable(new TransientRecoverableException()));
    }

    @Test
    void validationErrorWithOnlyConfiguredCode_isRecoverable() {
        when(configurationDomainService.getIdempotencyRecoverableErrorCodes()).thenReturn(DEFAULT_CODES);

        assertTrue(underTest.isRecoverable(validationError(CLIENT_ACTIVATION_IN_FUTURE)));
    }

    @Test
    void validationErrorWhoseEntriesAreAllConfigured_isRecoverable() {
        when(configurationDomainService.getIdempotencyRecoverableErrorCodes()).thenReturn(DEFAULT_CODES);

        assertTrue(underTest.isRecoverable(validationError(CLIENT_ACTIVATION_IN_FUTURE, GROUP_ACTIVATION_IN_FUTURE)));
    }

    @Test
    void validationErrorWithMixedCodes_isNotRecoverable() {
        when(configurationDomainService.getIdempotencyRecoverableErrorCodes()).thenReturn(DEFAULT_CODES);

        assertFalse(underTest.isRecoverable(validationError(CLIENT_ACTIVATION_IN_FUTURE, CLIENT_SUBMITTED_IN_FUTURE)));
    }

    @Test
    void settingDisabled_validationErrorIsNotRecoverable() {
        when(configurationDomainService.getIdempotencyRecoverableErrorCodes()).thenReturn(Set.of());

        assertFalse(underTest.isRecoverable(validationError(CLIENT_ACTIVATION_IN_FUTURE)));
    }

    @Test
    void domainRuleExceptionWithConfiguredCode_isRecoverable() {
        when(configurationDomainService.getIdempotencyRecoverableErrorCodes()).thenReturn(Set.of("error.msg.x"));

        assertTrue(underTest.isRecoverable(new GeneralPlatformDomainRuleException("error.msg.x", "msg")));
    }

    @Test
    void domainRuleExceptionWithUnlistedCode_isNotRecoverable() {
        when(configurationDomainService.getIdempotencyRecoverableErrorCodes()).thenReturn(DEFAULT_CODES);

        assertFalse(underTest.isRecoverable(new GeneralPlatformDomainRuleException("error.msg.x", "msg")));
    }

    @Test
    void unrelatedException_isNotRecoverable() {
        assertFalse(underTest.isRecoverable(new IllegalStateException()));
        assertFalse(underTest.isRecoverable(new RuntimeException(CLIENT_ACTIVATION_IN_FUTURE)));
    }

    @Test
    void validationErrorWithoutEntries_isNotRecoverable() {
        assertFalse(underTest.isRecoverable(new PlatformApiDataValidationException(List.of())));
    }

    @Test
    void validationErrorTopLevelCodeIsIgnored_entriesDecide() {
        when(configurationDomainService.getIdempotencyRecoverableErrorCodes()).thenReturn(Set.of(VALIDATION_WRAPPER));

        assertFalse(underTest.isRecoverable(validationError(CLIENT_SUBMITTED_IN_FUTURE)));
    }

    private static PlatformApiDataValidationException validationError(String... codes) {
        List<ApiParameterError> errors = Arrays.stream(codes)
                .map(code -> ApiParameterError.parameterError(code, "message for " + code, "activationDate")).toList();
        return new PlatformApiDataValidationException(errors);
    }

    private static final class TransientRecoverableException extends RuntimeException implements RecoverableError {}
}
