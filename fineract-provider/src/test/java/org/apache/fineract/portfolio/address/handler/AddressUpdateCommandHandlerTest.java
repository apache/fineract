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
package org.apache.fineract.portfolio.address.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.portfolio.address.command.AddressUpdateCommand;
import org.apache.fineract.portfolio.address.data.AddressUpdateRequest;
import org.apache.fineract.portfolio.address.data.AddressUpdateResponse;
import org.apache.fineract.portfolio.address.service.AddressDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddressUpdateCommandHandlerTest {

    @Mock
    private AddressDomainService domainService;

    @InjectMocks
    private AddressUpdateCommandHandler underTest;

    @Test
    void handle_delegatesToServiceAndReturnsResponse() {
        AddressUpdateRequest request = AddressUpdateRequest.builder().clientId(1L).addressId(5L).city("Mumbai").build();
        AddressUpdateResponse expected = AddressUpdateResponse.builder().resourceId(5L).clientId(1L).build();

        when(domainService.update(any(AddressUpdateRequest.class))).thenReturn(expected);

        AddressUpdateCommand command = new AddressUpdateCommand();
        command.setPayload(request);

        AddressUpdateResponse response = underTest.handle(command);

        verify(domainService).update(request);
        assertThat(response.getResourceId()).isEqualTo(5L);
        assertThat(response.getClientId()).isEqualTo(1L);
    }
}
