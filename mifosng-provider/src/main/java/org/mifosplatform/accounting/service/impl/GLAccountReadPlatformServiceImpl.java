package org.mifosplatform.accounting.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.api.data.GLAccountData;
import org.mifosplatform.accounting.domain.GLAccountType;
import org.mifosplatform.accounting.domain.GLAccountUsage;
import org.mifosplatform.accounting.exceptions.GLAccountInvalidClassificationException;
import org.mifosplatform.accounting.exceptions.GLAccountNotFoundException;
import org.mifosplatform.accounting.service.AccountingEnumerations;
import org.mifosplatform.accounting.service.GLAccountReadPlatformService;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class GLAccountReadPlatformServiceImpl implements GLAccountReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GLAccountReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class GLAccountMapper implements RowMapper<GLAccountData> {

        public String schema() {
            return " id as id, name as name, parent_id as parentId, gl_code as glCode, disabled as disabled, manual_journal_entries_allowed as manualEntriesAllowed, "
                    + "classification_enum as classification, account_usage as accountUsage, description as description "
                    + "from acc_gl_account";
        }

        @Override
        public GLAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long id = rs.getLong("id");
            String name = rs.getString("name");
            Long parentId = JdbcSupport.getLong(rs,"parentId"); 
            String glCode = rs.getString("glCode");
            boolean disabled = rs.getBoolean("disabled");
            boolean manualEntriesAllowed = rs.getBoolean("manualEntriesAllowed");
            final int accountTypeId = JdbcSupport.getInteger(rs, "classification");
            final EnumOptionData accountType = AccountingEnumerations.gLAccountType(accountTypeId);
            final int usageId = JdbcSupport.getInteger(rs, "accountUsage");
            final EnumOptionData usage = AccountingEnumerations.gLAccountUsage(usageId);
            String description = rs.getString("description");

            return new GLAccountData(id, name, parentId, glCode, disabled, manualEntriesAllowed, accountType, usage, description);
        }
    }

    @Override
    public List<GLAccountData> retrieveAllGLAccounts(Integer accountClassification, String searchParam, Integer usage,
            Boolean manualTransactionsAllowed, Boolean disabled) {
        if (accountClassification != null) {
            if (!checkValidGLAccountType(accountClassification)) { throw new GLAccountInvalidClassificationException(accountClassification); }
        }

        if (usage != null) {
            if (!checkValidGLAccountUsage(usage)) { throw new GLAccountInvalidClassificationException(accountClassification); }
        }

        GLAccountMapper rm = new GLAccountMapper();
        String sql = "select " + rm.schema();
        Object[] paramaterArray = new Object[3];
        int arrayPos = 0;
        boolean filtersPresent = false;
        if ((accountClassification != null) || StringUtils.isNotBlank(searchParam) || (usage != null)
                || (manualTransactionsAllowed != null) || (disabled != null)) {
            filtersPresent = true;
            sql += " where";
        }

        if (filtersPresent) {
            boolean firstWhereConditionAdded = false;
            if (accountClassification != null) {
                sql += " classification_enum like ?";
                paramaterArray[arrayPos] = accountClassification;
                arrayPos = arrayPos + 1;
                firstWhereConditionAdded = true;
            }
            if (StringUtils.isNotBlank(searchParam)) {
                if (firstWhereConditionAdded) {
                    sql += " and ";
                }
                sql += " ( name like %?% or gl_code like %?% )";
                paramaterArray[arrayPos] = searchParam;
                arrayPos = arrayPos + 1;
                paramaterArray[arrayPos] = searchParam;
                arrayPos = arrayPos + 1;
                firstWhereConditionAdded = true;
            }
            if (usage != null) {
                if (firstWhereConditionAdded) {
                    sql += " and ";
                }
                if (GLAccountUsage.HEADER.getValue().equals(usage)) {
                    sql += " account_usage = 2 ";
                } else if (GLAccountUsage.DETAIL.getValue().equals(usage)) {
                    sql += " account_usage = 1 ";
                }
                firstWhereConditionAdded = true;
            }
            if (manualTransactionsAllowed != null) {
                if (firstWhereConditionAdded) {
                    sql += " and ";
                }

                if (manualTransactionsAllowed) {
                    sql += " manual_journal_entries_allowed = 1";
                } else {
                    sql += " manual_journal_entries_allowed = 0";
                }
                firstWhereConditionAdded = true;
            }
            if (disabled != null) {
                if (firstWhereConditionAdded) {
                    sql += " and ";
                }

                if (disabled) {
                    sql += " disabled = 1";
                } else {
                    sql += " disabled = 0";
                }
                firstWhereConditionAdded = true;
            }
        }

        sql = sql + " order by glCode";
        Object[] finalObjectArray = Arrays.copyOf(paramaterArray, arrayPos);
        return this.jdbcTemplate.query(sql, rm, finalObjectArray);
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

    @Override
    public List<GLAccountData> retrieveAllEnabledDetailGLAccounts(GLAccountType accountType) {
        return retrieveAllGLAccounts(accountType.getValue(), null, GLAccountUsage.DETAIL.getValue(), null, false);
    }

    private static boolean checkValidGLAccountType(final int type) {
        for (GLAccountType accountType : GLAccountType.values()) {
            if (accountType.getValue().equals(type)) { return true; }
        }
        return false;
    }

    private static boolean checkValidGLAccountUsage(final int type) {
        for (GLAccountUsage accountUsage : GLAccountUsage.values()) {
            if (accountUsage.getValue().equals(type)) { return true; }
        }
        return false;
    }

    @Override
    public GLAccountData retrieveNewGLAccountDetails() {
        return GLAccountData.sensibleDefaultsForNewGLAccountCreation();
    }

}
