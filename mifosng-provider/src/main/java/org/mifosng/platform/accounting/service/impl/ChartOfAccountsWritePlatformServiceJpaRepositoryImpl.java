package org.mifosng.platform.accounting.service.impl;

import org.mifosng.platform.accounting.api.commands.ChartOfAccountCommand;
import org.mifosng.platform.accounting.domain.ChartOfAccounts;
import org.mifosng.platform.accounting.domain.ChartOfAccountsRepository;
import org.mifosng.platform.accounting.service.ChartOfAccountsWritePlatformService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChartOfAccountsWritePlatformServiceJpaRepositoryImpl implements ChartOfAccountsWritePlatformService {

//	private final static Logger logger = LoggerFactory.getLogger(ChartOfAccountsWritePlatformServiceJpaRepositoryImpl.class);
	
	private final PlatformSecurityContext context;
	private final ChartOfAccountsRepository chartOfAccountsRepository;

	@Autowired
	public ChartOfAccountsWritePlatformServiceJpaRepositoryImpl(
			final PlatformSecurityContext context, 
			final ChartOfAccountsRepository chartOfAccountsRepository) {
		this.context = context;
		this.chartOfAccountsRepository = chartOfAccountsRepository;
	}

	@Transactional
	@Override
	public Long createAccount(final ChartOfAccountCommand command) {
		
		context.authenticatedUser();
		
		final ChartOfAccounts entity = ChartOfAccounts.createNew(null, command.getName());
		
		this.chartOfAccountsRepository.save(entity);
		
		return entity.getId();
	}
}
