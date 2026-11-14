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
package org.apache.fineract.portfolio.client.service;

import com.google.gson.JsonElement;
import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.exception.ErrorHandler;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientActivateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientCreateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountNumberGenerator;
import org.apache.fineract.portfolio.address.service.AddressWritePlatformService;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.ClientDataValidator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.client.domain.ClientNonPerson;
import org.apache.fineract.portfolio.client.domain.ClientNonPersonRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.ClientActiveForUpdateException;
import org.apache.fineract.portfolio.client.exception.ClientMustBePendingToBeDeletedException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsProductNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates, updates and deletes clients.
 */
@AllArgsConstructor
@Service
@Slf4j
public class ClientCreateUpdateWritePlatformService {

    private final PlatformSecurityContext context;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientNonPersonRepositoryWrapper clientNonPersonRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final NoteRepository noteRepository;
    private final GroupRepository groupRepository;
    private final ClientDataValidator fromApiJsonDeserializer;
    private final AccountNumberGenerator accountNumberGenerator;
    private final StaffRepositoryWrapper staffRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final SavingsProductRepository savingsProductRepository;
    private final CommandProcessingService commandProcessingService;
    private final ConfigurationDomainService configurationDomainService;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final AddressWritePlatformService addressWritePlatformService;
    private final ClientFamilyMembersWritePlatformService clientFamilyMembersWritePlatformService;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final ExternalIdFactory externalIdFactory;
    private final ClientActivationHelper clientActivationHelper;

    @Transactional
    public CommandProcessingResult deleteClient(final Long clientId) {
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

            if (client.isNotPending()) {
                throw new ClientMustBePendingToBeDeletedException(clientId);
            }
            final List<Note> relatedNotes = this.noteRepository.findByClient(client);
            this.noteRepository.deleteAllInBatch(relatedNotes);

            final ClientNonPerson clientNonPerson = this.clientNonPersonRepository.findOneByClientId(clientId);
            if (clientNonPerson != null) {
                this.clientNonPersonRepository.delete(clientNonPerson);
            }

            this.clientRepository.delete(client);
            this.clientRepository.flush();
            return new CommandProcessingResultBuilder() //
                    .withOfficeId(client.officeId()) //
                    .withEntityExternalId(client.getExternalId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            log.error("Error occurred.", throwable);
            throw ErrorHandler.getMappable(dve, "error.msg.client.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    @Transactional
    public CommandProcessingResult createClient(final JsonCommand command) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Boolean isAddressEnabled = configurationDomainService.isAddressEnabled();

            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);

            final Office clientOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);

            final Long groupId = command.longValueOfParameterNamed(ClientApiConstants.groupIdParamName);

            Group clientParentGroup = null;
            if (groupId != null) {
                clientParentGroup = this.groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
            }

            Staff staff = null;
            final Long staffId = command.longValueOfParameterNamed(ClientApiConstants.staffIdParamName);
            if (staffId != null) {
                staff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(staffId, clientOffice.getHierarchy());
            }

            CodeValue gender = null;
            final Long genderId = command.longValueOfParameterNamed(ClientApiConstants.genderIdParamName);
            if (genderId != null) {
                gender = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.GENDER, genderId);
            }

            CodeValue clientType = null;
            final Long clientTypeId = command.longValueOfParameterNamed(ClientApiConstants.clientTypeIdParamName);
            if (clientTypeId != null) {
                clientType = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_TYPE,
                        clientTypeId);
            }

            CodeValue clientClassification = null;
            final Long clientClassificationId = command.longValueOfParameterNamed(ClientApiConstants.clientClassificationIdParamName);
            if (clientClassificationId != null) {
                clientClassification = this.codeValueRepository
                        .findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_CLASSIFICATION, clientClassificationId);
            }

            final Long savingsProductId = command.longValueOfParameterNamed(ClientApiConstants.savingsProductIdParamName);
            if (savingsProductId != null) {
                this.savingsProductRepository.findById(savingsProductId)
                        .orElseThrow(() -> new SavingsProductNotFoundException(savingsProductId));
            }

            boolean isEntity = false;
            LegalForm legalForm = null;
            final Integer legalFormParamValue = command.integerValueOfParameterNamed(ClientApiConstants.legalFormIdParamName);
            if (legalFormParamValue != null) {
                legalForm = LegalForm.fromInt(legalFormParamValue);
                if (legalForm != null) {
                    isEntity = legalForm.isEntity();
                }
            }
            if (legalForm == null) {
                legalForm = LegalForm.PERSON;
            }

            final String accountNo = command.stringValueOfParameterNamed(ClientApiConstants.accountNoParamName);
            final String mobileNo = command.stringValueOfParameterNamed(ClientApiConstants.mobileNoParamName);
            final String emailAddress = command.stringValueOfParameterNamed(ClientApiConstants.emailAddressParamName);
            final String firstname = command.stringValueOfParameterNamed(ClientApiConstants.firstnameParamName);
            final String middlename = command.stringValueOfParameterNamed(ClientApiConstants.middlenameParamName);
            final String lastname = command.stringValueOfParameterNamed(ClientApiConstants.lastnameParamName);
            final String fullname = command.stringValueOfParameterNamed(ClientApiConstants.fullnameParamName);
            final boolean isStaff = command.booleanPrimitiveValueOfParameterNamed(ClientApiConstants.isStaffParamName);
            final LocalDate dataOfBirth = command.localDateValueOfParameterNamed(ClientApiConstants.dateOfBirthParamName);

            ClientStatus status = ClientStatus.PENDING;
            boolean active = false;
            if (command.hasParameter("active")) {
                active = command.booleanPrimitiveValueOfParameterNamed(ClientApiConstants.activeParamName);
            }

            LocalDate activationDate = null;
            LocalDate officeJoiningDate = null;
            if (active) {
                status = ClientStatus.ACTIVE;
                activationDate = command.localDateValueOfParameterNamed(ClientApiConstants.activationDateParamName);
                officeJoiningDate = activationDate;
            }

            LocalDate submittedOnDate = DateUtils.getBusinessLocalDate();
            if (command.hasParameter(ClientApiConstants.submittedOnDateParamName)) {
                submittedOnDate = command.localDateValueOfParameterNamed(ClientApiConstants.submittedOnDateParamName);
            }
            if (active && DateUtils.isAfter(submittedOnDate, activationDate)) {
                submittedOnDate = activationDate;
            }
            final Long savingsAccountId = null;

            final ExternalId externalId = externalIdFactory.createFromCommand(command, ClientApiConstants.externalIdParamName);

            final Client newClient = Client.instance(currentUser, status, clientOffice, clientParentGroup, accountNo, firstname, middlename,
                    lastname, fullname, activationDate, officeJoiningDate, externalId, mobileNo, emailAddress, staff, submittedOnDate,
                    savingsProductId, savingsAccountId, dataOfBirth, gender, clientType, clientClassification, legalForm.getValue(),
                    isStaff);

            // Account Number generation
            this.clientRepository.saveAndFlush(newClient);
            if (StringUtils.isBlank(accountNo)) {
                AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository.findByAccountType(EntityAccountType.CLIENT);
                newClient.updateAccountNo(accountNumberGenerator.generate(newClient, accountNumberFormat));
                this.clientRepository.saveAndFlush(newClient);
            }

            boolean rollbackTransaction = false;
            if (newClient.isActive()) {
                this.clientActivationHelper.validateParentGroupRulesBeforeClientActivation(newClient);
                this.clientActivationHelper.runEntityDatatableCheck(newClient.getId(), newClient.getLegalForm());
                final CommandWrapper commandWrapper = new CommandWrapperBuilder().activateClient(null).build();
                rollbackTransaction = this.commandProcessingService.validateRollbackCommand(commandWrapper, currentUser);
            }
            this.clientRepository.saveAndFlush(newClient);

            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
            CommandProcessingResult result = this.clientActivationHelper.openSavingsAccount(newClient, fmt);
            if (result.getSavingsId() != null) {
                this.clientRepository.saveAndFlush(newClient);
            }

            if (isEntity) {
                extractAndCreateClientNonPerson(newClient, command);
            }

            if (isAddressEnabled) {
                this.addressWritePlatformService.addNewClientAddress(newClient, command);
            }

            if (command.arrayOfParameterNamed("familyMembers") != null) {
                this.clientFamilyMembersWritePlatformService.addClientFamilyMember(newClient, command);
            }

            if (command.parameterExists(ClientApiConstants.datatables)) {
                this.entityDatatableChecksWritePlatformService.saveDatatables(StatusEnum.CREATE.getValue(), EntityTables.CLIENT.getName(),
                        newClient.getId(), null, command.arrayOfParameterNamed(ClientApiConstants.datatables));
            }

            legalForm = LegalForm.fromInt(newClient.getLegalForm());
            entityDatatableChecksWritePlatformService.runTheCheck(newClient.getId(), EntityTables.CLIENT.getName(),
                    StatusEnum.CREATE.getValue(), EntityTables.CLIENT.getForeignKeyColumnNameOnDatatable(), legalForm.getLabel());
            businessEventNotifierService.notifyPostBusinessEvent(new ClientCreateBusinessEvent(newClient));
            if (newClient.isActive()) {
                businessEventNotifierService.notifyPostBusinessEvent(new ClientActivateBusinessEvent(newClient));
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityExternalId(newClient.getExternalId()) //
                    .withOfficeId(clientOffice.getId()) //
                    .withClientId(newClient.getId()) //
                    .withGroupId(groupId) //
                    .withEntityId(newClient.getId()) //
                    .withSavingsId(result.getSavingsId()) //
                    .setRollbackTransaction(rollbackTransaction) //
                    .setRollbackTransaction(result.isRollbackTransaction()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            ClientDataIntegrityIssueHandler.handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            ClientDataIntegrityIssueHandler.handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    /**
     * This method extracts ClientNonPerson details from Client command and creates a new ClientNonPerson record
     *
     * @param client
     * @param command
     */
    public void extractAndCreateClientNonPerson(Client client, JsonCommand command) {
        final JsonElement clientNonPersonElement = this.fromApiJsonHelper
                .parse(command.jsonFragment(ClientApiConstants.clientNonPersonDetailsParamName));

        if (clientNonPersonElement != null && !isEmpty(clientNonPersonElement)) {
            final String incorpNumber = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.incorpNumberParamName,
                    clientNonPersonElement);
            final String remarks = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.remarksParamName, clientNonPersonElement);
            final LocalDate incorpValidityTill = this.fromApiJsonHelper
                    .extractLocalDateNamed(ClientApiConstants.incorpValidityTillParamName, clientNonPersonElement);

            CodeValue clientNonPersonConstitution = null;
            final Long clientNonPersonConstitutionId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.constitutionIdParamName,
                    clientNonPersonElement);
            if (clientNonPersonConstitutionId != null) {
                clientNonPersonConstitution = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                        ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION, clientNonPersonConstitutionId);
            }

            CodeValue clientNonPersonMainBusinessLine = null;
            final Long clientNonPersonMainBusinessLineId = this.fromApiJsonHelper
                    .extractLongNamed(ClientApiConstants.mainBusinessLineIdParamName, clientNonPersonElement);
            if (clientNonPersonMainBusinessLineId != null) {
                clientNonPersonMainBusinessLine = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                        ClientApiConstants.CLIENT_NON_PERSON_MAIN_BUSINESS_LINE, clientNonPersonMainBusinessLineId);
            }

            final ClientNonPerson newClientNonPerson = ClientNonPerson.createNew(client, clientNonPersonConstitution,
                    clientNonPersonMainBusinessLine, incorpNumber, incorpValidityTill, remarks);

            this.clientNonPersonRepository.save(newClientNonPerson);
        }
    }

    public boolean isEmpty(final JsonElement element) {
        return element.toString().trim().length() < 4;
    }

    @Transactional
    public CommandProcessingResult updateClient(final Long clientId, final JsonCommand command) {

        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final String clientHierarchy = clientForUpdate.getOffice().getHierarchy();

            this.context.validateAccessRights(clientHierarchy);

            final Map<String, Object> changes = new LinkedHashMap<>(9);

            if (command.isChangeInIntegerParameterNamed(ClientApiConstants.statusParamName, clientForUpdate.getStatus())) {
                final Integer newValue = command.integerValueOfParameterNamed(ClientApiConstants.statusParamName);
                changes.put(ClientApiConstants.statusParamName, ClientEnumerations.status(newValue));
                clientForUpdate.setStatus(ClientStatus.fromInt(newValue).getValue());
            }

            if (command.isChangeInStringParameterNamed(ClientApiConstants.accountNoParamName, clientForUpdate.getAccountNumber())) {
                final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.accountNoParamName);
                changes.put(ClientApiConstants.accountNoParamName, newValue);
                clientForUpdate.setAccountNumber(StringUtils.defaultIfEmpty(newValue, null));
            }

            final ExternalId externalId = externalIdFactory.createFromCommand(command, ClientApiConstants.externalIdParamName);
            if (command.isChangeInStringParameterNamed(ClientApiConstants.externalIdParamName,
                    clientForUpdate.getExternalId().getValue())) {
                changes.put(ClientApiConstants.externalIdParamName, externalId.getValue());
                clientForUpdate.setExternalId(externalId);
            }

            if (command.isChangeInStringParameterNamed(ClientApiConstants.mobileNoParamName, clientForUpdate.getMobileNo())) {
                final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.mobileNoParamName);
                changes.put(ClientApiConstants.mobileNoParamName, newValue);
                clientForUpdate.setMobileNo(StringUtils.defaultIfEmpty(newValue, null));
            }

            if (command.isChangeInStringParameterNamed(ClientApiConstants.emailAddressParamName, clientForUpdate.getEmailAddress())) {
                final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.emailAddressParamName);
                changes.put(ClientApiConstants.emailAddressParamName, newValue);
                clientForUpdate.setEmailAddress(StringUtils.defaultIfEmpty(newValue, null));
            }

            if (command.isChangeInStringParameterNamed(ClientApiConstants.firstnameParamName, clientForUpdate.getFirstname())) {
                final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.firstnameParamName);
                changes.put(ClientApiConstants.firstnameParamName, newValue);
                clientForUpdate.setFirstname(StringUtils.defaultIfEmpty(newValue, null));
            }

            if (command.isChangeInStringParameterNamed(ClientApiConstants.middlenameParamName, clientForUpdate.getMiddlename())) {
                final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.middlenameParamName);
                changes.put(ClientApiConstants.middlenameParamName, newValue);
                clientForUpdate.setMiddlename(StringUtils.defaultIfEmpty(newValue, null));
            }

            if (command.isChangeInStringParameterNamed(ClientApiConstants.lastnameParamName, clientForUpdate.getLastname())) {
                final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.lastnameParamName);
                changes.put(ClientApiConstants.lastnameParamName, newValue);
                clientForUpdate.setLastname(StringUtils.defaultIfEmpty(newValue, null));
            }

            if (command.isChangeInStringParameterNamed(ClientApiConstants.fullnameParamName, clientForUpdate.getFullname())) {
                final String newValue = command.stringValueOfParameterNamed(ClientApiConstants.fullnameParamName);
                changes.put(ClientApiConstants.fullnameParamName, newValue);
                clientForUpdate.setFullname(newValue);
            }

            if (command.isChangeInLongParameterNamed(ClientApiConstants.staffIdParamName, clientForUpdate.staffId())) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.staffIdParamName);
                changes.put(ClientApiConstants.staffIdParamName, newValue);
            }

            if (command.isChangeInLongParameterNamed(ClientApiConstants.genderIdParamName, clientForUpdate.genderId())) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.genderIdParamName);
                changes.put(ClientApiConstants.genderIdParamName, newValue);
            }

            if (command.isChangeInLongParameterNamed(ClientApiConstants.savingsProductIdParamName, clientForUpdate.savingsProductId())) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.savingsProductIdParamName);
                changes.put(ClientApiConstants.savingsProductIdParamName, newValue);
            }

            if (command.isChangeInLongParameterNamed(ClientApiConstants.clientTypeIdParamName, clientForUpdate.clientTypeId())) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.clientTypeIdParamName);
                changes.put(ClientApiConstants.clientTypeIdParamName, newValue);
            }

            if (command.isChangeInLongParameterNamed(ClientApiConstants.clientClassificationIdParamName,
                    clientForUpdate.clientClassificationId())) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.clientClassificationIdParamName);
                changes.put(ClientApiConstants.clientClassificationIdParamName, newValue);
            }

            if (command.isChangeInIntegerParameterNamed(ClientApiConstants.legalFormIdParamName, clientForUpdate.getLegalForm())) {
                final Integer newValue = command.integerValueOfParameterNamed(ClientApiConstants.legalFormIdParamName);
                if (newValue != null) {
                    LegalForm legalForm = LegalForm.fromInt(newValue);
                    if (legalForm != null) {
                        changes.put(ClientApiConstants.legalFormIdParamName, ClientEnumerations.legalForm(newValue));
                        clientForUpdate.setLegalForm(legalForm.getValue());
                        clientForUpdate.resetDerivedNames(legalForm);
                    } else {
                        changes.put(ClientApiConstants.legalFormIdParamName, null);
                        clientForUpdate.setLegalForm(null);
                    }
                } else {
                    changes.put(ClientApiConstants.legalFormIdParamName, null);
                    clientForUpdate.setLegalForm(null);
                }
            }

            final String dateFormatAsInput = command.dateFormat();
            final String localeAsInput = command.locale();

            if (command.isChangeInLocalDateParameterNamed(ClientApiConstants.activationDateParamName,
                    clientForUpdate.getActivationDate())) {
                final String valueAsInput = command.stringValueOfParameterNamed(ClientApiConstants.activationDateParamName);
                changes.put(ClientApiConstants.activationDateParamName, valueAsInput);
                changes.put(ClientApiConstants.dateFormatParamName, dateFormatAsInput);
                changes.put(ClientApiConstants.localeParamName, localeAsInput);

                clientForUpdate.setActivationDate(command.localDateValueOfParameterNamed(ClientApiConstants.activationDateParamName));
                clientForUpdate.setOfficeJoiningDate(clientForUpdate.getActivationDate());
            }

            if (command.isChangeInLocalDateParameterNamed(ClientApiConstants.dateOfBirthParamName, clientForUpdate.getDateOfBirth())) {
                final String valueAsInput = command.stringValueOfParameterNamed(ClientApiConstants.dateOfBirthParamName);
                changes.put(ClientApiConstants.dateOfBirthParamName, valueAsInput);
                changes.put(ClientApiConstants.dateFormatParamName, dateFormatAsInput);
                changes.put(ClientApiConstants.localeParamName, localeAsInput);

                clientForUpdate.setDateOfBirth(command.localDateValueOfParameterNamed(ClientApiConstants.dateOfBirthParamName));
            }

            if (command.isChangeInLocalDateParameterNamed(ClientApiConstants.submittedOnDateParamName,
                    clientForUpdate.getSubmittedOnDate())) {
                final String valueAsInput = command.stringValueOfParameterNamed(ClientApiConstants.submittedOnDateParamName);
                changes.put(ClientApiConstants.submittedOnDateParamName, valueAsInput);
                changes.put(ClientApiConstants.dateFormatParamName, dateFormatAsInput);
                changes.put(ClientApiConstants.localeParamName, localeAsInput);

                clientForUpdate.setSubmittedOnDate(command.localDateValueOfParameterNamed(ClientApiConstants.submittedOnDateParamName));
            }

            clientForUpdate.validateUpdate();

            clientForUpdate.deriveDisplayName();

            if (changes.containsKey(ClientApiConstants.staffIdParamName)) {

                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.staffIdParamName);
                Staff newStaff = null;
                if (newValue != null) {
                    newStaff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(newValue,
                            clientForUpdate.getOffice().getHierarchy());
                }
                clientForUpdate.updateStaff(newStaff);
            }

            if (changes.containsKey(ClientApiConstants.genderIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.genderIdParamName);
                CodeValue gender = null;
                if (newValue != null) {
                    gender = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.GENDER, newValue);
                }
                clientForUpdate.updateGender(gender);
            }

            if (changes.containsKey(ClientApiConstants.savingsProductIdParamName)) {
                if (clientForUpdate.isActive()) {
                    throw new ClientActiveForUpdateException(clientId, ClientApiConstants.savingsProductIdParamName);
                }
                final Long savingsProductId = command.longValueOfParameterNamed(ClientApiConstants.savingsProductIdParamName);
                if (savingsProductId != null) {
                    this.savingsProductRepository.findById(savingsProductId)
                            .orElseThrow(() -> new SavingsProductNotFoundException(savingsProductId));
                }
                clientForUpdate.updateSavingsProduct(savingsProductId);
            }

            if (changes.containsKey(ClientApiConstants.clientTypeIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.clientTypeIdParamName);
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_TYPE,
                            newValue);
                }
                clientForUpdate.updateClientType(newCodeVal);
            }

            if (changes.containsKey(ClientApiConstants.clientClassificationIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.clientClassificationIdParamName);
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository
                            .findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_CLASSIFICATION, newValue);
                }
                clientForUpdate.updateClientClassification(newCodeVal);
            }

            if (!changes.isEmpty()) {
                this.clientRepository.saveAndFlush(clientForUpdate);
            }

            if (changes.containsKey(ClientApiConstants.legalFormIdParamName)) {
                Integer legalFormValue = clientForUpdate.getLegalForm();
                boolean isChangedToEntity = false;
                if (legalFormValue != null) {
                    LegalForm legalForm = LegalForm.fromInt(legalFormValue);
                    if (legalForm != null) {
                        isChangedToEntity = legalForm.isEntity();
                    }
                }

                if (isChangedToEntity) {
                    extractAndCreateClientNonPerson(clientForUpdate, command);
                } else {
                    final ClientNonPerson clientNonPerson = this.clientNonPersonRepository.findOneByClientId(clientForUpdate.getId());
                    if (clientNonPerson != null) {
                        this.clientNonPersonRepository.delete(clientNonPerson);
                    }
                }
            }

            final ClientNonPerson clientNonPersonForUpdate = this.clientNonPersonRepository.findOneByClientId(clientId);
            if (clientNonPersonForUpdate != null) {
                final JsonElement clientNonPersonElement = command.jsonElement(ClientApiConstants.clientNonPersonDetailsParamName);
                final Map<String, Object> clientNonPersonChanges = clientNonPersonForUpdate
                        .update(JsonCommand.fromExistingCommand(command, clientNonPersonElement));

                if (clientNonPersonChanges.containsKey(ClientApiConstants.constitutionIdParamName)) {

                    final Long newValue = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.constitutionIdParamName,
                            clientNonPersonElement);
                    CodeValue constitution = null;
                    if (newValue != null) {
                        constitution = this.codeValueRepository
                                .findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION, newValue);
                    }
                    clientNonPersonForUpdate.updateConstitution(constitution);
                }

                if (clientNonPersonChanges.containsKey(ClientApiConstants.mainBusinessLineIdParamName)) {

                    final Long newValue = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.mainBusinessLineIdParamName,
                            clientNonPersonElement);
                    CodeValue mainBusinessLine = null;
                    if (newValue != null) {
                        mainBusinessLine = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                                ClientApiConstants.CLIENT_NON_PERSON_MAIN_BUSINESS_LINE, newValue);
                    }
                    clientNonPersonForUpdate.updateMainBusinessLine(mainBusinessLine);
                }

                if (!clientNonPersonChanges.isEmpty()) {
                    this.clientNonPersonRepository.saveAndFlush(clientNonPersonForUpdate);
                }

                changes.putAll(clientNonPersonChanges);
            } else {
                final Integer legalFormParamValue = command.integerValueOfParameterNamed(ClientApiConstants.legalFormIdParamName);
                boolean isEntity = false;
                if (legalFormParamValue != null) {
                    final LegalForm legalForm = LegalForm.fromInt(legalFormParamValue);
                    if (legalForm != null) {
                        isEntity = legalForm.isEntity();
                    }
                }
                if (isEntity) {
                    extractAndCreateClientNonPerson(clientForUpdate, command);
                }
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityExternalId(clientForUpdate.getExternalId()) //
                    .withOfficeId(clientForUpdate.officeId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .with(changes) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            ClientDataIntegrityIssueHandler.handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            ClientDataIntegrityIssueHandler.handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }
}
