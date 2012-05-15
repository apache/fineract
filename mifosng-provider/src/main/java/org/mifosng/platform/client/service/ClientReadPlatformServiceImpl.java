package org.mifosng.platform.client.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.data.ClientData;
import org.mifosng.data.OfficeData;
import org.mifosng.platform.exceptions.PlatformResourceNotFoundException;
import org.mifosng.platform.organisation.domain.Organisation;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClientReadPlatformServiceImpl implements ClientReadPlatformService {

	private final SimpleJdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final OfficeReadPlatformService officeReadPlatformService;

	@Autowired
	public ClientReadPlatformServiceImpl(final PlatformSecurityContext context, final DataSource dataSource, final OfficeReadPlatformService officeReadPlatformService) {
		this.context = context;
		this.officeReadPlatformService = officeReadPlatformService;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
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
	public Collection<ClientData> retrieveAllIndividualClients() {

		AppUser currentUser = this.context.authenticatedUser();

		List<OfficeData> offices = new ArrayList<OfficeData>(officeReadPlatformService.retrieveAllOffices());
		String officeIdsList = generateOfficeIdInClause(offices);
		
		ClientMapper rm = new ClientMapper(offices, currentUser.getOrganisation());

		String sql = "select " + rm.clientSchema()
				+ " where c.org_id = ? and c.office_id in (" + officeIdsList
				+ ") order by c.lastname ASC, c.firstname ASC";

		return this.jdbcTemplate.query(sql, rm, new Object[] { currentUser
				.getOrganisation().getId() });
	}
	
	@Override
	public ClientData retrieveIndividualClient(final Long clientId) {

		try {
			AppUser currentUser = this.context.authenticatedUser();

			List<OfficeData> offices = new ArrayList<OfficeData>(officeReadPlatformService.retrieveAllOffices());
			ClientMapper rm = new ClientMapper(offices, currentUser.getOrganisation());

			String sql = "select " + rm.clientSchema() + " where c.id = ? and c.org_id = ?";

			return this.jdbcTemplate.queryForObject(sql, rm, new Object[] {clientId, currentUser.getOrganisation().getId() });
		} catch (EmptyResultDataAccessException e) {
			throw new PlatformResourceNotFoundException(
					"error.msg.client.id.invalid",
					"Client with identifier {0} does not exist", clientId);
		}
	}
	
	private static final class ClientMapper implements RowMapper<ClientData> {

		private final List<OfficeData> offices;
		private final Organisation organisation;

		public ClientMapper(final List<OfficeData> offices,
				Organisation organisation) {
			this.offices = offices;
			this.organisation = organisation;
		}

		public String clientSchema() {
			return "c.org_id as orgId, c.office_id as officeId, c.id as id, c.firstname as firstname, c.lastname as lastname, c.external_id as externalId, c.joining_date as joinedDate from portfolio_client c";
		}

		@Override
		public ClientData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long orgId = rs.getLong("orgId");
			Long officeId = rs.getLong("officeId");
			Long id = rs.getLong("id");
			String firstname = rs.getString("firstname");
			if (StringUtils.isBlank(firstname)) {
				firstname = "";
			}
			String lastname = rs.getString("lastname");
			String externalId = rs.getString("externalId");
			LocalDate joinedDate = new LocalDate(rs.getDate("joinedDate"));

			String officeName = fromOfficeList(this.offices, officeId);

			String orgname = "";
			if (organisation.getId().equals(orgId)) {
				orgname = organisation.getName();
			}

			return new ClientData(orgId, orgname, officeId, officeName, id,
					firstname, lastname, externalId, joinedDate);
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