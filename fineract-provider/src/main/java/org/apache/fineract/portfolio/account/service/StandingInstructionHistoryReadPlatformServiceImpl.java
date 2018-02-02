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

import static org.apache.fineract.portfolio.account.service.AccountTransferEnumerations.accountType;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.data.StandingInstructionDTO;
import org.apache.fineract.portfolio.account.data.StandingInstructionHistoryData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class StandingInstructionHistoryReadPlatformServiceImpl implements StandingInstructionHistoryReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ColumnValidator columnValidator;

    // mapper
    private final StandingInstructionHistoryMapper standingInstructionHistoryMapper;

    // pagination
    private final PaginationHelper<StandingInstructionHistoryData> paginationHelper = new PaginationHelper<>();

    @Autowired
    public StandingInstructionHistoryReadPlatformServiceImpl(final RoutingDataSource dataSource,
    		final ColumnValidator columnValidator) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.standingInstructionHistoryMapper = new StandingInstructionHistoryMapper();
        this.columnValidator = columnValidator;
    }

    @Override
    public Page<StandingInstructionHistoryData> retrieveAll(StandingInstructionDTO standingInstructionDTO) {

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.standingInstructionHistoryMapper.schema());
        if (standingInstructionDTO.transferType() != null || standingInstructionDTO.clientId() != null
                || standingInstructionDTO.clientName() != null
                || (standingInstructionDTO.fromAccountType() != null && standingInstructionDTO.fromAccount() != null)
                || standingInstructionDTO.startDateRange() != null || standingInstructionDTO.endDateRange() != null) {
            sqlBuilder.append(" where ");
        }
        boolean addAndCaluse = false;
        List<Object> paramObj = new ArrayList<>();
        if (standingInstructionDTO.transferType() != null) {
            if (addAndCaluse) {
                sqlBuilder.append(" and ");
            }
            sqlBuilder.append(" atd.transfer_type=? ");
            paramObj.add(standingInstructionDTO.transferType());
            addAndCaluse = true;
        }
        if (standingInstructionDTO.clientId() != null) {
            if (addAndCaluse) {
                sqlBuilder.append(" and ");
            }
            sqlBuilder.append(" fromclient.id=? ");
            paramObj.add(standingInstructionDTO.clientId());
            addAndCaluse = true;
        } else if (standingInstructionDTO.clientName() != null) {
            if (addAndCaluse) {
                sqlBuilder.append(" and ");
            }
            sqlBuilder.append(" fromclient.display_name=? ");
            paramObj.add(standingInstructionDTO.clientName());
            addAndCaluse = true;
        }

        if (standingInstructionDTO.fromAccountType() != null && standingInstructionDTO.fromAccount() != null) {
            PortfolioAccountType accountType = PortfolioAccountType.fromInt(standingInstructionDTO.fromAccountType());
            if (addAndCaluse) {
                sqlBuilder.append(" and ");
            }
            if (accountType.isSavingsAccount()) {
                sqlBuilder.append(" fromsavacc.id=? ");
                paramObj.add(standingInstructionDTO.fromAccount());
            } else if (accountType.isLoanAccount()) {
                sqlBuilder.append(" fromloanacc.id=? ");
                paramObj.add(standingInstructionDTO.fromAccount());
            }
            addAndCaluse = true;
        }

        if (standingInstructionDTO.startDateRange() != null) {
            if (addAndCaluse) {
                sqlBuilder.append(" and ");
            }
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            sqlBuilder.append(" atsih.execution_time >= ? ");
            paramObj.add(df.format(standingInstructionDTO.startDateRange()));
            addAndCaluse = true;
        }
        
        if (standingInstructionDTO.endDateRange() != null) {
            if (addAndCaluse) {
                sqlBuilder.append(" and ");
            }
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            sqlBuilder.append(" atsih.execution_time < ? ");
            paramObj.add(df.format(standingInstructionDTO.endDateRange()));
            addAndCaluse = true;
        }

        final SearchParameters searchParameters = standingInstructionDTO.searchParameters();
        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
            this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
            }
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final Object[] finalObjectArray = paramObj.toArray();
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), finalObjectArray,
                this.standingInstructionHistoryMapper);
    }

    private static final class StandingInstructionHistoryMapper implements RowMapper<StandingInstructionHistoryData> {

        private final String schemaSql;

        public StandingInstructionHistoryMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("atsi.id as id,atsi.name as name, ");
            sqlBuilder.append("atsih.status as status, atsih.execution_time as executionTime, ");
            sqlBuilder.append("atsih.amount as amount, atsih.error_log as errorLog, ");
            sqlBuilder.append("fromoff.id as fromOfficeId, fromoff.name as fromOfficeName,");
            sqlBuilder.append("tooff.id as toOfficeId, tooff.name as toOfficeName,");
            sqlBuilder.append("fromclient.id as fromClientId, fromclient.display_name as fromClientName,");
            sqlBuilder.append("toclient.id as toClientId, toclient.display_name as toClientName,");
            sqlBuilder.append("fromsavacc.id as fromSavingsAccountId, fromsavacc.account_no as fromSavingsAccountNo,");
            sqlBuilder.append("fromsp.id as fromProductId, fromsp.name as fromProductName, ");
            sqlBuilder.append("fromloanacc.id as fromLoanAccountId, fromloanacc.account_no as fromLoanAccountNo,");
            sqlBuilder.append("fromlp.id as fromLoanProductId, fromlp.name as fromLoanProductName,");
            sqlBuilder.append("tosavacc.id as toSavingsAccountId, tosavacc.account_no as toSavingsAccountNo,");
            sqlBuilder.append("tosp.id as toProductId, tosp.name as toProductName, ");
            sqlBuilder.append("toloanacc.id as toLoanAccountId, toloanacc.account_no as toLoanAccountNo, ");
            sqlBuilder.append("tolp.id as toLoanProductId, tolp.name as toLoanProductName ");
            sqlBuilder.append(" FROM m_account_transfer_standing_instructions_history atsih ");
            sqlBuilder.append(" join m_account_transfer_standing_instructions atsi on atsi.id = atsih.standing_instruction_id ");
            sqlBuilder.append("join m_account_transfer_details atd on atd.id = atsi.account_transfer_details_id ");
            sqlBuilder.append("join m_office fromoff on fromoff.id = atd.from_office_id ");
            sqlBuilder.append("join m_office tooff on tooff.id = atd.to_office_id ");
            sqlBuilder.append("join m_client fromclient on fromclient.id = atd.from_client_id ");
            sqlBuilder.append("join m_client toclient on toclient.id = atd.to_client_id ");
            sqlBuilder.append("left join m_savings_account fromsavacc on fromsavacc.id = atd.from_savings_account_id ");
            sqlBuilder.append("left join m_savings_product fromsp ON fromsavacc.product_id = fromsp.id ");
            sqlBuilder.append("left join m_loan fromloanacc on fromloanacc.id = atd.from_loan_account_id ");
            sqlBuilder.append("left join m_product_loan fromlp ON fromloanacc.product_id = fromlp.id ");
            sqlBuilder.append("left join m_savings_account tosavacc on tosavacc.id = atd.to_savings_account_id ");
            sqlBuilder.append("left join m_savings_product tosp ON tosavacc.product_id = tosp.id ");
            sqlBuilder.append("left join m_loan toloanacc on toloanacc.id = atd.to_loan_account_id ");
            sqlBuilder.append("left join m_product_loan tolp ON toloanacc.product_id = tolp.id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public StandingInstructionHistoryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");

            final String status = rs.getString("status");
            final LocalDate executionTime = JdbcSupport.getLocalDate(rs, "executionTime");
            final BigDecimal transferAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "amount");
            final String errorLog = rs.getString("errorLog");

            final Long fromOfficeId = JdbcSupport.getLong(rs, "fromOfficeId");
            final String fromOfficeName = rs.getString("fromOfficeName");
            final OfficeData fromOffice = OfficeData.dropdown(fromOfficeId, fromOfficeName, null);

            final Long toOfficeId = JdbcSupport.getLong(rs, "toOfficeId");
            final String toOfficeName = rs.getString("toOfficeName");
            final OfficeData toOffice = OfficeData.dropdown(toOfficeId, toOfficeName, null);

            final Long fromClientId = JdbcSupport.getLong(rs, "fromClientId");
            final String fromClientName = rs.getString("fromClientName");
            final ClientData fromClient = ClientData.lookup(fromClientId, fromClientName, fromOfficeId, fromOfficeName);

            final Long toClientId = JdbcSupport.getLong(rs, "toClientId");
            final String toClientName = rs.getString("toClientName");
            final ClientData toClient = ClientData.lookup(toClientId, toClientName, toOfficeId, toOfficeName);

            final Long fromSavingsAccountId = JdbcSupport.getLong(rs, "fromSavingsAccountId");
            final String fromSavingsAccountNo = rs.getString("fromSavingsAccountNo");
            final Long fromProductId = JdbcSupport.getLong(rs, "fromProductId");
            final String fromProductName = rs.getString("fromProductName");
            final Long fromLoanAccountId = JdbcSupport.getLong(rs, "fromLoanAccountId");
            final String fromLoanAccountNo = rs.getString("fromLoanAccountNo");
            final Long fromLoanProductId = JdbcSupport.getLong(rs, "fromLoanProductId");
            final String fromLoanProductName = rs.getString("fromLoanProductName");
            PortfolioAccountData fromAccount = null;
            EnumOptionData fromAccountType = null;
            if (fromSavingsAccountId != null) {
                fromAccount = new PortfolioAccountData(fromSavingsAccountId, fromSavingsAccountNo, null, null, null, null, null,
                        fromProductId, fromProductName, null, null, null);
                fromAccountType = accountType(PortfolioAccountType.SAVINGS);
            } else if (fromLoanAccountId != null) {
                fromAccount = new PortfolioAccountData(fromLoanAccountId, fromLoanAccountNo, null, null, null, null, null,
                        fromLoanProductId, fromLoanProductName, null, null, null);
                fromAccountType = accountType(PortfolioAccountType.LOAN);
            }

            PortfolioAccountData toAccount = null;
            EnumOptionData toAccountType = null;
            final Long toSavingsAccountId = JdbcSupport.getLong(rs, "toSavingsAccountId");
            final String toSavingsAccountNo = rs.getString("toSavingsAccountNo");
            final Long toProductId = JdbcSupport.getLong(rs, "toProductId");
            final String toProductName = rs.getString("toProductName");
            final Long toLoanAccountId = JdbcSupport.getLong(rs, "toLoanAccountId");
            final String toLoanAccountNo = rs.getString("toLoanAccountNo");
            final Long toLoanProductId = JdbcSupport.getLong(rs, "toLoanProductId");
            final String toLoanProductName = rs.getString("toLoanProductName");

            if (toSavingsAccountId != null) {
                toAccount = new PortfolioAccountData(toSavingsAccountId, toSavingsAccountNo, null, null, null, null, null, toProductId,
                        toProductName, null, null, null);
                toAccountType = accountType(PortfolioAccountType.SAVINGS);
            } else if (toLoanAccountId != null) {
                toAccount = new PortfolioAccountData(toLoanAccountId, toLoanAccountNo, null, null, null, null, null, toLoanProductId,
                        toLoanProductName, null, null, null);
                toAccountType = accountType(PortfolioAccountType.LOAN);
            }

            return new StandingInstructionHistoryData(id, name, fromOffice, fromClient, fromAccountType, fromAccount, toAccountType,
                    toAccount, toOffice, toClient, transferAmount, status, executionTime, errorLog);
        }
    }

}