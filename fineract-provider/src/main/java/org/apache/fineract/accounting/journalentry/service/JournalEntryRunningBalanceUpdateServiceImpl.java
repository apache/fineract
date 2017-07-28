/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.accounting.journalentry.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.accounting.journalentry.api.JournalEntryJsonInputParams;
import org.apache.fineract.accounting.journalentry.data.JournalEntryData;
import org.apache.fineract.accounting.journalentry.data.JournalEntryDataValidator;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class JournalEntryRunningBalanceUpdateServiceImpl implements JournalEntryRunningBalanceUpdateService {

    private final static Logger logger = LoggerFactory.getLogger(JournalEntryRunningBalanceUpdateServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;

    private final OfficeRepositoryWrapper officeRepositoryWrapper;

    private final JournalEntryDataValidator dataValidator;

    private final FromJsonHelper fromApiJsonHelper;

    private final GLJournalEntryMapper entryMapper = new GLJournalEntryMapper();
    
    // if a limit is not added to the running balance select statements below and the resultset is more than 400,000, 
    // the script will eat up all of the server memory
    private final String selectRunningBalanceSqlLimit = "limit 0, 10000";
    
    private final String officeRunningBalanceSql = "select je.office_running_balance as runningBalance,je.account_id as accountId from acc_gl_journal_entry je "
            + "inner join (select max(id) as id from acc_gl_journal_entry where office_id=?  and entry_date < ? group by account_id,entry_date) je2 "
            + "inner join (select max(entry_date) as date from acc_gl_journal_entry where office_id=? and entry_date < ? group by account_id) je3 "
            + "where je2.id = je.id and je.entry_date = je3.date group by je.id order by je.entry_date DESC " + selectRunningBalanceSqlLimit;

    private final String organizationRunningBalanceSql = "select je.organization_running_balance as runningBalance,je.account_id as accountId from acc_gl_journal_entry je "
            + "inner join (select max(id) as id from acc_gl_journal_entry where entry_date < ? group by account_id,entry_date) je2 "
            + "inner join (select max(entry_date) as date from acc_gl_journal_entry where entry_date < ? group by account_id) je3 "
            + "where je2.id = je.id and je.entry_date = je3.date group by je.id order by je.entry_date DESC " + selectRunningBalanceSqlLimit;

    private final String officesRunningBalanceSql = "select je.office_running_balance as runningBalance,je.account_id as accountId,je.office_id as officeId "
            + "from acc_gl_journal_entry je "
            + "inner join (select max(id) as id from acc_gl_journal_entry where entry_date < ? group by office_id,account_id,entry_date) je2 "
            + "inner join (select max(entry_date) as date from acc_gl_journal_entry where entry_date < ? group by office_id,account_id) je3 "
            + "where je2.id = je.id and je.entry_date = je3.date group by je.id order by je.entry_date DESC " + selectRunningBalanceSqlLimit;

    @Autowired
    public JournalEntryRunningBalanceUpdateServiceImpl(final RoutingDataSource dataSource, final OfficeRepositoryWrapper officeRepositoryWrapper,
            final JournalEntryDataValidator dataValidator, final FromJsonHelper fromApiJsonHelper) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.dataValidator = dataValidator;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    @CronTarget(jobName = JobName.ACCOUNTING_RUNNING_BALANCE_UPDATE)
    public void updateRunningBalance() {
        String dateFinder = "select MIN(je.entry_date) as entityDate from acc_gl_journal_entry  je "
                + "where je.is_running_balance_calculated=0 ";
        try {
            Date entityDate = this.jdbcTemplate.queryForObject(dateFinder, Date.class);
            updateOrganizationRunningBalance(entityDate);
        } catch (EmptyResultDataAccessException e) {
            logger.debug("No results found for updation of running balance ");
        }
    }

    @Override
    public CommandProcessingResult updateOfficeRunningBalance(JsonCommand command) {
        this.dataValidator.validateForUpdateRunningbalance(command);
        final Long officeId = this.fromApiJsonHelper.extractLongNamed(JournalEntryJsonInputParams.OFFICE_ID.getValue(),
                command.parsedJson());
        CommandProcessingResultBuilder commandProcessingResultBuilder = new CommandProcessingResultBuilder().withCommandId(command
                .commandId());
        if (officeId == null) {
            updateRunningBalance();
        } else {
            this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            String dateFinder = "select MIN(je.entry_date) as entityDate " + "from acc_gl_journal_entry  je "
                    + "where je.is_running_balance_calculated=0  and je.office_id=?";
            try {
                Date entityDate = this.jdbcTemplate.queryForObject(dateFinder, Date.class, officeId);
                updateRunningBalance(officeId, entityDate);
            } catch (EmptyResultDataAccessException e) {
                logger.debug("No results found for updation of office running balance with office id:" + officeId);
            }
            commandProcessingResultBuilder.withOfficeId(officeId);
        }
        return commandProcessingResultBuilder.build();
    }

    private void updateOrganizationRunningBalance(Date entityDate) {
        Map<Long, BigDecimal> runningBalanceMap = new HashMap<>(5);
        Map<Long, Map<Long, BigDecimal>> officesRunningBalance = new HashMap<>();

        List<Map<String, Object>> list = jdbcTemplate.queryForList(organizationRunningBalanceSql, entityDate, entityDate);
        for (Map<String, Object> entries : list) {
        	Long accountId = Long.parseLong(entries.get("accountId").toString()); //Drizzle is returning Big Integer where as MySQL returns Long.
            if (!runningBalanceMap.containsKey(accountId)) {
                runningBalanceMap.put(accountId, (BigDecimal) entries.get("runningBalance"));
            }
        }

        List<Map<String, Object>> officesRunningBalanceList = jdbcTemplate.queryForList(officesRunningBalanceSql, entityDate, entityDate);
        for (Map<String, Object> entries : officesRunningBalanceList) {
            Long accountId = Long.parseLong(entries.get("accountId").toString());
            Long officeId = Long.parseLong(entries.get("officeId").toString());
            Map<Long, BigDecimal> runningBalance = null;
            if (officesRunningBalance.containsKey(officeId)) {
                runningBalance = officesRunningBalance.get(officeId);
            } else {
                runningBalance = new HashMap<>();
                officesRunningBalance.put(officeId, runningBalance);
            }
            if (!runningBalance.containsKey(accountId)) {
                runningBalance.put(accountId, (BigDecimal) entries.get("runningBalance"));
            }
        }

        List<JournalEntryData> entryDatas = jdbcTemplate.query(entryMapper.organizationRunningBalanceSchema(), entryMapper,
                new Object[] { entityDate });
        if (entryDatas.size() > 0) {
            // run a batch update of 1000 SQL statements at a time
            final Integer batchUpdateSize = 1000;
            ArrayList<String> updateSql = new ArrayList<>();
            int batchIndex = 0;
            for (int index = 0 ; index < entryDatas.size() ; index++) {
            	JournalEntryData entryData = entryDatas.get(index) ;
                Map<Long, BigDecimal> officeRunningBalanceMap = null;
                if (officesRunningBalance.containsKey(entryData.getOfficeId())) {
                    officeRunningBalanceMap = officesRunningBalance.get(entryData.getOfficeId());
                } else {
                    officeRunningBalanceMap = new HashMap<>();
                    officesRunningBalance.put(entryData.getOfficeId(), officeRunningBalanceMap);
                }
                BigDecimal officeRunningBalance = calculateRunningBalance(entryData, officeRunningBalanceMap);
                BigDecimal runningBalance = calculateRunningBalance(entryData, runningBalanceMap);
                String sql = "UPDATE acc_gl_journal_entry je SET je.is_running_balance_calculated=1, je.organization_running_balance="
                        + runningBalance + ",je.office_running_balance=" + officeRunningBalance + " WHERE  je.id=" + entryData.getId();
                updateSql.add(sql) ;
                batchIndex++ ;
                if (batchIndex == batchUpdateSize || index == entryDatas.size()-1) {
                    // run a batch update of the 1000 update SQL statements
                	String[] batch = new String[updateSql.size()] ;
                	updateSql.toArray(batch) ;
                    this.jdbcTemplate.batchUpdate(batch);
                    // reset counter and string array
                    batchIndex = 0;
                    updateSql.clear();
                }
            }
        }

    }

    private void updateRunningBalance(Long officeId, Date entityDate) {
        Map<Long, BigDecimal> runningBalanceMap = new HashMap<>(5);

        List<Map<String, Object>> list = jdbcTemplate.queryForList(officeRunningBalanceSql, officeId, entityDate, officeId, entityDate);
        for (Map<String, Object> entries : list) {
            Long accountId = (Long) entries.get("accountId");
            if (!runningBalanceMap.containsKey(accountId)) {
                runningBalanceMap.put(accountId, (BigDecimal) entries.get("runningBalance"));
            }
        }
        List<JournalEntryData> entryDatas = jdbcTemplate.query(entryMapper.officeRunningBalanceSchema(), entryMapper, new Object[] {
                officeId, entityDate });
        String[] updateSql = new String[entryDatas.size()];
        int i = 0;
        for (JournalEntryData entryData : entryDatas) {
            BigDecimal runningBalance = calculateRunningBalance(entryData, runningBalanceMap);
            String sql = "UPDATE acc_gl_journal_entry je SET je.office_running_balance=" + runningBalance + " WHERE  je.id="
                    + entryData.getId();
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

        public String officeRunningBalanceSchema() {
            return "select je.id as id,je.account_id as glAccountId,je.type_enum as entryType,je.amount as amount, "
                    + "glAccount.classification_enum as classification,je.office_id as officeId "
                    + "from acc_gl_journal_entry je , acc_gl_account glAccount " + "where je.account_id = glAccount.id "
                    + "and je.office_id=? and je.entry_date >= ? order by je.entry_date,je.id";
        }

        public String organizationRunningBalanceSchema() {
            return "select je.id as id,je.account_id as glAccountId," + "je.type_enum as entryType,je.amount as amount, "
                    + "glAccount.classification_enum as classification,je.office_id as officeId  "
                    + "from acc_gl_journal_entry je , acc_gl_account glAccount " + "where je.account_id = glAccount.id "
                    + "and je.entry_date >= ? order by je.entry_date,je.id";
        }

        @Override
        public JournalEntryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long glAccountId = rs.getLong("glAccountId");
            final Long officeId = rs.getLong("officeId");
            final int accountTypeId = JdbcSupport.getInteger(rs, "classification");
            final EnumOptionData accountType = AccountingEnumerations.gLAccountType(accountTypeId);
            final BigDecimal amount = rs.getBigDecimal("amount");
            final int entryTypeId = JdbcSupport.getInteger(rs, "entryType");
            final EnumOptionData entryType = AccountingEnumerations.journalEntryType(entryTypeId);

            return new JournalEntryData(id, officeId, null, null, glAccountId, null, accountType, null, entryType, amount, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null, null);
        }
    }

}
