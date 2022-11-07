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
package org.apache.fineract.cob.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.apache.fineract.cob.exceptions.LoanAccountLockCannotBeOverruledException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InlineLoanCOBExecutorServiceImplTest {

    @InjectMocks
    private InlineLoanCOBExecutorServiceImpl testObj;
    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private InlineLoanCOBExecutionDataParser dataParser;

    @Test
    void shouldExceptionThrownIfLoanIsAlreadyLocked() {
        JsonCommand command = mock(JsonCommand.class);
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));

        when(dataParser.parseExecution(any())).thenReturn(List.of(3L));
        when(transactionTemplate.execute(any())).thenThrow(new LoanAccountLockCannotBeOverruledException(""));
        assertThrows(LoanAccountLockCannotBeOverruledException.class, () -> testObj.executeInlineJob(command, "INLINE_LOAN_COB"));
    }
}
