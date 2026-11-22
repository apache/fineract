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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.portfolio.group.command.GroupCloseCommand;
import org.apache.fineract.portfolio.group.data.GroupCloseRequest;
import org.apache.fineract.portfolio.group.data.GroupCommandResponse;
import org.apache.fineract.portfolio.group.domain.GroupTypes;
import org.apache.fineract.portfolio.group.service.GroupMakerCheckerGuard;
import org.apache.fineract.portfolio.group.service.GroupingTypesWritePlatformService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupCloseCommandHandlerTest {

    @Mock
    private GroupingTypesWritePlatformService service;

    @Mock
    private GroupMakerCheckerGuard makerCheckerGuard;

    @InjectMocks
    private GroupCloseCommandHandler handler;

    private static GroupCloseCommand command(final GroupTypes groupingType) {
        final GroupCloseCommand command = new GroupCloseCommand();
        command.setPayload(GroupCloseRequest.builder().groupId(1L).groupingType(groupingType).closureDate("05 March 2011")
                .closureReasonId(2L).dateFormat("dd MMMM yyyy").locale("en").build());
        return command;
    }

    @Test
    void closesTheCenterAfterCheckingCenterMakerChecker() {
        final GroupCloseCommand command = command(GroupTypes.CENTER);
        when(service.close(command.getPayload())).thenReturn(GroupCommandResponse.builder().resourceId(1L).build());

        assertThat(handler.handle(command).getResourceId()).isEqualTo(1L);
        verify(makerCheckerGuard).rejectIfMakerCheckerEnabled("CLOSE", GroupTypes.CENTER);
    }

    @Test
    void doesNotCloseWhenMakerCheckerIsEnabled() {
        final GroupCloseCommand command = command(GroupTypes.GROUP);
        doThrow(new GeneralPlatformDomainRuleException("error.msg.maker.checker.not.supported", "not supported")).when(makerCheckerGuard)
                .rejectIfMakerCheckerEnabled("CLOSE", GroupTypes.GROUP);

        assertThatThrownBy(() -> handler.handle(command)).isInstanceOf(GeneralPlatformDomainRuleException.class);
        verifyNoInteractions(service);
    }
}
