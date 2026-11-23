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
package org.apache.fineract.portfolio.savings.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SavingsAccountReadPlatformServiceImplTest {

    private static final Long SAVINGS_ID = 1L;
    private static final Long TRANSACTION_ID = 2L;

    @Mock
    private SavingsAccountTransactionReadService savingsAccountTransactionReadService;

    @Mock
    private SavingsAccountInterestPostingReadService savingsAccountInterestPostingReadService;

    private SavingsAccountReadPlatformServiceImpl savingsAccountReadPlatformService;

    @BeforeEach
    void setUp() {
        savingsAccountReadPlatformService = new SavingsAccountReadPlatformServiceImpl(null, null, null, null, null, null,
                savingsAccountTransactionReadService, savingsAccountInterestPostingReadService);
    }

    @Test
    void retrieveDepositTransactionTemplate_shouldDelegateToSavingsAccountTransactionReadService() {
        final SavingsAccountTransactionData expectedResult = mock(SavingsAccountTransactionData.class);
        when(savingsAccountTransactionReadService.retrieveDepositTransactionTemplate(SAVINGS_ID, DepositAccountType.SAVINGS_DEPOSIT))
                .thenReturn(expectedResult);

        final SavingsAccountTransactionData actualResult = savingsAccountReadPlatformService.retrieveDepositTransactionTemplate(SAVINGS_ID,
                DepositAccountType.SAVINGS_DEPOSIT);

        assertEquals(expectedResult, actualResult);
        verify(savingsAccountTransactionReadService).retrieveDepositTransactionTemplate(SAVINGS_ID, DepositAccountType.SAVINGS_DEPOSIT);
    }

    @Test
    void retrieveAllTransactions_shouldDelegateToSavingsAccountTransactionReadService() {
        final Collection<SavingsAccountTransactionData> expectedResult = List.of(mock(SavingsAccountTransactionData.class));
        when(savingsAccountTransactionReadService.retrieveAllTransactions(SAVINGS_ID, DepositAccountType.SAVINGS_DEPOSIT))
                .thenReturn(expectedResult);

        final Collection<SavingsAccountTransactionData> actualResult = savingsAccountReadPlatformService.retrieveAllTransactions(SAVINGS_ID,
                DepositAccountType.SAVINGS_DEPOSIT);

        assertEquals(expectedResult, actualResult);
        verify(savingsAccountTransactionReadService).retrieveAllTransactions(SAVINGS_ID, DepositAccountType.SAVINGS_DEPOSIT);
    }

    @Test
    void retrieveSavingsTransaction_shouldDelegateToSavingsAccountTransactionReadService() {
        final SavingsAccountTransactionData expectedResult = mock(SavingsAccountTransactionData.class);
        when(savingsAccountTransactionReadService.retrieveSavingsTransaction(SAVINGS_ID, TRANSACTION_ID,
                DepositAccountType.SAVINGS_DEPOSIT)).thenReturn(expectedResult);

        final SavingsAccountTransactionData actualResult = savingsAccountReadPlatformService.retrieveSavingsTransaction(SAVINGS_ID,
                TRANSACTION_ID, DepositAccountType.SAVINGS_DEPOSIT);

        assertEquals(expectedResult, actualResult);
        verify(savingsAccountTransactionReadService).retrieveSavingsTransaction(SAVINGS_ID, TRANSACTION_ID,
                DepositAccountType.SAVINGS_DEPOSIT);
    }

    @Test
    void retrieveAllTransactionData_shouldDelegateToSavingsAccountTransactionReadService() {
        final List<String> refNo = List.of("ref-1");
        final List<SavingsAccountTransactionData> expectedResult = List.of(mock(SavingsAccountTransactionData.class));
        when(savingsAccountTransactionReadService.retrieveAllTransactionData(refNo)).thenReturn(expectedResult);

        final List<SavingsAccountTransactionData> actualResult = savingsAccountReadPlatformService.retrieveAllTransactionData(refNo);

        assertEquals(expectedResult, actualResult);
        verify(savingsAccountTransactionReadService).retrieveAllTransactionData(refNo);
    }

    @Test
    void retrieveSavingsTransactionIdByExternalId_shouldDelegateToSavingsAccountTransactionReadService() {
        final ExternalId externalId = new ExternalId("ext-1");
        when(savingsAccountTransactionReadService.retrieveSavingsTransactionIdByExternalId(externalId)).thenReturn(TRANSACTION_ID);

        final Long actualResult = savingsAccountReadPlatformService.retrieveSavingsTransactionIdByExternalId(externalId);

        assertEquals(TRANSACTION_ID, actualResult);
        verify(savingsAccountTransactionReadService).retrieveSavingsTransactionIdByExternalId(externalId);
    }

    @Test
    void retrieveAllSavingsDataForInterestPosting_shouldDelegateToSavingsAccountInterestPostingReadService() {
        final List<SavingsAccountData> expectedResult = List.of(mock(SavingsAccountData.class));
        when(savingsAccountInterestPostingReadService.retrieveAllSavingsDataForInterestPosting(false, 10, 300, 0L))
                .thenReturn(expectedResult);

        final List<SavingsAccountData> actualResult = savingsAccountReadPlatformService.retrieveAllSavingsDataForInterestPosting(false, 10,
                300, 0L);

        assertEquals(expectedResult, actualResult);
        verify(savingsAccountInterestPostingReadService).retrieveAllSavingsDataForInterestPosting(false, 10, 300, 0L);
    }
}
