/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.mix.data.MixTaxonomyData;
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
        final String sql = "select " + this.mixTaxonomyMapper.schema() + " where tx.id =" + id;
        return this.jdbcTemplate.queryForObject(sql, this.mixTaxonomyMapper);
    }
}