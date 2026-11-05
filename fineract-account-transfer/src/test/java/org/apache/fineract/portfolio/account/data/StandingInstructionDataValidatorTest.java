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
package org.apache.fineract.portfolio.account.data;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.account.AccountDetailConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the NullPointerException thrown when the mandatory {@code transferType} parameter is omitted
 * from standing instruction create requests. The validator queued a {@code notNull} validation error but passed the
 * null value to {@code AccountTransferType.fromInt} before throwing, turning a client error into an HTTP 500.
 */
class StandingInstructionDataValidatorTest {

    private FromJsonHelper fromJsonHelper;
    private StandingInstructionDataValidator validator;

    @BeforeEach
    void setUp() {
        fromJsonHelper = new FromJsonHelper();
        validator = new StandingInstructionDataValidator(fromJsonHelper, new AccountTransfersDetailDataValidator(fromJsonHelper));
    }

    private JsonCommand jsonCommand(String json) {
        return new JsonCommand(1L, fromJsonHelper.parse(json), fromJsonHelper);
    }

    @Test
    void validateForCreateWithoutTransferTypeReportsValidationErrorInsteadOfNpe() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForCreate(jsonCommand("{}")));

        assertTrue(ex.getErrors().stream().anyMatch(e -> AccountDetailConstants.transferTypeParamName.equals(e.getParameterName())),
                "Expected validation error for parameter 'transferType'");
    }

    @Test
    void validateForCreateWithLoanAccountForAccountTransferReportsTransferTypeError() {
        String json = """
                {
                  "fromOfficeId": 1,
                  "fromClientId": 1,
                  "fromAccountId": 1,
                  "fromAccountType": 1,
                  "toOfficeId": 1,
                  "toClientId": 1,
                  "toAccountId": 2,
                  "toAccountType": 2,
                  "transferType": 1,
                  "priority": 1,
                  "status": 1,
                  "instructionType": 1,
                  "recurrenceType": 1,
                  "name": "test instruction",
                  "validFrom": "01 January 2026",
                  "locale": "en",
                  "dateFormat": "dd MMMM yyyy"
                }
                """;

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForCreate(jsonCommand(json)));

        assertTrue(ex.getErrors().stream().anyMatch(e -> AccountDetailConstants.transferTypeParamName.equals(e.getParameterName())),
                "Expected validation error for parameter 'transferType'");
    }
}
