package org.mifosng.platform.user.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import org.mifosng.data.RoleData;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.Role;
import org.mifosng.platform.user.domain.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class RoleReadPlatformServiceImpl implements RoleReadPlatformService {

	private final SimpleJdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final RoleRepository roleRepository;

	@Autowired
	public RoleReadPlatformServiceImpl(final PlatformSecurityContext context, final DataSource dataSource, final RoleRepository roleRepository) {
		this.context = context;
		this.roleRepository = roleRepository;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public Collection<RoleData> retrieveAllRoles() {
		AppUser currentUser = context.authenticatedUser();

		RoleMapper mapper = new RoleMapper();
		String sql = "select " + mapper.schema() + " where r.org_id = ?";

		return this.jdbcTemplate.query(sql, mapper, new Object[] {currentUser.getOrganisation().getId() });
	}

	// FIXME - does it make sense to use JDBC over repository for this?
	@Override
	public RoleData retrieveRole(Long roleId) {

		Role role = this.roleRepository.findOne(roleId);

		return role.toData();
	}

	protected static final class RoleMapper implements RowMapper<RoleData> {

		@Override
		public RoleData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = JdbcSupport.getLong(rs, "id");
			Long orgId = JdbcSupport.getLong(rs, "orgId");
			String name = rs.getString("name");
			String description = rs.getString("description");

			return new RoleData(id, orgId, name, description);
		}

		public String schema() {
			return " r.id as id, r.org_id as orgId, r.name as name, r.description as description from admin_role r order by r.id";
		}
	}
}