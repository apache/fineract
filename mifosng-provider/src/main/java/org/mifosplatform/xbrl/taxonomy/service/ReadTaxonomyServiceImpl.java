package org.mifosplatform.xbrl.taxonomy.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.xbrl.taxonomy.data.TaxonomyData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ReadTaxonomyServiceImpl implements ReadTaxonomyService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReadTaxonomyServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    static final class TaxonomyMapper implements RowMapper<TaxonomyData> {

        public String schema() {
            return "tx.id as id, name, dimension, type, description, prefix "
                    + "from mix_taxonomy tx left join mix_xbrl_namespace xn on tx.namespace_id=xn.id";
        }

        @Override
        public TaxonomyData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String namespace = rs.getString("prefix");

            final String dimension = rs.getString("dimension");
            final Integer type = rs.getInt("type");
            final String desc = rs.getString("description");
            return new TaxonomyData(id, name, namespace, dimension, type, desc);
        }

    }

    @Override
    public List<TaxonomyData> retrieveAllTaxonomy() {
        final TaxonomyMapper rm = new TaxonomyMapper();
        String sql = "select " + rm.schema();
        return this.jdbcTemplate.query(sql, rm);
    }

    @Override
    public TaxonomyData retrieveTaxonomyById(Long id) {
        final TaxonomyMapper rm = new TaxonomyMapper();
        String sql = "select " + rm.schema() + " where tx.id =" + id;
        return this.jdbcTemplate.queryForObject(sql, rm);
    }

}
