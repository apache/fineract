/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.documentmanagement.exception.DocumentManagementException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.serialization.ClientCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.group.domain.GroupRepository;
import org.mifosplatform.portfolio.group.exception.GroupNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientWritePlatformServiceJpaRepositoryImpl implements ClientWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ClientWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final ClientRepository clientRepository;
    private final OfficeRepository officeRepository;
    private final GroupRepository groupRepository;
    private final ClientCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;

    @Autowired
    public ClientWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final ClientRepository clientRepository,
            final OfficeRepository officeRepository, final ClientCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory, final GroupRepository groupRepository) {
        this.context = context;
        this.clientRepository = clientRepository;
        this.officeRepository = officeRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
        this.groupRepository = groupRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClient(final Long clientId) {

        final Client client = this.clientRepository.findOne(clientId);
        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

        final Long officeId = client.getOffice().getId();

        client.delete();
        this.clientRepository.save(client);

        return new CommandProcessingResultBuilder().withOfficeId(officeId).withClientId(clientId).withEntityId(clientId).build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId", "Client with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.accountNo", "Client with accountNo `" + accountNo
                    + "` already exists", "accountNo", accountNo);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult createClient(final JsonCommand command) {

        try {
            context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Long officeId = command.longValueOfParameterNamed("officeId");

            final Office clientOffice = this.officeRepository.findOne(officeId);
            if (clientOffice == null) { throw new OfficeNotFoundException(officeId); }

            final Long groupId = command.longValueOfParameterNamed("groupId");

            Group clientParentGroup = null;

            if (groupId != null) {
                clientParentGroup = this.groupRepository.findOne(groupId);
                if (clientParentGroup == null) { throw new GroupNotFoundException(groupId); }
            }

            final Client newClient = Client.fromJson(clientOffice, clientParentGroup, command);
            this.clientRepository.save(newClient);

            if (newClient.isAccountNumberRequiresAutoGeneration()) {
                final AccountNumberGenerator accountNoGenerator = this.accountIdentifierGeneratorFactory
                        .determineClientAccountNoGenerator(newClient.getId());
                newClient.updateAccountNo(accountNoGenerator.generate());
                this.clientRepository.save(newClient);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(clientOffice.getId()) //
                    .withClientId(newClient.getId()) //
                    .withEntityId(newClient.getId()) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClient(final Long clientId, final JsonCommand command) {

        try {
            context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Client clientForUpdate = this.clientRepository.findOne(clientId);
            if (clientForUpdate == null || clientForUpdate.isDeleted()) { throw new ClientNotFoundException(clientId); }

            final Map<String, Object> changes = clientForUpdate.update(command);

            if (changes.containsKey("officeId")) {
                final Long officeId = (Long) changes.get("officeId");
                final Office newOffice = this.officeRepository.findOne(officeId);
                if (newOffice == null) { throw new OfficeNotFoundException(officeId); }

                clientForUpdate.changeOffice(newOffice);
            }

            if (!changes.isEmpty()) {
                this.clientRepository.saveAndFlush(clientForUpdate);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(clientForUpdate.getOffice().getId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult saveOrUpdateClientImage(Long clientId, String imageName, InputStream inputStream) {
        try {
            final Client client = this.clientRepository.findOne(clientId);
            String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            String imageLocation = FileUtils.saveToFileSystem(inputStream, imageUploadLocation, imageName);

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException(imageName);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClientImage(final Long clientId) {

        final Client client = this.clientRepository.findOne(clientId);
        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

        // delete image from the file system
        if (StringUtils.isNotEmpty(client.getImageKey())) {
            FileUtils.deleteClientImage(clientId, client.getImageKey());
        }
        return updateClientImage(clientId, client, null);
    }

    @Override
    public CommandProcessingResult saveOrUpdateClientImage(final Long clientId, final Base64EncodedImage encodedImage) {
        try {
            final Client client = this.clientRepository.findOne(clientId);
            final String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            final String imageLocation = FileUtils.saveToFileSystem(encodedImage, imageUploadLocation, "image");

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException("image");
        }
    }

    private String setupForClientImageUpdate(final Long clientId, final Client client) {
        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

        final String imageUploadLocation = FileUtils.generateClientImageParentDirectory(clientId);
        // delete previous image from the file system
        if (StringUtils.isNotEmpty(client.getImageKey())) {
            FileUtils.deleteClientImage(clientId, client.getImageKey());
        }

        /** Recursively create the directory if it does not exist **/
        if (!new File(imageUploadLocation).isDirectory()) {
            new File(imageUploadLocation).mkdirs();
        }
        return imageUploadLocation;
    }

    private CommandProcessingResult updateClientImage(final Long clientId, final Client client, final String imageLocation) {
        client.setImageKey(imageLocation);
        this.clientRepository.save(client);

        return new CommandProcessingResult(clientId);
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

}