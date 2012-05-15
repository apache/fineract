package org.mifosng.platform.user.service;

import static org.mifosng.platform.Specifications.usersThatMatch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.mifosng.data.AppUserData;
import org.mifosng.data.OfficeData;
import org.mifosng.data.RoleData;
import org.mifosng.platform.exceptions.PlatformResourceNotFoundException;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.mifosng.platform.user.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AppUserReadPlatformServiceImpl implements AppUserReadPlatformService {

	private final SimpleJdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final OfficeReadPlatformService officeReadPlatformService;
	private final RoleReadPlatformService roleReadPlatformService;
	private final AppUserRepository appUserRepository;

	@Autowired
	public AppUserReadPlatformServiceImpl(final PlatformSecurityContext context, final DataSource dataSource, 
			final OfficeReadPlatformService officeReadPlatformService, final RoleReadPlatformService roleReadPlatformService,
			final AppUserRepository appUserRepository) {
		this.context = context;
		this.officeReadPlatformService = officeReadPlatformService;
		this.roleReadPlatformService = roleReadPlatformService;
		this.appUserRepository = appUserRepository;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}

	@Override
	public Collection<AppUserData> retrieveAllUsers() {

		AppUser currentUser = context.authenticatedUser();

		List<OfficeData> offices = new ArrayList<OfficeData>(officeReadPlatformService.retrieveAllOffices());
		String officeIdsList = generateOfficeIdInClause(offices);

		AppUserMapper mapper = new AppUserMapper(offices);
		String sql = "select " + mapper.schema()
				+ " where u.org_id = ? and u.office_id in (" + officeIdsList
				+ ")";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { currentUser
				.getOrganisation().getId() });
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

		List<OfficeData> offices = new ArrayList<OfficeData>(officeReadPlatformService.retrieveAllOffices());

		List<RoleData> availableRoles = new ArrayList<RoleData>(this.roleReadPlatformService.retrieveAllRoles());

		AppUserData userData = new AppUserData();
		userData.setAllowedOffices(offices);
		userData.setAvailableRoles(availableRoles);

		return userData;
	}

	@Override
	public AppUserData retrieveUser(Long userId) {

		AppUser currentUser = context.authenticatedUser();

		List<OfficeData> offices = new ArrayList<OfficeData>(officeReadPlatformService.retrieveAllOffices());

		List<RoleData> availableRoles = new ArrayList<RoleData>(this.roleReadPlatformService.retrieveAllRoles());

		AppUser user = this.appUserRepository.findOne(usersThatMatch(currentUser.getOrganisation(), userId));
		if (user == null) {
			throw new PlatformResourceNotFoundException("error.msg.user.id.invalid", "User with identifier {0} does not exist.", userId);
		}

		List<RoleData> userRoleData = new ArrayList<RoleData>();
		Set<Role> userRoles = user.getRoles();
		for (Role role : userRoles) {
			userRoleData.add(role.toData());
		}

		AppUserData userData = new AppUserData(user.getId(),
				user.getUsername(), user.getEmail(), user.getOrganisation()
						.getId(), user.getOffice().getId(), user.getOffice()
						.getName());
		userData.setFirstname(user.getFirstname());
		userData.setLastname(user.getLastname());

		userData.setAllowedOffices(offices);

		availableRoles.removeAll(userRoleData);
		userData.setAvailableRoles(availableRoles);
		userData.setSelectedRoles(userRoleData);

		return userData;
	}
	
	@Override
	public AppUserData retrieveCurrentUser() {
		AppUser currentUser = context.authenticatedUser();

		List<OfficeData> offices = new ArrayList<OfficeData>(officeReadPlatformService.retrieveAllOffices());

		List<RoleData> availableRoles = new ArrayList<RoleData>(this.roleReadPlatformService.retrieveAllRoles());
		
		AppUser user = this.appUserRepository.findOne(usersThatMatch(currentUser.getOrganisation(), currentUser.getId()));

		List<RoleData> userRoleData = new ArrayList<RoleData>();
		Set<Role> userRoles = user.getRoles();
		for (Role role : userRoles) {
			userRoleData.add(role.toData());
		}

		AppUserData userData = new AppUserData(user.getId(),
				user.getUsername(), user.getEmail(), user.getOrganisation()
						.getId(), user.getOffice().getId(), user.getOffice()
						.getName());
		userData.setFirstname(user.getFirstname());
		userData.setLastname(user.getLastname());

		userData.setAllowedOffices(offices);

		availableRoles.removeAll(userRoleData);
		userData.setAvailableRoles(availableRoles);
		userData.setSelectedRoles(userRoleData);

		return userData;
	}
	
	private static final class AppUserMapper implements RowMapper<AppUserData> {

		private final List<OfficeData> offices;

		public AppUserMapper(final List<OfficeData> offices) {
			this.offices = offices;
		}

		@Override
		public AppUserData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String username = rs.getString("username");
			String firstname = rs.getString("firstname");
			String lastname = rs.getString("lastname");
			String email = rs.getString("email");
			Long orgId = rs.getLong("orgId");
			Long officeId = rs.getLong("officeId");

			String officeName = fromOfficeList(this.offices, officeId);

			AppUserData user = new AppUserData(id, username, email, orgId,
					officeId, officeName);
			user.setLastname(lastname);
			user.setFirstname(firstname);

			return user;
		}

		public String schema() {
			return " u.id as id, u.username as username, u.firstname as firstname, u.lastname as lastname, u.email as email, u.org_id as orgId, u.office_id as officeId from admin_appuser u ";
		}

		private String fromOfficeList(final List<OfficeData> officeList,
				final Long officeId) {
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