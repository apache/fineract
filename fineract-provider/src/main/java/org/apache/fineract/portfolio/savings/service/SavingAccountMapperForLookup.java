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
package org.apache.fineract.portfolio.savings.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.springframework.jdbc.core.RowMapper;

final class SavingAccountMapperForLookup implements RowMapper<SavingsAccountData> {

    private final String schemaSql;

    SavingAccountMapperForLookup() {
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("sa.id as id, sa.account_no as accountNo, ");
        sqlBuilder.append("sa.deposit_type_enum as depositType, ");
        sqlBuilder.append("sp.id as productId, sp.name as productName, ");
        sqlBuilder.append("sa.status_enum as statusEnum ");

        sqlBuilder.append("from m_savings_account sa ");
        sqlBuilder.append("join m_savings_product sp ON sa.product_id = sp.id ");

        this.schemaSql = sqlBuilder.toString();
    }

    String schema() {
        return this.schemaSql;
    }

    @Override
    public SavingsAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

        final Long id = rs.getLong("id");
        final String accountNo = rs.getString("accountNo");
        final Integer depositTypeId = rs.getInt("depositType");
        final EnumOptionData depositType = SavingsEnumerations.depositType(depositTypeId);

        final Long productId = rs.getLong("productId");
        final String productName = rs.getString("productName");

        final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
        final SavingsAccountStatusEnumData status = SavingsEnumerations.status(statusEnum);

        return SavingsAccountData.lookupWithProductDetails(id, accountNo, depositType, productId, productName, status);
    }
}
