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
package org.apache.fineract.portfolio.shareaccounts.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountData;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountDividendData;
import org.apache.fineract.portfolio.shareaccounts.domain.ShareAccountDividendStatusType;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductDividendStatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ShareAccountDividendReadPlatformServiceImpl implements ShareAccountDividendReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ColumnValidator columnValidator;
    private final PaginationHelper<ShareAccountDividendData> paginationHelper = new PaginationHelper<>();

    @Autowired
    public ShareAccountDividendReadPlatformServiceImpl(final RoutingDataSource dataSource,
    		final ColumnValidator columnValidator) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.columnValidator = columnValidator;
    }

    @Override
    public List<Map<String, Object>> retriveDividendDetailsForPostDividents() {
        StringBuilder sb = new StringBuilder();
        sb.append("select ");
        sb.append(" sadd.id as id, ");
        sb.append(" sa.savings_account_id as savingsAccountId ");
        sb.append(" from m_share_account_dividend_details sadd");
        sb.append(" inner join m_share_product_dividend_pay_out spdpo on spdpo.id = sadd.dividend_pay_out_id ");
        sb.append(" inner join m_share_account sa on sa.id = sadd.account_id ");
        sb.append(" where spdpo.status = ? and sadd.status = ?");
        return this.jdbcTemplate.queryForList(sb.toString(), ShareProductDividendStatusType.APPROVED.getValue(),
                ShareAccountDividendStatusType.INITIATED.getValue());
    }

    @Override
    public Page<ShareAccountDividendData> retriveAll(final Long payoutDetailId, final SearchParameters searchParameters) {
        ShareAccountDividendMapper shareAccountDividendMapper = new ShareAccountDividendMapper();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(shareAccountDividendMapper.schema());
        sqlBuilder.append(" where sadd.dividend_pay_out_id = ? ");
        List<Object> params = new ArrayList<>(2);
        params.add(payoutDetailId);
        if (searchParameters.getAccountNo() != null) {
            sqlBuilder.append(" and sa.account_no = ? ");
            params.add(searchParameters.getAccountNo());
        }
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

        final String sqlCountRows = "SELECT FOUND_ROWS()";
        Object[] paramsObj = params.toArray();
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), paramsObj,
                shareAccountDividendMapper);
    }

    private static final class ShareAccountDividendMapper implements RowMapper<ShareAccountDividendData> {

        private final String sql;

        public ShareAccountDividendMapper() {
            StringBuilder sb = new StringBuilder();
            sb.append(" sadd.id as id, sadd.amount as amount,");
            sb.append(" sadd.status as status, sadd.savings_transaction_id as savingsTransactionId,");
            sb.append(" sa.id as accountId,sa.account_no as accountNumber, ");
            sb.append(" mc.id as clientId,mc.display_name as clientName ");
            sb.append(" from m_share_account_dividend_details sadd");
            sb.append(" inner join m_share_account sa on sa.id = sadd.account_id ");
            sb.append(" inner join m_client mc on mc.id=sa.client_id ");
            sql = sb.toString();
        }

        public String schema() {
            return this.sql;
        }

        @Override
        public ShareAccountDividendData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final BigDecimal amount = rs.getBigDecimal("amount");
            final Integer status = JdbcSupport.getInteger(rs, "status");
            final EnumOptionData statusEnum = SharesEnumerations.ShareAccountDividendStatusEnum(status);
            final Long savingsTransactionId = JdbcSupport.getLong(rs, "savingsTransactionId");

            final Long accounId = rs.getLong("accountId");
            final String accountNumber = rs.getString("accountNumber");
            final String clientName = rs.getString("clientName");
            final Long clientId = rs.getLong("clientId");
            final ShareAccountData accountData = ShareAccountData.lookup(accounId, accountNumber, clientId, clientName);
            return new ShareAccountDividendData(id, accountData, amount, statusEnum, savingsTransactionId);
        }

    }

}
