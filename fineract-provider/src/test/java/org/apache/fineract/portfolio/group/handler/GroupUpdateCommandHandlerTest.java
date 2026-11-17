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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.portfolio.group.command.GroupUpdateCommand;
import org.apache.fineract.portfolio.group.data.GroupUpdateRequest;
import org.apache.fineract.portfolio.group.data.GroupUpdateResponse;
import org.apache.fineract.portfolio.group.service.GroupingTypesWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupUpdateCommandHandlerTest {

    @Mock
    private GroupingTypesWritePlatformService groupingTypesWritePlatformService;

    @InjectMocks
    private GroupUpdateCommandHandler underTest;

    @Test
    void handle_delegatesToServiceAndReturnsResponse() {
        GroupUpdateRequest request = GroupUpdateRequest.builder() //
                .groupId(42L) //
                .name("Updated Group") //
                .build();

        GroupUpdateResponse expectedResponse = GroupUpdateResponse.builder() //
                .resourceId(42L) //
                .groupId(42L) //
                .build();

        when(groupingTypesWritePlatformService.updateGroup(request)).thenReturn(expectedResponse);

        GroupUpdateCommand command = new GroupUpdateCommand();
        command.setPayload(request);

        GroupUpdateResponse response = underTest.handle(command);

        verify(groupingTypesWritePlatformService).updateGroup(request);
        assertThat(response.getResourceId()).isEqualTo(42L);
    }
}
