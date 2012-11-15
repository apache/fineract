package org.mifosng.platform.organisation.service;

import org.mifosng.platform.api.commands.CodeCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.exceptions.CodeValueNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.organisation.domain.Code;
import org.mifosng.platform.organisation.domain.CodeRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeWritePlatformServiceJpaRepositoryImpl implements CodeWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(CodeWritePlatformServiceJpaRepositoryImpl.class);
	
	private final PlatformSecurityContext context;
	private final CodeRepository codeRepository;

	@Autowired
	public CodeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final CodeRepository codeRepository) {
		this.context = context;
		this.codeRepository = codeRepository;
	}

	@Transactional
	@Override
	public Long createCode(final CodeCommand command) {

		try {
			context.authenticatedUser();

			CodeCommandValidator validator = new CodeCommandValidator(command);
			validator.validateForCreate();

			Code code = Code.createNew(command.getCodeName());

			this.codeRepository.saveAndFlush(code);

			return code.getId();
		} catch (DataIntegrityViolationException dve) {
			 return Long.valueOf(-1);
		}
	}

	@Transactional
	@Override
	public Long updateCode(final CodeCommand command) {

		try
		{
			context.authenticatedUser();

			CodeCommandValidator validator = new CodeCommandValidator(command);
			validator.validateForUpdate();

			final Long codeId = command.getId();
			Code code = this.codeRepository.findOne(codeId);
			if (code == null) {
				throw new CodeValueNotFoundException(codeId);
			}
			code.update(command);

			this.codeRepository.saveAndFlush(code);

			return code.getId();
		}
		catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
			return Long.valueOf(-1);
		}

	}

	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue is.
	 */
	private void handleCodeDataIntegrityIssues(final CodeCommand command, final DataIntegrityViolationException dve)  {
		Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("code_name_org")) {
			throw new PlatformDataIntegrityException("error.msg.code.duplicate.name", "A code with name '" + command.getCodeName() + "' already exists");
		}

		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue", "Unknown data integrity issue with resource: " + realCause.getMessage());
	}

	@Transactional
	@Override
	public EntityIdentifier deleteCode(final Long codeId) {

		context.authenticatedUser();

		retrieveCodeBy(codeId);
		this.codeRepository.delete(codeId);

		return new EntityIdentifier(codeId);
	}

	private Code retrieveCodeBy(final Long codeId) {
		final Code code = this.codeRepository.findOne(codeId);
        if (code == null) {
            throw new CodeValueNotFoundException(codeId);
        }
		return code;
	}
}