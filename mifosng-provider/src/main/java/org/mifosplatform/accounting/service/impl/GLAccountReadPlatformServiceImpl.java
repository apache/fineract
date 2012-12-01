package org.mifosplatform.accounting.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.AccountingConstants.GL_ACCOUNT_CLASSIFICATION;
import org.mifosplatform.accounting.api.data.GLAccountData;
import org.mifosplatform.accounting.exceptions.GLAccountInvalidClassificationException;
import org.mifosplatform.accounting.exceptions.GLAccountNotFoundException;
import org.mifosplatform.accounting.service.GLAccountReadPlatformService;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class GLAccountReadPlatformServiceImpl implements GLAccountReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    @SuppressWarnings("unused")
    private final PlatformSecurityContext context;

    @Autowired
    public GLAccountReadPlatformServiceImpl(final PlatformSecurityContext context, final TenantAwareRoutingDataSource dataSource) {
        this.context = context;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class GLAccountMapper implements RowMapper<GLAccountData> {

        public String schema() {
            return " id as id, name as name, parent_id as parentId, gl_code as glCode, disabled as disabled, manual_entries_allowed as manualEntriesAllowed, "
                    + "classification as classification, header_account as headerAccount, description as description "
                    + "from acc_gl_account";
        }

        @Override
        public GLAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String name = rs.getString("name");
            Long parentId = rs.getLong("parentId");
            String glCode = rs.getString("glCode");
            boolean disabled = rs.getBoolean("disabled");
            boolean manualEntriesAllowed = rs.getBoolean("manualEntriesAllowed");
            String classification = rs.getString("classification");
            boolean headerAccount = rs.getBoolean("headerAccount");
            String description = rs.getString("description");

            return new GLAccountData(id, name, parentId, glCode, disabled, manualEntriesAllowed, classification, headerAccount, description);
        }
    }

    @Override
    public List<GLAccountData> retrieveAllGLAccounts(String accountClassification, String searchParam) {
        if (StringUtils.isNotBlank(accountClassification)) {
            if (!checkValidGLAccountClassification(accountClassification)) { throw new GLAccountInvalidClassificationException(
                    accountClassification); }
        }

        GLAccountMapper rm = new GLAccountMapper();
        String sql = "select " + rm.schema();
        if (StringUtils.isNotBlank(accountClassification) && searchParam != null) {
            sql += " where classification like ? and ( name like %?% or glCode like %?% )";
        } else if (StringUtils.isNotBlank(accountClassification)) {
            sql += " where classification like ?";
        } else if (searchParam != null) {
            sql += " where ( name like %?% or glCode like %?% )";
        }
        sql = sql + " order by glCode";
        return this.jdbcTemplate.query(sql, rm, new Object[] { accountClassification, searchParam });
    }

    @Override
    public GLAccountData retrieveGLAccountById(long glAccountId) {
        try {

            GLAccountMapper rm = new GLAccountMapper();
            String sql = "select " + rm.schema() + " where id = ?";

            GLAccountData glAccountData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { glAccountId });

            return glAccountData;
        } catch (EmptyResultDataAccessException e) {
            throw new GLAccountNotFoundException(glAccountId);
        }
    }

    private static boolean checkValidGLAccountClassification(final String entityType) {
        for (GL_ACCOUNT_CLASSIFICATION classification : GL_ACCOUNT_CLASSIFICATION.values()) {
            if (classification.name().equalsIgnoreCase(entityType)) { return true; }
        }
        return false;
    }

}
