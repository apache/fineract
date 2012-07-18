package org.mifosng.platform.organisation.service;

import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.ApplicationCurrencyRepository;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.exceptions.CurrencyNotFoundException;
import org.mifosng.platform.exceptions.NoAuthorizationException;
import org.mifosng.platform.exceptions.OfficeNotFoundException;
import org.mifosng.platform.exceptions.PlatformDataIntegrityException;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.OfficeRepository;
import org.mifosng.platform.organisation.domain.OfficeTransaction;
import org.mifosng.platform.organisation.domain.OfficeTransactionRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfficeWritePlatformServiceJpaRepositoryImpl implements OfficeWritePlatformService {

	private final static Logger logger = LoggerFactory.getLogger(OfficeWritePlatformServiceJpaRepositoryImpl.class);
	
	private final PlatformSecurityContext context;
	private final OfficeRepository officeRepository;
	private final OfficeTransactionRepository officeMonetaryTransferRepository;
	private final ApplicationCurrencyRepository applicationCurrencyRepository;

	@Autowired
	public OfficeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, 
			final OfficeRepository officeRepository, 
			final OfficeTransactionRepository officeMonetaryTransferRepository,
			final ApplicationCurrencyRepository applicationCurrencyRepository) {
		this.context = context;
		this.officeRepository = officeRepository;
		this.officeMonetaryTransferRepository = officeMonetaryTransferRepository;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
	}

	@Transactional
	@Override
	public Long createOffice(final OfficeCommand command) {
		
		try {
			AppUser currentUser = context.authenticatedUser();
			
			OfficeCommandValidator validator = new OfficeCommandValidator(command);
			validator.validateForCreate();
			
			Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, command.getParentId());
	
			Office office = Office.createNew(parent, command.getName(), command.getOpeningDate(), command.getExternalId());
			
			// pre save to generate id for use in office hierarchy
			this.officeRepository.save(office);
			
			office.generateHierarchy();
			
			this.officeRepository.saveAndFlush(office);
			
			return office.getId();
		} catch (DataIntegrityViolationException dve) {
			 handleOfficeDataIntegrityIssues(command, dve);
			 return Long.valueOf(-1);
		}
	}

	@Transactional
	@Override
	public Long updateOffice(final OfficeCommand command) {

		try {
			AppUser currentUser = context.authenticatedUser();
			
			OfficeCommandValidator validator = new OfficeCommandValidator(command);
			validator.validateForUpdate();
			
			Office office = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, command.getId());
			
			office.update(command);
			
			if (command.isParentChanged()) {
				Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, command.getParentId());
				office.update(parent);
			}
	
			this.officeRepository.saveAndFlush(office);
	
			return office.getId();
		} catch (DataIntegrityViolationException dve) {
			handleOfficeDataIntegrityIssues(command, dve);
			return Long.valueOf(-1);
		}
	}
	
	@Transactional
	@Override
	public Long externalBranchMoneyTransfer(BranchMoneyTransferCommand command) {
		
		context.authenticatedUser();
		
		BranchMoneyTransferCommandValidator validator = new BranchMoneyTransferCommandValidator(command);
		validator.validateExternalBranchTransfer();
		
		Office fromOffice = null;
		if (command.getFromOfficeId() != null) {
			fromOffice = this.officeRepository.findOne(command.getFromOfficeId());
		}
		Office toOffice = null;
		if (command.getToOfficeId() != null) {
			toOffice = this.officeRepository.findOne(command.getToOfficeId());
		}
		
		if (fromOffice == null && toOffice == null) {
			throw new OfficeNotFoundException(command.getToOfficeId());
		}

		final String currencyCode = command.getCurrencyCode();
		ApplicationCurrency appCurrency = this.applicationCurrencyRepository.findOneByCode(currencyCode);
		if (appCurrency == null) {
			throw new CurrencyNotFoundException(currencyCode);
		}
		
		MonetaryCurrency currency = new MonetaryCurrency(appCurrency.getCode(), appCurrency.getDecimalPlaces());
		Money amount = Money.of(currency, command.getTransactionAmount());
		
		OfficeTransaction entity = OfficeTransaction.create(fromOffice, toOffice, command.getTransactionDate(), amount);
		
		this.officeMonetaryTransferRepository.save(entity);
		
		return entity.getId();
	}

	/*
	 * Guaranteed to throw an exception no matter what the data integrity issue is.
	 */
	private void handleOfficeDataIntegrityIssues(final OfficeCommand command, DataIntegrityViolationException dve)  {
		
		Throwable realCause = dve.getMostSpecificCause();
		if (realCause.getMessage().contains("externalid_org")) {
			throw new PlatformDataIntegrityException("error.msg.office.duplicate.externalId", "Office with externalId {0} already exists", "externalId", command.getExternalId());
		} else if (realCause.getMessage().contains("name_org")) {
			throw new PlatformDataIntegrityException("error.msg.office.duplicate.name", "Office with name {0} already exists", "name", command.getName());
		} 
		
		logger.error(dve.getMessage(), dve);
		throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
	}

	/*
	 * used to restrict modifying operations to office that are either the users office or lower (child) in the office hierarchy
	 */
	private Office validateUserPriviledgeOnOfficeAndRetrieve(AppUser currentUser, Long officeId) {
		
		Office userOffice = this.officeRepository.findOne(currentUser.getOffice().getId());
		if (userOffice == null) {
			throw new OfficeNotFoundException(currentUser.getOffice().getId());
		}
		
		if (userOffice.doesNotHaveAnOfficeInHierarchyWithId(officeId)) {
			throw new NoAuthorizationException("User does not have sufficient priviledges to act on the provided office.");
		}
		
		Office officeToReturn = userOffice;
		if (!userOffice.identifiedBy(officeId)) {
			officeToReturn = this.officeRepository.findOne(officeId);
			if (officeToReturn == null) {
				throw new OfficeNotFoundException(officeId);
			}
		}
		
		return officeToReturn;
	}
}