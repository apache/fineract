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
package org.apache.fineract.accounting.provisioning.serialization;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression test: a missing {@code locale} used to be reported under the {@code dateFormat} parameter name instead of
 * {@code locale}, because the validator reused the wrong constant when building the validation error.
 */
class ProvisioningEntriesDefinitionJsonDeserializerTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private ProvisioningEntriesDefinitionJsonDeserializer deserializer;

    @BeforeEach
    void setUp() {
        deserializer = new ProvisioningEntriesDefinitionJsonDeserializer(fromJsonHelper);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2024, 1, 1))));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void validateForCreateWithoutLocaleReportsErrorForLocaleParameter() {
        String json = """
                {
                  "dateFormat": "dd MMMM yyyy"
                }
                """;

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> deserializer.validateForCreate(json));

        assertTrue(ex.getErrors().stream().anyMatch(e -> "locale".equals(e.getParameterName())),
                "Expected validation error for parameter 'locale'");
    }
}
