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
package org.apache.fineract.accounting.closure.service;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.fineract.accounting.closure.data.IncomeAndExpenseJournalEntryData;
import org.apache.fineract.accounting.glaccount.domain.GLAccountType;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;



@Service
public class IncomeAndExpenseReadPlatformServiceImpl implements IncomeAndExpenseReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    public IncomeAndExpenseReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    private static final class FinancialEndOfYearMapper implements RowMapper<IncomeAndExpenseJournalEntryData> {

        public String schema(){
            return  " je.id,je.account_id,je.reversed,je.office_id,je.is_running_balance_calculated,je.entry_date,je.type_enum,je.amount," +
                    " je.organization_running_balance,je.office_running_balance,je.type_enum,je.description,ac.classification_enum as income_or_expense_account," +
                    " ac.name as glAccountName" +
                    " from acc_gl_account as ac" +
                    " inner join ( select * from (" +
                    " select * from acc_gl_journal_entry " +
                    " where office_id = ? and reversed = 0 and currency_code = ? and entry_date <= ? " +
                    " order by entry_date desc,created_date desc,id desc ) t group by t.account_id )" +
                    " as je on je.account_id = ac.id " +
                    " where  ac.classification_enum IN (?,?) group by je.account_id, je.office_id " +
                    " order by entry_date,created_date";
        }
        @Override
        public IncomeAndExpenseJournalEntryData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long accountId = rs.getLong("account_id");
            final Long officeId = rs.getLong("office_id");
            final LocalDate entryDate = JdbcSupport.getLocalDate(rs, "entry_date");
            final Boolean reversed = rs.getBoolean("reversed");
            final Boolean isRunningBalanceCalculated = rs.getBoolean("is_running_balance_calculated");
            final String comments = rs.getString("description");
            final BigDecimal officeRunningBalance = rs.getBigDecimal("office_running_balance");
            final BigDecimal organizationRunningBalance = rs.getBigDecimal("organization_running_balance");
            final int accountTypeId = JdbcSupport.getInteger(rs, "income_or_expense_account");
            final int entryTypeId = JdbcSupport.getInteger(rs, "type_enum");
            final String glAccountName = rs.getString("glAccountName");

            return new IncomeAndExpenseJournalEntryData(id,accountId,officeId,entryDate,reversed,isRunningBalanceCalculated,comments,
                    officeRunningBalance,organizationRunningBalance,accountTypeId,entryTypeId,glAccountName,null);
        }
    }

    @Override
    public List<IncomeAndExpenseJournalEntryData> retrieveAllIncomeAndExpenseJournalEntryData(final Long officeId, final LocalDate date,
                                                                                              final String currencyCode) {
        final FinancialEndOfYearMapper rm = new FinancialEndOfYearMapper();

        final String sql = "select " + rm.schema() ;
        return this.jdbcTemplate.query(sql,rm, new Object[] {officeId,currencyCode,formatter.print(date),GLAccountType.INCOME.getValue(),GLAccountType.EXPENSE.getValue()});
    }
}
