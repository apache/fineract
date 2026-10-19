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
package org.apache.fineract.infrastructure.configuration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class GlobalConfigurationPropertyUpdateServiceTest {

    private final FromJsonHelper fromJsonHelper = new FromJsonHelper();
    private final GlobalConfigurationPropertyUpdateService underTest = new GlobalConfigurationPropertyUpdateService(
            mock(JdbcTemplate.class));

    @Test
    void storesCanonicalSpellingOfAllowedValue() {
        final GlobalConfigurationProperty property = property(GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME, "eod");

        final Map<String, Object> changes = underTest.update(property, stringValueCommand("REAL-TIME"));

        assertEquals("real-time", property.getStringValue());
        assertEquals(Map.of("stringValue", "real-time"), changes);
    }

    @Test
    void doesNotReportChangeWhenOnlyCaseDiffers() {
        final GlobalConfigurationProperty property = property(GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME, "eod");

        final Map<String, Object> changes = underTest.update(property, stringValueCommand("EOD"));

        assertEquals("eod", property.getStringValue());
        assertTrue(changes.isEmpty());
    }

    @Test
    void rewritesStoredLegacySpellingWhenSameValueIsSent() {
        final GlobalConfigurationProperty property = property(GlobalConfigurationConstants.WCL_CHARGE_ACCRUAL_TIME, "EOD");

        final Map<String, Object> changes = underTest.update(property, stringValueCommand("eod"));

        assertEquals("eod", property.getStringValue());
        assertEquals(Map.of("stringValue", "eod"), changes);
    }

    @Test
    void storesCanonicalMultipleValues() {
        final GlobalConfigurationProperty property = property(
                GlobalConfigurationConstants.ALLOWED_LOAN_STATUSES_FOR_EXTERNAL_ASSET_TRANSFER, "ACTIVE");

        underTest.update(property, stringValueCommand("active, overpaid,ACTIVE"));

        assertEquals("ACTIVE,OVERPAID", property.getStringValue());
    }

    @Test
    void storesUnrestrictedValueAsSent() {
        final GlobalConfigurationProperty property = property(GlobalConfigurationConstants.REPORT_EXPORT_S3_FOLDER_NAME, null);

        underTest.update(property, stringValueCommand(" Reports "));

        assertEquals(" Reports ", property.getStringValue());
    }

    private static GlobalConfigurationProperty property(final String name, final String stringValue) {
        return new GlobalConfigurationProperty().setName(name).setStringValue(stringValue);
    }

    private JsonCommand stringValueCommand(final String stringValue) {
        return new JsonCommand(1L, fromJsonHelper.parse("{\"stringValue\": \"" + stringValue + "\"}"), fromJsonHelper);
    }
}
