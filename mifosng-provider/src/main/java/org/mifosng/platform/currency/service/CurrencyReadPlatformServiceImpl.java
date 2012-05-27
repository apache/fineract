package org.mifosng.platform.currency.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.mifosng.data.CurrencyData;
import org.mifosng.platform.infrastructure.JdbcSupport;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CurrencyReadPlatformServiceImpl implements CurrencyReadPlatformService {

	private final SimpleJdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public CurrencyReadPlatformServiceImpl(final PlatformSecurityContext context, final DataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	@Override
	public List<CurrencyData> retrieveAllowedCurrencies() {

		AppUser currentUser = context.authenticatedUser();

		String sql = "select c.code as code, c.name as name, c.decimal_places as decimalPlaces, c.display_symbol as displaySymbol, c.internationalized_name_code as nameCode from org_organisation_currency c where c.org_id = ?";

		RowMapper<CurrencyData> rm = new CurrencyMapper();

		return this.jdbcTemplate.query(sql, rm, new Object[] { currentUser
				.getOrganisation().getId() });
	}

	@Override
	public List<CurrencyData> retrieveAllPlatformCurrencies() {

		String sql = "select c.code as code, c.name as name, c.decimal_places as decimalPlaces, c.display_symbol as displaySymbol, c.internationalized_name_code as nameCode from ref_currency c";

		RowMapper<CurrencyData> rm = new CurrencyMapper();

		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	private static final class CurrencyMapper implements RowMapper<CurrencyData> {

		@Override
		public CurrencyData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			String code = rs.getString("code");
			String name = rs.getString("name");
			int decimalPlaces = JdbcSupport.getInteger(rs, "decimalPlaces");
			String displaySymbol = rs.getString("displaySymbol");
			String nameCode = rs.getString("nameCode");

			return new CurrencyData(code, name, decimalPlaces, displaySymbol, nameCode);
		}
	}
}