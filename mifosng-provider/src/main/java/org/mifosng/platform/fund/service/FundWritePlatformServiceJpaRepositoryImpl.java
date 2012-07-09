package org.mifosng.platform.fund.service;

import static org.mifosng.platform.Specifications.fundsThatMatch;

import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.exceptions.FundNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.fund.domain.Fund;
import org.mifosng.platform.fund.domain.FundRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FundWritePlatformServiceJpaRepositoryImpl implements FundWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(FundWritePlatformServiceJpaRepositoryImpl.class);
	
	private final PlatformSecurityContext context;
	private final FundRepository fundRepository;

	@Autowired
	public FundWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final FundRepository fundRepository) {
		this.context = context;
		this.fundRepository = fundRepository;
	}

	@Transactional
	@Override
	public Long createFund(final FundCommand command) {
		
		try {
			AppUser currentUser = context.authenticatedUser();
			
			FundCommandValidator validator = new FundCommandValidator(command);
			validator.validateForCreate();

			Fund fund = Fund.createNew(currentUser.getOrganisation(), command.getName());
			
			this.fundRepository.saveAndFlush(fund);
			
			return fund.getId();
		} catch (DataIntegrityViolationException dve) {
			 handleFundDataIntegrityIssues(command, dve);
			 return Long.valueOf(-1);
		}
	}

	@Transactional
	@Override
	public Long updateFund(final FundCommand command) {

		try {
			AppUser currentUser = context.authenticatedUser();
			
			FundCommandValidator validator = new FundCommandValidator(command);
			validator.validateForCreate();
			
			final Long fundId = command.getId();
			Fund fund = this.fundRepository.findOne(fundsThatMatch(currentUser.getOrganisation(), fundId));
			if (fund == null) {
				throw new FundNotFoundException(fundId);
			}
			fund.update(command);
			
			this.fundRepository.save(fund);
	
			return fund.getId();
		} catch (DataIntegrityViolationException dve) {
			handleFundDataIntegrityIssues(command, dve);
			return Long.valueOf(-1);
		}
	}

	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue is.
	 */
	private void handleFundDataIntegrityIssues(final FundCommand command, DataIntegrityViolationException dve)  {
		
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("externalid_org")) {
			throw new PlatformDataIntegrityException("error.msg.office.duplicate.externalId", "Office with externalId {0} already exists", "externalId", command.getExternalId());
		} else if (realCause.getMessage().contains("name_org")) {
			throw new PlatformDataIntegrityException("error.msg.office.duplicate.name", "Office with name {0} already exists", "name", command.getName());
		} 
		
		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
	}
}