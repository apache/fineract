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
package org.apache.fineract.portfolio.loanaccount.guarantor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.guarantor.command.GuarantorCommand;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorRepository;
import org.apache.fineract.portfolio.loanaccount.guarantor.domain.GuarantorType;
import org.apache.fineract.portfolio.loanaccount.guarantor.serialization.GuarantorCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.service.SavingsAccountAssemblerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GuarantorWritePlatformServiceJpaRepositoryIImplTest {

    private static final Long LOAN_ID = 1L;
    private static final Long SAVINGS_ID = 30L;
    private static final Long GUARANTOR_CLIENT_ID = 5L;
    private static final Long OTHER_CLIENT_ID = 99L;
    private static final Long GROUP_ID = 12L;
    private static final Long OTHER_GROUP_ID = 13L;

    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private GuarantorRepository guarantorRepository;
    @Mock
    private GuarantorCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private SavingsAccountAssemblerService savingsAccountAssembler;
    @Mock
    private AccountAssociationsRepository accountAssociationsRepository;
    @Mock
    private JsonCommand command;
    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;

    private GuarantorWritePlatformServiceJpaRepositoryIImpl underTest;
    private SavingsAccount savingsAccount;

    @BeforeEach
    void setUp() {
        underTest = new GuarantorWritePlatformServiceJpaRepositoryIImpl(loanRepositoryWrapper, guarantorRepository, clientRepositoryWrapper,
                mock(StaffRepositoryWrapper.class), mock(GroupRepositoryWrapper.class), fromApiJsonDeserializer,
                mock(CodeValueRepositoryWrapper.class), savingsAccountAssembler, accountAssociationsRepository,
                mock(GuarantorDomainService.class));
        Loan loan = mock(Loan.class);
        when(loan.getStatus()).thenReturn(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        when(loanRepositoryWrapper.findOneWithNotFoundDetection(LOAN_ID, true)).thenReturn(loan);
        savingsAccount = mock(SavingsAccount.class);
        when(savingsAccountAssembler.assembleFrom(anyLong(), anyBoolean())).thenReturn(savingsAccount);
        lenient().when(savingsAccount.clientId()).thenReturn(OTHER_CLIENT_ID);
        lenient().when(savingsAccount.groupId()).thenReturn(null);
    }

    private void givenGuarantorCommand(final GuarantorType type, final Long entityId) {
        when(fromApiJsonDeserializer.commandFromApiJson(any()))
                .thenReturn(new GuarantorCommand(null, type.getValue(), entityId, null, null, null, null, null, null, null, null, null,
                        null, null, null, SAVINGS_ID, BigDecimal.TEN));
    }

    @Test
    void rejectsSavingsAccountOfAnotherClientForExistingClientGuarantor() {
        givenGuarantorCommand(GuarantorType.CUSTOMER, GUARANTOR_CLIENT_ID);

        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.createGuarantor(LOAN_ID, command));

        verify(accountAssociationsRepository, never()).saveAndFlush(any());
        verify(guarantorRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsSavingsAccountOfAnotherClientForSelfGuarantee() {
        // the borrower guarantees the loan with an account that is not theirs
        givenGuarantorCommand(GuarantorType.CUSTOMER, 7L);

        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.createGuarantor(LOAN_ID, command));

        verify(accountAssociationsRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsGroupSavingsAccountOfAGroupTheGuarantorDoesNotBelongTo() {
        givenGuarantorCommand(GuarantorType.CUSTOMER, GUARANTOR_CLIENT_ID);
        when(savingsAccount.clientId()).thenReturn(null);
        when(savingsAccount.groupId()).thenReturn(GROUP_ID);
        final Client guarantor = mock(Client.class);
        when(guarantor.isChildOfGroup(GROUP_ID)).thenReturn(false);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(GUARANTOR_CLIENT_ID, true)).thenReturn(guarantor);

        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.createGuarantor(LOAN_ID, command));

        verify(accountAssociationsRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsSavingsAccountOfAnotherGroupForGroupGuarantor() {
        givenGuarantorCommand(GuarantorType.GROUP, GROUP_ID);
        when(savingsAccount.groupId()).thenReturn(OTHER_GROUP_ID);

        assertThrows(GeneralPlatformDomainRuleException.class, () -> underTest.createGuarantor(LOAN_ID, command));

        verify(accountAssociationsRepository, never()).saveAndFlush(any());
        verify(guarantorRepository, never()).saveAndFlush(any());
    }

    @Test
    void doesNotRejectSavingsAccountOfTheGuarantorGroupOnOwnershipCheck() {
        givenGuarantorCommand(GuarantorType.GROUP, GROUP_ID);
        when(savingsAccount.groupId()).thenReturn(GROUP_ID);
        final Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(LOAN_ID, true);
        when(loan.getSubmittedOnDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(savingsAccount.getActivationDate()).thenReturn(LocalDate.of(2024, 2, 1));

        // the ownership check passes, so the flow moves on to the activation date check which rejects with a different
        // code
        final GeneralPlatformDomainRuleException exception = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.createGuarantor(LOAN_ID, command));

        assertEquals("error.msg.guarantor.saving.account.activation.date.is.on.or.before.loan.submitted.on.date",
                exception.getGlobalisationMessageCode());
    }

    @Test
    void rejectsSavingsAccountForStaffGuarantor() {
        givenGuarantorCommand(GuarantorType.STAFF, 8L);

        final GeneralPlatformDomainRuleException exception = assertThrows(GeneralPlatformDomainRuleException.class,
                () -> underTest.createGuarantor(LOAN_ID, command));

        assertEquals("error.msg.loan.guarantor.savings.account.not.owned.by.guarantor", exception.getGlobalisationMessageCode());
        verify(accountAssociationsRepository, never()).saveAndFlush(any());
    }
}
