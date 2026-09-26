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
package org.apache.fineract.portfolio.workingcapitalloan.handler;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.portfolio.workingcapitalloan.service.WorkingCapitalLoanWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkingCapitalLoanDiscountFeeAdjustmentCommandHandlerTest {

    private static final Long LOAN_ID = 11L;
    private static final Long DISCOUNT_FEE_ID = 33L;

    @Mock
    private WorkingCapitalLoanWritePlatformService writePlatformService;

    @Mock
    private JsonCommand command;

    @InjectMocks
    private WorkingCapitalLoanDiscountFeeAdjustmentCommandHandler underTest;

    @Test
    void commandWithoutASubEntityNamesTheDiscountFeeInTheBody() {
        final CommandProcessingResult expected = CommandProcessingResult.empty();
        when(command.entityId()).thenReturn(LOAN_ID);
        when(command.subentityId()).thenReturn(null);
        when(writePlatformService.makeDiscountFeeAdjustment(LOAN_ID, command)).thenReturn(expected);

        assertSame(expected, underTest.processCommand(command));

        verify(writePlatformService, never()).makeDiscountFeeAdjustmentForDiscountFee(any(), any(), any());
    }

    @Test
    void commandWithASubEntityNamesTheDiscountFeeInThePath() {
        final CommandProcessingResult expected = CommandProcessingResult.empty();
        when(command.entityId()).thenReturn(LOAN_ID);
        when(command.subentityId()).thenReturn(DISCOUNT_FEE_ID);
        when(writePlatformService.makeDiscountFeeAdjustmentForDiscountFee(LOAN_ID, DISCOUNT_FEE_ID, command)).thenReturn(expected);

        assertSame(expected, underTest.processCommand(command));

        verify(writePlatformService, never()).makeDiscountFeeAdjustment(any(), any());
    }
}
