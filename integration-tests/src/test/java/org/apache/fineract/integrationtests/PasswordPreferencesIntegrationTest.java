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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.integrationtests.common.PasswordPreferencesHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordPreferencesIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordPreferencesIntegrationTest.class);
    private int originalPasswordPolicyId;

    @BeforeEach
    public void setUp() {
        originalPasswordPolicyId = PasswordPreferencesHelper.getActivePasswordPreference().getId().intValue();
    }

    @AfterEach
    void tearDown() {
        PasswordPreferencesHelper.updatePasswordPreferences(Integer.toString(originalPasswordPolicyId));
    }

    @Test
    public void updatePasswordPreferences() {
        String validationPolicyId = "2";
        PasswordPreferencesHelper.updatePasswordPreferences(validationPolicyId);
        Integer id = PasswordPreferencesHelper.getActivePasswordPreference().getId().intValue();
        assertEquals(validationPolicyId, id.toString());
        LOG.info("---------------------------------PASSWORD PREFERENCE VALIDATED SUCCESSFULLY-----------------------------------------");
    }

    @Test
    public void updateWithInvalidPolicyId() {
        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> PasswordPreferencesHelper.updatePasswordPreferences("2000"));
        assertTrue(exception.getDeveloperMessage().contains("Password Validation Policy with identifier 2000 does not exist"));
    }
}
