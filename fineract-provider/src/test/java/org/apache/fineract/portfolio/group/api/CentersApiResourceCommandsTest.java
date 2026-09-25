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
package org.apache.fineract.portfolio.group.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import java.util.List;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.portfolio.group.command.CenterAssociateGroupsCommand;
import org.apache.fineract.portfolio.group.data.CenterAssociateGroupsRequest;
import org.apache.fineract.portfolio.group.data.CenterCommandRequest;
import org.apache.fineract.portfolio.group.data.CenterCommandResponse;
import org.apache.fineract.portfolio.group.mapping.CenterCommandRequestMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

class CentersApiResourceCommandsTest {

    private final CommandDispatcher dispatcher = mock(CommandDispatcher.class);

    private CentersApiResource resource() {
        return new CentersApiResource(null, null, null, null, null, null, null, null, null, null, null, null, null, dispatcher,
                Validation.buildDefaultValidatorFactory().getValidator(), Mappers.getMapper(CenterCommandRequestMapper.class));
    }

    @Test
    void associateGroupsDispatchesWithPathId() {
        CenterCommandResponse expected = CenterCommandResponse.builder().resourceId(3L).build();
        when(dispatcher.dispatch(any())).thenReturn(() -> expected);

        Object result = resource().activate(3L, "associateGroups", CenterCommandRequest.builder().groupMembers(List.of(8L)).build(), null);

        ArgumentCaptor<Command<CenterAssociateGroupsRequest>> captor = ArgumentCaptor.forClass(Command.class);
        verify(dispatcher).dispatch(captor.capture());
        assertTrue(captor.getValue() instanceof CenterAssociateGroupsCommand);
        assertEquals(3L, captor.getValue().getPayload().getId());
        assertEquals(List.of(8L), captor.getValue().getPayload().getGroupMembers());
        assertEquals(expected, result);
    }

    @Test
    void associateGroupsWithoutMembersIsRejected() {
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> resource().activate(3L, "associateGroups", CenterCommandRequest.builder().build(), null));
        assertEquals("groupMembers", ex.getConstraintViolations().iterator().next().getPropertyPath().toString());
        verifyNoInteractions(dispatcher);
    }

    @Test
    void unknownCommandIsRejected() {
        assertThrows(UnrecognizedQueryParamException.class,
                () -> resource().activate(3L, "nope", CenterCommandRequest.builder().build(), null));
    }
}
