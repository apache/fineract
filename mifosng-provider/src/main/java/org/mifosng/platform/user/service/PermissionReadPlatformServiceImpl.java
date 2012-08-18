package org.mifosng.platform.user.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PermissionReadPlatformServiceImpl implements PermissionReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public PermissionReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<PermissionData> retrieveAllPermissions() {

		context.authenticatedUser();

		PermissionMapper mapper = new PermissionMapper();
		String sql = "select " + mapper.schema() + " order by p.id";

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class PermissionMapper implements RowMapper<PermissionData> {

		@Override
		public PermissionData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			Long id = JdbcSupport.getLong(rs, "id");
			String name = rs.getString("name");
			String description = rs.getString("description");
			String code = rs.getString("code");
//			Integer groupType = JdbcSupport.getInteger(rs, "groupType");

			return new PermissionData(id, name, description, code);
		}

		public String schema() {
			return " p.id as id, p.default_name as name, p.default_description as description, p.code as code, p.group_enum as groupType from m_permission p ";
		}
	}
}