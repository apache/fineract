package org.mifosng.platform.organisation.service;

import org.mifosng.platform.api.commands.CodeCommand;
import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.organisation.domain.Code;
import org.mifosng.platform.organisation.domain.CodeRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeWritePlatformServiceJpaRepositoryImpl implements CodeWritePlatformService {

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

		/**
		 * TODO - NATU - 
		**/
		
		return null;
	}

	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue is.
	 */
	private void handleCodeDataIntegrityIssues(final FundCommand command, DataIntegrityViolationException dve)  {
		
	}
	
}