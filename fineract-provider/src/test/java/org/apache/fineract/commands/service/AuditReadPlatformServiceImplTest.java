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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.fineract.commands.data.request.AuditRequest;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditReadPlatformServiceImplTest {

    @InjectMocks
    private AuditReadPlatformServiceImpl auditReadPlatformService;

    @Test
    void statusFilterIsBoundAsInteger() {
        final AuditRequest auditRequest = new AuditRequest();
        auditRequest.setStatus(" 2 ");

        final SQLBuilder extraCriteria = auditReadPlatformService.getExtraCriteria(auditRequest);

        assertTrue(extraCriteria.getSQLTemplate().contains("aud.status = ?"));
        assertEquals(1, extraCriteria.getArguments().length);
        assertInstanceOf(Integer.class, extraCriteria.getArguments()[0]);
        assertEquals(2, extraCriteria.getArguments()[0]);
    }

    @Test
    void statusFilterIsOmittedWhenNotRequested() {
        final SQLBuilder extraCriteria = auditReadPlatformService.getExtraCriteria(new AuditRequest());

        assertEquals(0, extraCriteria.getArguments().length);
    }

    @Test
    void statusFilterIsOmittedWhenBlank() {
        final AuditRequest auditRequest = new AuditRequest();
        auditRequest.setStatus(" ");

        final SQLBuilder extraCriteria = auditReadPlatformService.getExtraCriteria(auditRequest);

        assertEquals(0, extraCriteria.getArguments().length);
    }

    @Test
    void nonNumericStatusFilterIsRejected() {
        final AuditRequest auditRequest = new AuditRequest();
        auditRequest.setStatus("abc");

        assertThrows(PlatformApiDataValidationException.class, () -> auditReadPlatformService.getExtraCriteria(auditRequest));
    }
}
