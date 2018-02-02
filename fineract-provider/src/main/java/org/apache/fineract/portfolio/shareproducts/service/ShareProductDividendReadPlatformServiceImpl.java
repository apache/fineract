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
package org.apache.fineract.portfolio.shareproducts.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.shareaccounts.data.ShareAccountDividendData;
import org.apache.fineract.portfolio.shareaccounts.service.SharesEnumerations;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductDividendPayOutData;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ShareProductDividendReadPlatformServiceImpl implements ShareProductDividendReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ColumnValidator columnValidator;
    private final PaginationHelper<ShareProductDividendPayOutData> paginationHelper = new PaginationHelper<>();

    @Autowired
    public ShareProductDividendReadPlatformServiceImpl(final RoutingDataSource dataSource,
    		final ColumnValidator columnValidator) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.columnValidator = columnValidator;
    }

    @Override
    public Page<ShareProductDividendPayOutData> retriveAll(final Long productId, final Integer status,
            final SearchParameters searchParameters) {
        ShareProductDividendMapper shareProductDividendMapper = new ShareProductDividendMapper();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(shareProductDividendMapper.schema());
        sqlBuilder.append(" where sp.id = ? ");
        List<Object> params = new ArrayList<>(2);
        params.add(productId);
        if (status != null) {
            sqlBuilder.append(" and pod.status = ?");
            params.add(status);
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
                shareProductDividendMapper);
    }

    private static final class ShareProductDividendMapper implements RowMapper<ShareProductDividendPayOutData> {

        private final String sql;

        public ShareProductDividendMapper() {
            StringBuilder sb = new StringBuilder();
            sb.append(" pod.id as id, pod.amount as amount,");
            sb.append(" pod.status as status, pod.dividend_period_start_date as startDate,");
            sb.append(" pod.dividend_period_end_date as endDate,");
            sb.append(" sp.id as productId,sp.name as productName ");
            sb.append(" from m_share_product_dividend_pay_out pod");
            sb.append(" inner join m_share_product sp on sp.id = pod.product_id ");
            sql = sb.toString();
        }

        public String schema() {
            return this.sql;
        }

        @Override
        public ShareProductDividendPayOutData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final BigDecimal amount = rs.getBigDecimal("amount");
            final Integer status = JdbcSupport.getInteger(rs, "status");
            final EnumOptionData statusEnum = SharesEnumerations.ShareProductDividendStatusEnum(status);
            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
            final LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");

            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");

            final ShareProductData productData = ShareProductData.lookup(productId, productName);
            final Collection<ShareAccountDividendData> accountDividendsData = null;
            return new ShareProductDividendPayOutData(id, productData, amount, startDate, endDate, accountDividendsData, statusEnum);
        }

    }

}
