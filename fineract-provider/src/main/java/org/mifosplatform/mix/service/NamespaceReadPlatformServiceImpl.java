/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.mix.data.NamespaceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class NamespaceReadPlatformServiceImpl implements NamespaceReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final NamespaceMapper namespaceMapper;

    @Autowired
    public NamespaceReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namespaceMapper = new NamespaceMapper();
    }

    private static final class NamespaceMapper implements RowMapper<NamespaceData> {

        public String schema() {
            return "select id, prefix, url " + "from mix_xbrl_namespace";
        }

        @Override
        public NamespaceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final long id = rs.getLong("id");
            final String prefix = rs.getString("prefix");
            final String url = rs.getString("url");
            return new NamespaceData(id, prefix, url);
        }

    }

    @Override
    public NamespaceData retrieveNamespaceById(final Long id) {
        final String sql = this.namespaceMapper.schema() + " where id=" + id;

        return this.jdbcTemplate.queryForObject(sql, this.namespaceMapper);
    }

    @Override
    public NamespaceData retrieveNamespaceByPrefix(final String prefix) {
        final String sql = this.namespaceMapper.schema() + " where prefix='" + prefix + "'";

        return this.jdbcTemplate.queryForObject(sql, this.namespaceMapper);
    }
}