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
package org.apache.fineract.portfolio.group.handler;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fineract.command.core.CommandHandler;
import org.apache.fineract.portfolio.group.command.GroupActivateCommand;
import org.apache.fineract.portfolio.group.command.GroupCreateCommand;
import org.apache.fineract.portfolio.group.data.GroupActivateRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.data.GroupCreateRequest;
import org.apache.fineract.portfolio.group.data.GroupCreateResponse;
import org.apache.fineract.portfolio.group.service.GroupingTypesWriteService;
import org.junit.jupiter.api.Test;

class GroupCommandHandlersTest {

    private final GroupingTypesWriteService service = mock(GroupingTypesWriteService.class);

    @Test
    void createHandlerDelegatesToService() {
        GroupCreateRequest request = GroupCreateRequest.builder().name("g").officeId(1L).build();
        GroupCreateResponse expected = GroupCreateResponse.builder().resourceId(1L).build();
        when(service.createGroup(request)).thenReturn(expected);
        GroupCreateCommand command = new GroupCreateCommand();
        command.setPayload(request);

        assertSame(expected, new GroupCreateCommandHandler(service).handle(command));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    void handlersMatchOnlyTheirOwnPayloadType() {
        GroupCreateCommand create = new GroupCreateCommand();
        create.setPayload(GroupCreateRequest.builder().build());
        GroupActivateCommand activate = new GroupActivateCommand();
        activate.setPayload(GroupActivateRequest.builder().build());

        CommandHandler createHandler = new GroupCreateCommandHandler(service);
        CommandHandler activateHandler = new GroupActivateCommandHandler(service);

        assertTrue(createHandler.matches(create));
        assertTrue(!createHandler.matches(activate));
        assertTrue(activateHandler.matches(activate));
    }

    @Test
    void activateHandlerDelegatesToService() {
        GroupActivateRequest request = GroupActivateRequest.builder().id(1L).activationDate("x").build();
        GroupCommandResponse expected = GroupCommandResponse.builder().groupId(1L).build();
        when(service.activateGroup(request)).thenReturn(expected);
        GroupActivateCommand command = new GroupActivateCommand();
        command.setPayload(request);

        assertSame(expected, new GroupActivateCommandHandler(service).handle(command));
    }
}
