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
package org.apache.fineract.commands.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;

class CommandSourceTest {

    @Test
    void updateForAuditKeepsRequestIdentifiersAndStoresReturnedOnesSeparately() {
        CommandSource commandSource = CommandSource.builder().officeId(1L).groupId(2L).clientId(3L).loanId(5L).savingsId(6L).productId(7L)
                .transactionId("8").resourceId(9L).resourceExternalId(new ExternalId("req-ext")).subResourceId(10L)
                .subResourceExternalId(new ExternalId("req-sub-ext")).loanExternalId(new ExternalId("req-loan-ext")).build();
        assertFalse(commandSource.isResultIdsWritten());

        CommandProcessingResult result = new CommandProcessingResultBuilder().withOfficeId(11L).withGroupId(12L).withClientId(13L)
                .withLoanId(15L).withSavingsId(16L).withProductId(17L).withTransactionId("18").withEntityId(19L)
                .withEntityExternalId(new ExternalId("res-ext")).withSubEntityId(20L).withSubEntityExternalId(new ExternalId("res-sub-ext"))
                .withLoanExternalId(new ExternalId("res-loan-ext")).build();

        commandSource.updateForAudit(result);

        assertEquals(1L, commandSource.getOfficeId());
        assertEquals(2L, commandSource.getGroupId());
        assertEquals(3L, commandSource.getClientId());
        assertEquals(5L, commandSource.getLoanId());
        assertEquals(6L, commandSource.getSavingsId());
        assertEquals(7L, commandSource.getProductId());
        assertEquals("8", commandSource.getTransactionId());
        assertEquals(9L, commandSource.getResourceId());
        assertEquals(new ExternalId("req-ext"), commandSource.getResourceExternalId());
        assertEquals(10L, commandSource.getSubResourceId());
        assertEquals(new ExternalId("req-sub-ext"), commandSource.getSubResourceExternalId());
        assertEquals(new ExternalId("req-loan-ext"), commandSource.getLoanExternalId());

        assertTrue(commandSource.isResultIdsWritten());
        assertEquals(11L, commandSource.getResultOfficeId());
        assertEquals(12L, commandSource.getResultGroupId());
        assertEquals(13L, commandSource.getResultClientId());
        assertEquals(15L, commandSource.getResultLoanId());
        assertEquals(16L, commandSource.getResultSavingsId());
        assertEquals(17L, commandSource.getResultProductId());
        assertEquals("18", commandSource.getResultTransactionId());
        assertEquals(19L, commandSource.getResultResourceId());
        assertEquals(new ExternalId("res-ext"), commandSource.getResultResourceExternalId());
        assertEquals(20L, commandSource.getResultSubResourceId());
        assertEquals(new ExternalId("res-sub-ext"), commandSource.getResultSubResourceExternalId());
        assertEquals(new ExternalId("res-loan-ext"), commandSource.getResultLoanExternalId());
    }

    @Test
    void updateForAuditLeavesEmptyRequestIdentifiersEmpty() {
        CommandSource commandSource = CommandSource.builder().build();
        CommandProcessingResult result = new CommandProcessingResultBuilder().withEntityId(42L).withClientId(3L).build();

        commandSource.updateForAudit(result);

        assertNull(commandSource.getResourceId());
        assertNull(commandSource.getClientId());
        assertEquals(42L, commandSource.getResultResourceId());
        assertEquals(3L, commandSource.getResultClientId());
        assertNull(commandSource.getResultLoanId());
        assertTrue(commandSource.isResultIdsWritten());
    }

    @Test
    void newEntryMarksItsRequestIdentifiersAsSubmitted() {
        CommandWrapper wrapper = mock(CommandWrapper.class);
        JsonCommand command = mock(JsonCommand.class);
        when(command.getLoanId()).thenReturn(5L);

        CommandSource commandSource = CommandSource.fullEntryFrom(wrapper, command, mock(AppUser.class), null,
                CommandProcessingResultType.UNDER_PROCESSING.getValue(), false);

        assertTrue(commandSource.isSubmittedIdsKept());
        assertFalse(commandSource.isResultIdsWritten());
        assertEquals(5L, commandSource.getLoanId());
    }
}
