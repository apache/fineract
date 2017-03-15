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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.address.service.AddressWritePlatformService;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.ClientDataValidator;
import org.apache.fineract.portfolio.client.domain.AccountNumberGenerator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientNonPerson;
import org.apache.fineract.portfolio.client.domain.ClientNonPersonRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.ClientActiveForUpdateException;
import org.apache.fineract.portfolio.client.exception.ClientHasNoStaffException;
import org.apache.fineract.portfolio.client.exception.ClientMustBePendingToBeDeletedException;
import org.apache.fineract.portfolio.client.exception.InvalidClientSavingProductException;
import org.apache.fineract.portfolio.client.exception.InvalidClientStateTransitionException;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_ENTITY;
import org.apache.fineract.portfolio.common.BusinessEventNotificationConstants.BUSINESS_EVENTS;
import org.apache.fineract.portfolio.common.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.GroupMemberCountNotInPermissibleRangeException;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.data.SavingsAccountDataDTO;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsProductNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsApplicationProcessWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;

@Service
public class ClientWritePlatformServiceJpaRepositoryImpl implements ClientWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ClientWritePlatformServiceJpaRepositoryImpl.class);

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
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final SavingsAccountRepositoryWrapper savingsRepositoryWrapper;
    private final SavingsProductRepository savingsProductRepository;
    private final SavingsApplicationProcessWritePlatformService savingsApplicationProcessWritePlatformService;
    private final CommandProcessingService commandProcessingService;
    private final ConfigurationDomainService configurationDomainService;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final AddressWritePlatformService addressWritePlatformService;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    @Autowired
    public ClientWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ClientRepositoryWrapper clientRepository, final ClientNonPersonRepositoryWrapper clientNonPersonRepository,
            final OfficeRepositoryWrapper officeRepositoryWrapper, final NoteRepository noteRepository,
            final ClientDataValidator fromApiJsonDeserializer, final AccountNumberGenerator accountNumberGenerator,
            final GroupRepository groupRepository, final StaffRepositoryWrapper staffRepository,
            final CodeValueRepositoryWrapper codeValueRepository, final LoanRepositoryWrapper loanRepositoryWrapper,
            final SavingsAccountRepositoryWrapper savingsRepositoryWrapper, final SavingsProductRepository savingsProductRepository,
            final SavingsApplicationProcessWritePlatformService savingsApplicationProcessWritePlatformService,
            final CommandProcessingService commandProcessingService, final ConfigurationDomainService configurationDomainService,
            final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository, final FromJsonHelper fromApiJsonHelper,
            final ConfigurationReadPlatformService configurationReadPlatformService,
            final AddressWritePlatformService addressWritePlatformService, final BusinessEventNotifierService businessEventNotifierService,
            final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService) {
        this.context = context;
        this.clientRepository = clientRepository;
        this.clientNonPersonRepository = clientNonPersonRepository;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.noteRepository = noteRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.accountNumberGenerator = accountNumberGenerator;
        this.groupRepository = groupRepository;
        this.staffRepository = staffRepository;
        this.codeValueRepository = codeValueRepository;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.savingsRepositoryWrapper = savingsRepositoryWrapper;
        this.savingsProductRepository = savingsProductRepository;
        this.savingsApplicationProcessWritePlatformService = savingsApplicationProcessWritePlatformService;
        this.commandProcessingService = commandProcessingService;
        this.configurationDomainService = configurationDomainService;
        this.accountNumberFormatRepository = accountNumberFormatRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.configurationReadPlatformService = configurationReadPlatformService;
        this.addressWritePlatformService = addressWritePlatformService;
        this.businessEventNotifierService = businessEventNotifierService;
        this.entityDatatableChecksWritePlatformService = entityDatatableChecksWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClient(final Long clientId) {
        try {
            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

            if (client.isNotPending()) { throw new ClientMustBePendingToBeDeletedException(clientId); }
            final List<Note> relatedNotes = this.noteRepository.findByClientId(clientId);
            this.noteRepository.deleteInBatch(relatedNotes);

            final ClientNonPerson clientNonPerson = this.clientNonPersonRepository.findOneByClientId(clientId);
            if (clientNonPerson != null) this.clientNonPersonRepository.delete(clientNonPerson);

            this.clientRepository.delete(client);
            this.clientRepository.flush();
            return new CommandProcessingResultBuilder() //
                    .withOfficeId(client.officeId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            logger.error(throwable.getMessage());
            throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId", "Client with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.accountNo", "Client with accountNo `" + accountNo
                    + "` already exists", "accountNo", accountNo);
        } else if (realCause.getMessage().contains("mobile_no")) {
            final String mobileNo = command.stringValueOfParameterNamed("mobileNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.mobileNo", "Client with mobileNo `" + mobileNo
                    + "` already exists", "mobileNo", mobileNo);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult createClient(final JsonCommand command) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());
            
			final GlobalConfigurationPropertyData configuration = this.configurationReadPlatformService
					.retrieveGlobalConfiguration("Enable-Address");

			final Boolean isAddressEnabled = configuration.isEnabled();
			
			final Boolean isStaff = command.booleanObjectValueOfParameterNamed(ClientApiConstants.isStaffParamName);

            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);

            final Office clientOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);

            final Long groupId = command.longValueOfParameterNamed(ClientApiConstants.groupIdParamName);

            Group clientParentGroup = null;
            if (groupId != null) {
                clientParentGroup = this.groupRepository.findOne(groupId);
                if (clientParentGroup == null) { throw new GroupNotFoundException(groupId); }
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
                clientClassification = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                        ClientApiConstants.CLIENT_CLASSIFICATION, clientClassificationId);
            }

           
            final Long savingsProductId = command.longValueOfParameterNamed(ClientApiConstants.savingsProductIdParamName);
            if (savingsProductId != null) {
                SavingsProduct savingsProduct = this.savingsProductRepository.findOne(savingsProductId);
                if (savingsProduct == null) { throw new SavingsProductNotFoundException(savingsProductId); }
            }
            
            final Integer legalFormParamValue = command.integerValueOfParameterNamed(ClientApiConstants.legalFormIdParamName);
            boolean isEntity = false;
            Integer legalFormValue = null;
            if(legalFormParamValue != null)
            {
            	LegalForm legalForm = LegalForm.fromInt(legalFormParamValue);
            	if(legalForm != null)
                {
                	legalFormValue = legalForm.getValue();
                	isEntity = legalForm.isEntity();
                }
            }
            
            final Client newClient = Client.createNew(currentUser, clientOffice, clientParentGroup, staff, savingsProductId, gender,
                    clientType, clientClassification, legalFormValue, command);
            this.clientRepository.save(newClient);
            boolean rollbackTransaction = false;
            if (newClient.isActive()) {
                validateParentGroupRulesBeforeClientActivation(newClient);
                runEntityDatatableCheck(newClient.getId());
                final CommandWrapper commandWrapper = new CommandWrapperBuilder().activateClient(null).build();
                rollbackTransaction = this.commandProcessingService.validateCommand(commandWrapper, currentUser);
            }
			
            this.clientRepository.save(newClient);
            if (newClient.isActive()) {
                this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.CLIENTS_ACTIVATE,
                        constructEntityMap(BUSINESS_ENTITY.CLIENT, newClient));
            }
            if (newClient.isAccountNumberRequiresAutoGeneration()) {
                AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository.findByAccountType(EntityAccountType.CLIENT);
                newClient.updateAccountNo(accountNumberGenerator.generate(newClient, accountNumberFormat));
                this.clientRepository.save(newClient);
            }
                        
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
            CommandProcessingResult result = openSavingsAccount(newClient, fmt);
            if (result.getSavingsId() != null) {
                this.clientRepository.save(newClient);
                
            }
            
            if(isEntity) {
                extractAndCreateClientNonPerson(newClient, command);
            }
            	
            if (isAddressEnabled) {
                this.addressWritePlatformService.addNewClientAddress(newClient, command);
            }

            if(command.parameterExists(ClientApiConstants.datatables)){
                this.entityDatatableChecksWritePlatformService.saveDatatables(StatusEnum.CREATE.getCode().longValue(),
                        EntityTables.CLIENT.getName(), newClient.getId(), null,
                        command.arrayOfParameterNamed(ClientApiConstants.datatables));
            }

            this.entityDatatableChecksWritePlatformService.runTheCheck(newClient.getId(), EntityTables.CLIENT.getName(),
                    StatusEnum.CREATE.getCode().longValue(), EntityTables.CLIENT.getForeignKeyColumnNameOnDatatable());
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(clientOffice.getId()) //
                    .withClientId(newClient.getId()) //
                    .withGroupId(groupId) //
                    .withEntityId(newClient.getId()) //
                    .withSavingsId(result.getSavingsId())//
                    .setRollbackTransaction(rollbackTransaction)//
                    .setRollbackTransaction(result.isRollbackTransaction())//
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch(final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
         	return CommandProcessingResult.empty();
        }
    }
    
    /**
     * This method extracts ClientNonPerson details from Client command and creates a new ClientNonPerson record
     * @param client
     * @param command
     */
    public void extractAndCreateClientNonPerson(Client client, JsonCommand command)
    {    	
    	final JsonElement clientNonPersonElement = this.fromApiJsonHelper.parse(command.jsonFragment(ClientApiConstants.clientNonPersonDetailsParamName));

		if(clientNonPersonElement != null)
		{
			final String incorpNumber = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.incorpNumberParamName, clientNonPersonElement);
	        final String remarks = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.remarksParamName, clientNonPersonElement);                
	        final LocalDate incorpValidityTill = this.fromApiJsonHelper.extractLocalDateNamed(ClientApiConstants.incorpValidityTillParamName, clientNonPersonElement);
	        
	    	//JsonCommand clientNonPersonCommand = JsonCommand.fromExistingCommand(command, command.arrayOfParameterNamed(ClientApiConstants.clientNonPersonDetailsParamName).getAsJsonObject());
	    	CodeValue clientNonPersonConstitution = null;
	        final Long clientNonPersonConstitutionId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.constitutionIdParamName, clientNonPersonElement);
	        if (clientNonPersonConstitutionId != null) {
	        	clientNonPersonConstitution = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION,
	        			clientNonPersonConstitutionId);
	        }
	        
	        CodeValue clientNonPersonMainBusinessLine = null;
	        final Long clientNonPersonMainBusinessLineId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.mainBusinessLineIdParamName, clientNonPersonElement);
	        if (clientNonPersonMainBusinessLineId != null) {
	        	clientNonPersonMainBusinessLine = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_NON_PERSON_MAIN_BUSINESS_LINE,
	        			clientNonPersonMainBusinessLineId);
	        }
	        
	    	final ClientNonPerson newClientNonPerson = ClientNonPerson.createNew(client, clientNonPersonConstitution, clientNonPersonMainBusinessLine, incorpNumber, incorpValidityTill, remarks);
	    	
	    	this.clientNonPersonRepository.save(newClientNonPerson);
		}
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClient(final Long clientId, final JsonCommand command) {

        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final String clientHierarchy = clientForUpdate.getOffice().getHierarchy();

            this.context.validateAccessRights(clientHierarchy);

            final Map<String, Object> changes = clientForUpdate.update(command);

            if (changes.containsKey(ClientApiConstants.staffIdParamName)) {

                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.staffIdParamName);
                Staff newStaff = null;
                if (newValue != null) {
                    newStaff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(newValue, clientForUpdate.getOffice()
                            .getHierarchy());
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
                if (clientForUpdate.isActive()) { throw new ClientActiveForUpdateException(clientId,
                        ClientApiConstants.savingsProductIdParamName); }
                SavingsProduct savingsProduct = null;
                final Long savingsProductId = command.longValueOfParameterNamed(ClientApiConstants.savingsProductIdParamName);
                if (savingsProductId != null) {
                    savingsProduct = this.savingsProductRepository.findOne(savingsProductId);
                    if (savingsProduct == null) { throw new SavingsProductNotFoundException(savingsProductId); }
                }
                clientForUpdate.updateSavingsProduct(savingsProductId);
            }

            if (changes.containsKey(ClientApiConstants.genderIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.genderIdParamName);
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.GENDER, newValue);
                }
                clientForUpdate.updateGender(newCodeVal);
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
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                            ClientApiConstants.CLIENT_CLASSIFICATION, newValue);
                }
                clientForUpdate.updateClientClassification(newCodeVal);
            }

            if (!changes.isEmpty()) {
                this.clientRepository.saveAndFlush(clientForUpdate);
            }
            
            if (changes.containsKey(ClientApiConstants.legalFormIdParamName)) {
            	Integer legalFormValue = clientForUpdate.getLegalForm();
            	boolean isChangedToEntity = false;
            	if(legalFormValue != null)
            	{
            		LegalForm legalForm = LegalForm.fromInt(legalFormValue);
            		if(legalForm != null)
            			isChangedToEntity = legalForm.isEntity();
            	}
                
                if(isChangedToEntity)
                {
                	extractAndCreateClientNonPerson(clientForUpdate, command);
                }
                else
                {
                	final ClientNonPerson clientNonPerson = this.clientNonPersonRepository.findOneByClientId(clientForUpdate.getId());
                	if(clientNonPerson != null)
                		this.clientNonPersonRepository.delete(clientNonPerson);
                }
            }
            
            final ClientNonPerson clientNonPersonForUpdate = this.clientNonPersonRepository.findOneByClientId(clientId);
            if(clientNonPersonForUpdate != null)
            {
            	final JsonElement clientNonPersonElement = command.jsonElement(ClientApiConstants.clientNonPersonDetailsParamName);
            	final Map<String, Object> clientNonPersonChanges = clientNonPersonForUpdate.update(JsonCommand.fromExistingCommand(command, clientNonPersonElement));
                
                if (clientNonPersonChanges.containsKey(ClientApiConstants.constitutionIdParamName)) {

                    final Long newValue = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.constitutionIdParamName, clientNonPersonElement);
                    CodeValue constitution = null;
                    if (newValue != null) {
                        constitution = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION, newValue);
                    }
                    clientNonPersonForUpdate.updateConstitution(constitution);
                }
                
                if (clientNonPersonChanges.containsKey(ClientApiConstants.mainBusinessLineIdParamName)) {

                    final Long newValue = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.mainBusinessLineIdParamName, clientNonPersonElement);
                    CodeValue mainBusinessLine = null;
                    if (newValue != null) {
                        mainBusinessLine = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_NON_PERSON_MAIN_BUSINESS_LINE, newValue);
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
                    .withOfficeId(clientForUpdate.officeId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .with(changes) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }catch(final PersistenceException dve) {
        	Throwable throwable = ExceptionUtils.getRootCause(dve.getCause()) ;
            handleDataIntegrityIssues(command, throwable, dve);
         	return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult activateClient(final Long clientId, final JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateActivation(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId, true);
            validateParentGroupRulesBeforeClientActivation(client);
            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormat.forPattern(command.dateFormat()).withLocale(locale);
            final LocalDate activationDate = command.localDateValueOfParameterNamed("activationDate");

            runEntityDatatableCheck(clientId);

            final AppUser currentUser = this.context.authenticatedUser();
            client.activate(currentUser, fmt, activationDate);
            CommandProcessingResult result = openSavingsAccount(client, fmt);
            this.clientRepository.saveAndFlush(client);
            this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.CLIENTS_ACTIVATE,
                    constructEntityMap(BUSINESS_ENTITY.CLIENT, client));
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(client.officeId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .withSavingsId(result.getSavingsId())//
                    .setRollbackTransaction(result.isRollbackTransaction())//
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }
    }

    private CommandProcessingResult openSavingsAccount(final Client client, final DateTimeFormatter fmt) {
        CommandProcessingResult commandProcessingResult = CommandProcessingResult.empty();
        if (client.isActive() && client.savingsProductId() != null) {
            SavingsAccountDataDTO savingsAccountDataDTO = new SavingsAccountDataDTO(client, null, client.savingsProductId(),
                    client.getActivationLocalDate(), client.activatedBy(), fmt);
            commandProcessingResult = this.savingsApplicationProcessWritePlatformService.createActiveApplication(savingsAccountDataDTO);
            if (commandProcessingResult.getSavingsId() != null) {
                this.savingsRepositoryWrapper.findOneWithNotFoundDetection(commandProcessingResult.getSavingsId());
                client.updateSavingsAccount(commandProcessingResult.getSavingsId());
                client.updateSavingsProduct(null);
            }
        }
        return commandProcessingResult;
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        logger.error(dve.getMessage(), dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult unassignClientStaff(final Long clientId, final JsonCommand command) {

        this.context.authenticatedUser();

        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);

        this.fromApiJsonDeserializer.validateForUnassignStaff(command.json());

        final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);

        final Staff presentStaff = clientForUpdate.getStaff();
        Long presentStaffId = null;
        if (presentStaff == null) { throw new ClientHasNoStaffException(clientId); }
        presentStaffId = presentStaff.getId();
        final String staffIdParamName = ClientApiConstants.staffIdParamName;
        if (!command.isChangeInLongParameterNamed(staffIdParamName, presentStaffId)) {
            clientForUpdate.unassignStaff();
        }
        this.clientRepository.saveAndFlush(clientForUpdate);

        actualChanges.put(staffIdParamName, presentStaffId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(clientForUpdate.officeId()) //
                .withEntityId(clientForUpdate.getId()) //
                .withClientId(clientId) //
                .with(actualChanges) //
                .build();
    }

    @Override
    public CommandProcessingResult assignClientStaff(final Long clientId, final JsonCommand command) {

        this.context.authenticatedUser();

        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);

        this.fromApiJsonDeserializer.validateForAssignStaff(command.json());

        final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);
        Staff staff = null;
        final Long staffId = command.longValueOfParameterNamed(ClientApiConstants.staffIdParamName);
        if (staffId != null) {
            staff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(staffId, clientForUpdate.getOffice().getHierarchy());
            /**
             * TODO Vishwas: We maintain history of chage of loan officer w.r.t
             * loan in a history table, should we do the same for a client?
             * Especially useful when the change happens due to a transfer etc
             **/
            clientForUpdate.assignStaff(staff);
        }

        this.clientRepository.saveAndFlush(clientForUpdate);

        actualChanges.put(ClientApiConstants.staffIdParamName, staffId);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(clientForUpdate.officeId()) //
                .withEntityId(clientForUpdate.getId()) //
                .withClientId(clientId) //
                .with(actualChanges) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult closeClient(final Long clientId, final JsonCommand command) {
        try {

            final AppUser currentUser = this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateClose(command);

            final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final LocalDate closureDate = command.localDateValueOfParameterNamed(ClientApiConstants.closureDateParamName);
            final Long closureReasonId = command.longValueOfParameterNamed(ClientApiConstants.closureReasonIdParamName);

            final CodeValue closureReason = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                    ClientApiConstants.CLIENT_CLOSURE_REASON, closureReasonId);

            if (ClientStatus.fromInt(client.getStatus()).isClosed()) {
                final String errorMessage = "Client is already closed.";
                throw new InvalidClientStateTransitionException("close", "is.already.closed", errorMessage);
            } else if (ClientStatus.fromInt(client.getStatus()).isUnderTransfer()) {
                final String errorMessage = "Cannot Close a Client under Transfer";
                throw new InvalidClientStateTransitionException("close", "is.under.transfer", errorMessage);
            }

            if (client.isNotPending() && client.getActivationLocalDate().isAfter(closureDate)) {
                final String errorMessage = "The client closureDate cannot be before the client ActivationDate.";
                throw new InvalidClientStateTransitionException("close", "date.cannot.before.client.actvation.date", errorMessage,
                        closureDate, client.getActivationLocalDate());
            }
            entityDatatableChecksWritePlatformService.runTheCheck(clientId,EntityTables.CLIENT.getName(),
                    StatusEnum.CLOSE.getCode().longValue(),EntityTables.CLIENT.getForeignKeyColumnNameOnDatatable());

            final List<Loan> clientLoans = this.loanRepositoryWrapper.findLoanByClientId(clientId);
            for (final Loan loan : clientLoans) {
                final LoanStatusMapper loanStatus = new LoanStatusMapper(loan.status().getValue());
                if (loanStatus.isOpen() || loanStatus.isPendingApproval() || loanStatus.isAwaitingDisbursal()) {
                    final String errorMessage = "Client cannot be closed because of non-closed loans.";
                    throw new InvalidClientStateTransitionException("close", "loan.non-closed", errorMessage);
                } else if (loanStatus.isClosed() && loan.getClosedOnDate().after(closureDate.toDate())) {
                    final String errorMessage = "The client closureDate cannot be before the loan closedOnDate.";
                    throw new InvalidClientStateTransitionException("close", "date.cannot.before.loan.closed.date", errorMessage,
                            closureDate, loan.getClosedOnDate());
                } else if (loanStatus.isOverpaid()) {
                    final String errorMessage = "Client cannot be closed because of overpaid loans.";
                    throw new InvalidClientStateTransitionException("close", "loan.overpaid", errorMessage);
                }
            }
            final List<SavingsAccount> clientSavingAccounts = this.savingsRepositoryWrapper.findSavingAccountByClientId(clientId);

            for (final SavingsAccount saving : clientSavingAccounts) {
                if (saving.isActive() || saving.isSubmittedAndPendingApproval() || saving.isApproved()) {
                    final String errorMessage = "Client cannot be closed because of non-closed savings account.";
                    throw new InvalidClientStateTransitionException("close", "non-closed.savings.account", errorMessage);
                }
            }

            client.close(currentUser, closureReason, closureDate.toDate());
            this.clientRepository.saveAndFlush(client);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult updateDefaultSavingsAccount(final Long clientId, final JsonCommand command) {

        this.context.authenticatedUser();

        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);

        this.fromApiJsonDeserializer.validateForSavingsAccount(command.json());

        final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);

        SavingsAccount savingsAccount = null;
        final Long savingsId = command.longValueOfParameterNamed(ClientApiConstants.savingsAccountIdParamName);
        if (savingsId != null) {
            savingsAccount = this.savingsRepositoryWrapper.findOneWithNotFoundDetection(savingsId);
            if (!savingsAccount.getClient().identifiedBy(clientId)) {
                String defaultUserMessage = "saving account must belongs to client";
                throw new InvalidClientSavingProductException("saving.account", "must.belongs.to.client", defaultUserMessage, savingsId,
                        clientForUpdate.getId());
            }
            clientForUpdate.updateSavingsAccount(savingsId);
        }

        this.clientRepository.saveAndFlush(clientForUpdate);

        actualChanges.put(ClientApiConstants.savingsAccountIdParamName, savingsId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withOfficeId(clientForUpdate.officeId()) //
                .withEntityId(clientForUpdate.getId()) //
                .withClientId(clientId) //
                .with(actualChanges) //
                .build();
    }

    /*
     * To become a part of a group, group may have set of criteria to be m et
     * before client can become member of it.
     */
    private void validateParentGroupRulesBeforeClientActivation(Client client) {
        Integer minNumberOfClients = configurationDomainService.retrieveMinAllowedClientsInGroup();
        Integer maxNumberOfClients = configurationDomainService.retrieveMaxAllowedClientsInGroup();
        if (client.getGroups() != null && maxNumberOfClients != null) {
            for (Group group : client.getGroups()) {
                /**
                 * Since this Client has not yet been associated with the group,
                 * reduce maxNumberOfClients by 1
                 **/
                final boolean validationsuccess = group.isGroupsClientCountWithinMaxRange(maxNumberOfClients - 1);
                if (!validationsuccess) { throw new GroupMemberCountNotInPermissibleRangeException(group.getId(), minNumberOfClients,
                        maxNumberOfClients); }
            }
        }
    }

    private void runEntityDatatableCheck(final Long clientId) {
        entityDatatableChecksWritePlatformService.runTheCheck(clientId, EntityTables.CLIENT.getName(), StatusEnum.ACTIVATE.getCode()
                .longValue(), EntityTables.CLIENT.getForeignKeyColumnNameOnDatatable());
    }

    @Override
    public CommandProcessingResult rejectClient(final Long entityId, final JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateRejection(command);

        final Client client = this.clientRepository.findOneWithNotFoundDetection(entityId);
        final LocalDate rejectionDate = command.localDateValueOfParameterNamed(ClientApiConstants.rejectionDateParamName);
        final Long rejectionReasonId = command.longValueOfParameterNamed(ClientApiConstants.rejectionReasonIdParamName);

        final CodeValue rejectionReason = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                ClientApiConstants.CLIENT_REJECT_REASON, rejectionReasonId);

        if (client.isNotPending()) {
            final String errorMessage = "Only clients pending activation may be withdrawn.";
            throw new InvalidClientStateTransitionException("rejection", "on.account.not.in.pending.activation.status", errorMessage,
                    rejectionDate, client.getSubmittedOnDate());
        } else if (client.getSubmittedOnDate().isAfter(rejectionDate)) {
            final String errorMessage = "The client rejection date cannot be before the client submitted date.";
            throw new InvalidClientStateTransitionException("rejection", "date.cannot.before.client.submitted.date", errorMessage,
                    rejectionDate, client.getSubmittedOnDate());
        }
        client.reject(currentUser, rejectionReason, rejectionDate.toDate());
        this.clientRepository.saveAndFlush(client);
        this.businessEventNotifierService.notifyBusinessEventWasExecuted(BUSINESS_EVENTS.CLIENTS_REJECT,
                constructEntityMap(BUSINESS_ENTITY.CLIENT, client));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withClientId(entityId) //
                .withEntityId(entityId) //
                .build();
    }

    @Override
    public CommandProcessingResult withdrawClient(Long entityId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateWithdrawn(command);

        final Client client = this.clientRepository.findOneWithNotFoundDetection(entityId);
        final LocalDate withdrawalDate = command.localDateValueOfParameterNamed(ClientApiConstants.withdrawalDateParamName);
        final Long withdrawalReasonId = command.longValueOfParameterNamed(ClientApiConstants.withdrawalReasonIdParamName);

        final CodeValue withdrawalReason = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                ClientApiConstants.CLIENT_WITHDRAW_REASON, withdrawalReasonId);

        if (client.isNotPending()) {
            final String errorMessage = "Only clients pending activation may be withdrawn.";
            throw new InvalidClientStateTransitionException("withdrawal", "on.account.not.in.pending.activation.status", errorMessage,
                    withdrawalDate, client.getSubmittedOnDate());
        } else if (client.getSubmittedOnDate().isAfter(withdrawalDate)) {
            final String errorMessage = "The client withdrawal date cannot be before the client submitted date.";
            throw new InvalidClientStateTransitionException("withdrawal", "date.cannot.before.client.submitted.date", errorMessage,
                    withdrawalDate, client.getSubmittedOnDate());
        }
        client.withdraw(currentUser, withdrawalReason, withdrawalDate.toDate());
        this.clientRepository.saveAndFlush(client);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withClientId(entityId) //
                .withEntityId(entityId) //
                .build();
    }

    @Override
    public CommandProcessingResult reActivateClient(Long entityId, JsonCommand command) {
        final AppUser currentUser = this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateReactivate(command);

        final Client client = this.clientRepository.findOneWithNotFoundDetection(entityId);
        final LocalDate reactivateDate = command.localDateValueOfParameterNamed(ClientApiConstants.reactivationDateParamName);

        if (!client.isClosed()) {
            final String errorMessage = "only closed clients may be reactivated.";
            throw new InvalidClientStateTransitionException("reactivation", "on.nonclosed.account", errorMessage);
        } else if (client.getClosureDate().isAfter(reactivateDate)) {
            final String errorMessage = "The client reactivation date cannot be before the client closed date.";
            throw new InvalidClientStateTransitionException("reactivation", "date.cannot.before.client.closed.date", errorMessage,
                    reactivateDate, client.getClosureDate());
        }
        client.reActivate(currentUser, reactivateDate.toDate());
        this.clientRepository.saveAndFlush(client);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withClientId(entityId) //
                .withEntityId(entityId) //
                .build();
    }

	@Override
	public CommandProcessingResult undoRejection(Long entityId, JsonCommand command) {
		final AppUser currentUser = this.context.authenticatedUser();
		this.fromApiJsonDeserializer.validateUndoRejection(command);

		final Client client = this.clientRepository.findOneWithNotFoundDetection(entityId);
		final LocalDate undoRejectDate = command
				.localDateValueOfParameterNamed(ClientApiConstants.reopenedDateParamName);

		if (!client.isRejected()) {
			final String errorMessage = "only rejected clients may be reactivated.";
			throw new InvalidClientStateTransitionException("undorejection", "on.nonrejected.account", errorMessage);
		} else if (client.getRejectedDate().isAfter(undoRejectDate)) {
			final String errorMessage = "The client reactivation date cannot be before the client rejected date.";
			throw new InvalidClientStateTransitionException("reopened", "date.cannot.before.client.rejected.date",
					errorMessage, undoRejectDate, client.getRejectedDate());
		}

		client.reOpened(currentUser, undoRejectDate.toDate());
		this.clientRepository.saveAndFlush(client);

		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withClientId(entityId) //
				.withEntityId(entityId) //
				.build();
	}

	@Override
	public CommandProcessingResult undoWithdrawal(Long entityId, JsonCommand command) {
		final AppUser currentUser = this.context.authenticatedUser();
		this.fromApiJsonDeserializer.validateUndoWithDrawn(command);

		final Client client = this.clientRepository.findOneWithNotFoundDetection(entityId);
		final LocalDate undoWithdrawalDate = command
				.localDateValueOfParameterNamed(ClientApiConstants.reopenedDateParamName);

		if (!client.isWithdrawn()) {
			final String errorMessage = "only withdrawal clients may be reactivated.";
			throw new InvalidClientStateTransitionException("undoWithdrawal", "on.nonwithdrawal.account", errorMessage);
		} else if (client.getWithdrawalDate().isAfter(undoWithdrawalDate)) {
			final String errorMessage = "The client reactivation date cannot be before the client withdrawal date.";
			throw new InvalidClientStateTransitionException("reopened", "date.cannot.before.client.withdrawal.date",
					errorMessage, undoWithdrawalDate, client.getWithdrawalDate());
		}
		client.reOpened(currentUser, undoWithdrawalDate.toDate());
		this.clientRepository.saveAndFlush(client);

		return new CommandProcessingResultBuilder() //
				.withCommandId(command.commandId()) //
				.withClientId(entityId) //
				.withEntityId(entityId) //
				.build();
	}

    private Map<BUSINESS_ENTITY, Object> constructEntityMap(final BUSINESS_ENTITY entityEvent, Object entity) {
        Map<BUSINESS_ENTITY, Object> map = new HashMap<>(1);
        map.put(entityEvent, entity);
        return map;
    }
}
