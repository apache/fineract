/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.mix.data.MixTaxonomyMappingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class MixTaxonomyMappingReadPlatformServiceImpl implements MixTaxonomyMappingReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MixTaxonomyMappingReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class TaxonomyMappingMapper implements RowMapper<MixTaxonomyMappingData> {

        public String schema() {
            return "identifier, config " + "from mix_taxonomy_mapping";
        }

        @Override
        public MixTaxonomyMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final String identifier = rs.getString("identifier");
            final String config = rs.getString("config");
            return new MixTaxonomyMappingData(identifier, config);
        }

    }

    @Override
    public MixTaxonomyMappingData retrieveTaxonomyMapping() {
        try {
            final TaxonomyMappingMapper rm = new TaxonomyMappingMapper();
            final String sqlString = "select " + rm.schema();
            return this.jdbcTemplate.queryForObject(sqlString, rm);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }

    }
}