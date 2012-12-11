package org.mifosplatform.audit.api;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.data.AppUserLookup;
import org.mifosplatform.useradministration.service.AppUserReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditReadPlatformServiceImpl implements AuditReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final AppUserReadPlatformService appUserReadPlatformService;
	@Autowired
	public AuditReadPlatformServiceImpl(final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource, final AppUserReadPlatformService appUserReadPlatformService) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.appUserReadPlatformService = appUserReadPlatformService;
	}

	private static final class AuditMapper implements RowMapper<AuditData> {

		public String schema(boolean includeJson) {

			String commandAsJsonString = "";
			if (includeJson)
				commandAsJsonString = ", aud.command_as_json as commandAsJson ";

			return " aud.id as id, aud.api_operation as apiOperation, aud.api_resource as resource, aud.resource_id as resourceId,"
					+ " mk.username as maker, aud.made_on_date as madeOnDate, ck.username as checker, aud.checked_on_date as checkedOnDate"
					+ commandAsJsonString
					+ " from m_portfolio_command_source aud "
					+ " left join m_appuser mk on mk.id = aud.maker_id"
					+ " left join m_appuser ck on ck.id = aud.checker_id";
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
			String commandAsJson;
			// commandAsJson might not be on the select list of columns
			try {
				commandAsJson = rs.getString("commandAsJson");
			} catch (SQLException e) {
				commandAsJson = null;
			}
			return new AuditData(id, apiOperation, resource, resourceId, maker,
					madeOnDate, checker, checkedOnDate, commandAsJson);
		}
	}



	@Override
	public Collection<AuditData> retrieveAuditEntries(String extraCriteria,
			boolean includeJson) {
		context.authenticatedUser();

		final AuditMapper rm = new AuditMapper();

		String sql = "select " + rm.schema(includeJson);
		if (StringUtils.isNotBlank(extraCriteria))
			sql += " where (" + extraCriteria + ")";
		sql += " order by aud.id DESC";

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	@Override
	public AuditData retrieveAuditEntry(Long auditId) {
		context.authenticatedUser();

		final AuditMapper rm = new AuditMapper();

		String sql = "select " + rm.schema(true);
		sql += " where aud.id = " + auditId;

		return this.jdbcTemplate.queryForObject(sql, rm, new Object[] {});
	}

	@Override
	public AuditSearchData retrieveSearchTemplate() {
		Collection<AppUserLookup> appUsers = appUserReadPlatformService.retrieveSearchTemplate();
		List<String> apiOperations = null;
		List<String> resources = null;
		return new AuditSearchData(appUsers, apiOperations, resources);
	}

}