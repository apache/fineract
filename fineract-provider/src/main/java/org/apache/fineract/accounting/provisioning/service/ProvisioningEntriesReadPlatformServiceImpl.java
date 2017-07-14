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
package org.apache.fineract.accounting.provisioning.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fineract.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ProvisioningEntriesReadPlatformServiceImpl implements ProvisioningEntriesReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    private final PaginationHelper<LoanProductProvisioningEntryData> loanProductProvisioningEntryDataPaginationHelper = new PaginationHelper<>();
    private final PaginationHelper<ProvisioningEntryData> provisioningEntryDataPaginationHelper = new PaginationHelper<>();

    @Autowired
    public ProvisioningEntriesReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Collection<LoanProductProvisioningEntryData> retrieveLoanProductsProvisioningData(Date date) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
        formattedDate = "'" + formattedDate + "'";
        LoanProductProvisioningEntryMapper mapper = new LoanProductProvisioningEntryMapper(formattedDate);
        final String sql = mapper.schema();
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});
    }

    private static final class LoanProductProvisioningEntryMapper implements RowMapper<LoanProductProvisioningEntryData> {

        private final StringBuilder sqlQuery;

        protected LoanProductProvisioningEntryMapper(String formattedDate) {
            sqlQuery = new StringBuilder()
                    .append("select if(loan.loan_type_enum=1, mclient.office_id, mgroup.office_id) as office_id, loan.loan_type_enum, pcd.criteria_id as criteriaid, loan.product_id,loan.currency_code,")
                    .append("GREATEST(datediff(")
                    .append(formattedDate)
                    .append(",sch.duedate),0) as numberofdaysoverdue,sch.duedate, pcd.category_id, pcd.provision_percentage,")
                    .append("loan.total_outstanding_derived as outstandingbalance, pcd.liability_account, pcd.expense_account from m_loan_repayment_schedule sch")
                    .append(" LEFT JOIN m_loan loan on sch.loan_id = loan.id")
                    .append(" JOIN m_loanproduct_provisioning_mapping lpm on lpm.product_id = loan.product_id")
                    .append(" JOIN m_provisioning_criteria_definition pcd on pcd.criteria_id = lpm.criteria_id and ")
                    .append("(pcd.min_age <= GREATEST(datediff(").append(formattedDate).append(",sch.duedate),0) and ")
                    .append("GREATEST(datediff(").append(formattedDate).append(",sch.duedate),0) <= pcd.max_age) and ")
                    .append("pcd.criteria_id is not null ").append("LEFT JOIN m_client mclient ON mclient.id = loan.client_id ")
                    .append("LEFT JOIN m_group mgroup ON mgroup.id = loan.group_id ")
                    .append("where loan.loan_status_id=300 and sch.duedate = ")
                    .append("(select MIN(sch1.duedate) from m_loan_repayment_schedule sch1 where sch1.loan_id=loan.id and sch1.completed_derived=false)");
        }

        @Override
        @SuppressWarnings("unused")
        public LoanProductProvisioningEntryData mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long officeId = rs.getLong("office_id");
            Long productId = rs.getLong("product_id");
            String currentcyCode = rs.getString("currency_code");
            Long overdueDays = rs.getLong("numberofdaysoverdue");
            Long categoryId = rs.getLong("category_id");
            BigDecimal percentage = rs.getBigDecimal("provision_percentage");
            BigDecimal outstandingBalance = rs.getBigDecimal("outstandingbalance");
            Long liabilityAccountCode = rs.getLong("liability_account");
            Long expenseAccountCode = rs.getLong("expense_account");
            Long criteriaId = rs.getLong("criteriaid");
            Long historyId = null;

            return new LoanProductProvisioningEntryData(historyId, officeId, currentcyCode, productId, categoryId, overdueDays, percentage,
                    outstandingBalance, liabilityAccountCode, expenseAccountCode, criteriaId);
        }

        public String schema() {
            return sqlQuery.toString();
        }
    }

    @Override
    public ProvisioningEntryData retrieveProvisioningEntryData(Long entryId) {
        ProvisioningEntryDataMapperWithSumReserved mapper1 = new ProvisioningEntryDataMapperWithSumReserved();
        final String sql1 = "select" + mapper1.getSchema() + " where entry.id = ?";
        ProvisioningEntryData data = this.jdbcTemplate.queryForObject(sql1, mapper1, new Object[] { entryId });
        return data;
    }

    private static final class ProvisioningEntryDataMapper implements RowMapper<ProvisioningEntryData> {

        private final StringBuilder sqlQuery = new StringBuilder()
                .append(" entry.id, entry.journal_entry_created, entry.createdby_id, entry.created_date, created.username as createduser,")
                .append("entry.lastmodifiedby_id, modified.username as modifieduser, entry.lastmodified_date ")
                .append("from m_provisioning_history entry ").append("left JOIN m_appuser created ON created.id = entry.createdby_id ")
                .append("left JOIN m_appuser modified ON modified.id = entry.lastmodifiedby_id ");

        @Override
        @SuppressWarnings("unused")
        public ProvisioningEntryData mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            Boolean journalEntry = rs.getBoolean("journal_entry_created");
            Long createdById = rs.getLong("createdby_id");
            String createdUser = rs.getString("createduser");
            Date createdDate = rs.getDate("created_date");
            Long modifiedById = rs.getLong("lastmodifiedby_id");
            String modifieUser = rs.getString("modifieduser");
            BigDecimal totalReservedAmount = null;
            return new ProvisioningEntryData(id, journalEntry, createdById, createdUser, createdDate, modifiedById, modifieUser,
                    totalReservedAmount);
        }

        public String getSchema() {
            return sqlQuery.toString();
        }

    }

    private static final class LoanProductProvisioningEntryRowMapper implements RowMapper<LoanProductProvisioningEntryData> {

        private final StringBuilder sqlQuery = new StringBuilder()
                .append(" entry.id, entry.history_id as historyId, office_id, entry.criteria_id as criteriaid, office.name as officename, product.name as productname, entry.product_id, ")
                .append("category_id, category.category_name, liability.id as liabilityid, liability.gl_code as liabilitycode, liability.name as liabilityname, ")
                .append("expense.id as expenseid, expense.gl_code as expensecode, expense.name as expensename, entry.currency_code, entry.overdue_in_days, entry.reseve_amount from m_loanproduct_provisioning_entry entry ")
                .append("left join m_office office ON office.id = entry.office_id ")
                .append("left join m_product_loan product ON product.id = entry.product_id ")
                .append("left join m_provision_category category ON category.id = entry.category_id ")
                .append("left join acc_gl_account liability ON liability.id = entry.liability_account ")
                .append("left join acc_gl_account expense ON expense.id = entry.expense_account ");

        @Override
        @SuppressWarnings("unused")
        public LoanProductProvisioningEntryData mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long historyId = rs.getLong("historyId");
            Long officeId = rs.getLong("office_id");
            String officeName = rs.getString("officename");
            Long productId = rs.getLong("product_id");
            String productName = rs.getString("productname");
            String currentcyCode = rs.getString("currency_code");
            Long overdueDays = rs.getLong("overdue_in_days");
            Long categoryId = rs.getLong("category_id");
            String categoryName = rs.getString("category_name");
            BigDecimal amountreserved = rs.getBigDecimal("reseve_amount");
            Long liabilityAccountCode = rs.getLong("liabilityid");
            String liabilityAccountglCode = rs.getString("liabilitycode");
            String expenseAccountglCode = rs.getString("expensecode");
            Long expenseAccountCode = rs.getLong("expenseid");
            Long criteriaId = rs.getLong("criteriaid");
            String liabilityAccountName = rs.getString("liabilityname");
            String expenseAccountName = rs.getString("expensename");
            return new LoanProductProvisioningEntryData(historyId, officeId, officeName, currentcyCode, productId, productName, categoryId,
                    categoryName, overdueDays, amountreserved, liabilityAccountCode, liabilityAccountglCode, liabilityAccountName,
                    expenseAccountCode, expenseAccountglCode, expenseAccountName, criteriaId);
        }

        public String getSchema() {
            return sqlQuery.toString();
        }
    }

    private static final class ProvisioningEntryDataMapperWithSumReserved implements RowMapper<ProvisioningEntryData> {

        private final StringBuilder sqlQuery = new StringBuilder()
                .append(" entry.id, journal_entry_created, createdby_id, created_date, created.username as createduser,")
                .append("lastmodifiedby_id, modified.username as modifieduser, lastmodified_date, SUM(reserved.reseve_amount) as totalreserved ")
                .append("from m_provisioning_history entry ")
                .append("JOIN m_loanproduct_provisioning_entry reserved on entry.id = reserved.history_id ")
                .append("left JOIN m_appuser created ON created.id = entry.createdby_id ")
                .append("left JOIN m_appuser modified ON modified.id = entry.lastmodifiedby_id ");

        @Override
        @SuppressWarnings("unused")
        public ProvisioningEntryData mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            Boolean journalEntry = rs.getBoolean("journal_entry_created");
            Long createdById = rs.getLong("createdby_id");
            String createdUser = rs.getString("createduser");
            Date createdDate = rs.getDate("created_date");
            Long modifiedById = rs.getLong("lastmodifiedby_id");
            String modifieUser = rs.getString("modifieduser");
            BigDecimal totalReservedAmount = rs.getBigDecimal("totalreserved");
            return new ProvisioningEntryData(id, journalEntry, createdById, createdUser, createdDate, modifiedById, modifieUser,
                    totalReservedAmount);
        }

        public String getSchema() {
            return sqlQuery.toString();
        }

    }

    @Override
    public Page<ProvisioningEntryData> retrieveAllProvisioningEntries(Integer offset, Integer limit) {
        ProvisioningEntryDataMapper mapper = new ProvisioningEntryDataMapper();
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(mapper.getSchema());
        sqlBuilder.append(" order by entry.created_date");
        if(limit != null ) {
            sqlBuilder.append(" limit ").append(limit);    
        }
        if(offset != null) {
            sqlBuilder.append(" offset ").append(offset);    
        }
        
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        Object[] whereClauseItemsitems = new Object[] {};
        return this.provisioningEntryDataPaginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                whereClauseItemsitems, mapper);
    }

    @Override
    public ProvisioningEntryData retrieveProvisioningEntryData(String date) {
        ProvisioningEntryDataMapper mapper1 = new ProvisioningEntryDataMapper();
        date = date+"%";
        final String sql1 = "select " + mapper1.getSchema() + " where entry.created_date like ? ";
        ProvisioningEntryData data = null;
        try {
            data = this.jdbcTemplate.queryForObject(sql1, mapper1, new Object[] {date});
        } catch (EmptyResultDataAccessException e) {

        }

        return data;
    }

    @Override
    public ProvisioningEntryData retrieveProvisioningEntryDataByCriteriaId(Long criteriaId) {
        ProvisioningEntryData data = null;
        LoanProductProvisioningEntryRowMapper mapper = new LoanProductProvisioningEntryRowMapper();
        final String sql = "select " + mapper.getSchema() + " where entry.criteria_id = ?";
        Collection<LoanProductProvisioningEntryData> entries = this.jdbcTemplate.query(sql, mapper, new Object[] { criteriaId });
        if (entries != null && entries.size() > 0) {
            Long entryId = ((LoanProductProvisioningEntryData) entries.toArray()[0]).getHistoryId();
            ProvisioningEntryDataMapper mapper1 = new ProvisioningEntryDataMapper();
            final String sql1 = "select " + mapper1.getSchema() + " where entry.id = ?";
            data = this.jdbcTemplate.queryForObject(sql1, mapper1, new Object[] { entryId });
            data.setEntries(entries);
        }
        return data;
    }

    @Override
    public ProvisioningEntryData retrieveExistingProvisioningIdDateWithJournals() {
        ProvisioningEntryData data = null;
        ProvisioningEntryIdDateRowMapper mapper = new ProvisioningEntryIdDateRowMapper();
        try {
            data = this.jdbcTemplate.queryForObject(mapper.schema(), mapper, new Object[] {});
        } catch (EmptyResultDataAccessException e) {
            data = null;
        }
        return data;
    }

    private static final class ProvisioningEntryIdDateRowMapper implements RowMapper<ProvisioningEntryData> {

        StringBuffer buff = new StringBuffer().append("select history1.id, history1.created_date from m_provisioning_history history1 ")
                .append("where history1.created_date = (select max(history2.created_date) from m_provisioning_history history2 ")
                .append("where history2.journal_entry_created='1')");

        @Override
        @SuppressWarnings("unused")
        public ProvisioningEntryData mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> map = new HashMap<>();
            Long id = rs.getLong("id");
            Date createdDate = rs.getDate("created_date");
            Long createdBy = null;
            String createdName = null;
            Long modifiedBy = null;
            String modifiedName = null;
            BigDecimal totalReservedAmount = null;
            return new ProvisioningEntryData(id, Boolean.TRUE, createdBy, createdName, createdDate, modifiedBy, modifiedName,
                    totalReservedAmount);
        }

        public String schema() {
            return buff.toString();
        }
    }

    @Override
    public Page<LoanProductProvisioningEntryData> retrieveProvisioningEntries(SearchParameters searchParams) {
        LoanProductProvisioningEntryRowMapper mapper = new LoanProductProvisioningEntryRowMapper();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(mapper.getSchema());
        String whereClose = " where ";
        List<Object> items = new ArrayList<>();

        if (searchParams.isProvisioningEntryIdPassed()) {
            sqlBuilder.append(whereClose + " entry.history_id = ?");
            items.add(searchParams.getProvisioningEntryId());
            whereClose = " and ";
        }

        if (searchParams.isOfficeIdPassed()) {
            sqlBuilder.append(whereClose + " entry.office_id = ?");
            items.add(searchParams.getOfficeId());
            whereClose = " and ";
        }

        if (searchParams.isProductIdPassed()) {
            sqlBuilder.append(whereClose + " entry.product_id = ?");
            items.add(searchParams.getProductId());
            whereClose = " and ";
        }

        if (searchParams.isCategoryIdPassed()) {
            sqlBuilder.append(whereClose + " entry.category_id = ?");
            items.add(searchParams.getCategoryId());
        }
        sqlBuilder.append(" order by entry.id");

        if (searchParams.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParams.getLimit());
            if (searchParams.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParams.getOffset());
            }
        }
        Object[] whereClauseItemsitems = items.toArray();
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.loanProductProvisioningEntryDataPaginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(),
                whereClauseItemsitems, mapper);
    }

}