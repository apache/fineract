package org.mifosplatform.xbrl.report.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.xbrl.report.data.NamespaceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ReadNamespaceServiceImpl implements ReadNamespaceService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReadNamespaceServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class NamespaceMapper implements RowMapper<NamespaceData> {

        public String schema() {
            return "select id, prefix, url " + "from mix_xbrl_namespace";
        }

        @Override
        public NamespaceData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            final long id = rs.getLong("id");
            final String prefix = rs.getString("prefix");
            final String url = rs.getString("url");
            return new NamespaceData(id, prefix, url);
        }

    }

    @Override
    public NamespaceData retrieveNamespaceById(Long id) {
        final NamespaceMapper mapper = new NamespaceMapper();
        String sql = mapper.schema() + " where id=" + id;

        return this.jdbcTemplate.queryForObject(sql, mapper);
    }

    @Override
    public NamespaceData retrieveNamespaceByPrefix(String prefix) {
        final NamespaceMapper mapper = new NamespaceMapper();
        String sql = mapper.schema() + " where prefix='" + prefix + "'";

        return this.jdbcTemplate.queryForObject(sql, mapper);
    }

}
