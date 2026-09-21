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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.portfolio.group.command.GroupActivateCommand;
import org.apache.fineract.portfolio.group.data.GroupActivateRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.mapping.GroupCommandRequestMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;

class GroupsApiResourceCommandsTest {

    private final CommandDispatcher dispatcher = mock(CommandDispatcher.class);

    private GroupsApiResource resource() {
        return new GroupsApiResource(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, dispatcher, Validation.buildDefaultValidatorFactory().getValidator(),
                Mappers.getMapper(GroupCommandRequestMapper.class));
    }

    @Test
    void activateDispatchesActivateCommandWithPathId() {
        GroupCommandResponse expected = GroupCommandResponse.builder().groupId(5L).build();
        when(dispatcher.dispatch(any())).thenReturn(() -> expected);
        GroupCommandRequest body = GroupCommandRequest.builder().activationDate("01 January 2024").dateFormat("dd MMMM yyyy").locale("en")
                .build();

        Object result = resource().activateOrGenerateCollectionSheet(5L, "ACTIVATE", null, body, null);

        ArgumentCaptor<Command<GroupActivateRequest>> captor = ArgumentCaptor.forClass(Command.class);
        org.mockito.Mockito.verify(dispatcher).dispatch(captor.capture());
        assertTrue(captor.getValue() instanceof GroupActivateCommand);
        assertEquals(5L, captor.getValue().getPayload().getId());
        assertEquals(expected, result);
    }

    @Test
    void activateWithoutDateIsRejectedBeforeDispatch() {
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class,
                () -> resource().activateOrGenerateCollectionSheet(5L, "activate", null, GroupCommandRequest.builder().build(), null));
        assertEquals("activationDate", ex.getConstraintViolations().iterator().next().getPropertyPath().toString());
        verifyNoInteractions(dispatcher);
    }

    @Test
    void unknownCommandIsRejected() {
        assertThrows(UnrecognizedQueryParamException.class,
                () -> resource().activateOrGenerateCollectionSheet(5L, "explode", null, GroupCommandRequest.builder().build(), null));
    }
}
