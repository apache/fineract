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

package org.apache.fineract.organisation.teller.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.organisation.teller.data.CashierTransactionDataValidator;
import org.apache.fineract.organisation.teller.domain.Cashier;
import org.apache.fineract.organisation.teller.domain.CashierRepository;
import org.apache.fineract.organisation.teller.domain.CashierTransaction;
import org.apache.fineract.organisation.teller.domain.CashierTransactionRepository;
import org.apache.fineract.organisation.teller.domain.Teller;
import org.apache.fineract.organisation.teller.domain.TellerRepositoryWrapper;
import org.apache.fineract.organisation.teller.exception.CashierExistForTellerException;
import org.apache.fineract.organisation.teller.exception.CashierNotFoundException;
import org.apache.fineract.organisation.teller.serialization.TellerCommandFromApiJsonDeserializer;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.jpa.JpaSystemException;

@ExtendWith(MockitoExtension.class)
public class TellerWritePlatformServiceJpaImplTest {

    @Mock
    private AppUser currentUser;

    @Mock
    private JsonCommand command;

    @Mock
    private Office office;

    @Mock
    private Teller teller;

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private TellerCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;

    @Mock
    private TellerRepositoryWrapper tellerRepositoryWrapper;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private CashierTransactionDataValidator cashierTransactionDataValidator;

    @Mock
    private CashierRepository cashierRepository;

    @Mock
    private CashierTransactionRepository cashierTxnRepository;

    @Mock
    private FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper;

    @Mock
    private JournalEntryRepository glJournalEntryRepository;

    @InjectMocks
    private TellerWritePlatformServiceJpaImpl underTest;

    @BeforeEach
    void initGlobalMocks() {
        HashMap<BusinessDateType, LocalDate> businessDates = new HashMap<>();
        businessDates.put(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 1, 1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);

        lenient().when(context.authenticatedUser()).thenReturn(currentUser);
    }

    @Nested
    public class CreateTeller {

        @BeforeEach
        void setUp() {
            when(command.longValueOfParameterNamed("officeId")).thenReturn(1L);
            doNothing().when(fromApiJsonDeserializer).validateForCreateAndUpdateTeller(any());
            when(officeRepositoryWrapper.findOneWithNotFoundDetection(anyLong())).thenReturn(office);
        }

        @Test
        void shouldCreateATeller() {
            when(tellerRepositoryWrapper.saveAndFlush(any(Teller.class))).thenReturn(teller);
            when(command.commandId()).thenReturn(1L);
            when(office.getId()).thenReturn(1L);

            CommandProcessingResult result = underTest.createTeller(command);

            verify(fromApiJsonDeserializer, times(1)).validateForCreateAndUpdateTeller(any());
            verify(tellerRepositoryWrapper, times(1)).saveAndFlush(any(Teller.class));
            assertNotNull(result);
            assertEquals(1L, result.getCommandId());
            assertEquals(null, result.getResourceId());
            assertEquals(1L, result.getOfficeId());
        }

        @Test
        void shouldThrowExceptionWhenTellerExistsInTheSameOffice() {
            JpaSystemException exception = newException("m_tellers_office_name_unq");
            when(tellerRepositoryWrapper.saveAndFlush(any(Teller.class))).thenThrow(exception);
            when(command.stringValueOfParameterNamed("name")).thenReturn("DUPLICATED TELLER");

            assertDataIntegrityCode("error.msg.teller.duplicate.name", () -> underTest.createTeller(command));
            verify(fromApiJsonDeserializer, times(1)).validateForCreateAndUpdateTeller(any());
            verify(tellerRepositoryWrapper, times(1)).saveAndFlush(any(Teller.class));
        }

        @Test
        void shouldThrowExceptionWhenUnknownDataIntegrityIssueOccurs() {
            JpaSystemException exception = newException("Persistence provider encountered an internal error");
            when(tellerRepositoryWrapper.saveAndFlush(any(Teller.class))).thenThrow(exception);

            assertDataIntegrityCode("error.msg.teller.unknown.data.integrity.issue", () -> underTest.createTeller(command));
            verify(fromApiJsonDeserializer, times(1)).validateForCreateAndUpdateTeller(any());
            verify(tellerRepositoryWrapper, times(1)).saveAndFlush(any(Teller.class));
        }
    }

    @Nested
    public class ModifyTeller {

        @BeforeEach
        void setUp() {
            when(command.longValueOfParameterNamed("officeId")).thenReturn(1L);
            when(officeRepositoryWrapper.findOneWithNotFoundDetection(anyLong())).thenReturn(office);
            doNothing().when(fromApiJsonDeserializer).validateForCreateAndUpdateTeller(any());
            when(officeRepositoryWrapper.findOfficeHierarchy(anyLong())).thenReturn(office);
            when(tellerRepositoryWrapper.findOneWithNotFoundDetection(anyLong())).thenReturn(teller);
        }

        @Test
        void shouldModifyATellerWhenChangesArePresent() {
            when(currentUser.getOffice()).thenReturn(office);
            when(office.getId()).thenReturn(1L);
            when(teller.update(any(Office.class), any(JsonCommand.class))).thenReturn(getTellerChanges());
            when(tellerRepositoryWrapper.saveAndFlush(any(Teller.class))).thenReturn(teller);
            when(command.commandId()).thenReturn(1L);
            when(teller.getId()).thenReturn(1L);
            when(teller.officeId()).thenReturn(1L);

            CommandProcessingResult result = underTest.modifyTeller(1L, command);

            verify(fromApiJsonDeserializer, times(1)).validateForCreateAndUpdateTeller(any());
            verify(tellerRepositoryWrapper, times(1)).saveAndFlush(any(Teller.class));
            assertNotNull(result);
            assertEquals(1L, result.getCommandId());
            assertEquals(1L, result.getResourceId());
            assertEquals(1L, result.getOfficeId());
        }
    }

    @Nested
    class DeleteTeller {

        @BeforeEach
        void setUp() {
            when(tellerRepositoryWrapper.findOneWithNotFoundDetection(anyLong())).thenReturn(teller);
        }

        @Test
        void shouldDeleteATeller() {
            when(teller.getCashiers()).thenReturn(Collections.emptySet());
            when(teller.getId()).thenReturn(1L);

            CommandProcessingResult result = underTest.deleteTeller(1L);

            verify(tellerRepositoryWrapper, times(1)).delete(any(Teller.class));
            assertNotNull(result);
            assertEquals(1L, result.getResourceId());
        }

        @Test
        void shouldThrowExceptionWhenCashierExistsForTeller() {
            Cashier cashier = mock(Cashier.class);
            Set<Cashier> cashiers = new HashSet<>();
            cashiers.add(cashier);

            when(teller.getCashiers()).thenReturn(cashiers);
            when(cashier.getTeller()).thenReturn(teller);
            when(teller.getId()).thenReturn(1L);

            assertThrows(CashierExistForTellerException.class, () -> underTest.deleteTeller(1L));
            verify(tellerRepositoryWrapper, times(0)).delete(any(Teller.class));
        }
    }

    @Nested
    class AllocateCashier {

        @BeforeEach
        void setUp() {
            when(tellerRepositoryWrapper.findOneWithNotFoundDetection(anyLong())).thenReturn(teller);
            when(teller.getOffice()).thenReturn(office);
            when(command.longValueOfParameterNamed("staffId")).thenReturn(1L);
            doNothing().when(fromApiJsonDeserializer).validateForAllocateCashier(any());
        }

        @Test
        void shouldAllocateCashierToTeller() {
            Staff staff = mock(Staff.class);
            Cashier cashier = mock(Cashier.class);

            when(staffRepository.findById(anyLong())).thenReturn(Optional.of(staff));
            when(command.booleanObjectValueOfParameterNamed("isFullDay")).thenReturn(true);
            doNothing().when(cashierTransactionDataValidator).validateCashierAllowedDateAndTime(any(Cashier.class), any(Teller.class));
            when(cashierRepository.save(any(Cashier.class))).thenReturn(cashier);
            when(command.commandId()).thenReturn(1L);
            when(teller.getId()).thenReturn(1L);

            CommandProcessingResult result = underTest.allocateCashierToTeller(1L, command);

            verify(fromApiJsonDeserializer, times(1)).validateForAllocateCashier(any());
            verify(cashierTransactionDataValidator).validateCashierAllowedDateAndTime(any(Cashier.class), any(Teller.class));
            verify(cashierRepository, times(1)).save(any(Cashier.class));
            assertNotNull(result);
            assertEquals(1L, result.getCommandId());
            assertEquals(1L, result.getResourceId());
        }

        @Test
        void shouldThrowStaffNotFoundException() {
            when(staffRepository.findById(anyLong())).thenThrow(new StaffNotFoundException(1L));

            assertThrows(StaffNotFoundException.class, () -> underTest.allocateCashierToTeller(1L, command));
            verify(fromApiJsonDeserializer, times(1)).validateForAllocateCashier(any());
        }

        @Test
        void shouldAllocateCashierToTellerWithStartAndEndTimePeriod() {
            Staff staff = mock(Staff.class);
            Cashier cashier = mock(Cashier.class);

            when(staffRepository.findById(anyLong())).thenReturn(Optional.of(staff));
            when(command.booleanObjectValueOfParameterNamed("isFullDay")).thenReturn(false);
            when(command.longValueOfParameterNamed("hourStartTime")).thenReturn(9L);
            when(command.longValueOfParameterNamed("minStartTime")).thenReturn(0L);
            when(command.longValueOfParameterNamed("hourEndTime")).thenReturn(17L);
            when(command.longValueOfParameterNamed("minEndTime")).thenReturn(0L);

            doNothing().when(cashierTransactionDataValidator).validateCashierAllowedDateAndTime(any(Cashier.class), any(Teller.class));
            when(cashierRepository.save(any(Cashier.class))).thenReturn(cashier);
            when(command.commandId()).thenReturn(1L);
            when(teller.getId()).thenReturn(1L);

            CommandProcessingResult result = underTest.allocateCashierToTeller(1L, command);

            verify(fromApiJsonDeserializer, times(1)).validateForAllocateCashier(any());
            verify(cashierTransactionDataValidator).validateCashierAllowedDateAndTime(any(Cashier.class), any(Teller.class));
            verify(cashierRepository, times(1)).save(any(Cashier.class));
            assertNotNull(result);
            assertEquals(1L, result.getCommandId());
            assertEquals(1L, result.getResourceId());
        }
    }

    @Nested
    class UpdateCashierAllocation {

        @BeforeEach
        void setUp() {
            doNothing().when(fromApiJsonDeserializer).validateForAllocateCashier(any());
            when(command.longValueOfParameterNamed("staffId")).thenReturn(1L);
        }

        @Test
        void shouldUpdateCashierAllocation() {
            Staff staff = mock(Staff.class);
            Cashier cashier = mock(Cashier.class);
            Map<String, Object> simulatedChanges = Map.of("isFullDay", "false");

            when(staffRepository.findById(anyLong())).thenReturn(Optional.of(staff));
            when(currentUser.getOffice()).thenReturn(office);
            when(office.getId()).thenReturn(1L);
            when(officeRepositoryWrapper.findOfficeHierarchy(anyLong())).thenReturn(office);
            when(tellerRepositoryWrapper.findOneWithNotFoundDetection(anyLong())).thenReturn(teller);
            when(teller.officeId()).thenReturn(1L);
            when(cashierRepository.findById(anyLong())).thenReturn(Optional.of(cashier));
            when(cashier.update(any(JsonCommand.class))).thenReturn(simulatedChanges);
            when(cashierRepository.saveAndFlush(any(Cashier.class))).thenReturn(cashier);
            when(command.commandId()).thenReturn(1L);
            when(cashier.getTeller()).thenReturn(teller);
            when(teller.getId()).thenReturn(1L);
            when(cashier.getId()).thenReturn(1L);

            CommandProcessingResult result = underTest.updateCashierAllocation(1L, 1L, command);

            verify(fromApiJsonDeserializer, times(1)).validateForAllocateCashier(any());
            verify(cashierRepository, times(1)).saveAndFlush(any(Cashier.class));
            assertNotNull(result);
            assertEquals(1L, result.getCommandId());
            assertEquals(1L, result.getResourceId());
            assertEquals(1L, result.getSubResourceId());
        }

        @Test
        void shouldThrowStaffNotFoundException() {
            when(staffRepository.findById(anyLong())).thenThrow(new StaffNotFoundException(1L));

            assertThrows(StaffNotFoundException.class, () -> underTest.updateCashierAllocation(1L, 1L, command));
            verify(fromApiJsonDeserializer, times(1)).validateForAllocateCashier(any());
        }
    }

    @Nested
    class DeleteCashierAllocation {

        @Test
        void shouldDeleteCashierAllocation() {
            Cashier cashier = mock(Cashier.class);

            when(currentUser.getOffice()).thenReturn(office);
            when(office.getId()).thenReturn(1L);
            when(officeRepositoryWrapper.findOfficeHierarchy(anyLong())).thenReturn(office);
            when(tellerRepositoryWrapper.findOneWithNotFoundDetection(anyLong())).thenReturn(teller);
            when(teller.officeId()).thenReturn(1L);
            when(cashierRepository.findById(anyLong())).thenReturn(Optional.of(cashier));

            CommandProcessingResult result = underTest.deleteCashierAllocation(1L, 1L, command);

            verify(cashierRepository, times(1)).delete(any(Cashier.class));
            assertNotNull(result);
            assertEquals(1L, result.getResourceId());
        }
    }

    @Nested
    class AllocateCashToTeller {

        @Test
        void shouldAllocateCashToTeller() {
            Cashier cashier = mock(Cashier.class);
            CashierTransaction cashierTxn = mock(CashierTransaction.class);
            setupSharedTransactionMocks(cashier, cashierTxn);

            CommandProcessingResult result = underTest.allocateCashToCashier(1L, command);

            verify(fromApiJsonDeserializer, times(1)).validateForCashTxnForCashier(any());
            verify(cashierTxnRepository, times(1)).save(any(CashierTransaction.class));
            verify(glJournalEntryRepository, times(2)).saveAndFlush(any(JournalEntry.class));
            assertNotNull(result);
            assertEquals(1L, result.getCommandId());
            assertEquals(1L, result.getResourceId());
        }

        @Test
        void shouldThrowCashierNotFoundException() {
            when(cashierRepository.findById(anyLong())).thenThrow(new CashierNotFoundException(1L));

            assertThrows(CashierNotFoundException.class, () -> underTest.allocateCashToCashier(1L, command));
        }
    }

    @Nested
    class SettleCashFromCashier {

        @Test
        void shouldSettleCashFromCashier() {
            doNothing().when(cashierTransactionDataValidator).validateSettleCashAndCashOutTransactions(anyLong(), any(JsonCommand.class));

            Cashier cashier = mock(Cashier.class);
            CashierTransaction cashierTxn = mock(CashierTransaction.class);
            setupSharedTransactionMocks(cashier, cashierTxn);

            CommandProcessingResult result = underTest.settleCashFromCashier(1L, command);

            verify(cashierTransactionDataValidator, times(1)).validateSettleCashAndCashOutTransactions(anyLong(), any(JsonCommand.class));
            verify(fromApiJsonDeserializer, times(1)).validateForCashTxnForCashier(any());
            verify(cashierTxnRepository, times(1)).save(any(CashierTransaction.class));
            verify(glJournalEntryRepository, times(2)).saveAndFlush(any(JournalEntry.class));
            assertNotNull(result);
            assertEquals(1L, result.getCommandId());
            assertEquals(1L, result.getResourceId());
        }
    }

    private void setupSharedTransactionMocks(Cashier cashier, CashierTransaction cashierTxn) {
        when(cashierRepository.findById(anyLong())).thenReturn(Optional.of(cashier));
        doNothing().when(fromApiJsonDeserializer).validateForCashTxnForCashier(any());
        when(command.stringValueOfParameterNamed("entityType")).thenReturn("loan account");
        when(cashierTxnRepository.save(any(CashierTransaction.class))).thenReturn(cashierTxn);

        FinancialActivityAccount financialActivityAccount = mock(FinancialActivityAccount.class);
        when(financialActivityAccountRepositoryWrapper.findByFinancialActivityTypeWithNotFoundDetection(anyInt())).thenReturn(financialActivityAccount);

        GLAccount glAccount = mock(GLAccount.class);
        when(financialActivityAccount.getGlAccount()).thenReturn(glAccount);
        when(cashier.getTeller()).thenReturn(teller);
        when(teller.getOffice()).thenReturn(office);

        lenient().when(currentUser.getId()).thenReturn(1L);
        lenient().when(office.getId()).thenReturn(1L);

        lenient().when(cashierTxn.getCurrencyCode()).thenReturn("USD");
        lenient().when(cashierTxn.getTxnDate()).thenReturn(LocalDate.of(2026, 1, 1));
        lenient().when(cashierTxn.getTxnAmount()).thenReturn(BigDecimal.valueOf(100));
        lenient().when(cashierTxn.getTxnNote()).thenReturn("CASHIER ALLOCATION");

        JournalEntry journalEntry = mock(JournalEntry.class);
        when(glJournalEntryRepository.saveAndFlush(any(JournalEntry.class))).thenReturn(journalEntry);

        when(command.commandId()).thenReturn(1L);
        when(cashier.getId()).thenReturn(1L);
    }

    private JpaSystemException newException(final String message) {
        return new JpaSystemException(new RuntimeException(message));
    }

    private void assertDataIntegrityCode(String expectedCode, Runnable runnable) {
        PlatformDataIntegrityException ex = assertThrows(PlatformDataIntegrityException.class, runnable::run);
        assertEquals(expectedCode, ex.getGlobalisationMessageCode());
    }

    private Map<String, Object> getTellerChanges() {
        return Map.ofEntries(Map.entry("name", "NEW NAME"));
    }
}
