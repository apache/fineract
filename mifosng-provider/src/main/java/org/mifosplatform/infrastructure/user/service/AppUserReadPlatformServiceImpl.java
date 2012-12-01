package org.mifosplatform.infrastructure.user.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.office.data.OfficeData;
import org.mifosplatform.infrastructure.office.data.OfficeLookup;
import org.mifosplatform.infrastructure.office.service.OfficeReadPlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.api.data.AppUserData;
import org.mifosplatform.infrastructure.user.api.data.RoleData;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.mifosplatform.infrastructure.user.domain.AppUserRepository;
import org.mifosplatform.infrastructure.user.domain.Role;
import org.mifosplatform.infrastructure.user.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AppUserReadPlatformServiceImpl implements AppUserReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final OfficeReadPlatformService officeReadPlatformService;
	private final RoleReadPlatformService roleReadPlatformService;
	private final AppUserRepository appUserRepository;

	@Autowired
	public AppUserReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource, 
			final OfficeReadPlatformService officeReadPlatformService, final RoleReadPlatformService roleReadPlatformService,
			final AppUserRepository appUserRepository) {
		this.context = context;
		this.officeReadPlatformService = officeReadPlatformService;
		this.roleReadPlatformService = roleReadPlatformService;
		this.appUserRepository = appUserRepository;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<AppUserData> retrieveAllUsers() {

		context.authenticatedUser();

		List<OfficeData> offices = new ArrayList<OfficeData>(officeReadPlatformService.retrieveAllOffices());
		String officeIdsList = generateOfficeIdInClause(offices);

		AppUserMapper mapper = new AppUserMapper(offices);
		String sql = "select " + mapper.schema()
				+ " where u.office_id in (" + officeIdsList + ") and u.is_deleted = 0 order by u.id";

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}
	
	private String generateOfficeIdInClause(List<OfficeData> offices) {
		String officeIdsList = "";
		for (int i = 0; i < offices.size(); i++) {
			Long id = offices.get(i).getId();
			if (i == 0) {
				officeIdsList = id.toString();
			} else {
				officeIdsList += "," + id.toString();
			}
		}
		return officeIdsList;
	}

	@Override
	public AppUserData retrieveNewUserDetails() {

		List<OfficeLookup> offices = new ArrayList<OfficeLookup>(officeReadPlatformService.retrieveAllOfficesForLookup());

		List<RoleData> availableRoles = new ArrayList<RoleData>(this.roleReadPlatformService.retrieveAllRoles());

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

		List<RoleData> availableRoles = new ArrayList<RoleData>(this.roleReadPlatformService.retrieveAllRoles());
		
		List<RoleData> selectedUserRoles = new ArrayList<RoleData>();
		Set<Role> userRoles = user.getRoles();
		for (Role role : userRoles) {
			selectedUserRoles.add(role.toData());
		}

		availableRoles.removeAll(selectedUserRoles);
		
		AppUserData userData = new AppUserData(user.getId(),
				user.getUsername(), user.getEmail(), user.getOffice().getId(), user.getOffice()
						.getName(), user.getFirstname(), user.getLastname(), availableRoles, selectedUserRoles);

		return userData;
	}
	
	private static final class AppUserMapper implements RowMapper<AppUserData> {

		private final List<OfficeData> offices;

		public AppUserMapper(final List<OfficeData> offices) {
			this.offices = offices;
		}

		@Override
		public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String username = rs.getString("username");
			String firstname = rs.getString("firstname");
			String lastname = rs.getString("lastname");
			String email = rs.getString("email");
			Long officeId = JdbcSupport.getLong(rs, "officeId");
			
			// FIXME - change sql query to join to get office id and name information.
			String officeName = fromOfficeList(this.offices, officeId);

			AppUserData user = new AppUserData(id, username, email, officeId, officeName, firstname, lastname, new ArrayList<RoleData>(),  new ArrayList<RoleData>());

			return user;
		}

		public String schema() {
			return " u.id as id, u.username as username, u.firstname as firstname, u.lastname as lastname, u.email as email, u.office_id as officeId from m_appuser u ";
		}

		private String fromOfficeList(final List<OfficeData> officeList, final Long officeId) {
			String match = "";
			for (OfficeData office : officeList) {
				if (office.getId().equals(officeId)) {
					match = office.getName();
				}
			}

			return match;
		}
	}
}