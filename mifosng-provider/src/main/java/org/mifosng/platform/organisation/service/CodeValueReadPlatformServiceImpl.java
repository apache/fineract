package org.mifosng.platform.organisation.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosng.platform.api.data.CodeValueData;
import org.mifosng.platform.infrastructure.TenantAwareRoutingDataSource;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CodeValueReadPlatformServiceImpl implements CodeValueReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public CodeValueReadPlatformServiceImpl(final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private static final class CodeValueDataMapper implements
			RowMapper<CodeValueData> {

		public String schema() {
			return " cv.id as id,cv.code_value as value,cv.code_id as codeId, cv.order_position as position"
					+ " from m_code_value as cv ";
		}

		@Override
		public CodeValueData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String value = rs.getString("value");
			Integer position = rs.getInt("position");

			return new CodeValueData(id, value, position);
		}
	}

	@Override
	public Collection<CodeValueData> retrieveAllCodeValues(final Long codeId) {
		context.authenticatedUser();

		CodeValueDataMapper rm = new CodeValueDataMapper();
		String sql = "select " + rm.schema()
				+ "where cv.code_id = ? order by position";

		return this.jdbcTemplate.query(sql, rm, new Object[] { codeId });
	}

}