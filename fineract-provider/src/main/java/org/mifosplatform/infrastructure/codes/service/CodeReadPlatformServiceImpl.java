/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.codes.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CodeReadPlatformServiceImpl implements CodeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public CodeReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class CodeMapper implements RowMapper<CodeData> {

        public String schema() {
            return " c.id as id, c.code_name as code_name, c.is_system_defined as systemDefined from m_code c ";
        }

        @Override
        public CodeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String code_name = rs.getString("code_name");
            final boolean systemDefined = rs.getBoolean("systemDefined");

            return CodeData.instance(id, code_name, systemDefined);
        }
    }

    @Override
    @Cacheable(value = "codes", key = "T(org.mifosplatform.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('CD')")
    public Collection<CodeData> retrieveAllCodes() {
        this.context.authenticatedUser();

        final CodeMapper rm = new CodeMapper();
        final String sql = "select " + rm.schema() + " order by c.code_name";

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public CodeData retrieveCode(final Long codeId) {
        try {
            this.context.authenticatedUser();

            final CodeMapper rm = new CodeMapper();
            final String sql = "select " + rm.schema() + " where c.id = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { codeId });
        } catch (final EmptyResultDataAccessException e) {
            throw new CodeNotFoundException(codeId);
        }
    }

    @Override
    public CodeData retriveCode(final String codeName) {
        try {
            this.context.authenticatedUser();

            final CodeMapper rm = new CodeMapper();
            final String sql = "select " + rm.schema() + " where c.code_name = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { codeName });
        } catch (final EmptyResultDataAccessException e) {
            throw new CodeNotFoundException(codeName);
        }
    }
}