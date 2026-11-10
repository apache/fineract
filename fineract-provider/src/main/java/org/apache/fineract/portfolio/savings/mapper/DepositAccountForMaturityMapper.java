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
package org.apache.fineract.portfolio.savings.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.data.DepositAccountData;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.springframework.jdbc.core.RowMapper;

public final class DepositAccountForMaturityMapper implements RowMapper<DepositAccountData> {

    public static final String ID = "id";
    public static final String ACCOUNT_NUMBER = "accountNumber";
    public static final String DEPOSIT_TYPE_ID = "depositTypeId";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String schema() {
        LocalDate today = DateUtils.getBusinessLocalDate();
        String formattedToday = DATE_TIME_FORMATTER.format(today);
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("da.id as id, ");
        sqlBuilder.append("da.account_no as accountNumber, ");
        sqlBuilder.append("da.deposit_type_enum as depositTypeId ");
        sqlBuilder.append("FROM m_savings_account da ");
        sqlBuilder.append("inner join m_deposit_account_term_and_preclosure dat on dat.savings_account_id = da.id ");
        sqlBuilder.append("and dat.maturity_date is not null and dat.maturity_date <= '" + formattedToday + "' ");

        return sqlBuilder.toString();
    }

    @Override
    public DepositAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

        final Long id = rs.getLong(ID);
        final String name = rs.getString(ACCOUNT_NUMBER);
        final Integer depositTypeId = JdbcSupport.getInteger(rs, DEPOSIT_TYPE_ID);
        final EnumOptionData depositType = (depositTypeId == null) ? null : SavingsEnumerations.depositType(depositTypeId);

        return DepositAccountData.lookup(id, name, depositType);
    }
}
