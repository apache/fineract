/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.journalentry.data.JournalEntryData;
import org.mifosplatform.accounting.journalentry.domain.JournalEntryType;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.jobs.annotation.CronTarget;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JournalEntryRunningBalanceUpdateServiceImpl implements JournalEntryRunningBalanceUpdateService {

    private final JdbcTemplate jdbcTemplate;

    private final GLJournalEntryMapper entryMapper = new GLJournalEntryMapper();
    private final String runningBalanceSql = "select je.office_running_balance as runningBalance,je.account_id as accountId from acc_gl_journal_entry je "
            + "inner join (select max(id) as id from acc_gl_journal_entry where office_id=?  and entry_date < ? group by account_id,entry_date) je2 "
            + "inner join (select max(entry_date) as date from acc_gl_journal_entry where office_id=? and entry_date < ? group by account_id) je3 "
            + "where je2.id = je.id and je.entry_date = je3.date group by je.id order by je.entry_date DESC";

    @Autowired
    public JournalEntryRunningBalanceUpdateServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    @CronTarget(jobName = JobName.ACCOUNTING_RUNNING_BALANCE_UPDATE)
    public void updateRunningBalance() {

        String dateFinder = "select je.office_id as officeId,MIN(je.entry_date) as entityDate " + "from acc_gl_journal_entry  je "
                + "where je.is_running_balance_caculated=0  " + "group by je.office_id";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(dateFinder);
        for (Map<String, Object> entries : list) {
            Long officeId = (Long) entries.get("officeId");
            Date entityDate = (Date) entries.get("entityDate");
            updateRunningBalance(officeId, entityDate);

        }
    }

    @Transactional
    private void updateRunningBalance(Long officeId, Date entityDate) {
        Map<Long, BigDecimal> runningBalanceMap = new HashMap<Long, BigDecimal>(5);

        List<Map<String, Object>> list = jdbcTemplate.queryForList(runningBalanceSql, officeId, entityDate, officeId, entityDate);
        for (Map<String, Object> entries : list) {
            Long accountId = (Long) entries.get("accountId");
            if (!runningBalanceMap.containsKey(accountId)) {
                runningBalanceMap.put(accountId, (BigDecimal) entries.get("runningBalance"));
            }
        }
        List<JournalEntryData> entryDatas = jdbcTemplate.query(entryMapper.schema(), entryMapper, new Object[] { officeId, entityDate });
        String[] updateSql = new String[entryDatas.size()];
        int i = 0;
        for (JournalEntryData entryData : entryDatas) {
            BigDecimal runningBalance = calculateRunningBalance(entryData, runningBalanceMap);
            String sql = "UPDATE acc_gl_journal_entry je SET je.is_running_balance_caculated=1, je.office_running_balance="
                    + runningBalance + " WHERE  je.id=" + entryData.getId();
            updateSql[i++] = sql;
        }
        this.jdbcTemplate.batchUpdate(updateSql);
    }

    private BigDecimal calculateRunningBalance(JournalEntryData entry, Map<Long, BigDecimal> runningBalanceMap) {
        BigDecimal runningBalance = BigDecimal.ZERO;
        if (runningBalanceMap.containsKey(entry.getGlAccountId())) {
            runningBalance = runningBalanceMap.get(entry.getGlAccountId());
        }
        GLAccountType accounttype = GLAccountType.fromInt(entry.getGlAccountType().getId().intValue());
        JournalEntryType entryType = JournalEntryType.fromInt(entry.getEntryType().getId().intValue());
        boolean isIncrease = false;
        switch (accounttype) {
            case ASSET:
                if (entryType.isDebitType()) {
                    isIncrease = true;
                }
            break;
            case EQUITY:
                if (entryType.isCreditType()) {
                    isIncrease = true;
                }
            break;
            case EXPENSE:
                if (entryType.isDebitType()) {
                    isIncrease = true;
                }
            break;
            case INCOME:
                if (entryType.isCreditType()) {
                    isIncrease = true;
                }
            break;
            case LIABILITY:
                if (entryType.isCreditType()) {
                    isIncrease = true;
                }
            break;
        }
        if (isIncrease) {
            runningBalance = runningBalance.add(entry.getAmount());
        } else {
            runningBalance = runningBalance.subtract(entry.getAmount());
        }
        runningBalanceMap.put(entry.getGlAccountId(), runningBalance);
        return runningBalance;
    }

    private static final class GLJournalEntryMapper implements RowMapper<JournalEntryData> {

        public String schema() {
            return "select je.id as id,je.account_id as glAccountId," + "je.type_enum as entryType,je.amount as amount, "
                    + "glAccount.classification_enum as classification " + "from acc_gl_journal_entry je , acc_gl_account glAccount "
                    + "where je.account_id = glAccount.id " + "and je.office_id=? and je.entry_date >= ? order by je.entry_date,je.id";
        }

        @Override
        public JournalEntryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long glAccountId = rs.getLong("glAccountId");
            final int accountTypeId = JdbcSupport.getInteger(rs, "classification");
            final EnumOptionData accountType = AccountingEnumerations.gLAccountType(accountTypeId);
            final BigDecimal amount = rs.getBigDecimal("amount");
            final int entryTypeId = JdbcSupport.getInteger(rs, "entryType");
            final EnumOptionData entryType = AccountingEnumerations.journalEntryType(entryTypeId);

            return new JournalEntryData(id, null, null, null, glAccountId, null, accountType, null, entryType, amount, null, null, null,
                    null, null, null, null, null, null, null);
        }
    }

}
