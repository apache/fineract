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
package org.apache.fineract.infrastructure.campaigns.sms.serialization;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.infrastructure.campaigns.sms.constants.SmsCampaignTriggerType;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the NullPointerException thrown when the mandatory {@code triggerType} parameter is omitted from
 * SMS campaign create/update requests. The validator queued a {@code notNull} validation error but dereferenced the
 * value before throwing, turning a client error into an HTTP 500.
 */
class SmsCampaignValidatorTest {

    private SmsCampaignValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SmsCampaignValidator(new FromJsonHelper());
    }

    @Test
    void validateCreateWithoutTriggerTypeReportsValidationErrorInsteadOfNpe() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateCreate("{}"));

        assertTrue(ex.getErrors().stream().anyMatch(e -> SmsCampaignValidator.triggerType.equals(e.getParameterName())),
                "Expected validation error for parameter 'triggerType'");
    }

    @Test
    void validateForUpdateWithoutTriggerTypeReportsValidationErrorInsteadOfNpe() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForUpdate("{}"));

        assertTrue(ex.getErrors().stream().anyMatch(e -> SmsCampaignValidator.triggerType.equals(e.getParameterName())),
                "Expected validation error for parameter 'triggerType'");
    }

    @Test
    void validateCreateWithScheduleTriggerTypeStillRequiresRecurrenceDetails() {
        String json = """
                {
                  "campaignName": "Repayment reminders",
                  "campaignType": 1,
                  "triggerType": TRIGGER_TYPE,
                  "runReportId": 1,
                  "message": "Your repayment is due",
                  "locale": "en"
                }
                """.replace("TRIGGER_TYPE", String.valueOf(SmsCampaignTriggerType.SCHEDULE.getValue()));

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateCreate(json));

        assertTrue(ex.getErrors().stream().anyMatch(e -> SmsCampaignValidator.recurrenceStartDate.equals(e.getParameterName())),
                "Expected validation error for parameter 'recurrenceStartDate'");
    }
}
