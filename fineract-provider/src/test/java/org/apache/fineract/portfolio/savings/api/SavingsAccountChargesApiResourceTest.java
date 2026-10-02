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
package org.apache.fineract.portfolio.savings.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountChargeData;
import org.apache.fineract.portfolio.savings.data.SavingsChargeTransactionData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingsAccountChargesApiResourceTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private ChargeReadPlatformService chargeReadPlatformService;
    @Mock
    private SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService;
    @Mock
    private DefaultToApiJsonSerializer<SavingsAccountChargeData> toApiJsonSerializer;
    @Mock
    private ApiRequestParameterHelper apiRequestParameterHelper;
    @Mock
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @InjectMocks
    private SavingsAccountChargesApiResource underTest;

    @Test
    void retrieveSavingsAccountChargeWithTransactionsLoadsAllocationHistory() {
        Long savingsAccountId = 11L;
        Long savingsAccountChargeId = 22L;
        UriInfo uriInfo = mock(UriInfo.class);
        AppUser user = mock(AppUser.class);
        SavingsAccountChargeData charge = mock(SavingsAccountChargeData.class);
        SavingsChargeTransactionData transaction = mock(SavingsChargeTransactionData.class);
        Collection<SavingsChargeTransactionData> transactions = List.of(transaction);
        ApiRequestJsonSerializationSettings settings = mock(ApiRequestJsonSerializationSettings.class);
        MultivaluedHashMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.add("associations", "transactions");

        when(context.authenticatedUser()).thenReturn(user);
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(savingsAccountChargeReadPlatformService.retrieveSavingsAccountChargeDetails(savingsAccountChargeId, savingsAccountId))
                .thenReturn(charge);
        when(savingsAccountChargeReadPlatformService.retrieveChargeTransactions(savingsAccountChargeId, savingsAccountId))
                .thenReturn(transactions);
        when(apiRequestParameterHelper.process(queryParameters)).thenReturn(settings);
        when(toApiJsonSerializer.serialize(eq(settings), eq(charge), any())).thenReturn("serialized");

        String result = underTest.retrieveSavingsAccountCharge(savingsAccountId, savingsAccountChargeId, uriInfo);

        assertThat(result).isEqualTo("serialized");
        verify(user).validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_CHARGE_RESOURCE_NAME);
        verify(user).validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);
        verify(savingsAccountChargeReadPlatformService).retrieveChargeTransactions(savingsAccountChargeId, savingsAccountId);
        verify(charge).setTransactions(transactions);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> responseFields = ArgumentCaptor.forClass(Set.class);
        verify(toApiJsonSerializer).serialize(eq(settings), eq(charge), responseFields.capture());
        assertThat(responseFields.getValue()).contains("transactions");
    }

    @Test
    void retrieveSavingsAccountChargeWithoutAssociationsKeepsExistingResponse() {
        Long savingsAccountId = 11L;
        Long savingsAccountChargeId = 22L;
        UriInfo uriInfo = mock(UriInfo.class);
        AppUser user = mock(AppUser.class);
        SavingsAccountChargeData charge = mock(SavingsAccountChargeData.class);
        ApiRequestJsonSerializationSettings settings = mock(ApiRequestJsonSerializationSettings.class);
        MultivaluedHashMap<String, String> queryParameters = new MultivaluedHashMap<>();

        when(context.authenticatedUser()).thenReturn(user);
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(savingsAccountChargeReadPlatformService.retrieveSavingsAccountChargeDetails(savingsAccountChargeId, savingsAccountId))
                .thenReturn(charge);
        when(apiRequestParameterHelper.process(queryParameters)).thenReturn(settings);
        when(toApiJsonSerializer.serialize(eq(settings), eq(charge), any())).thenReturn("serialized");

        assertThat(underTest.retrieveSavingsAccountCharge(savingsAccountId, savingsAccountChargeId, uriInfo)).isEqualTo("serialized");

        verify(savingsAccountChargeReadPlatformService, never()).retrieveChargeTransactions(any(), any());
        verify(charge, never()).setTransactions(any());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Set<String>> responseFields = ArgumentCaptor.forClass(Set.class);
        verify(toApiJsonSerializer).serialize(eq(settings), eq(charge), responseFields.capture());
        assertThat(responseFields.getValue()).doesNotContain("transactions");
    }

    @Test
    void retrieveSavingsAccountChargeWithAllLoadsTransactionHistory() {
        Long savingsAccountId = 11L;
        Long savingsAccountChargeId = 22L;
        UriInfo uriInfo = mock(UriInfo.class);
        AppUser user = mock(AppUser.class);
        SavingsAccountChargeData charge = mock(SavingsAccountChargeData.class);
        ApiRequestJsonSerializationSettings settings = mock(ApiRequestJsonSerializationSettings.class);
        MultivaluedHashMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.add("associations", "all");

        when(context.authenticatedUser()).thenReturn(user);
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(savingsAccountChargeReadPlatformService.retrieveSavingsAccountChargeDetails(savingsAccountChargeId, savingsAccountId))
                .thenReturn(charge);
        when(savingsAccountChargeReadPlatformService.retrieveChargeTransactions(savingsAccountChargeId, savingsAccountId))
                .thenReturn(List.of());
        when(apiRequestParameterHelper.process(queryParameters)).thenReturn(settings);
        when(toApiJsonSerializer.serialize(eq(settings), eq(charge), any())).thenReturn("serialized");

        assertThat(underTest.retrieveSavingsAccountCharge(savingsAccountId, savingsAccountChargeId, uriInfo)).isEqualTo("serialized");

        verify(user).validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);
        verify(charge).setTransactions(List.of());
    }
}
