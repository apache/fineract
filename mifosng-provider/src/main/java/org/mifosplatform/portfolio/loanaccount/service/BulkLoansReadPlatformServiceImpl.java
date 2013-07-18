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
import org.mifosplatform.portfolio.client.data.ClientLoanAccountSummaryData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.group.data.GroupAccountSummaryData;
import org.mifosplatform.portfolio.group.service.GroupReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class BulkLoansReadPlatformServiceImpl implements BulkLoansReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;

    @Autowired
    public BulkLoansReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final ClientReadPlatformService clientReadPlatformService, final GroupReadPlatformService groupReadPlatformService) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.clientReadPlatformService = clientReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
    }

    @Override
    public StaffAccountSummaryCollectionData retrieveLoanOfficerAccountSummary(final Long loanOfficerId) {

        context.authenticatedUser();

        final StaffClientMapper staffClientMapper = new StaffClientMapper();
        final String clientSql = "select distinct " + staffClientMapper.schema();

        final StaffGroupMapper staffGroupMapper = new StaffGroupMapper();
        final String groupSql = "select distinct " + staffGroupMapper.schema();

        final List<StaffAccountSummaryCollectionData.ClientSummary> clientSummaryList = this.jdbcTemplate.query(clientSql,
                staffClientMapper, new Object[] { loanOfficerId });

        for (StaffAccountSummaryCollectionData.ClientSummary clientSummary : clientSummaryList) {

            final Collection<ClientLoanAccountSummaryData> clientLoanAccounts = this.clientReadPlatformService
                    .retrieveClientLoanAccountsByLoanOfficerId(clientSummary.getId(), loanOfficerId);

            clientSummary.setLoans(clientLoanAccounts);
        }

        final List<StaffAccountSummaryCollectionData.GroupSummary> groupSummaryList = this.jdbcTemplate.query(groupSql, staffGroupMapper,
                new Object[] { loanOfficerId });

        for (StaffAccountSummaryCollectionData.GroupSummary groupSummary : groupSummaryList) {

            final Collection<GroupAccountSummaryData> groupLoanAccounts = this.groupReadPlatformService
                    .retrieveGroupLoanAccountsByLoanOfficerId(groupSummary.getId(), loanOfficerId);

            groupSummary.setLoans(groupLoanAccounts);
        }

        return new StaffAccountSummaryCollectionData(clientSummaryList, groupSummaryList);
    }

    private static final class StaffClientMapper implements RowMapper<StaffAccountSummaryCollectionData.ClientSummary> {

        public String schema() {
            return " c.id as id, c.display_name as displayName from m_client c "
                    + " join m_loan l on c.id = l.client_id where l.loan_officer_id = ? ";
        }

        @Override
        public StaffAccountSummaryCollectionData.ClientSummary mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String displayName = rs.getString("displayName");

            return new StaffAccountSummaryCollectionData.ClientSummary(id, displayName);
        }
    }

    private static final class StaffGroupMapper implements RowMapper<StaffAccountSummaryCollectionData.GroupSummary> {

        public String schema() {
            return " g.id as id, g.display_name as name from m_group g"
                    + " join m_loan l on g.id = l.group_id where l.loan_officer_id = ? ";
        }

        @Override
        public StaffAccountSummaryCollectionData.GroupSummary mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String name = rs.getString("name");

            return new StaffAccountSummaryCollectionData.GroupSummary(id, name);
        }
    }
}