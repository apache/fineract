package org.mifosng.platform.user.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PermissionReadPlatformServiceImpl implements PermissionReadPlatformService {

	private final SimpleJdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public PermissionReadPlatformServiceImpl(final PlatformSecurityContext context, final DataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public Collection<PermissionData> retrieveAllPermissions() {

		AppUser currentUser = context.authenticatedUser();

		PermissionMapper mapper = new PermissionMapper();
		String sql = "select " + mapper.schema() + " where p.org_id = ? order by p.id";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { currentUser
				.getOrganisation().getId() });
	}

	private static final class PermissionMapper implements RowMapper<PermissionData> {

		@Override
		public PermissionData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			Long id = JdbcSupport.getLong(rs, "id");
			Long orgId = JdbcSupport.getLong(rs, "orgId");
			String name = rs.getString("name");
			String description = rs.getString("description");
			String code = rs.getString("code");
			Integer groupType = JdbcSupport.getInteger(rs, "groupType");

			return new PermissionData(id, orgId, name, description, code, groupType);
		}

		public String schema() {
			return " p.id as id, p.org_id as orgId, p.default_name as name, p.default_description as description, p.code as code, p.group_enum as groupType from admin_permission p ";
		}
	}
}