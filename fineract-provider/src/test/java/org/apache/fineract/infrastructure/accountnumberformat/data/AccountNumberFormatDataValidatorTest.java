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
package org.apache.fineract.infrastructure.accountnumberformat.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatEnumerations.AccountNumberPrefixType;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression test: a blank {@code prefixCharacter} used to be reported under the {@code prefixType} parameter name
 * instead of {@code prefixCharacter}, because the validator reused the wrong constant when building the validation
 * error.
 */
class AccountNumberFormatDataValidatorTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private AccountNumberFormatDataValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AccountNumberFormatDataValidator(fromJsonHelper);
    }

    @Test
    void validateForCreateWithBlankPrefixCharacterReportsErrorForPrefixCharacterParameter() {
        String json = """
                {
                  "accountType": ACCOUNT_TYPE,
                  "prefixType": PREFIX_TYPE,
                  "prefixCharacter": ""
                }
                """.replace("ACCOUNT_TYPE", String.valueOf(EntityAccountType.CLIENT.getValue())).replace("PREFIX_TYPE",
                String.valueOf(AccountNumberPrefixType.PREFIX_SHORT_NAME.getValue()));

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForCreate(json));

        assertTrue(ex.getErrors().stream().anyMatch(e -> "prefixCharacter".equals(e.getParameterName())),
                "Expected validation error for parameter 'prefixCharacter'");
        assertEquals(1, ex.getErrors().size(), "Expected exactly one validation error");
    }
}
