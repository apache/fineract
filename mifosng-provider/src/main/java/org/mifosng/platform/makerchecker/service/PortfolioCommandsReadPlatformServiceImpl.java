package org.mifosng.platform.makerchecker.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.CommandSourceData;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PortfolioCommandsReadPlatformServiceImpl implements PortfolioCommandsReadPlatformService {
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public PortfolioCommandsReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private static final class CommandSourceMapper implements RowMapper<CommandSourceData> {
		
		public String schema() {
			return " mc.id as id, mc.api_operation as taskOperation, mc.api_resource as taskEntity, mc.resource_id as entityId," +
					"mc.command_as_json as taskJson, mc.made_on_date as madeOnDate from m_portfolio_command_source mc ";
		}

		@Override
		public CommandSourceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String taskOperation = rs.getString("taskOperation");
			final String taskEntity = rs.getString("taskEntity");
			final Long entityId = JdbcSupport.getLong(rs, "entityId");
			final String taskJson = rs.getString("taskJson");
			
			final LocalDate madeOnDate = JdbcSupport.getLocalDate(rs, "madeOnDate");
			
			return new CommandSourceData(id, taskOperation, taskEntity, entityId, taskJson, madeOnDate);
		}
	}

	@Override
	public Collection<CommandSourceData> retrieveAllEntriesToBeChecked() {
		context.authenticatedUser();

		final CommandSourceMapper rm = new CommandSourceMapper();
		final String sql = "select " + rm.schema() + " where mc.checker_id is null order by mc.made_on_date DESC, mc.api_resource ASC, mc.api_operation ASC";

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	@Override
	public CommandSourceData retrieveById(final Long id) {
		
		final CommandSourceMapper rm = new CommandSourceMapper();
		final String sql = "select " + rm.schema() + " where mc.id=?";

		return this.jdbcTemplate.queryForObject(sql, rm, new Object[] {id});
	}
}