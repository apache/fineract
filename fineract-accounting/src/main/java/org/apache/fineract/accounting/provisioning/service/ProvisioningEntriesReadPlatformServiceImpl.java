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

import static com.querydsl.sql.DatePart.day;
import static com.querydsl.sql.SQLExpressions.datediff;
import static com.querydsl.sql.SQLExpressions.select;
import static org.apache.fineract.query.QMClient.mClient;
import static org.apache.fineract.query.QMGroup.mGroup;
import static org.apache.fineract.query.QMLoan.mLoan;
import static org.apache.fineract.query.QMLoanRepaymentSchedule.mLoanRepaymentSchedule;
import static org.apache.fineract.query.QMLoanproductProvisioningMapping.mLoanproductProvisioningMapping;
import static org.apache.fineract.query.QMProvisioningCriteriaDefinition.mProvisioningCriteriaDefinition;
import static org.apache.fineract.query.QMProvisioningHistory.mProvisioningHistory;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.provisioning.data.LoanProductProvisioningEntryData;
import org.apache.fineract.accounting.provisioning.data.ProvisioningEntryData;
import org.apache.fineract.accounting.provisioning.domain.LoanProductProvisioningDataProjection;
import org.apache.fineract.accounting.provisioning.domain.LoanProductProvisioningEntryProjection;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.query.QAccGlAccount;
import org.apache.fineract.query.QMAppuser;
import org.apache.fineract.query.QMLoanRepaymentSchedule;
import org.apache.fineract.query.QMLoanproductProvisioningEntry;
import org.apache.fineract.query.QMOffice;
import org.apache.fineract.query.QMProductLoan;
import org.apache.fineract.query.QMProvisionCategory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
@Service
@ConditionalOnMissingBean(value = ProvisioningEntriesReadPlatformService.class, ignored = ProvisioningEntriesReadPlatformServiceImpl.class)
public class ProvisioningEntriesReadPlatformServiceImpl implements ProvisioningEntriesReadPlatformService {

    protected final JdbcTemplate jdbcTemplate;

    protected final PaginationHelper loanProductProvisioningEntryDataPaginationHelper;
    protected final DatabaseSpecificSQLGenerator sqlGenerator;
    protected final SQLQueryFactory queryFactory;

    @Override
    public Collection<LoanProductProvisioningEntryData> retrieveLoanProductsProvisioningData(final LocalDate date) {
        final QMLoanRepaymentSchedule unpaidSchedule = new QMLoanRepaymentSchedule("unpaidSchedule");
        return loanProductProvisioningQuery(date)
                .where(mLoan.loanStatusId.eq(300), mLoanRepaymentSchedule.duedate.eq(select(unpaidSchedule.duedate.min())
                        .from(unpaidSchedule).where(unpaidSchedule.loanId.eq(mLoan.id), unpaidSchedule.completedDerived.isFalse())))
                .fetch();
    }

    private SQLQuery<LoanProductProvisioningEntryData> loanProductProvisioningQuery(final LocalDate date) {
        final NumberExpression<Long> officeId = new CaseBuilder().when(mLoan.loanTypeEnum.eq(1)).then(mClient.officeId)
                .otherwise(mGroup.officeId);
        final NumberExpression<Long> overdueDays = Expressions.numberTemplate(Long.class, "greatest({0}, 0)",
                datediff(day, mLoanRepaymentSchedule.duedate, Date.valueOf(date)));
        return queryFactory.select(new LoanProductProvisioningDataProjection(officeId, overdueDays)).from(mLoanRepaymentSchedule)
                .leftJoin(mLoan).on(mLoanRepaymentSchedule.loanId.eq(mLoan.id)).join(mLoanproductProvisioningMapping)
                .on(mLoanproductProvisioningMapping.productId.eq(mLoan.productId)).join(mProvisioningCriteriaDefinition)
                .on(mProvisioningCriteriaDefinition.criteriaId.eq(mLoanproductProvisioningMapping.criteriaId),
                        mProvisioningCriteriaDefinition.minAge.loe(overdueDays), overdueDays.loe(mProvisioningCriteriaDefinition.maxAge),
                        mProvisioningCriteriaDefinition.criteriaId.isNotNull())
                .leftJoin(mClient).on(mClient.id.eq(mLoan.clientId)).leftJoin(mGroup).on(mGroup.id.eq(mLoan.groupId));
    }

    @Override
    public ProvisioningEntryData retrieveProvisioningEntryData(Long entryId) {
        ProvisioningEntryDataMapperWithSumReserved mapper1 = new ProvisioningEntryDataMapperWithSumReserved();
        // Programmatic query, disable sonar
        final String sql = "select" + mapper1.getSchema() + " where entry.id = ? group by entry.id, created.username, modified.username";
        return this.jdbcTemplate.queryForObject(sql, mapper1, entryId);// NOSONAR
    }

    private static final class ProvisioningEntryDataMapper implements RowMapper<ProvisioningEntryData> {

        private static final String PROVISIONING_ENTRY_SCHEMA = """
                 entry.id, entry.journal_entry_created, entry.createdby_id, entry.created_date, created.username as createduser,
                entry.lastmodifiedby_id, modified.username as modifieduser, entry.lastmodified_date
                from m_provisioning_history entry left JOIN m_appuser created ON created.id = entry.createdby_id
                left JOIN m_appuser modified ON modified.id = entry.lastmodifiedby_id\s""";

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
            LocalDate createdLocalDate = createdDate != null ? createdDate.toLocalDate() : null;
            return new ProvisioningEntryData().setId(id).setJournalEntry(journalEntry).setCreatedById(createdById)
                    .setCreatedUser(createdUser).setCreatedDate(createdLocalDate).setModifiedById(modifiedById).setModifiedUser(modifieUser)
                    .setReservedAmount(totalReservedAmount);
        }

        private String getSchema() {
            return PROVISIONING_ENTRY_SCHEMA;
        }

    }

    private static final class LoanProductProvisioningEntryRowMapper implements RowMapper<LoanProductProvisioningEntryData> {

        private static final String LOAN_PRODUCT_PROVISIONING_ENTRY_SCHEMA = """
                 entry.id, entry.history_id as historyId, office_id, entry.criteria_id as criteriaid, office.name as officename, product.name as productname, entry.product_id,
                category_id, category.category_name, liability.id as liabilityid, liability.gl_code as liabilitycode, liability.name as liabilityname,
                expense.id as expenseid, expense.gl_code as expensecode, expense.name as expensename, entry.currency_code, entry.overdue_in_days, entry.reseve_amount from m_loanproduct_provisioning_entry entry
                left join m_office office ON office.id = entry.office_id
                left join m_product_loan product ON product.id = entry.product_id
                left join m_provision_category category ON category.id = entry.category_id
                left join acc_gl_account liability ON liability.id = entry.liability_account
                left join acc_gl_account expense ON expense.id = entry.expense_account\s""";

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
            return new LoanProductProvisioningEntryData().setHistoryId(historyId).setOfficeId(officeId).setOfficeName(officeName)
                    .setCurrencyCode(currentcyCode).setProductId(productId).setProductName(productName).setCategoryId(categoryId)
                    .setCategoryName(categoryName).setOverdueInDays(overdueDays).setAmountreserved(amountreserved)
                    .setLiablityAccount(liabilityAccountCode).setLiabilityAccountCode(liabilityAccountglCode)
                    .setLiabilityAccountName(liabilityAccountName).setExpenseAccount(expenseAccountCode)
                    .setExpenseAccountCode(expenseAccountglCode).setExpenseAccountName(expenseAccountName).setCriteriaId(criteriaId);

        }

        private String getSchema() {
            return LOAN_PRODUCT_PROVISIONING_ENTRY_SCHEMA;
        }
    }

    private static final class ProvisioningEntryDataMapperWithSumReserved implements RowMapper<ProvisioningEntryData> {

        private static final String PROVISIONING_ENTRY_SUM_RESERVED_SCHEMA = """
                 entry.id, journal_entry_created, createdby_id, created_date, created.username as createduser,
                lastmodifiedby_id, modified.username as modifieduser, lastmodified_date, SUM(reserved.reseve_amount) as totalreserved
                from m_provisioning_history entry
                LEFT JOIN m_loanproduct_provisioning_entry reserved on entry.id = reserved.history_id
                left JOIN m_appuser created ON created.id = entry.createdby_id
                left JOIN m_appuser modified ON modified.id = entry.lastmodifiedby_id\s""";

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
            LocalDate createdLocalDate = createdDate != null ? createdDate.toLocalDate() : null;
            return new ProvisioningEntryData().setId(id).setJournalEntry(journalEntry).setCreatedById(createdById)
                    .setCreatedUser(createdUser).setCreatedDate(createdLocalDate).setModifiedById(modifiedById).setModifiedUser(modifieUser)
                    .setReservedAmount(totalReservedAmount);
        }

        private String getSchema() {
            return PROVISIONING_ENTRY_SUM_RESERVED_SCHEMA;
        }

    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProvisioningEntryData> retrieveAllProvisioningEntries(Integer offset, Integer limit) {
        final QMAppuser created = new QMAppuser("created");
        final QMAppuser modified = new QMAppuser("modified");

        final SQLQuery<Tuple> query = queryFactory
                .select(mProvisioningHistory.id, mProvisioningHistory.journalEntryCreated, mProvisioningHistory.createdbyId,
                        mProvisioningHistory.createdDate, created.username, mProvisioningHistory.lastmodifiedbyId, modified.username)
                .from(mProvisioningHistory).leftJoin(created).on(created.id.eq(mProvisioningHistory.createdbyId)).leftJoin(modified)
                .on(modified.id.eq(mProvisioningHistory.lastmodifiedbyId)).orderBy(mProvisioningHistory.createdDate.asc());
        // QueryDSL rejects limit <= 0 and offset < 0; limit=0 used to return an empty page, so keep that
        final boolean emptyPage = limit != null && limit == 0;
        if (limit != null && limit > 0) {
            query.limit(limit);
        }
        if (offset != null && offset > 0) {
            query.offset(offset);
        }

        final List<ProvisioningEntryData> items = emptyPage ? List.of() : query.fetch().stream().map(row -> {
            final java.sql.Date createdDate = row.get(mProvisioningHistory.createdDate);
            return new ProvisioningEntryData().setId(row.get(mProvisioningHistory.id))
                    .setJournalEntry(Boolean.TRUE.equals(row.get(mProvisioningHistory.journalEntryCreated)))
                    .setCreatedById(Objects.requireNonNullElse(row.get(mProvisioningHistory.createdbyId), 0L))
                    .setCreatedUser(row.get(created.username)).setCreatedDate(createdDate != null ? createdDate.toLocalDate() : null)
                    .setModifiedById(Objects.requireNonNullElse(row.get(mProvisioningHistory.lastmodifiedbyId), 0L))
                    .setModifiedUser(row.get(modified.username));
        }).toList();

        final Long total = queryFactory.select(Wildcard.count).from(mProvisioningHistory).fetchOne();
        return new Page<>(items, total == null ? 0 : total.intValue());
    }

    @Override
    public ProvisioningEntryData retrieveProvisioningEntryData(String date) {
        ProvisioningEntryDataMapper mapper1 = new ProvisioningEntryDataMapper();
        date = date + "%";
        final String sql1 = "select " + mapper1.getSchema() + " where entry.created_date like ? ";
        ProvisioningEntryData data = null;
        try {
            data = this.jdbcTemplate.queryForObject(sql1, mapper1, date); // NOSONAR
        } catch (EmptyResultDataAccessException e) {
            log.error("Problem occurred in retrieveProvisioningEntryData function", e);
        }

        return data;
    }

    @Override
    public ProvisioningEntryData retrieveProvisioningEntryDataByCriteriaId(Long criteriaId) {
        ProvisioningEntryData data = null;
        QMLoanproductProvisioningEntry entry = new QMLoanproductProvisioningEntry("entry");
        QMOffice office = new QMOffice("office");
        QMProductLoan product = new QMProductLoan("product");
        QMProvisionCategory category = new QMProvisionCategory("category");
        QAccGlAccount liability = new QAccGlAccount("liability");
        QAccGlAccount expense = new QAccGlAccount("expense");

        List<LoanProductProvisioningEntryData> entries = this.queryFactory
                .select(new LoanProductProvisioningEntryProjection(entry, office, product, category, liability, expense)).from(entry)
                .leftJoin(office).on(office.id.eq(entry.officeId)).leftJoin(product).on(product.id.eq(entry.productId)).leftJoin(category)
                .on(category.id.eq(entry.categoryId)).leftJoin(liability).on(liability.id.eq(entry.liabilityAccount)).leftJoin(expense)
                .on(expense.id.eq(entry.expenseAccount)).where(entry.criteriaId.eq(criteriaId)).fetch();

        if (entries != null && !entries.isEmpty()) {
            Long entryId = entries.get(0).getHistoryId();
            ProvisioningEntryDataMapper mapper1 = new ProvisioningEntryDataMapper();
            final String sql1 = "select " + mapper1.getSchema() + " where entry.id = ?";
            data = this.jdbcTemplate.queryForObject(sql1, mapper1, entryId); // NOSONAR
            data.setProvisioningEntries(entries);
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

        private static final String PROVISIONING_ENTRY_ID_DATE_SCHEMA = """
                select history1.id, history1.created_date from m_provisioning_history history1
                where history1.created_date = (select max(history2.created_date) from m_provisioning_history history2
                where history2.journal_entry_created='1')\s""";

        @Override
        public ProvisioningEntryData mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long id = rs.getLong("id");
            Date createdDate = rs.getDate("created_date");
            Long createdBy = null;
            String createdName = null;
            Long modifiedBy = null;
            String modifiedName = null;
            BigDecimal totalReservedAmount = null;
            LocalDate createdLocalDate = createdDate != null ? createdDate.toLocalDate() : null;
            return new ProvisioningEntryData().setId(id).setJournalEntry(Boolean.TRUE).setCreatedById(createdBy).setCreatedUser(createdName)
                    .setCreatedDate(createdLocalDate).setModifiedById(modifiedBy).setModifiedUser(modifiedName)
                    .setReservedAmount(totalReservedAmount);
        }

        private String schema() {
            return PROVISIONING_ENTRY_ID_DATE_SCHEMA;
        }
    }

    @Override
    public Page<LoanProductProvisioningEntryData> retrieveProvisioningEntries(SearchParameters searchParams) {
        LoanProductProvisioningEntryRowMapper mapper = new LoanProductProvisioningEntryRowMapper();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append(mapper.getSchema());
        String whereClose = " where ";
        List<Object> items = new ArrayList<>();

        if (searchParams.hasProvisioningEntryId()) {
            sqlBuilder.append(whereClose + " entry.history_id = ?");
            items.add(searchParams.getProvisioningEntryId());
            whereClose = " and ";
        }

        if (searchParams.hasOfficeId()) {
            sqlBuilder.append(whereClose + " entry.office_id = ?");
            items.add(searchParams.getOfficeId());
            whereClose = " and ";
        }

        if (searchParams.hasProductId()) {
            sqlBuilder.append(whereClose + " entry.product_id = ?");
            items.add(searchParams.getProductId());
            whereClose = " and ";
        }

        if (searchParams.hasCategoryId()) {
            sqlBuilder.append(whereClose + " entry.category_id = ?");
            items.add(searchParams.getCategoryId());
        }
        sqlBuilder.append(" order by entry.id");

        if (searchParams.hasLimit()) {
            sqlBuilder.append(" limit ").append(searchParams.getLimit());
            if (searchParams.hasOffset()) {
                sqlBuilder.append(" offset ").append(searchParams.getOffset());
            }
        }
        Object[] whereClauseItemsitems = items.toArray();
        return this.loanProductProvisioningEntryDataPaginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(),
                whereClauseItemsitems, mapper);
    }

}
