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
package org.apache.fineract.commands.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.domain.SavingsDepositCommandEnvelope;
import org.apache.fineract.commands.domain.SavingsDepositOrigin;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.jdbc.core.RowMapper;

class SavingsDepositAuditTest {

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void auditMapperReturnsFlatPayloadForLegacyAndEnvelopedDeposits(boolean envelope) throws Exception {
        String payload = "{\"transactionAmount\":50}";
        String stored = envelope ? SavingsDepositCommandEnvelope.encode(payload, SavingsDepositOrigin.STAFF_API) : payload;
        assertThat(map("DEPOSIT", stored).getCommandAsJson()).isEqualTo(payload);
    }

    @ParameterizedTest
    @ValueSource(strings = { "WITHDRAWAL", "POSTINTEREST", "ACTIVATE" })
    void unrelatedAuditCommandsAreUntouched(String action) throws Exception {
        String stored = "{\"_serverCommand\":\"ordinary data for another command\"}";
        assertThat(map(action, stored).getCommandAsJson()).isEqualTo(stored);
    }

    private AuditData map(String action, String stored) throws Exception {
        // Exercise the actual shared mapper used by audit detail, list and maker-checker queries.
        var constructor = Class.forName(AuditReadPlatformServiceImpl.class.getName() + "$AuditMapper").getDeclaredConstructor();
        constructor.setAccessible(true);
        @SuppressWarnings("unchecked")
        RowMapper<AuditData> mapper = (RowMapper<AuditData>) constructor.newInstance();
        ResultSet row = mock(ResultSet.class);
        when(row.getString("actionName")).thenReturn(action);
        when(row.getString("entityName")).thenReturn("SAVINGSACCOUNT");
        when(row.getString("commandAsJson")).thenReturn(stored);
        return mapper.mapRow(row, 0);
    }
}
