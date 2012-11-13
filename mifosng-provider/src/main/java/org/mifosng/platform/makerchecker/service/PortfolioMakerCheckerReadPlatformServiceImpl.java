package org.mifosng.platform.makerchecker.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.data.MakerCheckerData;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PortfolioMakerCheckerReadPlatformServiceImpl implements PortfolioMakerCheckerReadPlatformService {
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public PortfolioMakerCheckerReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private static final class MakerCheckerMapper implements RowMapper<MakerCheckerData> {
		
		public String schema() {
			return " mc.id as id, mc.task_name as taskName, mc.task_json as taskJson, mc.made_on_date as madeOnDate from m_maker_checker mc ";
		}

		@Override
		public MakerCheckerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String taskName = rs.getString("taskName");
			final String taskJson = rs.getString("taskJson");
			
			final LocalDate madeOnDate = JdbcSupport.getLocalDate(rs, "madeOnDate");
			
			return new MakerCheckerData(id, taskName, taskJson, madeOnDate);
		}
	}

	@Override
	public Collection<MakerCheckerData> retrieveAllEntriesToBeChecked() {
		context.authenticatedUser();

		final MakerCheckerMapper rm = new MakerCheckerMapper();
		final String sql = "select " + rm.schema() + " order by mc.made_on_date DESC, mc.task_name ASC";

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	@Override
	public MakerCheckerData retrieveById(final Long id) {
		
		final MakerCheckerMapper rm = new MakerCheckerMapper();
		final String sql = "select " + rm.schema() + " where mc.id=?";

		return this.jdbcTemplate.queryForObject(sql, rm, new Object[] {id});
	}
}