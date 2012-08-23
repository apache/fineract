package org.mifosng.platform.client.service;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.commands.NoteCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.client.domain.Note;
import org.mifosng.platform.client.domain.NoteRepository;
import org.mifosng.platform.exceptions.ClientNotFoundException;
import org.mifosng.platform.exceptions.NoteNotFoundException;
import org.mifosng.platform.exceptions.OfficeNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.OfficeRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
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
	private final NoteRepository noteRepository;

	@Autowired
	public ClientWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final ClientRepository clientRepository, 
			final OfficeRepository officeRepository, NoteRepository noteRepository) {
		this.context = context;
		this.clientRepository = clientRepository;
		this.officeRepository = officeRepository;
		this.noteRepository = noteRepository;
	}
	
	@Transactional
	@Override
	public EntityIdentifier deleteClient(final Long clientId) {
		
		Client client = this.clientRepository.findOne(clientId);
		if (client == null || client.isDeleted()) {
			throw new ClientNotFoundException(clientId);
		}
		
		client.delete();
		this.clientRepository.save(client);

		return new EntityIdentifier(client.getId());
	}
	
	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue is.
	 */
	private void handleDataIntegrityIssues(final ClientCommand command, final DataIntegrityViolationException dve)  {
		
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("external_id")) {
			throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId", "Client with externalId `"+ command.getExternalId() +"` already exists", "externalId", command.getExternalId());
		}
		
		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
	}
	
	@Transactional
	@Override
	public Long enrollClient(final ClientCommand command) {

		try{
			context.authenticatedUser();
			
			ClientCommandValidator validator = new ClientCommandValidator(command);
			validator.validateForCreate();
	
			Office clientOffice = this.officeRepository.findOne(command.getOfficeId());
			if (clientOffice == null) {
				throw new OfficeNotFoundException(command.getOfficeId());
			}
			
			String firstname = command.getFirstname();
			String lastname = command.getLastname();
			if (StringUtils.isNotBlank(command.getClientOrBusinessName())) {
				lastname = command.getClientOrBusinessName();
				firstname = null;
			}
	
			Client newClient = Client.newClient(clientOffice, firstname, lastname, command.getJoiningDate(), command.getExternalId());
					
			this.clientRepository.save(newClient);
	
			return newClient.getId();
		} catch (DataIntegrityViolationException dve) {
			 handleDataIntegrityIssues(command, dve);
			 return Long.valueOf(-1);
		}
	}
	
	@Transactional
	@Override
	public EntityIdentifier updateClientDetails(final ClientCommand command) {
		
		try {
			context.authenticatedUser();
			
			ClientCommandValidator validator = new ClientCommandValidator(command);
			validator.validateForUpdate();
			
			Office clientOffice = null;
			Long officeId = command.getOfficeId();
			if (command.isOfficeChanged() && officeId != null) {
				clientOffice = this.officeRepository.findOne(officeId);
				if (clientOffice == null) {
					throw new OfficeNotFoundException(command.getOfficeId());
				}			
			}
	
			Client clientForUpdate = this.clientRepository.findOne(command.getId());
			if (clientForUpdate == null || clientForUpdate.isDeleted()) {
				throw new ClientNotFoundException(command.getId());
			}
			clientForUpdate.update(clientOffice, command);
					
			this.clientRepository.saveAndFlush(clientForUpdate);
	
			return new EntityIdentifier(clientForUpdate.getId());
		} catch (DataIntegrityViolationException dve) {
			 handleDataIntegrityIssues(command, dve);
			 return new EntityIdentifier(Long.valueOf(-1));
		}
	}
	
	@Transactional
	@Override
	public EntityIdentifier addClientNote(final NoteCommand command) {
		
		context.authenticatedUser();
		
		Client clientForUpdate = this.clientRepository.findOne(command.getClientId());
		if (clientForUpdate == null) {
			throw new ClientNotFoundException(command.getClientId());
		}
		
		Note note = Note.clientNote(clientForUpdate, command.getNote());
		
		this.noteRepository.save(note);
		
		return new EntityIdentifier(note.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier updateNote(final NoteCommand command) {
		
		context.authenticatedUser();
		
		Note noteForUpdate = this.noteRepository.findOne(command.getId());
		if (noteForUpdate == null || noteForUpdate.isNotAgainstClientWithIdOf(command.getClientId())) {
			throw new NoteNotFoundException(command.getId(), command.getClientId(), "client");
		}
		
		noteForUpdate.update(command.getNote());
		
		return new EntityIdentifier(noteForUpdate.getId());
	}
}