package org.mifosng.platform.staff.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.data.StaffData;
import org.mifosng.platform.exceptions.StaffNotFoundException;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class StaffReadPlatformServiceImpl implements StaffReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public StaffReadPlatformServiceImpl(final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class StaffMapper implements RowMapper<StaffData> {

		public String schema() {
			return " s.id as id,s.office_id as officeId, o.name as officeName, s.firstname as firstname, s.lastname as lastname,"
					+ " s.display_name as displayName, s.is_loan_officer as isLoanOfficer from m_staff s "
					+ " join m_office o on o.id = s.office_id";
		}

		@Override
		public StaffData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String firstname = rs.getString("firstname");
			String lastname = rs.getString("lastname");
			String displayName = rs.getString("displayName");
			Long officeId = rs.getLong("officeId");
			boolean isLoanOfficer = rs.getBoolean("isLoanOfficer");
			String officeName = rs.getString("officeName");

			return new StaffData(id, firstname, lastname, displayName,
					officeId, officeName, isLoanOfficer);
		}

	}

	@Override
	public Collection<StaffData> retrieveAllStaff(final String extraCriteria) {

		context.authenticatedUser();

		StaffMapper rm = new StaffMapper();
		String sql = "select " + rm.schema();
		if (StringUtils.isNotBlank(extraCriteria)) {
			sql += " where " + extraCriteria;
		}
		sql = sql + " order by s.lastname";
		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	@Override
	public Collection<StaffData> retrieveAllLoanOfficersByOffice(long officeId) {
		return retrieveAllStaff(" office_id = " + officeId
				+ " and is_loan_officer = 1");

	}

	@Override
	public StaffData retrieveStaff(final Long staffId) {

		try {
			context.authenticatedUser();

			StaffMapper rm = new StaffMapper();
			String sql = "select " + rm.schema() + " where s.id = ?";

			StaffData selectedStaff = this.jdbcTemplate.queryForObject(sql, rm,
					new Object[] { staffId });

			return selectedStaff;
		} catch (EmptyResultDataAccessException e) {
			throw new StaffNotFoundException(staffId);
		}
	}
}