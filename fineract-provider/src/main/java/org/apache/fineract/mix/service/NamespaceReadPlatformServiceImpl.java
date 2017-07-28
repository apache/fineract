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
package org.apache.fineract.mix.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.mix.data.NamespaceData;
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
        final String sql = this.namespaceMapper.schema() + " where id= ? ";

        return this.jdbcTemplate.queryForObject(sql, this.namespaceMapper, new Object[] { id });
    }

    @Override
    public NamespaceData retrieveNamespaceByPrefix(final String prefix) {
        final String sql = this.namespaceMapper.schema() + " where prefix = ? ";

        return this.jdbcTemplate.queryForObject(sql, this.namespaceMapper, new Object[] { prefix });
    }
}