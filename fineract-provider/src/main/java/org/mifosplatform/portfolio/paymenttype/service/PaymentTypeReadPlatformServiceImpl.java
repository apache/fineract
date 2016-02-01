/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.paymenttype.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
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
