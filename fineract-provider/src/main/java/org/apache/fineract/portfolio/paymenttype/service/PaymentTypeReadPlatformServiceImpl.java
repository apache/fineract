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
package org.apache.fineract.portfolio.paymenttype.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PaymentTypeReadPlatformServiceImpl implements PaymentTypeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public PaymentTypeReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<PaymentTypeData> retrieveAllPaymentTypes() {
        // TODO Auto-generated method stub
        this.context.authenticatedUser();

        final PaymentTypeMapper ptm = new PaymentTypeMapper();
        final String sql = "select " + ptm.schema() + "order by position";

        return this.jdbcTemplate.query(sql, ptm, new Object[] {});
    }

    @Override
    public PaymentTypeData retrieveOne(Long paymentTypeId) {
        // TODO Auto-generated method stub
        this.context.authenticatedUser();

        final PaymentTypeMapper ptm = new PaymentTypeMapper();
        final String sql = "select " + ptm.schema() + "where pt.id = ?";

        return this.jdbcTemplate.queryForObject(sql, ptm, new Object[] { paymentTypeId });
    }

    private static final class PaymentTypeMapper implements RowMapper<PaymentTypeData> {

        public String schema() {
            return " pt.id as id, pt.value as name, pt.description as description,pt.is_cash_payment as isCashPayment,pt.order_position as position from m_payment_type pt ";
        }

        @Override
        public PaymentTypeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final boolean isCashPayment = rs.getBoolean("isCashPayment");
            final Long position = rs.getLong("position");

            return PaymentTypeData.instance(id, name, description, isCashPayment, position);
        }

    }

}
