package org.mifosng.platform.documentmanagement.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.mifosng.platform.api.commands.DocumentCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.common.ApplicationConstants;
import org.mifosng.platform.common.FileUtils;
import org.mifosng.platform.documentmanagement.domain.Document;
import org.mifosng.platform.documentmanagement.domain.DocumentRepository;
import org.mifosng.platform.exceptions.DocumentNotFoundException;
import org.mifosng.platform.exceptions.DocumentManagementException;
import org.mifosng.platform.exceptions.InvalidEntityTypeForDocumentManagementException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.organisation.domain.OfficeRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.staff.domain.StaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentWritePlatformServiceJpaRepositoryImpl implements
		DocumentWritePlatformService {

	private final static Logger logger = LoggerFactory
			.getLogger(DocumentWritePlatformServiceJpaRepositoryImpl.class);

	// TODO: Use these services to ensure that the appropriate entities exist,
	// are active, check data scope for user etc
	private final PlatformSecurityContext context;
	private final ClientRepository clientRepository;
	private final OfficeRepository officeRepository;
	private final LoanRepository loanRepository;
	private final StaffRepository staffRepository;
	private final DocumentRepository documentRepository;

	@Autowired
	public DocumentWritePlatformServiceJpaRepositoryImpl(
			final PlatformSecurityContext context,
			final ClientRepository clientRepository,
			final DocumentRepository documentRepository,
			final OfficeRepository officeRepository,
			final LoanRepository loanRepository,
			final StaffRepository staffRepository) {
		this.context = context;
		this.clientRepository = clientRepository;
		this.documentRepository = documentRepository;
		this.officeRepository = officeRepository;
		this.loanRepository = loanRepository;
		this.staffRepository = staffRepository;
	}

	@Transactional
	@Override
	public Long createDocument(DocumentCommand documentCommand,
			InputStream inputStream) {
		try {
			DocumentCommandValidator validator = new DocumentCommandValidator(
					documentCommand);

			validateParentEntityType(documentCommand);

			validator.validateForCreate();

			String fileUploadLocation = FileUtils.generateFileParentDirectory(
					documentCommand.getParentEntityType(),
					documentCommand.getParentEntityId());

			/** Recursively create the directory if it does not exist **/
			if (!new File(fileUploadLocation).isDirectory()) {
				new File(fileUploadLocation).mkdirs();
			}

			String fileLocation = FileUtils.saveToFileSystem(inputStream,
					fileUploadLocation, documentCommand.getFileName());

			Document document = Document.createNew(
					documentCommand.getParentEntityType(),
					documentCommand.getParentEntityId(),
					documentCommand.getName(), documentCommand.getFileName(),
					documentCommand.getSize(), documentCommand.getType(),
					documentCommand.getDescription(), fileLocation);

			this.documentRepository.save(document);

			return document.getId();
		} catch (DataIntegrityViolationException dve) {
			logger.error(dve.getMessage(), dve);
			throw new PlatformDataIntegrityException(
					"error.msg.document.unknown.data.integrity.issue",
					"Unknown data integrity issue with resource.");
		} catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
			throw new DocumentManagementException(documentCommand.getName());
		}
	}

	@Transactional
	@Override
	public EntityIdentifier updateDocument(DocumentCommand documentCommand,
			InputStream inputStream) {
		try {
			String oldLocation = null;
			DocumentCommandValidator validator = new DocumentCommandValidator(
					documentCommand);
			validator.validateForUpdate();
			// TODO check if entity id is valid and within data scope for the
			// user
			Document documentForUpdate = this.documentRepository
					.findOne(documentCommand.getId());
			if (documentForUpdate == null) {
				throw new DocumentNotFoundException(
						documentCommand.getParentEntityType(),
						documentCommand.getParentEntityId(),
						documentCommand.getId());
			}
			oldLocation = documentForUpdate.getLocation();
			// if a new file is also passed in
			if (inputStream != null && documentCommand.isFileNameChanged()) {
				String fileUploadLocation = FileUtils
						.generateFileParentDirectory(
								documentCommand.getParentEntityType(),
								documentCommand.getParentEntityId());

				/** Recursively create the directory if it does not exist **/
				if (!new File(fileUploadLocation).isDirectory()) {
					new File(fileUploadLocation).mkdirs();
				}

				// TODO replace file system appender with an Amazon S3 appender
				String fileLocation = FileUtils.saveToFileSystem(inputStream,
						fileUploadLocation, documentCommand.getFileName());
				documentCommand.setLocation(fileLocation);
			}

			documentForUpdate.update(documentCommand);

			if (inputStream != null && documentCommand.isFileNameChanged()) {
				// delete previous file
				deleteFile(documentCommand.getName(), oldLocation);
			}

			this.documentRepository.saveAndFlush(documentForUpdate);

			return new EntityIdentifier(documentForUpdate.getId());
		} catch (DataIntegrityViolationException dve) {
			logger.error(dve.getMessage(), dve);
			throw new PlatformDataIntegrityException(
					"error.msg.document.unknown.data.integrity.issue",
					"Unknown data integrity issue with resource.");
		} catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
			throw new DocumentManagementException(documentCommand.getName());
		}
	}

	@Transactional
	@Override
	public EntityIdentifier deleteDocument(DocumentCommand documentCommand) {
		validateParentEntityType(documentCommand);
		// TODO: Check document is present under this entity Id
		Document document = this.documentRepository.findOne(documentCommand
				.getId());
		if (document == null) {
			throw new DocumentNotFoundException(
					documentCommand.getParentEntityType(),
					documentCommand.getParentEntityId(),
					documentCommand.getId());
		}

		this.documentRepository.delete(document);
		deleteFile(document.getName(), document.getLocation());
		return new EntityIdentifier(document.getId());
	}

	private void deleteFile(String documentName, String location) {
		File fileToBeDeleted = new File(location);
		boolean fileDeleted = fileToBeDeleted.delete();
		if (!fileDeleted) {
			throw new DocumentManagementException(documentName);
		}
	}

	private void validateParentEntityType(DocumentCommand documentCommand) {
		if (!checkValidEntityType(documentCommand.getParentEntityType())) {
			throw new InvalidEntityTypeForDocumentManagementException(
					documentCommand.getParentEntityType());
		}
	}

	private static boolean checkValidEntityType(String entityType) {
		for (ApplicationConstants.DOCUMENT_MANAGEMENT_ENTITIES entities : ApplicationConstants.DOCUMENT_MANAGEMENT_ENTITIES
				.values()) {
			if (entities.name().equalsIgnoreCase(entityType)) {
				return true;
			}
		}
		return false;
	}
}