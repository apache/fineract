/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.useradministration.data.PasswordValidationPolicyData;
import org.mifosplatform.useradministration.exception.PasswordValidationPolicyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PasswordValidationPolicyReadPlatformServiceImpl implements PasswordValidationPolicyReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordValidationPolicyMapper passwordValidationPolicyMapper;

    @Autowired
    public PasswordValidationPolicyReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.passwordValidationPolicyMapper = new PasswordValidationPolicyMapper();
    }

    @Override
    public Collection<PasswordValidationPolicyData> retrieveAll() {
        final String sql = "select " + this.passwordValidationPolicyMapper.schema() + " order by pvp.id";

        return this.jdbcTemplate.query(sql, this.passwordValidationPolicyMapper);
    }

    @Override
    public PasswordValidationPolicyData retrieveActiveValidationPolicy() {
        try {
            final String sql = "select " + this.passwordValidationPolicyMapper.schema() + " where pvp.active = true";
            return this.jdbcTemplate.queryForObject(sql, this.passwordValidationPolicyMapper);
        } catch (final EmptyResultDataAccessException e) {
            throw new PasswordValidationPolicyNotFoundException();
        }
    }

    protected static final class PasswordValidationPolicyMapper implements RowMapper<PasswordValidationPolicyData> {

        @Override
        public PasswordValidationPolicyData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final Boolean active = rs.getBoolean("active");
            final String description = rs.getString("description");

            return new PasswordValidationPolicyData(id, active, description);
        }

        public String schema() {
            return " pvp.id as id, pvp.active as active, pvp.description as description from m_password_validation_policy pvp";
        }
    }

}