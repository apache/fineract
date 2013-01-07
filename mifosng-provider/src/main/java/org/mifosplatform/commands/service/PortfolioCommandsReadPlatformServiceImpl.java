package org.mifosplatform.commands.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.data.CommandSourceData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PortfolioCommandsReadPlatformServiceImpl implements PortfolioCommandsReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public PortfolioCommandsReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class CommandSourceMapper implements RowMapper<CommandSourceData> {

        public String schema() {
            return " mc.id as id, mc.action_name as actionName, mc.entity_name as entityName, mc.resource_id as entityId, mc.api_get_url as getHref, "
                    + "mc.command_as_json as taskJson, mc.made_on_date as madeOnDate from m_portfolio_command_source mc ";
        }

        @Override
        public CommandSourceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String actionName = rs.getString("actionName");
            final String entityName = rs.getString("entityName");
            final Long entityId = JdbcSupport.getLong(rs, "entityId");
            final String taskJson = rs.getString("taskJson");
            final String getHref = rs.getString("getHref");
            final LocalDate madeOnDate = JdbcSupport.getLocalDate(rs, "madeOnDate");

            return new CommandSourceData(id, actionName, entityName, entityId, getHref, taskJson, madeOnDate);
        }
    }

    @Override
    public Collection<CommandSourceData> retrieveAllEntriesToBeChecked() {
        context.authenticatedUser();

        final CommandSourceMapper rm = new CommandSourceMapper();
        final String sql = "select " + rm.schema()
                + " where mc.checker_id is null and mc.processing_result_enum = 2 order by mc.made_on_date DESC, mc.entity_name ASC, mc.action_name ASC";

        return this.jdbcTemplate.query(sql, rm, new Object[] {});
    }

    @Override
    public CommandSourceData retrieveById(final Long id) {

        final CommandSourceMapper rm = new CommandSourceMapper();
        final String sql = "select " + rm.schema() + " where mc.id=?";

        return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { id });
    }

    @Override
    public Collection<CommandSourceData> retrieveUnprocessChangesByEntityNameAndId(final String entityName, final Long entityId) {

        final CommandSourceMapper rm = new CommandSourceMapper();
        final String sql = "select "
                + rm.schema()
                + " where mc.entity_name like ? and mc.resource_id = ? and mc.checker_id is null and mc.processing_result_enum = 2 order by mc.made_on_date DESC, mc.action_name ASC";

        return this.jdbcTemplate.query(sql, rm, new Object[] { entityName, entityId });
    }
}