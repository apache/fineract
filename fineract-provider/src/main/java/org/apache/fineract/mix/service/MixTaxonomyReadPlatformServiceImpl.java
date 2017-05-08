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
import java.util.List;

import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.mix.data.MixTaxonomyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class MixTaxonomyReadPlatformServiceImpl implements MixTaxonomyReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final MixTaxonomyMapper mixTaxonomyMapper;

    @Autowired
    public MixTaxonomyReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.mixTaxonomyMapper = new MixTaxonomyMapper();
    }

    private static final class MixTaxonomyMapper implements RowMapper<MixTaxonomyData> {

        public String schema() {
            return "tx.id as id, name, dimension, type, description, prefix "
                    + "from mix_taxonomy tx left join mix_xbrl_namespace xn on tx.namespace_id=xn.id";
        }

        @Override
        public MixTaxonomyData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String namespace = rs.getString("prefix");

            final String dimension = rs.getString("dimension");
            final Integer type = rs.getInt("type");
            final String desc = rs.getString("description");
            return new MixTaxonomyData(id, name, namespace, dimension, type, desc);
        }

    }

    @Override
    public List<MixTaxonomyData> retrieveAll() {
        final String sql = "select " + this.mixTaxonomyMapper.schema();
        return this.jdbcTemplate.query(sql, this.mixTaxonomyMapper);
    }

    @Override
    public MixTaxonomyData retrieveOne(final Long id) {
        final String sql = "select " + this.mixTaxonomyMapper.schema() + " where tx.id = ? ";
        return this.jdbcTemplate.queryForObject(sql, this.mixTaxonomyMapper, new Object[] { id });
    }
}