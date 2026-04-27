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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.PagedLocalRequest;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.search.SavingsAccountTransactionSearchService;
import org.apache.fineract.portfolio.search.data.AdvancedQueryRequest;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class SavingsAccountTransactionsApiResourceTest {

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer;
    @Mock
    private PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    @Mock
    private ApiRequestParameterHelper apiRequestParameterHelper;
    @Mock
    private SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    @Mock
    private PaymentTypeReadService paymentTypeReadPlatformService;
    @Mock
    private SavingsAccountTransactionSearchService transactionsSearchService;

    @InjectMocks
    private SavingsAccountTransactionsApiResource underTest;

    @Test
    void retrieveTemplateBySavingsExternalId_shouldResolveSavingsIdAndDelegateToReadService() {
        String savingsExternalId = "savings-external-id";
        Long resolvedSavingsId = 11L;
        UriInfo uriInfo = org.mockito.Mockito.mock(UriInfo.class);
        AppUser user = org.mockito.Mockito.mock(AppUser.class);
        ApiRequestJsonSerializationSettings settings = org.mockito.Mockito.mock(ApiRequestJsonSerializationSettings.class);
        SavingsAccountTransactionData templateData = org.mockito.Mockito.mock(SavingsAccountTransactionData.class);

        when(context.authenticatedUser()).thenReturn(user);
        when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        when(savingsAccountReadPlatformService.retrieveAccountIdByExternalId(argThat(id -> savingsExternalId.equals(id.getValue()))))
                .thenReturn(resolvedSavingsId);
        when(savingsAccountReadPlatformService.retrieveDepositTransactionTemplate(resolvedSavingsId, DepositAccountType.SAVINGS_DEPOSIT))
                .thenReturn(templateData);
        when(paymentTypeReadPlatformService.retrieveAllPaymentTypes()).thenReturn(List.of(org.mockito.Mockito.mock(PaymentTypeData.class)));
        when(apiRequestParameterHelper.process(any())).thenReturn(settings);
        when(toApiJsonSerializer.serialize(eq(settings), any(SavingsAccountTransactionData.class),
                eq(SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS))).thenReturn("serialized");

        String result = underTest.retrieveTemplate(savingsExternalId, uriInfo);

        assertThat(result).isEqualTo("serialized");
        verify(savingsAccountReadPlatformService).retrieveDepositTransactionTemplate(resolvedSavingsId, DepositAccountType.SAVINGS_DEPOSIT);
        verify(paymentTypeReadPlatformService).retrieveAllPaymentTypes();
    }

    @Test
    void retrieveOneByExternalId_shouldResolveTransactionIdAndDelegateToReadService() {
        Long savingsId = 1L;
        String transactionExternalId = "tx-external-id";
        Long resolvedTransactionId = 42L;
        UriInfo uriInfo = org.mockito.Mockito.mock(UriInfo.class);
        AppUser user = org.mockito.Mockito.mock(AppUser.class);
        ApiRequestJsonSerializationSettings settings = org.mockito.Mockito.mock(ApiRequestJsonSerializationSettings.class);
        SavingsAccountTransactionData transactionData = org.mockito.Mockito.mock(SavingsAccountTransactionData.class);

        when(context.authenticatedUser()).thenReturn(user);
        when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        when(savingsAccountReadPlatformService
                .retrieveSavingsTransactionIdByExternalId(argThat(id -> transactionExternalId.equals(id.getValue()))))
                .thenReturn(resolvedTransactionId);
        when(savingsAccountReadPlatformService.retrieveSavingsTransaction(savingsId, resolvedTransactionId,
                DepositAccountType.SAVINGS_DEPOSIT)).thenReturn(transactionData);
        when(apiRequestParameterHelper.process(any())).thenReturn(settings);
        when(settings.isTemplate()).thenReturn(false);
        when(toApiJsonSerializer.serialize(settings, transactionData, SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS))
                .thenReturn("serialized");

        String result = underTest.retrieveOne(savingsId, transactionExternalId, uriInfo);

        assertThat(result).isEqualTo("serialized");
        verify(savingsAccountReadPlatformService).retrieveSavingsTransaction(savingsId, resolvedTransactionId,
                DepositAccountType.SAVINGS_DEPOSIT);
    }

    @Test
    void retrieveOneBySavingsExternalId_shouldResolveSavingsIdAndDelegateToReadService() {
        String savingsExternalId = "savings-external-id";
        Long resolvedSavingsId = 11L;
        Long transactionId = 42L;
        UriInfo uriInfo = org.mockito.Mockito.mock(UriInfo.class);
        AppUser user = org.mockito.Mockito.mock(AppUser.class);
        ApiRequestJsonSerializationSettings settings = org.mockito.Mockito.mock(ApiRequestJsonSerializationSettings.class);
        SavingsAccountTransactionData transactionData = org.mockito.Mockito.mock(SavingsAccountTransactionData.class);

        when(context.authenticatedUser()).thenReturn(user);
        when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        when(savingsAccountReadPlatformService.retrieveAccountIdByExternalId(argThat(id -> savingsExternalId.equals(id.getValue()))))
                .thenReturn(resolvedSavingsId);
        when(savingsAccountReadPlatformService.retrieveSavingsTransaction(resolvedSavingsId, transactionId,
                DepositAccountType.SAVINGS_DEPOSIT)).thenReturn(transactionData);
        when(apiRequestParameterHelper.process(any())).thenReturn(settings);
        when(settings.isTemplate()).thenReturn(false);
        when(toApiJsonSerializer.serialize(settings, transactionData, SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS))
                .thenReturn("serialized");

        String result = underTest.retrieveOne(savingsExternalId, transactionId, uriInfo);

        assertThat(result).isEqualTo("serialized");
        verify(savingsAccountReadPlatformService).retrieveSavingsTransaction(resolvedSavingsId, transactionId,
                DepositAccountType.SAVINGS_DEPOSIT);
    }

    @Test
    void retrieveOneBySavingsAndTransactionExternalId_shouldResolveBothExternalIdsAndDelegateToReadService() {
        String savingsExternalId = "savings-external-id";
        Long resolvedSavingsId = 11L;
        String transactionExternalId = "tx-external-id";
        Long resolvedTransactionId = 42L;
        UriInfo uriInfo = org.mockito.Mockito.mock(UriInfo.class);
        AppUser user = org.mockito.Mockito.mock(AppUser.class);
        ApiRequestJsonSerializationSettings settings = org.mockito.Mockito.mock(ApiRequestJsonSerializationSettings.class);
        SavingsAccountTransactionData transactionData = org.mockito.Mockito.mock(SavingsAccountTransactionData.class);

        when(context.authenticatedUser()).thenReturn(user);
        when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
        when(savingsAccountReadPlatformService.retrieveAccountIdByExternalId(argThat(id -> savingsExternalId.equals(id.getValue()))))
                .thenReturn(resolvedSavingsId);
        when(savingsAccountReadPlatformService
                .retrieveSavingsTransactionIdByExternalId(argThat(id -> transactionExternalId.equals(id.getValue()))))
                .thenReturn(resolvedTransactionId);
        when(savingsAccountReadPlatformService.retrieveSavingsTransaction(resolvedSavingsId, resolvedTransactionId,
                DepositAccountType.SAVINGS_DEPOSIT)).thenReturn(transactionData);
        when(apiRequestParameterHelper.process(any())).thenReturn(settings);
        when(settings.isTemplate()).thenReturn(false);
        when(toApiJsonSerializer.serialize(settings, transactionData, SavingsApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS))
                .thenReturn("serialized");

        String result = underTest.retrieveOne(savingsExternalId, transactionExternalId, uriInfo);

        assertThat(result).isEqualTo("serialized");
        verify(savingsAccountReadPlatformService).retrieveSavingsTransaction(resolvedSavingsId, resolvedTransactionId,
                DepositAccountType.SAVINGS_DEPOSIT);
    }

    @Test
    void searchTransactionsBySavingsExternalId_shouldResolveSavingsIdAndDelegateToSearchService() {
        String savingsExternalId = "savings-external-id";
        Long resolvedSavingsId = 11L;
        Page<SavingsAccountTransactionData> page = org.mockito.Mockito.mock(Page.class);

        when(savingsAccountReadPlatformService.retrieveAccountIdByExternalId(argThat(id -> savingsExternalId.equals(id.getValue()))))
                .thenReturn(resolvedSavingsId);
        when(transactionsSearchService.searchTransactions(eq(resolvedSavingsId), any())).thenReturn(page);
        when(toApiJsonSerializer.serialize(page)).thenReturn("serialized");

        String result = underTest.searchTransactions(savingsExternalId, "2024-01-10", "2024-01-20", null, null, new BigDecimal("10.00"),
                new BigDecimal("20.00"), "1,2", true, false, 1, 2, "id", Sort.Direction.ASC, "en", "yyyy-MM-dd");

        assertThat(result).isEqualTo("serialized");
        verify(transactionsSearchService).searchTransactions(eq(resolvedSavingsId),
                argThat(request -> resolvedSavingsId.equals(request.getAccountId())
                        && LocalDate.of(2024, 1, 10).equals(request.getFromDate()) && LocalDate.of(2024, 1, 20).equals(request.getToDate())
                        && new BigDecimal("10.00").compareTo(request.getFromAmount()) == 0
                        && new BigDecimal("20.00").compareTo(request.getToAmount()) == 0 && request.getCredit() && !request.getDebit()
                        && request.getTypes().length == 2 && "1".equals(request.getTypes()[0]) && request.getPageable().getPageNumber() == 1
                        && request.getPageable().getPageSize() == 2
                        && "id".equals(request.getPageable().getSort().iterator().next().getProperty())));
    }

    @Test
    void adjustTransactionByExternalId_shouldResolveTransactionIdInCommandWrapper() {
        Long savingsId = 1L;
        String transactionExternalId = "tx-external-id";
        Long resolvedTransactionId = 42L;
        CommandProcessingResult result = org.mockito.Mockito.mock(CommandProcessingResult.class);

        when(savingsAccountReadPlatformService
                .retrieveSavingsTransactionIdByExternalId(argThat(id -> transactionExternalId.equals(id.getValue()))))
                .thenReturn(resolvedTransactionId);
        when(commandsSourceWritePlatformService.logCommandSource(any())).thenReturn(result);

        underTest.adjustTransaction(savingsId, transactionExternalId, SavingsApiConstants.COMMAND_REVERSE_TRANSACTION, "{}");

        ArgumentCaptor<CommandWrapper> captor = ArgumentCaptor.forClass(CommandWrapper.class);
        verify(commandsSourceWritePlatformService).logCommandSource(captor.capture());
        assertThat(captor.getValue().getSavingsId()).isEqualTo(savingsId);
        assertThat(captor.getValue().getTransactionId()).isEqualTo(resolvedTransactionId.toString());
        assertThat(captor.getValue().getJson()).isEqualTo("{}");
    }

    @Test
    void createTransactionBySavingsExternalId_shouldResolveSavingsIdInCommandWrapper() {
        String savingsExternalId = "savings-external-id";
        Long resolvedSavingsId = 11L;
        CommandProcessingResult result = org.mockito.Mockito.mock(CommandProcessingResult.class);

        when(savingsAccountReadPlatformService.retrieveAccountIdByExternalId(argThat(id -> savingsExternalId.equals(id.getValue()))))
                .thenReturn(resolvedSavingsId);
        when(commandsSourceWritePlatformService.logCommandSource(any())).thenReturn(result);

        underTest.transaction(savingsExternalId, "deposit", "{}");

        ArgumentCaptor<CommandWrapper> captor = ArgumentCaptor.forClass(CommandWrapper.class);
        verify(commandsSourceWritePlatformService).logCommandSource(captor.capture());
        assertThat(captor.getValue().getSavingsId()).isEqualTo(resolvedSavingsId);
        assertThat(captor.getValue().getJson()).isEqualTo("{}");
    }

    @Test
    void advancedQueryBySavingsExternalId_shouldResolveSavingsIdAndDelegateToQueryService() {
        String savingsExternalId = "savings-external-id";
        Long resolvedSavingsId = 11L;
        PagedLocalRequest<AdvancedQueryRequest> queryRequest = new PagedLocalRequest<>();
        Page<com.google.gson.JsonObject> page = org.mockito.Mockito.mock(Page.class);
        UriInfo uriInfo = org.mockito.Mockito.mock(UriInfo.class);

        when(savingsAccountReadPlatformService.retrieveAccountIdByExternalId(argThat(id -> savingsExternalId.equals(id.getValue()))))
                .thenReturn(resolvedSavingsId);
        when(transactionsSearchService.queryAdvanced(resolvedSavingsId, queryRequest)).thenReturn(page);
        when(toApiJsonSerializer.serialize(page)).thenReturn("serialized");

        String result = underTest.advancedQuery(savingsExternalId, queryRequest, uriInfo);

        assertThat(result).isEqualTo("serialized");
        verify(transactionsSearchService).queryAdvanced(resolvedSavingsId, queryRequest);
    }

    @Test
    void adjustTransactionBySavingsAndTransactionExternalId_shouldResolveIdsInCommandWrapper() {
        String savingsExternalId = "savings-external-id";
        Long resolvedSavingsId = 11L;
        String transactionExternalId = "tx-external-id";
        Long resolvedTransactionId = 42L;
        CommandProcessingResult result = org.mockito.Mockito.mock(CommandProcessingResult.class);

        when(savingsAccountReadPlatformService.retrieveAccountIdByExternalId(argThat(id -> savingsExternalId.equals(id.getValue()))))
                .thenReturn(resolvedSavingsId);
        when(savingsAccountReadPlatformService
                .retrieveSavingsTransactionIdByExternalId(argThat(id -> transactionExternalId.equals(id.getValue()))))
                .thenReturn(resolvedTransactionId);
        when(commandsSourceWritePlatformService.logCommandSource(any())).thenReturn(result);

        underTest.adjustTransaction(savingsExternalId, transactionExternalId, SavingsApiConstants.COMMAND_REVERSE_TRANSACTION, "{}");

        ArgumentCaptor<CommandWrapper> captor = ArgumentCaptor.forClass(CommandWrapper.class);
        verify(commandsSourceWritePlatformService).logCommandSource(captor.capture());
        assertThat(captor.getValue().getSavingsId()).isEqualTo(resolvedSavingsId);
        assertThat(captor.getValue().getTransactionId()).isEqualTo(resolvedTransactionId.toString());
        assertThat(captor.getValue().getJson()).isEqualTo("{}");
    }
}
