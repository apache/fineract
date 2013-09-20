/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.staff.data.StaffAccountSummaryCollectionData;
import org.mifosplatform.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.mifosplatform.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.mifosplatform.portfolio.client.domain.ClientStatus;
import org.mifosplatform.portfolio.group.domain.GroupingTypeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class BulkLoansReadPlatformServiceImpl implements BulkLoansReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;

    @Autowired
    public BulkLoansReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final AccountDetailsReadPlatformService accountDetailsReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
    }

    @Override
    public StaffAccountSummaryCollectionData retrieveLoanOfficerAccountSummary(final Long loanOfficerId) {

        this.context.authenticatedUser();

        final StaffClientMapper staffClientMapper = new StaffClientMapper();
        final String clientSql = "select distinct " + staffClientMapper.schema() + " and c.status_enum=?";

        final StaffGroupMapper staffGroupMapper = new StaffGroupMapper();
        final String groupSql = "select distinct " + staffGroupMapper.schema() + " and g.status_enum=?";

        final List<StaffAccountSummaryCollectionData.LoanAccountSummary> clientSummaryList = this.jdbcTemplate.query(clientSql,
                staffClientMapper, new Object[] { loanOfficerId, ClientStatus.ACTIVE.getValue() });

        for (final StaffAccountSummaryCollectionData.LoanAccountSummary clientSummary : clientSummaryList) {

            final Collection<LoanAccountSummaryData> clientLoanAccounts = this.accountDetailsReadPlatformService
                    .retrieveClientLoanAccountsByLoanOfficerId(clientSummary.getId(), loanOfficerId);

            clientSummary.setLoans(clientLoanAccounts);
        }

        final List<StaffAccountSummaryCollectionData.LoanAccountSummary> groupSummaryList = this.jdbcTemplate.query(groupSql,
                staffGroupMapper, new Object[] { loanOfficerId, GroupingTypeStatus.ACTIVE.getValue() });

        for (final StaffAccountSummaryCollectionData.LoanAccountSummary groupSummary : groupSummaryList) {

            final Collection<LoanAccountSummaryData> groupLoanAccounts = this.accountDetailsReadPlatformService
                    .retrieveGroupLoanAccountsByLoanOfficerId(groupSummary.getId(), loanOfficerId);

            groupSummary.setLoans(groupLoanAccounts);
        }

        return new StaffAccountSummaryCollectionData(clientSummaryList, groupSummaryList);
    }

    private static final class StaffClientMapper implements RowMapper<StaffAccountSummaryCollectionData.LoanAccountSummary> {

        public String schema() {
            return " c.id as id, c.display_name as displayName from m_client c "
                    + " join m_loan l on c.id = l.client_id where l.loan_officer_id = ? ";
        }

        @Override
        public StaffAccountSummaryCollectionData.LoanAccountSummary mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String displayName = rs.getString("displayName");

            return new StaffAccountSummaryCollectionData.LoanAccountSummary(id, displayName);
        }
    }

    private static final class StaffGroupMapper implements RowMapper<StaffAccountSummaryCollectionData.LoanAccountSummary> {

        public String schema() {
            return " g.id as id, g.display_name as name from m_group g"
                    + " join m_loan l on g.id = l.group_id where l.loan_officer_id = ? ";
        }

        @Override
        public StaffAccountSummaryCollectionData.LoanAccountSummary mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String name = rs.getString("name");

            return new StaffAccountSummaryCollectionData.LoanAccountSummary(id, name);
        }
    }
}