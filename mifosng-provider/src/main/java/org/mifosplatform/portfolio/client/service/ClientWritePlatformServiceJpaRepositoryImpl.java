package org.mifosplatform.portfolio.client.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepository;
import org.mifosplatform.infrastructure.codes.exception.CodeValueNotFoundException;
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
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.client.command.ClientNoteCommand;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientIdentifier;
import org.mifosplatform.portfolio.client.domain.ClientIdentifierRepository;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.Note;
import org.mifosplatform.portfolio.client.domain.NoteRepository;
import org.mifosplatform.portfolio.client.exception.ClientIdentifierNotFoundException;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.exception.DuplicateClientIdentifierException;
import org.mifosplatform.portfolio.client.exception.NoteNotFoundException;
import org.mifosplatform.portfolio.client.serialization.ClientCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.client.serialization.ClientIdentifierCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.client.serialization.ClientNoteCommandFromApiJsonDeserializer;
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
    private final ClientIdentifierRepository clientIdentifierRepository;
    private final OfficeRepository officeRepository;
    private final NoteRepository noteRepository;
    private final CodeValueRepository codeValueRepository;
    private final ClientCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final ClientNoteCommandFromApiJsonDeserializer clientNoteFromApiJsonDeserializer;
    private final ClientIdentifierCommandFromApiJsonDeserializer clientIdentifierCommandFromApiJsonDeserializer;
    private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;

    @Autowired
    public ClientWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final ClientRepository clientRepository,
            final ClientIdentifierRepository clientIdentifierRepository, final OfficeRepository officeRepository,
            final NoteRepository noteRepository, final CodeValueRepository codeValueRepository,
            final ClientCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final ClientNoteCommandFromApiJsonDeserializer clientNoteFromApiJsonDeserializer,
            final ClientIdentifierCommandFromApiJsonDeserializer clientIdentifierCommandFromApiJsonDeserializer,
            final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory) {
        this.context = context;
        this.clientRepository = clientRepository;
        this.clientIdentifierRepository = clientIdentifierRepository;
        this.officeRepository = officeRepository;
        this.noteRepository = noteRepository;
        this.codeValueRepository = codeValueRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.clientNoteFromApiJsonDeserializer = clientNoteFromApiJsonDeserializer;
        this.clientIdentifierCommandFromApiJsonDeserializer = clientIdentifierCommandFromApiJsonDeserializer;
        this.accountIdentifierGeneratorFactory = accountIdentifierGeneratorFactory;
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

            final Client newClient = Client.fromJson(clientOffice, command);
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
    public CommandProcessingResult addClientNote(final Long clientId, final JsonCommand command) {

        context.authenticatedUser();

        final ClientNoteCommand clientNoteCommand = this.clientNoteFromApiJsonDeserializer.commandFromApiJson(command.json());
        clientNoteCommand.validate();

        final Client clientForUpdate = this.clientRepository.findOne(clientId);
        if (clientForUpdate == null) { throw new ClientNotFoundException(clientId); }

        final Note note = Note.clientNoteFromJson(clientForUpdate, command);

        this.noteRepository.save(note);

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(clientForUpdate.getOffice().getId())
                .withClientId(clientId).withEntityId(note.getId()).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClientNote(final Long clientId, final Long noteId, final JsonCommand command) {

        context.authenticatedUser();

        final ClientNoteCommand clientNoteCommand = this.clientNoteFromApiJsonDeserializer.commandFromApiJson(command.json());
        clientNoteCommand.validate();

        final Note noteForUpdate = this.noteRepository.findOne(noteId);
        if (noteForUpdate == null || noteForUpdate.isNotAgainstClientWithIdOf(clientId)) { throw new NoteNotFoundException(noteId,
                clientId, "client"); }

        final Client client = this.clientRepository.findOne(clientId);

        final Map<String, Object> changes = noteForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.noteRepository.save(noteForUpdate);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(client.getOffice().getId())
                .withClientId(clientId).withEntityId(noteId).with(changes).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult addClientIdentifier(final Long clientId, final JsonCommand command) {

        context.authenticatedUser();
        final ClientIdentifierCommand clientIdentifierCommand = this.clientIdentifierCommandFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        clientIdentifierCommand.validateForCreate();

        String documentKey = clientIdentifierCommand.getDocumentKey();
        String documentTypeLabel = null;
        Long documentTypeId = null;
        try {
            final Client client = this.clientRepository.findOne(clientId);
            if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

            final CodeValue documentType = this.codeValueRepository.findOne(clientIdentifierCommand.getDocumentTypeId());
            if (documentType == null) { throw new CodeValueNotFoundException(clientIdentifierCommand.getDocumentTypeId()); }

            documentTypeId = documentType.getId();
            documentTypeLabel = documentType.label();

            final ClientIdentifier clientIdentifier = ClientIdentifier.fromJson(client, documentType, command);

            this.clientIdentifierRepository.save(clientIdentifier);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(client.getOffice().getId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientIdentifier.getId()) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleClientIdentifierDataIntegrityViolation(documentTypeLabel, documentTypeId, documentKey, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClientIdentifier(final Long clientId, final Long identifierId, final JsonCommand command) {

        context.authenticatedUser();
        final ClientIdentifierCommand clientIdentifierCommand = this.clientIdentifierCommandFromApiJsonDeserializer
                .commandFromApiJson(command.json());
        clientIdentifierCommand.validateForUpdate();

        String documentTypeLabel = null;
        String documentKey = null;
        Long documentTypeId = clientIdentifierCommand.getDocumentTypeId();
        try {
            CodeValue documentType = null;

            final ClientIdentifier clientIdentifierForUpdate = this.clientIdentifierRepository.findOne(identifierId);
            if (clientIdentifierForUpdate == null) { throw new ClientIdentifierNotFoundException(identifierId); }

            final Map<String, Object> changes = clientIdentifierForUpdate.update(command);

            if (changes.containsKey("documentTypeId")) {
                documentType = this.codeValueRepository.findOne(documentTypeId);
                if (documentType == null) { throw new CodeValueNotFoundException(documentTypeId); }

                documentTypeId = documentType.getId();
                documentTypeLabel = documentType.label();
                clientIdentifierForUpdate.update(documentType);
            }

            if (changes.containsKey("documentTypeId") && changes.containsKey("documentKey")) {
                documentTypeId = clientIdentifierCommand.getDocumentTypeId();
                documentKey = clientIdentifierCommand.getDocumentKey();
            } else if (changes.containsKey("documentTypeId") && !changes.containsKey("documentKey")) {
                documentTypeId = clientIdentifierCommand.getDocumentTypeId();
                documentKey = clientIdentifierForUpdate.documentKey();
            } else if (!changes.containsKey("documentTypeId") && changes.containsKey("documentKey")) {
                documentTypeId = clientIdentifierForUpdate.documentTypeId();
                documentKey = clientIdentifierForUpdate.documentKey();
            }

            if (!changes.isEmpty()) {
                this.clientIdentifierRepository.saveAndFlush(clientIdentifierForUpdate);
            }

            final Client client = this.clientRepository.findOne(clientId);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(client.getOffice().getId()) //
                    .withClientId(clientId) //
                    .withEntityId(identifierId) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleClientIdentifierDataIntegrityViolation(documentTypeLabel, documentTypeId, documentKey, dve);
            return new CommandProcessingResult(Long.valueOf(-1));
        }
    }

    private void handleClientIdentifierDataIntegrityViolation(final String documentTypeLabel, final Long documentTypeId,
            final String documentKey, final DataIntegrityViolationException dve) {

        if (dve.getMostSpecificCause().getMessage().contains("unique_client_identifier")) {
            throw new DuplicateClientIdentifierException(documentTypeLabel);
        } else if (dve.getMostSpecificCause().getMessage().contains("unique_identifier_key")) { throw new DuplicateClientIdentifierException(
                documentTypeId, documentTypeLabel, documentKey); }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.clientIdentifier.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteClientIdentifier(final Long clientId, final Long identifierId, final Long commandId) {
        final ClientIdentifier clientIdentifier = this.clientIdentifierRepository.findOne(identifierId);
        if (clientIdentifier == null) { throw new ClientIdentifierNotFoundException(identifierId); }
        this.clientIdentifierRepository.delete(clientIdentifier);

        final Client client = this.clientRepository.findOne(clientId);
        return new CommandProcessingResultBuilder().withCommandId(commandId).withOfficeId(client.getOffice().getId())
                .withClientId(clientId).withEntityId(identifierId).build();
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
}