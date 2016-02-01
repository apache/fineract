/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.account.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.portfolio.account.data.AccountAssociationsData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AccountAssociationsReadPlatformServiceImpl implements AccountAssociationsReadPlatformService {

    private final static Logger logger = LoggerFactory.getLogger(AccountAssociationsReadPlatformServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AccountAssociationsReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public PortfolioAccountData retriveLoanLinkedAssociation(final Long loanId) {
        PortfolioAccountData linkedAccount = null;
        final AccountAssociationsMapper mapper = new AccountAssociationsMapper();
        final String sql = "select " + mapper.schema() + " where aa.loan_account_id = ? and aa.association_type_enum = ?";
        try {
            final AccountAssociationsData accountAssociationsData = this.jdbcTemplate.queryForObject(sql, mapper, loanId,
                    AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());
            if (accountAssociationsData != null) {
                linkedAccount = accountAssociationsData.linkedAccount();
            }
        } catch (final EmptyResultDataAccessException e) {
            logger.debug("Linking account is not configured");
        }
        return linkedAccount;
    }

    @Override
    public Collection<AccountAssociationsData> retriveLoanAssociations(final Long loanId, final Integer associationType) {
        final AccountAssociationsMapper mapper = new AccountAssociationsMapper();
        final String sql = "select " + mapper.schema() + " where aa.loan_account_id = ? and aa.association_type_enum = ?";
        try {
            return this.jdbcTemplate.query(sql, mapper, loanId, associationType);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }

    }

    @Override
    public PortfolioAccountData retriveSavingsLinkedAssociation(final Long savingsId) {
        PortfolioAccountData linkedAccount = null;
        final AccountAssociationsMapper mapper = new AccountAssociationsMapper();
        final String sql = "select " + mapper.schema() + " where aa.savings_account_id = ? and aa.association_type_enum = ?";
        try {
            final AccountAssociationsData accountAssociationsData = this.jdbcTemplate.queryForObject(sql, mapper, savingsId,
                    AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());
            if (accountAssociationsData != null) {
                linkedAccount = accountAssociationsData.linkedAccount();
            }
        } catch (final EmptyResultDataAccessException e) {
            logger.debug("Linking account is not configured");
        }
        return linkedAccount;
    }

    @Override
    public boolean isLinkedWithAnyActiveAccount(final Long savingsId) {
        boolean hasActiveAccount = false;

        final String sql1 = "select aa.is_active as active,aa.association_type_enum as type, loanAccount.loan_status_id as loanStatus,"
                + "savingAccount.status_enum as savingsStatus from m_portfolio_account_associations aa "
                + "left join m_loan loanAccount on loanAccount.id = aa.loan_account_id "
                + "left join m_savings_account savingAccount on savingAccount.id = aa.savings_account_id "
                + "where aa.linked_savings_account_id = ?";

        final List<Map<String, Object>> statusList = this.jdbcTemplate.queryForList(sql1, savingsId);
        for (final Map<String, Object> statusMap : statusList) {
            AccountAssociationType associationType = AccountAssociationType.fromInt((Integer) statusMap.get("type"));
            if (!associationType.isLinkedAccountAssociation() && (Boolean) statusMap.get("active")) {
                hasActiveAccount = true;
                break;
            }

            if (statusMap.get("loanStatus") != null) {
                final LoanStatus loanStatus = LoanStatus.fromInt((Integer) statusMap.get("loanStatus"));
                if (loanStatus.isActiveOrAwaitingApprovalOrDisbursal() || loanStatus.isUnderTransfer()) {
                    hasActiveAccount = true;
                    break;
                }
            }

            if (statusMap.get("savingsStatus") != null) {
                final SavingsAccountStatusType saveStatus = SavingsAccountStatusType.fromInt((Integer) statusMap.get("savingsStatus"));
                if (saveStatus.isActiveOrAwaitingApprovalOrDisbursal() || saveStatus.isUnderTransfer()) {
                    hasActiveAccount = true;
                    break;
                }
            }
        }

        return hasActiveAccount;
    }

    private static final class AccountAssociationsMapper implements RowMapper<AccountAssociationsData> {

        private final String schemaSql;

        public AccountAssociationsMapper() {
            final StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("aa.id as id,");
            // sqlBuilder.append("savingsAccount.id as savingsAccountId, savingsAccount.account_no as savingsAccountNo,");
            sqlBuilder.append("loanAccount.id as loanAccountId, loanAccount.account_no as loanAccountNo,");
            // sqlBuilder.append("linkLoanAccount.id as linkLoanAccountId, linkLoanAccount.account_no as linkLoanAccountNo, ");
            sqlBuilder.append("linkSavingsAccount.id as linkSavingsAccountId, linkSavingsAccount.account_no as linkSavingsAccountNo ");
            sqlBuilder.append("from m_portfolio_account_associations aa ");
            // sqlBuilder.append("left join m_savings_account savingsAccount on savingsAccount.id = aa.savings_account_id ");
            sqlBuilder.append("left join m_loan loanAccount on loanAccount.id = aa.loan_account_id ");
            sqlBuilder.append("left join m_savings_account linkSavingsAccount on linkSavingsAccount.id = aa.linked_savings_account_id ");
            // sqlBuilder.append("left join m_loan linkLoanAccount on linkLoanAccount.id = aa.linked_loan_account_id ");
            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public AccountAssociationsData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            // final Long savingsAccountId = JdbcSupport.getLong(rs,
            // "savingsAccountId");
            // final String savingsAccountNo = rs.getString("savingsAccountNo");
            final Long loanAccountId = JdbcSupport.getLong(rs, "loanAccountId");
            final String loanAccountNo = rs.getString("loanAccountNo");
            final PortfolioAccountData account = PortfolioAccountData.lookup(loanAccountId, loanAccountNo);
            /*
             * if (savingsAccountId != null) { account =
             * PortfolioAccountData.lookup(savingsAccountId, savingsAccountNo);
             * } else if (loanAccountId != null) { account =
             * PortfolioAccountData.lookup(loanAccountId, loanAccountNo); }
             */
            final Long linkSavingsAccountId = JdbcSupport.getLong(rs, "linkSavingsAccountId");
            final String linkSavingsAccountNo = rs.getString("linkSavingsAccountNo");
            // final Long linkLoanAccountId = JdbcSupport.getLong(rs,
            // "linkLoanAccountId");
            // final String linkLoanAccountNo =
            // rs.getString("linkLoanAccountNo");
            final PortfolioAccountData linkedAccount = PortfolioAccountData.lookup(linkSavingsAccountId, linkSavingsAccountNo);
            /*
             * if (linkSavingsAccountId != null) { linkedAccount =
             * PortfolioAccountData.lookup(linkSavingsAccountId,
             * linkSavingsAccountNo); } else if (linkLoanAccountId != null) {
             * linkedAccount = PortfolioAccountData.lookup(linkLoanAccountId,
             * linkLoanAccountNo); }
             */

            return new AccountAssociationsData(id, account, linkedAccount);
        }

    }
}
