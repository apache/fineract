package org.mifosng.platform.user.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.api.data.RoleData;
import org.mifosng.platform.exceptions.RoleNotFoundException;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.Role;
import org.mifosng.platform.user.domain.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class RoleReadPlatformServiceImpl implements RoleReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final RoleRepository roleRepository;

	@Autowired
	public RoleReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource, final RoleRepository roleRepository) {
		this.context = context;
		this.roleRepository = roleRepository;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<RoleData> retrieveAllRoles() {
		context.authenticatedUser();

		RoleMapper mapper = new RoleMapper();
		String sql = "select " + mapper.schema() + " order by r.id";

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	@Override
	public RoleData retrieveRole(final Long id) {
		context.authenticatedUser();
		
		Role role = this.roleRepository.findOne(id);
		if (role == null) {
			throw new RoleNotFoundException(id);
		}
		return role.toData();
	}

	protected static final class RoleMapper implements RowMapper<RoleData> {

		@Override
		public RoleData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = JdbcSupport.getLong(rs, "id");
			String name = rs.getString("name");
			String description = rs.getString("description");

			Collection<PermissionData> selectedPermissions = new ArrayList<PermissionData>(0);
			return new RoleData(id, name, description, selectedPermissions);
		}

		public String schema() {
			return " r.id as id, r.name as name, r.description as description from m_role r";
		}
	}
}