package org.mifosplatform.useradministration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.useradministration.data.AppUserData;
import org.mifosplatform.useradministration.data.AppUserLookup;
import org.mifosplatform.useradministration.data.RoleData;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.useradministration.domain.AppUserRepository;
import org.mifosplatform.useradministration.domain.Role;
import org.mifosplatform.useradministration.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AppUserReadPlatformServiceImpl implements
		AppUserReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final OfficeReadPlatformService officeReadPlatformService;
	private final RoleReadPlatformService roleReadPlatformService;
	private final AppUserRepository appUserRepository;

	@Autowired
	public AppUserReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource,
			final OfficeReadPlatformService officeReadPlatformService,
			final RoleReadPlatformService roleReadPlatformService,
			final AppUserRepository appUserRepository) {
		this.context = context;
		this.officeReadPlatformService = officeReadPlatformService;
		this.roleReadPlatformService = roleReadPlatformService;
		this.appUserRepository = appUserRepository;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<AppUserData> retrieveAllUsers() {

		AppUser currentUser = context.authenticatedUser();
		String hierarchy = currentUser.getOffice().getHierarchy();
		String hierarchySearchString = hierarchy + "%";

		AppUserMapper mapper = new AppUserMapper();
		String sql = "select " + mapper.schema() ;

		return this.jdbcTemplate.query(sql, mapper,
				new Object[] { hierarchySearchString });
	}

	@Override
	public Collection<AppUserLookup> retrieveSearchTemplate() {
		AppUser currentUser = context.authenticatedUser();
		String hierarchy = currentUser.getOffice().getHierarchy();
		String hierarchySearchString = hierarchy + "%";

		AppUserLookupMapper mapper = new AppUserLookupMapper();
		String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper,
				new Object[] { hierarchySearchString });
	}

	@Override
	public AppUserData retrieveNewUserDetails() {

		List<OfficeLookup> offices = new ArrayList<OfficeLookup>(
				officeReadPlatformService.retrieveAllOfficesForLookup());

		List<RoleData> availableRoles = new ArrayList<RoleData>(
				this.roleReadPlatformService.retrieveAllRoles());

		AppUserData userData = new AppUserData(offices, availableRoles);

		return userData;
	}

	@Override
	public AppUserData retrieveUser(final Long userId) {

		context.authenticatedUser();

		AppUser user = this.appUserRepository.findOne(userId);
		if (user == null || user.isDeleted()) {
			throw new UserNotFoundException(userId);
		}

		List<RoleData> availableRoles = new ArrayList<RoleData>(
				this.roleReadPlatformService.retrieveAllRoles());

		List<RoleData> selectedUserRoles = new ArrayList<RoleData>();
		Set<Role> userRoles = user.getRoles();
		for (Role role : userRoles) {
			selectedUserRoles.add(role.toData());
		}

		availableRoles.removeAll(selectedUserRoles);

		AppUserData userData = new AppUserData(user.getId(),
				user.getUsername(), user.getEmail(), user.getOffice().getId(),
				user.getOffice().getName(), user.getFirstname(),
				user.getLastname(), availableRoles, selectedUserRoles);

		return userData;
	}

	private static final class AppUserMapper implements RowMapper<AppUserData> {

		@Override
		public AppUserData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String username = rs.getString("username");
			String firstname = rs.getString("firstname");
			String lastname = rs.getString("lastname");
			String email = rs.getString("email");
			Long officeId = JdbcSupport.getLong(rs, "officeId");

			String officeName = rs.getString("officeName");

			AppUserData user = new AppUserData(id, username, email, officeId,
					officeName, firstname, lastname, new ArrayList<RoleData>(),
					new ArrayList<RoleData>());

			return user;
		}

		public String schema() {
			return " u.id as id, u.username as username, u.firstname as firstname, u.lastname as lastname, u.email as email,"
					+ " u.office_id as officeId, o.name as officeName from m_appuser u "
					+ " join m_office o on o.id = u.office_id where o.hierarchy like ? and u.is_deleted=0 order by u.username";
		}

	}

	private static final class AppUserLookupMapper implements
			RowMapper<AppUserLookup> {

		@Override
		public AppUserLookup mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String username = rs.getString("username");

			AppUserLookup user = new AppUserLookup(id, username);

			return user;
		}

		public String schema() {
			return " u.id as id, u.username as username from m_appuser u "
					+ " join m_office o on o.id = u.office_id where o.hierarchy like ? and u.is_deleted=0 order by u.username";
		}

	}

}