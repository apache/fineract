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
package org.apache.fineract.portfolio.shareaccounts.domain;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.client.domain.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShareAccountRepositoryWrapperTest {

    private static final Long SHARE_ACCOUNT_ID = 1L;
    private static final String ACCOUNT_OFFICE_HIERARCHY = ".2.5.";

    @Mock
    private ShareAccountRepository repository;

    @Mock
    private PlatformSecurityContext context;

    @InjectMocks
    private ShareAccountRepositoryWrapper underTest;

    private ShareAccount account;

    @BeforeEach
    void setUp() {
        final Office office = mock(Office.class);
        when(office.getHierarchy()).thenReturn(ACCOUNT_OFFICE_HIERARCHY);
        final Client client = mock(Client.class);
        when(client.getOffice()).thenReturn(office);
        account = mock(ShareAccount.class);
        when(account.getClient()).thenReturn(client);
        when(repository.findById(SHARE_ACCOUNT_ID)).thenReturn(Optional.of(account));
    }

    @Test
    void findOneInUserScopeReturnsAccountWhenUserHasAccessToAccountOffice() {
        final ShareAccount result = underTest.findOneInUserScope(SHARE_ACCOUNT_ID);

        assertSame(account, result);
        verify(context).validateAccessRights(ACCOUNT_OFFICE_HIERARCHY);
    }

    @Test
    void findOneInUserScopeThrowsWhenAccountOfficeIsOutsideUserHierarchy() {
        doThrow(new NoAuthorizationException("The user doesn't have enough permissions to access the resource.")).when(context)
                .validateAccessRights(ACCOUNT_OFFICE_HIERARCHY);

        assertThrows(NoAuthorizationException.class, () -> underTest.findOneInUserScope(SHARE_ACCOUNT_ID));
    }
}
