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
package org.apache.fineract.portfolio.loanaccount.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanApplicationWritePlatformServiceGlimApprovalTest {

    private static final FromJsonHelper JSON_HELPER = new FromJsonHelper();
    private static final Long GLIM_ID = 10L;

    @Mock
    private GLIMAccountInfoRepository glimRepository;
    @Mock
    private LoanRepository loanRepository;

    @Spy
    @InjectMocks
    private LoanApplicationWritePlatformServiceJpaRepositoryImpl underTest;

    private GroupLoanIndividualMonitoringAccount glim;
    private Loan child1;
    private Loan child2;

    @BeforeEach
    void setUp() {
        glim = mock(GroupLoanIndividualMonitoringAccount.class);
        child1 = mock(Loan.class);
        child2 = mock(Loan.class);
        when(child1.getId()).thenReturn(1L);
        when(child2.getId()).thenReturn(2L);
        when(glimRepository.findById(GLIM_ID)).thenReturn(Optional.of(glim));
        when(loanRepository.findByGlimId(GLIM_ID)).thenReturn(List.of(child1, child2));
    }

    private static JsonCommand command(final String json) {
        return JsonCommand.from(json, JSON_HELPER.parse(json), JSON_HELPER, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    @Test
    void rejectsChildLoanThatDoesNotBelongToTheGlimAccount() {
        JsonCommand command = command(
                "{\"locale\":\"en\",\"glimPrincipal\":\"100\",\"approvalFormData\":[{\"loanId\":1},{\"loanId\":99}]}");

        assertThrows(LoanNotFoundException.class, () -> underTest.approveGLIMLoanAppication(GLIM_ID, command));

        verify(underTest, never()).approveApplication(anyLong(), any(JsonCommand.class));
        verify(glimRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateChildLoans() {
        JsonCommand command = command("{\"locale\":\"en\",\"glimPrincipal\":\"100\",\"approvalFormData\":[{\"loanId\":1},{\"loanId\":1}]}");

        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.approveGLIMLoanAppication(GLIM_ID, command));

        verify(underTest, never()).approveApplication(anyLong(), any(JsonCommand.class));
        verify(glimRepository, never()).save(any());
    }

    @Test
    void doesNotApproveParentWhenAChildLoanIsNotApproved() {
        when(child1.isApproved()).thenReturn(true);
        when(child2.isApproved()).thenReturn(false);
        doReturn(mock(CommandProcessingResult.class)).when(underTest).approveApplication(anyLong(), any(JsonCommand.class));
        JsonCommand command = command("{\"locale\":\"en\",\"glimPrincipal\":\"100\",\"approvalFormData\":[{\"loanId\":1}]}");

        underTest.approveGLIMLoanAppication(GLIM_ID, command);

        verify(glimRepository, never()).save(any());
    }

    @Test
    void approvesParentWhenAllChildLoansAreApproved() {
        when(child1.isApproved()).thenReturn(true);
        when(child2.isApproved()).thenReturn(true);
        doReturn(mock(CommandProcessingResult.class)).when(underTest).approveApplication(anyLong(), any(JsonCommand.class));
        JsonCommand command = command("{\"locale\":\"en\",\"glimPrincipal\":\"100\",\"approvalFormData\":[{\"loanId\":1},{\"loanId\":2}]}");

        underTest.approveGLIMLoanAppication(GLIM_ID, command);

        verify(glim).setPrincipalAmount(any(BigDecimal.class));
        verify(glimRepository).save(glim);
    }
}
