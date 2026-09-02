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
package org.apache.fineract.infrastructure.campaigns.email.data;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.infrastructure.campaigns.email.domain.EmailCampaignType;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Regression tests for the NullPointerException thrown when the mandatory {@code campaignType} parameter is omitted
 * from email campaign create/update requests. The validator queued a {@code notNull} validation error but dereferenced
 * the value before throwing, turning a client error into an HTTP 500.
 */
class EmailCampaignValidatorTest {

    private EmailCampaignValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailCampaignValidator(new FromJsonHelper());
    }

    @Test
    void validateCreateWithoutCampaignTypeReportsValidationErrorInsteadOfNpe() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateCreate("{}"));

        assertTrue(ex.getErrors().stream().anyMatch(e -> EmailCampaignValidator.campaignType.equals(e.getParameterName())),
                "Expected validation error for parameter 'campaignType'");
    }

    @Test
    void validateForUpdateWithoutCampaignTypeReportsValidationErrorInsteadOfNpe() {
        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateForUpdate("{}"));

        assertTrue(ex.getErrors().stream().anyMatch(e -> EmailCampaignValidator.campaignType.equals(e.getParameterName())),
                "Expected validation error for parameter 'campaignType'");
    }

    @Test
    void validateCreateWithScheduleCampaignTypeStillRequiresRecurrenceDetails() {
        String json = """
                {
                  "campaignName": "Loan reminders",
                  "campaignType": CAMPAIGN_TYPE,
                  "businessRuleId": 1,
                  "emailSubject": "Reminder",
                  "emailMessage": "Your repayment is due",
                  "paramValue": "value"
                }
                """.replace("CAMPAIGN_TYPE", String.valueOf(EmailCampaignType.SCHEDULE.getValue()));

        PlatformApiDataValidationException ex = assertThrows(PlatformApiDataValidationException.class,
                () -> validator.validateCreate(json));

        assertTrue(ex.getErrors().stream().anyMatch(e -> EmailCampaignValidator.recurrenceStartDate.equals(e.getParameterName())),
                "Expected validation error for parameter 'recurrenceStartDate'");
    }
}
