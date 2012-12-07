package org.mifosplatform.audit.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditReadPlatformServiceImpl implements AuditReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public AuditReadPlatformServiceImpl(final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class AuditMapper implements RowMapper<AuditData> {

		public String schema() {
			return " mc.id as id, mc.api_operation as apiOperation, mc.api_resource as resource, mc.resource_id as resourceId,"
					+ " mk.username as maker, mc.made_on_date as madeOnDate, ck.username as checker, mc.checked_on_date as checkedOnDate, mc.command_as_json as commandAsJson "
					+ " from m_portfolio_command_source mc "
					+ " left join m_appuser mk on mk.id = mc.maker_id"
					+ " left join m_appuser ck on ck.id = mc.checker_id";
		}

		@Override
		public AuditData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String apiOperation = rs.getString("apiOperation");
			final String resource = rs.getString("resource");
			final Long resourceId = JdbcSupport.getLong(rs, "resourceId");
			final String maker = rs.getString("maker");
			final LocalDate madeOnDate = JdbcSupport.getLocalDate(rs,
					"madeOnDate");
			final String checker = rs.getString("checker");
			final LocalDate checkedOnDate = JdbcSupport.getLocalDate(rs,
					"checkedOnDate");
			final String taskJson = rs.getString("taskJson");
			return new AuditData(id, apiOperation, resource, resourceId, maker,
					madeOnDate, checker, checkedOnDate, taskJson);
		}
	}

	@Override
	public Collection<AuditData> retrieveAuditEntries() {
		context.authenticatedUser();

		final AuditMapper rm = new AuditMapper();
		final String sql = "select "
				+ rm.schema()
				+ " where mc.checker_id is null order by mc.made_on_date DESC, mc.api_resource ASC, mc.api_operation ASC";

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

}