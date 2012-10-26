package org.mifosng.platform.organisation.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosng.platform.api.data.CodeData;
import org.mifosng.platform.exceptions.FundNotFoundException;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CodeReadPlatformServiceImpl implements CodeReadPlatformService {
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public CodeReadPlatformServiceImpl(final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private static final class CodeMapper implements RowMapper<CodeData> {
		public String schema() {
			return " c.id as id, c.code_name as code_name from m_code c ";
		}

		@Override
		public CodeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String code_name = rs.getString("code_name");

			return new CodeData(id, code_name);
		}
	}
	
	@Override
	public Collection<CodeData> retrieveAllCodes()
	{
		context.authenticatedUser();
		
		CodeMapper rm = new CodeMapper();
		String sql = "select "
				+ rm.schema()
				+ " order by c.code_name";
		
		return this.jdbcTemplate.query(sql,rm,new Object[] {} );
	}
	
	@Override
	public CodeData retrieveCode(Long codeId)
	{
		try {
			context.authenticatedUser();

			CodeMapper rm = new CodeMapper();
			String sql = "select " + rm.schema() + " where c.id = ?";

			CodeData selectedCode = this.jdbcTemplate.queryForObject(sql, rm, new Object[] {codeId});

			return selectedCode;
		} catch (EmptyResultDataAccessException e) {
			throw new FundNotFoundException(codeId);
		}
	}
	
}
