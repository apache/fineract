package org.mifosplatform.portfolio.loanaccount.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanaccount.data.LoanCollateralData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class LoanCollateralReadPlatformServiceImpl implements LoanCollateralReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public LoanCollateralReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class LoanCollateralMapper implements RowMapper<LoanCollateralData> {

        private StringBuilder sqlBuilder = new StringBuilder(
                "lc.id as id, lc.description as description, cv.id as typeId, cv.code_value as typeName") //
                .append(" FROM m_loan_collateral lc") //
                .append(" JOIN m_code_value cv on lc.type_cv_id = cv.id");

        public String schema() {
            return sqlBuilder.toString();
        }

        @Override
        public LoanCollateralData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("description");
            final Long typeId = rs.getLong("typeId");
            final String typeName = rs.getString("typeName");

            final CodeValueData type = CodeValueData.instance(typeId, typeName);

            return LoanCollateralData.instance(id, type, name);
        }
    }

    @Override
    public Collection<LoanCollateralData> retrieveLoanCollateral(final Long loanId) {
        this.context.authenticatedUser();

        final LoanCollateralMapper rm = new LoanCollateralMapper();

        final String sql = "select " + rm.schema() + " where lc.loan_id=? order by id ASC";

        return this.jdbcTemplate.query(sql, rm, new Object[] { loanId });
    }
}