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

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.domain.savings.SavingsApproveBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.savings.SavingsCreateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.savings.SavingsRejectBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.CenterNotActiveException;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountDataDTO;
import org.apache.fineract.portfolio.savings.data.SavingsAccountDataValidator;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.GroupSavingsIndividualMonitoring;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountCharge;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargeAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsProductNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class SavingsApplicationProcessWritePlatformServiceJpaRepositoryImpl implements SavingsApplicationProcessWritePlatformService {

    private final PlatformSecurityContext context;
    private final SavingsAccountRepositoryWrapper savingAccountRepository;
    private final SavingsAccountAssembler savingAccountAssembler;
    private final SavingsAccountDataValidator savingsAccountDataValidator;
    private final AccountNumberGenerator accountNumberGenerator;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final SavingsProductRepository savingsProductRepository;
    private final NoteRepository noteRepository;
    private final StaffRepositoryWrapper staffRepository;
    private final SavingsAccountApplicationTransitionApiJsonValidator savingsAccountApplicationTransitionApiJsonValidator;
    private final SavingsAccountChargeAssembler savingsAccountChargeAssembler;
    private final CommandProcessingService commandProcessingService;
    private final SavingsAccountDomainService savingsAccountDomainService;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final GSIMRepositoy gsimRepository;
    private final GroupRepositoryWrapper groupRepositoryWrapper;
    private final GroupSavingsIndividualMonitoringWritePlatformService gsimWritePlatformService;

    @Transactional
    @Override
    public CommandProcessingResult submitGSIMApplication(final JsonCommand command) {

        CommandProcessingResult result = null;

        JsonArray gsimApplications = command.arrayOfParameterNamed("clientArray");

        final Object lock = new Object();
        synchronized (lock) {
            for (JsonElement gsimApplication : gsimApplications) {
                // result=submitApplication(JsonCommand.fromExistingCommand(command,
                // gsimApplication));
                result = submitApplication(JsonCommand.fromExistingCommand(command, gsimApplication,
                        gsimApplication.getAsJsonObject().get("clientId").getAsLong()));
            }
        }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult submitApplication(final JsonCommand command) {
        try {
            this.savingsAccountDataValidator.validateForSubmit(command.json());
            final AppUser submittedBy = this.context.authenticatedUser();

            final SavingsAccount account = this.savingAccountAssembler.assembleFrom(command, submittedBy);
            this.savingAccountRepository.save(account);
            String accountNumber = "";
            GroupSavingsIndividualMonitoring gsimAccount = null;
            BigDecimal applicationId = BigDecimal.ZERO;
            Boolean isLastChildApplication = false;

            // gsim
            if (account.isAccountNumberRequiresAutoGeneration()) {

                final AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository
                        .findByAccountType(EntityAccountType.SAVINGS);
                // if application is of GSIM type
                if (account.getAccountTypes() == 5) {
                    final Long groupId = command.longValueOfParameterNamed("groupId");
                    // GSIM specific parameters
                    if (command.bigDecimalValueOfParameterNamedDefaultToNullIfZero("applicationId") != null) {
                        applicationId = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero("applicationId");
                    }

                    if (command.booleanObjectValueOfParameterNamed("lastApplication") != null) {
                        isLastChildApplication = command.booleanPrimitiveValueOfParameterNamed("lastApplication");
                    }

                    Group group = this.groupRepositoryWrapper.findOneWithNotFoundDetection(groupId);

                    if (command.booleanObjectValueOfParameterNamed("isParentAccount") != null) {
                        // empty table check
                        if (gsimRepository.count() != 0) {
                            // Parent-Not an empty table

                            accountNumber = this.accountNumberGenerator.generate(account, accountNumberFormat);
                            account.updateAccountNo(accountNumber + "1");
                            gsimAccount = gsimWritePlatformService.addGSIMAccountInfo(accountNumber, group, BigDecimal.ZERO,
                                    Long.valueOf(1), true, SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue(),
                                    applicationId);
                            account.setGsim(gsimAccount);
                            this.savingAccountRepository.saveAndFlush(account);

                        } else {
                            // Parent-empty table
                            accountNumber = this.accountNumberGenerator.generate(account, accountNumberFormat);
                            account.updateAccountNo(accountNumber + "1");
                            gsimWritePlatformService.addGSIMAccountInfo(accountNumber, group, BigDecimal.ZERO, Long.valueOf(1), true,
                                    SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue(), applicationId);
                            account.setGsim(gsimRepository.findOneByAccountNumber(accountNumber));
                            this.savingAccountRepository.saveAndFlush(account);
                        }
                    } else {
                        if (gsimRepository.count() != 0) {
                            // Child-Not an empty table check
                            gsimAccount = gsimRepository.findOneByIsAcceptingChildAndApplicationId(true, applicationId);
                            accountNumber = gsimAccount.getAccountNumber() + (gsimAccount.getChildAccountsCount() + 1);
                            account.updateAccountNo(accountNumber);
                            this.gsimWritePlatformService.incrementChildAccountCount(gsimAccount);
                            account.setGsim(gsimAccount);
                            this.savingAccountRepository.saveAndFlush(account);

                        } else {
                            // Child-empty table
                            // if the gsim info is empty set the current account
                            // as parent
                            accountNumber = this.accountNumberGenerator.generate(account, accountNumberFormat);
                            account.updateAccountNo(accountNumber + "1");
                            gsimWritePlatformService.addGSIMAccountInfo(accountNumber, group, BigDecimal.ZERO, Long.valueOf(1), true,
                                    SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue(), applicationId);
                            account.setGsim(gsimAccount);
                            this.savingAccountRepository.saveAndFlush(account);
                        }
                        // reset isAcceptingChild when processing last
                        // application of GSIM
                        if (isLastChildApplication) {
                            this.gsimWritePlatformService
                                    .resetIsAcceptingChild(gsimRepository.findOneByIsAcceptingChildAndApplicationId(true, applicationId));
                        }
                    }
                } else {
                    // for applications other than GSIM
                    generateAccountNumber(account);
                }
            }
            // end of gsim
            final Long savingsId = account.getId();
            if (command.parameterExists(SavingsApiConstants.datatables)) {
                this.entityDatatableChecksWritePlatformService.saveDatatables(StatusEnum.CREATE.getCode().longValue(),
                        EntityTables.SAVINGS.getName(), savingsId, account.productId(),
                        command.arrayOfParameterNamed(SavingsApiConstants.datatables));
            }
            this.entityDatatableChecksWritePlatformService.runTheCheckForProduct(savingsId, EntityTables.SAVINGS.getName(),
                    StatusEnum.CREATE.getCode().longValue(), EntityTables.SAVINGS.getForeignKeyColumnNameOnDatatable(),
                    account.productId());

            businessEventNotifierService.notifyPostBusinessEvent(new SavingsCreateBusinessEvent(account));

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(savingsId) //
                    .withOfficeId(account.officeId()) //
                    .withClientId(account.clientId()) //
                    .withGroupId(account.groupId()) //
                    .withSavingsId(savingsId) //
                    .withGsimId(gsimAccount == null ? 0 : gsimAccount.getId()).build();
        } catch (final DataAccessException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    private void generateAccountNumber(final SavingsAccount account) {
        if (account.isAccountNumberRequiresAutoGeneration()) {
            final AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository.findByAccountType(EntityAccountType.SAVINGS);
            account.updateAccountNo(this.accountNumberGenerator.generate(account, accountNumberFormat));

            this.savingAccountRepository.saveAndFlush(account);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult modifyGSIMApplication(final Long gsimId, final JsonCommand command) {

        final Long parentSavingId = gsimId;
        List<SavingsAccount> childSavings = this.savingAccountRepository.findByGsimId(parentSavingId);

        CommandProcessingResult result = null;

        for (SavingsAccount account : childSavings) {
            result = modifyApplication(account.getId(), command);
        }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult modifyApplication(final Long savingsId, final JsonCommand command) {
        try {
            this.savingsAccountDataValidator.validateForUpdate(command.json());

            final Map<String, Object> changes = new LinkedHashMap<>(20);

            final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
            checkClientOrGroupActive(account);
            account.modifyApplication(command, changes);
            account.validateNewApplicationState(SAVINGS_ACCOUNT_RESOURCE_NAME);
            account.validateAccountValuesWithProduct();

            if (!changes.isEmpty()) {

                if (changes.containsKey(SavingsApiConstants.clientIdParamName)) {
                    final Long clientId = command.longValueOfParameterNamed(SavingsApiConstants.clientIdParamName);
                    if (clientId != null) {
                        final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
                        if (client.isNotActive()) {
                            throw new ClientNotActiveException(clientId);
                        }
                        account.update(client);
                    } else {
                        final Client client = null;
                        account.update(client);
                    }
                }

                if (changes.containsKey(SavingsApiConstants.groupIdParamName)) {
                    final Long groupId = command.longValueOfParameterNamed(SavingsApiConstants.groupIdParamName);
                    if (groupId != null) {
                        final Group group = this.groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
                        if (group.isNotActive()) {
                            if (group.isCenter()) {
                                throw new CenterNotActiveException(groupId);
                            }
                            throw new GroupNotActiveException(groupId);
                        }
                        account.update(group);
                    } else {
                        final Group group = null;
                        account.update(group);
                    }
                }

                if (changes.containsKey(SavingsApiConstants.productIdParamName)) {
                    final Long productId = command.longValueOfParameterNamed(SavingsApiConstants.productIdParamName);
                    final SavingsProduct product = this.savingsProductRepository.findById(productId)
                            .orElseThrow(() -> new SavingsProductNotFoundException(productId));
                    account.update(product);
                }

                if (changes.containsKey(SavingsApiConstants.fieldOfficerIdParamName)) {
                    final Long fieldOfficerId = command.longValueOfParameterNamed(SavingsApiConstants.fieldOfficerIdParamName);
                    Staff fieldOfficer = null;
                    if (fieldOfficerId != null) {
                        fieldOfficer = this.staffRepository.findOneWithNotFoundDetection(fieldOfficerId);
                    } else {
                        changes.put(SavingsApiConstants.fieldOfficerIdParamName, "");
                    }
                    account.update(fieldOfficer);
                }

                if (changes.containsKey("charges")) {
                    final Set<SavingsAccountCharge> charges = this.savingsAccountChargeAssembler.fromParsedJson(command.parsedJson(),
                            account.getCurrency().getCode());
                    final boolean updated = account.update(charges);
                    if (!updated) {
                        changes.remove("charges");
                    }
                }

                this.savingAccountRepository.saveAndFlush(account);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(savingsId) //
                    .withOfficeId(account.officeId()) //
                    .withClientId(account.clientId()) //
                    .withGroupId(account.groupId()) //
                    .withSavingsId(savingsId) //
                    .with(changes) //
                    .build();
        } catch (final DataAccessException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.resourceResult(-1L);
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteApplication(final Long savingsId) {

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(account);

        if (account.isNotSubmittedAndPendingApproval()) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                    .resource(SAVINGS_ACCOUNT_RESOURCE_NAME + SavingsApiConstants.deleteApplicationAction);

            baseDataValidator.reset().parameter(SavingsApiConstants.activatedOnDateParamName)
                    .failWithCodeNoParameterAddedToErrorCode("not.in.submittedandpendingapproval.state");

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }

        final List<Note> relatedNotes = this.noteRepository.findBySavingsAccount(account);
        this.noteRepository.deleteAllInBatch(relatedNotes);

        this.savingAccountRepository.delete(account);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult approveGSIMApplication(final Long gsimId, final JsonCommand command) {

        // GroupLoanIndividualMonitoringAccount
        // glimAccount=glimRepository.findOne(loanId);
        Long parentSavingId = gsimId;
        GroupSavingsIndividualMonitoring parentSavings = gsimRepository.findById(parentSavingId).orElseThrow();
        List<SavingsAccount> childSavings = this.savingAccountRepository.findByGsimId(gsimId);
        CommandProcessingResult result = null;
        int count = 0;
        for (SavingsAccount account : childSavings) {

            result = approveApplication(account.getId(), command);

            if (result != null) {
                count++;
                if (count == parentSavings.getChildAccountsCount()) {
                    parentSavings.setSavingsStatus(SavingsAccountStatusType.APPROVED.getValue());
                    gsimRepository.save(parentSavings);
                }
            }
        }
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult approveApplication(final Long savingsId, final JsonCommand command) {

        final AppUser currentUser = this.context.authenticatedUser();

        this.savingsAccountApplicationTransitionApiJsonValidator.validateApproval(command.json());

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(savingsAccount);

        entityDatatableChecksWritePlatformService.runTheCheckForProduct(savingsId, EntityTables.SAVINGS.getName(),
                StatusEnum.APPROVE.getCode().longValue(), EntityTables.SAVINGS.getForeignKeyColumnNameOnDatatable(),
                savingsAccount.productId());

        final Map<String, Object> changes = savingsAccount.approveApplication(currentUser, command);
        if (!changes.isEmpty()) {
            this.savingAccountRepository.save(savingsAccount);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.savingNote(savingsAccount, noteText);
                changes.put("note", noteText);
                this.noteRepository.save(note);
            }
        }

        businessEventNotifierService.notifyPostBusinessEvent(new SavingsApproveBusinessEvent(savingsAccount));

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(savingsId) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult undoGSIMApplicationApproval(final Long gsimId, final JsonCommand command) {
        final Long parentSavingId = gsimId;
        GroupSavingsIndividualMonitoring parentSavings = gsimRepository.findById(parentSavingId).orElseThrow();
        List<SavingsAccount> childSavings = this.savingAccountRepository.findByGsimId(gsimId);

        CommandProcessingResult result = null;
        int count = 0;
        for (SavingsAccount account : childSavings) {
            result = undoApplicationApproval(account.getId(), command);

            if (result != null) {
                count++;
                if (count == parentSavings.getChildAccountsCount()) {
                    parentSavings.setSavingsStatus(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue());
                    gsimRepository.save(parentSavings);
                }
            }

        }

        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult undoApplicationApproval(final Long savingsId, final JsonCommand command) {

        this.context.authenticatedUser();

        this.savingsAccountApplicationTransitionApiJsonValidator.validateForUndo(command.json());

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(savingsAccount);

        final Map<String, Object> changes = savingsAccount.undoApplicationApproval();
        if (!changes.isEmpty()) {
            this.savingAccountRepository.save(savingsAccount);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.savingNote(savingsAccount, noteText);
                changes.put("note", noteText);
                this.noteRepository.save(note);
            }
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(savingsId) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult rejectGSIMApplication(final Long gsimId, final JsonCommand command) {

        final Long parentSavingId = gsimId;
        GroupSavingsIndividualMonitoring parentSavings = gsimRepository.findById(parentSavingId).orElseThrow();
        List<SavingsAccount> childSavings = this.savingAccountRepository.findByGsimId(gsimId);

        CommandProcessingResult result = null;
        int count = 0;
        for (SavingsAccount account : childSavings) {
            result = rejectApplication(account.getId(), command);

            if (result != null) {
                count++;
                if (count == parentSavings.getChildAccountsCount()) {
                    parentSavings.setSavingsStatus(SavingsAccountStatusType.REJECTED.getValue());
                    gsimRepository.save(parentSavings);
                }
            }
        }
        return result;
    }

    @Transactional
    @Override
    public CommandProcessingResult rejectApplication(final Long savingsId, final JsonCommand command) {

        final AppUser currentUser = this.context.authenticatedUser();

        this.savingsAccountApplicationTransitionApiJsonValidator.validateRejection(command.json());

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(savingsAccount);

        entityDatatableChecksWritePlatformService.runTheCheckForProduct(savingsId, EntityTables.SAVINGS.getName(),
                StatusEnum.REJECTED.getCode().longValue(), EntityTables.SAVINGS.getForeignKeyColumnNameOnDatatable(),
                savingsAccount.productId());

        final Map<String, Object> changes = savingsAccount.rejectApplication(currentUser, command);
        if (!changes.isEmpty()) {
            this.savingAccountRepository.save(savingsAccount);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.savingNote(savingsAccount, noteText);
                changes.put("note", noteText);
                this.noteRepository.save(note);
            }
        }
        businessEventNotifierService.notifyPostBusinessEvent(new SavingsRejectBusinessEvent(savingsAccount));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(savingsId) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult applicantWithdrawsFromApplication(final Long savingsId, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();

        this.savingsAccountApplicationTransitionApiJsonValidator.validateApplicantWithdrawal(command.json());

        final SavingsAccount savingsAccount = this.savingAccountAssembler.assembleFrom(savingsId, false);
        checkClientOrGroupActive(savingsAccount);

        entityDatatableChecksWritePlatformService.runTheCheckForProduct(savingsId, EntityTables.SAVINGS.getName(),
                StatusEnum.WITHDRAWN.getCode().longValue(), EntityTables.SAVINGS.getForeignKeyColumnNameOnDatatable(),
                savingsAccount.productId());

        final Map<String, Object> changes = savingsAccount.applicantWithdrawsFromApplication(currentUser, command);
        if (!changes.isEmpty()) {
            this.savingAccountRepository.save(savingsAccount);

            final String noteText = command.stringValueOfParameterNamed("note");
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.savingNote(savingsAccount, noteText);
                changes.put("note", noteText);
                this.noteRepository.save(note);
            }
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(savingsId) //
                .withOfficeId(savingsAccount.officeId()) //
                .withClientId(savingsAccount.clientId()) //
                .withGroupId(savingsAccount.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes) //
                .build();
    }

    private void checkClientOrGroupActive(final SavingsAccount account) {
        final Client client = account.getClient();
        if (client != null) {
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }
        final Group group = account.group();
        if (group != null) {
            if (group.isNotActive()) {
                if (group.isCenter()) {
                    throw new CenterNotActiveException(group.getId());
                }
                throw new GroupNotActiveException(group.getId());
            }
        }
    }

    @Override
    public CommandProcessingResult createActiveApplication(final SavingsAccountDataDTO savingsAccountDataDTO) {

        final CommandWrapper commandWrapper = new CommandWrapperBuilder().savingsAccountActivation(null).build();
        boolean rollbackTransaction = this.commandProcessingService.validateRollbackCommand(commandWrapper,
                savingsAccountDataDTO.getAppliedBy());

        final SavingsAccount account = this.savingAccountAssembler.assembleFrom(savingsAccountDataDTO.getClient(),
                savingsAccountDataDTO.getGroup(), savingsAccountDataDTO.getSavingsProduct(), savingsAccountDataDTO.getApplicationDate(),
                savingsAccountDataDTO.getAppliedBy());
        account.approveAndActivateApplication(savingsAccountDataDTO.getApplicationDate(), savingsAccountDataDTO.getAppliedBy());
        Money amountForDeposit = account.activateWithBalance();

        final Set<Long> existingTransactionIds = new HashSet<>();
        final Set<Long> existingReversedTransactionIds = new HashSet<>();

        if (amountForDeposit.isGreaterThanZero()) {
            this.savingAccountRepository.save(account);
        }
        this.savingsAccountWritePlatformService.processPostActiveActions(account, savingsAccountDataDTO.getFmt(), existingTransactionIds,
                existingReversedTransactionIds);
        this.savingAccountRepository.saveAndFlush(account);

        generateAccountNumber(account);
        // post journal entries for activation charges
        this.savingsAccountDomainService.postJournalEntries(account, existingTransactionIds, existingReversedTransactionIds, false);

        return new CommandProcessingResultBuilder() //
                .withSavingsId(account.getId()) //
                .setRollbackTransaction(rollbackTransaction)//
                .build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        String msgCode = "error.msg." + SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME;
        String msg = "Unknown data integrity issue with savings account.";
        String param = null;
        Object[] msgArgs;
        Throwable checkEx = realCause == null ? dve : realCause;
        if (checkEx.getMessage().contains("sa_account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            msgCode += ".duplicate.accountNo";
            msg = "Savings account with accountNo " + accountNo + " already exists";
            param = "accountNo";
            msgArgs = new Object[] { accountNo, dve };
        } else if (checkEx.getMessage().contains("sa_externalid_UNIQUE")) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            msgCode += ".duplicate.externalId";
            msg = "Savings account with externalId " + externalId + " already exists";
            param = "externalId";
            msgArgs = new Object[] { externalId, dve };
        } else {
            msgCode += ".unknown.data.integrity.issue";
            msgArgs = new Object[] { dve };
        }
        log.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, msgCode, msg, param, msgArgs);
    }
}
