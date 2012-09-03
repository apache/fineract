package org.mifosng.platform.staff.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

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
			return " s.id as id, s.firstname as firstname, s.lastname as lastname, s.display_name as displayName from m_staff s ";
		}

		@Override
		public StaffData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String firstname = rs.getString("firstname");
			String lastname = rs.getString("lastname");
			String displayName = rs.getString("displayName");
			
			return new StaffData(id, firstname, lastname,displayName);
		}
	}

	@Override
	public Collection<StaffData> retrieveAllStaff() {

		context.authenticatedUser();

		StaffMapper rm = new StaffMapper();
		String sql = "select " + rm.schema() + " order by s.lastname";

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
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