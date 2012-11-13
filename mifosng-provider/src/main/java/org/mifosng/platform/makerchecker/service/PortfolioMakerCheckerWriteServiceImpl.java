package org.mifosng.platform.makerchecker.service;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.client.service.ClientWritePlatformService;
import org.mifosng.platform.makerchecker.domain.MakerChecker;
import org.mifosng.platform.makerchecker.domain.MakerCheckerRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Service
public class PortfolioMakerCheckerWriteServiceImpl implements PortfolioMakerCheckerWriteService {

	private final PlatformSecurityContext context;
	private final MakerCheckerRepository makerCheckerRepository;
	private final PortfolioApiDataConversionService apiDataConversionService;
	private final ClientWritePlatformService clientWritePlatformService;

	@Autowired
	public PortfolioMakerCheckerWriteServiceImpl(final PlatformSecurityContext context, final MakerCheckerRepository makerCheckerRepository,
			final PortfolioApiDataConversionService apiDataConversionService,
			final ClientWritePlatformService clientWritePlatformService) {
		this.context = context;
		this.makerCheckerRepository = makerCheckerRepository;
		this.apiDataConversionService = apiDataConversionService;
		this.clientWritePlatformService = clientWritePlatformService;
	}
	
	@Transactional
	@Override
	public Long logNewEntry(final String taskName, final String taskJson) {
		
		final AppUser maker = context.authenticatedUser();
		
		final MakerChecker entity = MakerChecker.makerEntry(taskName, taskJson, maker, new LocalDate());
		
		makerCheckerRepository.save(entity);
		
		return entity.getId();
	}

	@Transactional
	@Override
	public Long approveEntry(final Long id) {
		
		final AppUser checker = context.authenticatedUser();
		
		final MakerChecker entity = this.makerCheckerRepository.findOne(id);
		entity.markAsChecked(checker, new LocalDate());
		
		makerCheckerRepository.save(entity);
		
		// assume client creation for now
		final ClientCommand command = this.apiDataConversionService.convertJsonToClientCommand(null, entity.json());
		
		this.clientWritePlatformService.enrollClient(command);
		
		return entity.getId();
	}
}