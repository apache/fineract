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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.paymentdetail.PaymentDetailConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountTransfersDataValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private AccountTransfersDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AccountTransfersDataValidator(fromJsonHelper, new AccountTransfersDetailDataValidator(fromJsonHelper));
    }

    @Test
    void validateWithoutPaymentDetailsStillSucceeds() {
        assertDoesNotThrow(() -> validator.validate(command(baseTransferJson())));
    }

    @Test
    void validateWithPaymentDetailsSucceeds() {
        assertDoesNotThrow(() -> validator.validate(command(baseTransferJson("""
                ,
                  "paymentTypeId": 1,
                  "accountNumber": "ACC-123",
                  "checkNumber": "CHK-123",
                  "routingCode": "RT-123",
                  "receiptNumber": "RC-123",
                  "bankNumber": "BNK-123"
                """))));
    }

    @Test
    void validateRejectsPaymentDetailFieldsWithoutPaymentType() {
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validate(command(baseTransferJson("""
                        ,
                          "accountNumber": "ACC-123"
                        """))));

        assertTrue(exception.getErrors().stream()
                .anyMatch(error -> PaymentDetailConstants.paymentTypeParamName.equals(error.getParameterName())));
    }

    @Test
    void validateRejectsPaymentDetailFieldsExceedingLength() {
        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validate(command(baseTransferJson("""
                        ,
                          "paymentTypeId": 1,
                          "accountNumber": "123456789012345678901234567890123456789012345678901"
                        """))));

        assertTrue(exception.getErrors().stream()
                .anyMatch(error -> PaymentDetailConstants.accountNumberParamName.equals(error.getParameterName())));
    }

    private JsonCommand command(final String json) {
        return new JsonCommand(1L, fromJsonHelper.parse(json), fromJsonHelper);
    }

    private String baseTransferJson() {
        return baseTransferJson("");
    }

    private String baseTransferJson(final String additionalProperties) {
        return """
                {
                  "dateFormat": "dd MMMM yyyy",
                  "locale": "en",
                  "fromOfficeId": 1,
                  "fromClientId": 1,
                  "fromAccountType": 2,
                  "fromAccountId": 1,
                  "toOfficeId": 1,
                  "toClientId": 1,
                  "toAccountType": 2,
                  "toAccountId": 2,
                  "transferDate": "01 March 2026",
                  "transferAmount": "100.0",
                  "transferDescription": "Transfer"
                  %s
                }
                """.formatted(additionalProperties);
    }
}
