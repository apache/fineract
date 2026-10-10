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
package org.apache.fineract.infrastructure.dataqueries.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EntityDatatableChecksDataValidatorTest {

    private final EntityDatatableChecksDataValidator underTest = new EntityDatatableChecksDataValidator(new FromJsonHelper());

    @ParameterizedTest
    @ValueSource(strings = { "m_center", "m_office", "m_product_loan", "m_savings_product", "m_savings_account_transaction",
            "m_share_product", "m_wc_loan_product" })
    void rejectsEntityWithoutCheckStatusesOnTheEntityParameter(String entity) {
        String json = "{\"entity\":\"" + entity + "\",\"status\":100,\"datatableName\":\"dt_check\"}";

        PlatformApiDataValidationException exception = assertThrows(PlatformApiDataValidationException.class,
                () -> underTest.validateForCreate(json));

        List<ApiParameterError> errors = exception.getErrors();
        assertEquals(1, errors.size());
        assertEquals("entity", errors.get(0).getParameterName());
        assertEquals("validation.msg.entityDatatableChecks.entity.is.not.one.of.expected.enumerations",
                errors.get(0).getUserMessageGlobalisationCode());
    }

    @ParameterizedTest
    @ValueSource(strings = { "m_client", "m_group", "m_loan", "m_savings_account", "m_wc_loan" })
    void acceptsCreateStatusForEntityWithChecks(String entity) {
        String json = "{\"entity\":\"" + entity + "\",\"status\":100,\"datatableName\":\"dt_check\"}";

        assertDoesNotThrow(() -> underTest.validateForCreate(json));
    }
}
