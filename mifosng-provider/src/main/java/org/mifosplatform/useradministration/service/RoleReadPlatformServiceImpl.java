package org.mifosplatform.useradministration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.data.RoleData;
import org.mifosplatform.useradministration.domain.Role;
import org.mifosplatform.useradministration.domain.RoleRepository;
import org.mifosplatform.useradministration.exception.RoleNotFoundException;
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
    public RoleReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource,
            final RoleRepository roleRepository) {
        this.context = context;
        this.roleRepository = roleRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<RoleData> retrieveAllRoles() {
        context.authenticatedUser();

        final RoleMapper mapper = new RoleMapper();
        final String sql = "select " + mapper.schema() + " order by r.id";

        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    @Override
    public RoleData retrieveRole(final Long id) {
        context.authenticatedUser();

        final Role role = this.roleRepository.findOne(id);
        if (role == null) { throw new RoleNotFoundException(id); }
        
        return role.toData();
    }

    protected static final class RoleMapper implements RowMapper<RoleData> {

        @Override
        public RoleData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");

            return new RoleData(id, name, description, null);
        }

        public String schema() {
            return " r.id as id, r.name as name, r.description as description from m_role r";
        }
    }
}