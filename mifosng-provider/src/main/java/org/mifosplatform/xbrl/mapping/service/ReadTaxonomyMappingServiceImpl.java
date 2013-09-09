package org.mifosplatform.xbrl.mapping.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.xbrl.mapping.data.TaxonomyMappingData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ReadTaxonomyMappingServiceImpl implements ReadTaxonomyMappingService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReadTaxonomyMappingServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class TaxonomyMappingMapper implements RowMapper<TaxonomyMappingData> {

        public String schema() {
            return "identifier, config " + "from mix_taxonomy_mapping";
        }

        @Override
        public TaxonomyMappingData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final String identifier = rs.getString("identifier");
            final String config = rs.getString("config");
            return new TaxonomyMappingData(identifier, config);
        }

    }

    @Override
    public TaxonomyMappingData retrieveTaxonomyMapping() {
        try {
            final TaxonomyMappingMapper rm = new TaxonomyMappingMapper();
            String sqlString = "select " + rm.schema();
            return this.jdbcTemplate.queryForObject(sqlString, rm);
        } catch (final EmptyResultDataAccessException e) {
            return null;
        }

    }
}
