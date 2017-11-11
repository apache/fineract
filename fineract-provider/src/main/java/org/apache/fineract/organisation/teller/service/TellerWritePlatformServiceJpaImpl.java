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

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.accounting.common.AccountingConstants.FINANCIAL_ACTIVITY;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
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
import org.apache.fineract.organisation.teller.domain.CashierTxnType;
import org.apache.fineract.organisation.teller.domain.Teller;
import org.apache.fineract.organisation.teller.domain.TellerRepositoryWrapper;
import org.apache.fineract.organisation.teller.exception.CashierExistForTellerException;
import org.apache.fineract.organisation.teller.exception.CashierNotFoundException;
import org.apache.fineract.organisation.teller.serialization.TellerCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.client.domain.ClientTransaction;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TellerWritePlatformServiceJpaImpl implements TellerWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(TellerWritePlatformServiceJpaImpl.class);

    private final PlatformSecurityContext context;
    private final TellerCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final TellerRepositoryWrapper tellerRepositoryWrapper;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final StaffRepository staffRepository;
    private final CashierRepository cashierRepository;
    private final CashierTransactionRepository cashierTxnRepository;
    private final JournalEntryRepository glJournalEntryRepository;
    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper;
    private final CashierTransactionDataValidator cashierTransactionDataValidator;

    @Autowired
    public TellerWritePlatformServiceJpaImpl(final PlatformSecurityContext context,
            final TellerCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final TellerRepositoryWrapper tellerRepositoryWrapper, final OfficeRepositoryWrapper officeRepositoryWrapper,
            final StaffRepository staffRepository, CashierRepository cashierRepository, CashierTransactionRepository cashierTxnRepository,
            JournalEntryRepository glJournalEntryRepository,
            FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper,
            final CashierTransactionDataValidator cashierTransactionDataValidator) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.tellerRepositoryWrapper = tellerRepositoryWrapper;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.staffRepository = staffRepository;
        this.cashierRepository = cashierRepository;
        this.cashierTxnRepository = cashierTxnRepository;
        this.glJournalEntryRepository = glJournalEntryRepository;
        this.financialActivityAccountRepositoryWrapper = financialActivityAccountRepositoryWrapper;
        this.cashierTransactionDataValidator = cashierTransactionDataValidator;
    }

    @Override
    @Transactional
    public CommandProcessingResult createTeller(JsonCommand command) {
        try {
            this.context.authenticatedUser();

            final Long officeId = command.longValueOfParameterNamed("officeId");

            this.fromApiJsonDeserializer.validateForCreateAndUpdateTeller(command.json());

            // final Office parent =
            // validateUserPriviledgeOnOfficeAndRetrieve(currentUser, officeId);
            final Office tellerOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            final Teller teller = Teller.fromJson(tellerOffice, command);

            // pre save to generate id for use in office hierarchy
            this.tellerRepositoryWrapper.save(teller);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(teller.getId()) //
                    .withOfficeId(teller.getOffice().getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleTellerDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult modifyTeller(Long tellerId, JsonCommand command) {
        try {

            final Long officeId = command.longValueOfParameterNamed("officeId");
            final Office tellerOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreateAndUpdateTeller(command.json());

            final Teller teller = validateUserPriviledgeOnTellerAndRetrieve(currentUser, tellerId);

            final Map<String, Object> changes = teller.update(tellerOffice, command);

            if (!changes.isEmpty()) {
                this.tellerRepositoryWrapper.saveAndFlush(teller);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(teller.getId()) //
                    .withOfficeId(teller.officeId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleTellerDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    /*
     * used to restrict modifying operations to office that are either the users
     * office or lower (child) in the office hierarchy
     */
    private Teller validateUserPriviledgeOnTellerAndRetrieve(final AppUser currentUser, final Long tellerId) {

        final Long userOfficeId = currentUser.getOffice().getId();
        final Office userOffice = this.officeRepositoryWrapper.findOfficeHierarchy(userOfficeId);
        final Teller tellerToReturn = this.tellerRepositoryWrapper.findOneWithNotFoundDetection(tellerId);
        final Long tellerOfficeId = tellerToReturn.officeId();
        if (userOffice.doesNotHaveAnOfficeInHierarchyWithId(tellerOfficeId)) { throw new NoAuthorizationException(
                    "User does not have sufficient priviledges to act on the provided office."); }
        return tellerToReturn;
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteTeller(Long tellerId) {
        // TODO Auto-generated method stub

        Teller teller = tellerRepositoryWrapper.findOneWithNotFoundDetection(tellerId);
        Set<Cashier> isTellerIdPresentInCashier = teller.getCashiers();

        for (final Cashier tellerIdInCashier : isTellerIdPresentInCashier) {
            if (tellerIdInCashier.getTeller().getId().toString()
                    .equalsIgnoreCase(tellerId.toString())) { throw new CashierExistForTellerException(tellerId); }

        }
        tellerRepositoryWrapper.delete(teller);
        return new CommandProcessingResultBuilder() //
                .withEntityId(teller.getId()) //
                .build();

    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleTellerDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("m_tellers_name_unq")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.teller.duplicate.name", "Teller with name `" + name + "` already exists",
                    "name", name);
        }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.teller.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Override
    public CommandProcessingResult allocateCashierToTeller(final Long tellerId, JsonCommand command) {
        try {
            this.context.authenticatedUser();
            Long hourStartTime;
            Long minStartTime;
            Long hourEndTime;
            Long minEndTime;
            String startTime = " ";
            String endTime = " ";
            final Teller teller = this.tellerRepositoryWrapper.findOneWithNotFoundDetection(tellerId);
            final Office tellerOffice = teller.getOffice();

            final Long staffId = command.longValueOfParameterNamed("staffId");

            this.fromApiJsonDeserializer.validateForAllocateCashier(command.json());

            final Staff staff = this.staffRepository.findOne(staffId);
            if (staff == null) { throw new StaffNotFoundException(staffId); }
            final Boolean isFullDay = command.booleanObjectValueOfParameterNamed("isFullDay");
            if (!isFullDay) {
                hourStartTime = command.longValueOfParameterNamed("hourStartTime");
                minStartTime = command.longValueOfParameterNamed("minStartTime");

                if (minStartTime == 0)
                    startTime = hourStartTime.toString() + ":" + minStartTime.toString() + "0";
                else
                    startTime = hourStartTime.toString() + ":" + minStartTime.toString();

                hourEndTime = command.longValueOfParameterNamed("hourEndTime");
                minEndTime = command.longValueOfParameterNamed("minEndTime");
                if (minEndTime == 0)
                    endTime = hourEndTime.toString() + ":" + minEndTime.toString() + "0";
                else
                    endTime = hourEndTime.toString() + ":" + minEndTime.toString();

            }
            final Cashier cashier = Cashier.fromJson(tellerOffice, teller, staff, startTime, endTime, command);
            this.cashierTransactionDataValidator.validateCashierAllowedDateAndTime(cashier, teller);
            
            this.cashierRepository.save(cashier);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(teller.getId()) //
                    .withSubEntityId(cashier.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleTellerDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateCashierAllocation(Long tellerId, Long cashierId, JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForAllocateCashier(command.json());

            final Long staffId = command.longValueOfParameterNamed("staffId");
            final Staff staff = this.staffRepository.findOne(staffId);
            if (staff == null) { throw new StaffNotFoundException(staffId); }

            final Cashier cashier = validateUserPriviledgeOnCashierAndRetrieve(currentUser, tellerId, cashierId);

            cashier.setStaff(staff);

            // TODO - check if staff office and teller office match

            final Map<String, Object> changes = cashier.update(command);

            if (!changes.isEmpty()) {
                this.cashierRepository.saveAndFlush(cashier);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(cashier.getTeller().getId()) //
                    .withSubEntityId(cashier.getId()) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleTellerDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

    private Cashier validateUserPriviledgeOnCashierAndRetrieve(final AppUser currentUser, final Long tellerId, final Long cashierId) {

        validateUserPriviledgeOnTellerAndRetrieve(currentUser, tellerId);

        final Cashier cashierToReturn = this.cashierRepository.findOne(cashierId);

        return cashierToReturn;
    }

    @Override
    @Transactional
    public CommandProcessingResult deleteCashierAllocation(Long tellerId, Long cashierId, JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final Cashier cashier = validateUserPriviledgeOnCashierAndRetrieve(currentUser, tellerId, cashierId);
            this.cashierRepository.delete(cashier);

        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleTellerDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(cashierId) //
                .build();
    }

    /*
     * @Override public CommandProcessingResult inwardCashToCashier (final Long
     * cashierId, final CashierTransaction cashierTxn) { CashierTxnType txnType
     * = CashierTxnType.INWARD_CASH_TXN; // pre save to generate id for use in
     * office hierarchy this.cashierTxnRepository.save(cashierTxn); }
     */

    @Override
    public CommandProcessingResult allocateCashToCashier(final Long cashierId, JsonCommand command) {
    	return doTransactionForCashier(cashierId, CashierTxnType.ALLOCATE, command); // For
                                                                                     // fund
                                                                                     // allocation
                                                                                     // to
                                                                                     // cashier
    }

    @Override
    public CommandProcessingResult settleCashFromCashier(final Long cashierId, JsonCommand command) {
    	
    	this.cashierTransactionDataValidator.validateSettleCashAndCashOutTransactions(cashierId, command);
    	
    	return doTransactionForCashier(cashierId, CashierTxnType.SETTLE, command); // For
                                                                                   // fund
                                                                                   // settlement
                                                                                   // from
                                                                                   // cashier
    }

    private CommandProcessingResult doTransactionForCashier(final Long cashierId, final CashierTxnType txnType, JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();

            final Cashier cashier = this.cashierRepository.findOne(cashierId);
            if (cashier == null) { throw new CashierNotFoundException(cashierId); }

            this.fromApiJsonDeserializer.validateForCashTxnForCashier(command.json());

            final String entityType = command.stringValueOfParameterNamed("entityType");
            final Long entityId = command.longValueOfParameterNamed("entityId");
            if (entityType != null) {
                if (entityType.equals("loan account")) {
                    // TODO : Check if loan account exists
                    // LoanAccount loan = null;
                    // if (loan == null) { throw new
                    // LoanAccountFoundException(entityId); }
                } else if (entityType.equals("savings account")) {
                    // TODO : Check if loan account exists
                    // SavingsAccount savingsaccount = null;
                    // if (savingsaccount == null) { throw new
                    // SavingsAccountNotFoundException(entityId); }

                }
                if (entityType.equals("client")) {
                    // TODO: Check if client exists
                    // Client client = null;
                    // if (client == null) { throw new
                    // ClientNotFoundException(entityId); }
                } else {
                    // TODO : Invalid type handling
                }
            }

            final CashierTransaction cashierTxn = CashierTransaction.fromJson(cashier, command);
            cashierTxn.setTxnType(txnType.getId());

            this.cashierTxnRepository.save(cashierTxn);

            // Pass the journal entries
            FinancialActivityAccount mainVaultFinancialActivityAccount = this.financialActivityAccountRepositoryWrapper
                    .findByFinancialActivityTypeWithNotFoundDetection(FINANCIAL_ACTIVITY.CASH_AT_MAINVAULT.getValue());
            FinancialActivityAccount tellerCashFinancialActivityAccount = this.financialActivityAccountRepositoryWrapper
                    .findByFinancialActivityTypeWithNotFoundDetection(FINANCIAL_ACTIVITY.CASH_AT_TELLER.getValue());
            GLAccount creditAccount = null;
            GLAccount debitAccount = null;
            if (txnType.equals(CashierTxnType.ALLOCATE)) {
                debitAccount = tellerCashFinancialActivityAccount.getGlAccount();
                creditAccount = mainVaultFinancialActivityAccount.getGlAccount();
            } else if (txnType.equals(CashierTxnType.SETTLE)) {
                debitAccount = mainVaultFinancialActivityAccount.getGlAccount();
                creditAccount = tellerCashFinancialActivityAccount.getGlAccount();
            }

            final Office cashierOffice = cashier.getTeller().getOffice();

            final Long time = System.currentTimeMillis();
            final String uniqueVal = String.valueOf(time) + currentUser.getId() + cashierOffice.getId();
            final String transactionId = Long.toHexString(Long.parseLong(uniqueVal));
            ClientTransaction clientTransaction = null;
            final Long shareTransactionId = null;

            final JournalEntry debitJournalEntry = JournalEntry.createNew(cashierOffice, null, // payment
                                                                                               // detail
                    debitAccount, cashierTxn.getCurrencyCode(), 
                                         
                    transactionId, false, // manual entry
                    cashierTxn.getTxnDate(), JournalEntryType.DEBIT, cashierTxn.getTxnAmount(), cashierTxn.getTxnNote(), // Description
                    null, null, null, // entity Type, entityId, reference number
                    null, null, clientTransaction, shareTransactionId); // Loan and Savings Txn

            final JournalEntry creditJournalEntry = JournalEntry.createNew(cashierOffice, null, // payment
                                                                                                // detail
                    creditAccount, cashierTxn.getCurrencyCode(), 
                                          
                    transactionId, false, // manual entry
                    cashierTxn.getTxnDate(), JournalEntryType.CREDIT, cashierTxn.getTxnAmount(), cashierTxn.getTxnNote(), // Description
                    null, null, null, // entity Type, entityId, reference number
                    null, null, clientTransaction, shareTransactionId); // Loan and Savings Txn

            this.glJournalEntryRepository.saveAndFlush(debitJournalEntry);
            this.glJournalEntryRepository.saveAndFlush(creditJournalEntry);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(cashier.getId()) //
                    .withSubEntityId(cashierTxn.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleTellerDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch (final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
        	handleTellerDataIntegrityIssues(command, throwable, dve);
        	return CommandProcessingResult.empty();
        }
    }

}
