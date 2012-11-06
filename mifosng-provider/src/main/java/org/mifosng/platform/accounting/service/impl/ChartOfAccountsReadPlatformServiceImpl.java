package org.mifosng.platform.accounting.service.impl;

import java.util.Arrays;
import java.util.Collection;

import org.mifosng.platform.accounting.api.data.ChartOfAccountsData;
import org.mifosng.platform.accounting.api.data.GeneralLedgerAccountData;
import org.mifosng.platform.accounting.service.ChartOfAccountsReadPlatformService;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChartOfAccountsReadPlatformServiceImpl implements ChartOfAccountsReadPlatformService {

	@SuppressWarnings("unused")
	private final JdbcTemplate jdbcTemplate;
	@SuppressWarnings("unused")
	private final PlatformSecurityContext context;
	
	@Autowired
	public ChartOfAccountsReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public ChartOfAccountsData retrieveChartOfAccounts() {
		// FIXME - KW - just stubbing impl for now.
		
		boolean isLedgerAccountDisabled = false;
		boolean isManualEntriesAllowed = true;
		
		final String assetsGlCode = "1";
		final String category = "ASSETS";
		final Long parentId = null;
		final String generalLedgerName = "Assets";
		final String ledgerType = "HEADER";
		final String description = "Some descriptoin of the general ledger accounts purpose.";
		
		GeneralLedgerAccountData assetsHeader = new GeneralLedgerAccountData(Long.valueOf(1), generalLedgerName, parentId, assetsGlCode, isLedgerAccountDisabled, isManualEntriesAllowed, category, ledgerType, description);
		
		GeneralLedgerAccountData liabilitiesHeader = new GeneralLedgerAccountData(Long.valueOf(1), "Liabilities", parentId, "2", isLedgerAccountDisabled, isManualEntriesAllowed, "LIABILITIES", ledgerType, description);
		
		Collection<GeneralLedgerAccountData> accounts = Arrays.asList(assetsHeader, liabilitiesHeader);
		
		return new ChartOfAccountsData(Long.valueOf(1), "Default Chart of Accounts", accounts);
	}
}
