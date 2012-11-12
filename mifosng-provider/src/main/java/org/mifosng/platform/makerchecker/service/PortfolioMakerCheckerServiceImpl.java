package org.mifosng.platform.makerchecker.service;

import org.joda.time.LocalDate;
import org.mifosng.platform.makerchecker.domain.MakerChecker;
import org.mifosng.platform.makerchecker.domain.MakerCheckerRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class PortfolioMakerCheckerServiceImpl implements PortfolioMakerCheckerService {

	private final PlatformSecurityContext context;
	private final MakerCheckerRepository makerCheckerRepository;

	@Autowired
	public PortfolioMakerCheckerServiceImpl(final PlatformSecurityContext context, final MakerCheckerRepository makerCheckerRepository) {
		this.context = context;
		this.makerCheckerRepository = makerCheckerRepository;
	}
	
	@Override
	public Long logNewEntry(final String taskName, final String taskJson) {
		
		final AppUser maker = context.authenticatedUser();
		
		final MakerChecker entity = MakerChecker.makerEntry(taskName, taskJson, maker, new LocalDate());
		
		makerCheckerRepository.save(entity);
		
		return entity.getId();
	}
}