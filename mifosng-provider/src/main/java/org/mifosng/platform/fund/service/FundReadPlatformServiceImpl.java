package org.mifosng.platform.fund.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import org.mifosng.platform.api.data.FundData;
import org.mifosng.platform.exceptions.FundNotFoundException;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class FundReadPlatformServiceImpl implements FundReadPlatformService {

	private final SimpleJdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public FundReadPlatformServiceImpl(final PlatformSecurityContext context,
			final DataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	private static final class FundMapper implements RowMapper<FundData> {

		public String officeSchema() {
			return " f.id as id, f.name as name, f.external_id as externalId from org_fund f ";
		}

		@Override
		public FundData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String name = rs.getString("name");
			String externalId = rs.getString("externalId");

			return new FundData(id, name, externalId);
		}
	}

	@Override
	public Collection<FundData> retrieveAllFunds() {

		AppUser currentUser = context.authenticatedUser();

		FundMapper rm = new FundMapper();
		String sql = "select "
				+ rm.officeSchema()
				+ " where f.org_id = ? order by f.name";

		return this.jdbcTemplate.query(sql, rm, new Object[] {currentUser.getOrganisation().getId()});
	}
	
	@Override
	public FundData retrieveFund(final Long fundId) {

		try {
			AppUser currentUser = context.authenticatedUser();

			FundMapper rm = new FundMapper();
			String sql = "select " + rm.officeSchema()
					+ " where f.org_id = ? and f.id = ?";

			FundData selectedFund = this.jdbcTemplate.queryForObject(sql,
					rm, new Object[] { currentUser.getOrganisation().getId(), fundId});

			return selectedFund;
		} catch (EmptyResultDataAccessException e) {
			throw new FundNotFoundException(fundId);
		}
	}
}