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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.portfolio.group.command.GroupCreateCommand;
import org.apache.fineract.portfolio.group.data.GroupCreateRequest;
import org.apache.fineract.portfolio.group.data.GroupCreateResponse;
import org.apache.fineract.portfolio.group.service.GroupingTypesWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupCreateCommandHandlerTest {

    @Mock
    private GroupingTypesWritePlatformService groupingTypesWritePlatformService;

    @InjectMocks
    private GroupCreateCommandHandler underTest;

    @Test
    void handle_delegatesToServiceAndReturnsResponse() {
        GroupCreateRequest request = GroupCreateRequest.builder() //
                .name("Test Group") //
                .officeId(1L) //
                .active(false) //
                .build();

        GroupCreateResponse expectedResponse = GroupCreateResponse.builder() //
                .resourceId(42L) //
                .officeId(1L) //
                .groupId(42L) //
                .build();

        when(groupingTypesWritePlatformService.createGroup(any(GroupCreateRequest.class))).thenReturn(expectedResponse);

        GroupCreateCommand command = new GroupCreateCommand();
        command.setPayload(request);

        GroupCreateResponse response = underTest.handle(command);

        verify(groupingTypesWritePlatformService).createGroup(request);
        assertThat(response.getResourceId()).isEqualTo(42L);
        assertThat(response.getOfficeId()).isEqualTo(1L);
    }
}
