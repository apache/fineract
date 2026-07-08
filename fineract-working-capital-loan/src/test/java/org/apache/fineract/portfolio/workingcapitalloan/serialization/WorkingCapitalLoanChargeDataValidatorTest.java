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
package org.apache.fineract.portfolio.workingcapitalloan.serialization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.UnsupportedParameterException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorkingCapitalLoanChargeDataValidatorTest {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private WorkingCapitalLoanChargeDataValidator validator;
    private LocalDate businessDate;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        businessDate = LocalDate.now(ZoneId.systemDefault());
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, businessDate)));

        validator = new WorkingCapitalLoanChargeDataValidator(new FromJsonHelper());
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    void shouldThrowInvalidJsonWhenJsonIsBlank() {
        assertThrows(InvalidJsonException.class, () -> validator.validateChargeAdjustmentRequest(""));
    }

    @Test
    void shouldAcceptRequestWithoutTransactionDate() {
        final JsonObject json = baseRequest();
        assertDoesNotThrow(() -> validator.validateChargeAdjustmentRequest(json.toString()));
    }

    @Test
    void shouldRejectRequestContainingTransactionDate() {
        final JsonObject json = baseRequest();
        json.addProperty("transactionDate", DATE_FORMAT.format(businessDate));
        assertThrows(UnsupportedParameterException.class, () -> validator.validateChargeAdjustmentRequest(json.toString()));
    }

    private JsonObject baseRequest() {
        final JsonObject json = new JsonObject();
        json.addProperty("locale", "en");
        json.addProperty("dateFormat", "dd MMMM yyyy");
        json.addProperty("amount", "40");
        return json;
    }
}
