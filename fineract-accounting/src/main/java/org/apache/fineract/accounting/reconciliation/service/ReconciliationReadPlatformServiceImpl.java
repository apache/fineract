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
package org.apache.fineract.accounting.reconciliation.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.reconciliation.data.BankStatementImportData;
import org.apache.fineract.accounting.reconciliation.data.BankStatementTransactionData;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationAdjustmentData;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationMatchData;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationRuleData;
import org.apache.fineract.accounting.reconciliation.data.ReconciliationSummaryData;
import org.apache.fineract.accounting.reconciliation.data.UnreconciledGLEntryData;
import org.apache.fineract.accounting.reconciliation.domain.BankStatementImport;
import org.apache.fineract.accounting.reconciliation.domain.BankStatementImportRepository;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationRule;
import org.apache.fineract.accounting.reconciliation.domain.ReconciliationRuleRepository;
import org.apache.fineract.accounting.reconciliation.exception.BankStatementImportNotFoundException;
import org.apache.fineract.accounting.reconciliation.exception.ReconciliationRuleNotFoundException;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReconciliationReadPlatformServiceImpl implements ReconciliationReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final BankStatementImportRepository bankStatementImportRepository;
    private final ReconciliationRuleRepository reconciliationRuleRepository;
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;

    private static final class BankStatementImportMapper implements RowMapper<BankStatementImportData> {

        public String schema() {
            return " bsi.id as id, bsi.gl_account_id as glAccountId, gl.name as glAccountName, gl.gl_code as glAccountCode, "
                    + " bsi.file_name as fileName, bsi.import_date as importDate, bsi.from_date as fromDate, "
                    + " bsi.to_date as toDate, bsi.status as status, bsi.total_transactions as totalTransactions, "
                    + " bsi.matched_count as matchedCount, bsi.unmatched_count as unmatchedCount, "
                    + " bsi.total_debits as totalDebits, bsi.total_credits as totalCredits, "
                    + " bsi.opening_balance as openingBalance, bsi.closing_balance as closingBalance, "
                    + " bsi.completed_date as completedDate, bsi.approved_date as approvedDate, "
                    + " bsi.approved_by_user_id as approvedByUserId, approver.username as approvedByUsername, "
                    + " bsi.created_by as createdBy, creator.username as createdByUsername, "
                    + " bsi.created_date as createdDate, bsi.last_modified_by as lastModifiedBy, "
                    + " modifier.username as lastModifiedByUsername, bsi.last_modified_date as lastModifiedDate "
                    + " FROM acc_bank_statement_import bsi "
                    + " LEFT JOIN acc_gl_account gl ON gl.id = bsi.gl_account_id "
                    + " LEFT JOIN m_appuser approver ON approver.id = bsi.approved_by_user_id "
                    + " LEFT JOIN m_appuser creator ON creator.id = bsi.created_by "
                    + " LEFT JOIN m_appuser modifier ON modifier.id = bsi.last_modified_by ";
        }

        @Override
        public BankStatementImportData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long glAccountId = rs.getLong("glAccountId");
            final String glAccountName = rs.getString("glAccountName");
            final String glAccountCode = rs.getString("glAccountCode");
            final String fileName = rs.getString("fileName");
            final LocalDate importDate = JdbcSupport.getLocalDate(rs, "importDate");
            final LocalDate fromDate = JdbcSupport.getLocalDate(rs, "fromDate");
            final LocalDate toDate = JdbcSupport.getLocalDate(rs, "toDate");
            final String status = rs.getString("status");
            final Integer totalTransactions = JdbcSupport.getInteger(rs, "totalTransactions");
            final Integer matchedCount = JdbcSupport.getInteger(rs, "matchedCount");
            final Integer unmatchedCount = JdbcSupport.getInteger(rs, "unmatchedCount");
            final BigDecimal totalDebits = rs.getBigDecimal("totalDebits");
            final BigDecimal totalCredits = rs.getBigDecimal("totalCredits");
            final BigDecimal openingBalance = rs.getBigDecimal("openingBalance");
            final BigDecimal closingBalance = rs.getBigDecimal("closingBalance");
            final LocalDate completedDate = JdbcSupport.getLocalDate(rs, "completedDate");
            final LocalDate approvedDate = JdbcSupport.getLocalDate(rs, "approvedDate");
            final Long approvedByUserId = JdbcSupport.getLong(rs, "approvedByUserId");
            final String approvedByUsername = rs.getString("approvedByUsername");
            final Long createdBy = JdbcSupport.getLong(rs, "createdBy");
            final String createdByUsername = rs.getString("createdByUsername");
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");
            final Long lastModifiedBy = JdbcSupport.getLong(rs, "lastModifiedBy");
            final String lastModifiedByUsername = rs.getString("lastModifiedByUsername");
            final LocalDate lastModifiedDate = JdbcSupport.getLocalDate(rs, "lastModifiedDate");

            return BankStatementImportData.instance(id, glAccountId, glAccountName, glAccountCode, fileName, importDate, fromDate,
                    toDate, status, totalTransactions, matchedCount, unmatchedCount, totalDebits, totalCredits, openingBalance,
                    closingBalance, completedDate, approvedDate, approvedByUserId, approvedByUsername, createdBy, createdByUsername,
                    createdDate, lastModifiedBy, lastModifiedByUsername, lastModifiedDate);
        }
    }

    private static final class BankStatementTransactionMapper implements RowMapper<BankStatementTransactionData> {

        public String schema() {
            return " bst.id as id, bst.import_id as importId, bst.transaction_date as transactionDate, "
                    + " bst.value_date as valueDate, bst.description as description, bst.reference_number as referenceNumber, "
                    + " bst.debit_amount as debitAmount, bst.credit_amount as creditAmount, bst.balance as balance, "
                    + " bst.is_matched as isMatched, bst.match_confidence as matchConfidence "
                    + " FROM acc_bank_statement_transaction bst ";
        }

        @Override
        public BankStatementTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long id = rs.getLong("id");
            final Long importId = rs.getLong("importId");
            final LocalDate transactionDate = JdbcSupport.getLocalDate(rs, "transactionDate");
            final LocalDate valueDate = JdbcSupport.getLocalDate(rs, "valueDate");
            final String description = rs.getString("description");
            final String referenceNumber = rs.getString("referenceNumber");
            final BigDecimal debitAmount = rs.getBigDecimal("debitAmount");
            final BigDecimal creditAmount = rs.getBigDecimal("creditAmount");
            final BigDecimal balance = rs.getBigDecimal("balance");
            final Boolean isMatched = rs.getBoolean("isMatched");
            final Integer matchConfidence = JdbcSupport.getInteger(rs, "matchConfidence");

            return BankStatementTransactionData.instance(id, importId, transactionDate, valueDate, description, referenceNumber,
                    debitAmount, creditAmount, balance, isMatched, matchConfidence);
        }
    }

    private static final class ReconciliationMatchMapper implements RowMapper<ReconciliationMatchData> {

        public String schema() {
            return " rm.id as id, rm.import_id as importId, rm.bank_transaction_id as bankTransactionId, "
                    + " rm.gl_entry_id as glEntryId, rm.match_type as matchType, rm.match_confidence as matchConfidence, "
                    + " rm.is_system_generated as isSystemGenerated, rm.created_by as createdBy, "
                    + " creator.username as createdByUsername, rm.created_date as createdDate, "
                    + " bst.transaction_date as bankTransactionDate, bst.description as bankDescription, "
                    + " bst.debit_amount as bankDebitAmount, bst.credit_amount as bankCreditAmount, "
                    + " je.entry_date as glEntryDate, je.description as glDescription, je.amount as glAmount, "
                    + " je.type_enum as glEntryType, je.transaction_id as glTransactionId "
                    + " FROM acc_reconciliation_match rm "
                    + " LEFT JOIN acc_bank_statement_transaction bst ON bst.id = rm.bank_transaction_id "
                    + " LEFT JOIN acc_gl_journal_entry je ON je.id = rm.gl_entry_id "
                    + " LEFT JOIN m_appuser creator ON creator.id = rm.created_by ";
        }

        @Override
        public ReconciliationMatchData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long importId = rs.getLong("importId");
            final Long bankTransactionId = rs.getLong("bankTransactionId");
            final Long glEntryId = rs.getLong("glEntryId");
            final String matchType = rs.getString("matchType");
            final Integer matchConfidence = JdbcSupport.getInteger(rs, "matchConfidence");
            final Boolean isSystemGenerated = rs.getBoolean("isSystemGenerated");
            final Long createdBy = JdbcSupport.getLong(rs, "createdBy");
            final String createdByUsername = rs.getString("createdByUsername");
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");
            final LocalDate bankTransactionDate = JdbcSupport.getLocalDate(rs, "bankTransactionDate");
            final String bankDescription = rs.getString("bankDescription");
            final BigDecimal bankDebitAmount = rs.getBigDecimal("bankDebitAmount");
            final BigDecimal bankCreditAmount = rs.getBigDecimal("bankCreditAmount");
            final LocalDate glEntryDate = JdbcSupport.getLocalDate(rs, "glEntryDate");
            final String glDescription = rs.getString("glDescription");
            final BigDecimal glAmount = rs.getBigDecimal("glAmount");
            final Integer glEntryType = JdbcSupport.getInteger(rs, "glEntryType");
            final String glTransactionId = rs.getString("glTransactionId");

            return ReconciliationMatchData.instance(id, importId, bankTransactionId, glEntryId, matchType, matchConfidence,
                    isSystemGenerated, createdBy, createdByUsername, createdDate, bankTransactionDate, bankDescription, bankDebitAmount,
                    bankCreditAmount, glEntryDate, glDescription, glAmount, glEntryType, glTransactionId);
        }
    }

    private static final class ReconciliationAdjustmentMapper implements RowMapper<ReconciliationAdjustmentData> {

        public String schema() {
            return " ra.id as id, ra.import_id as importId, ra.adjustment_date as adjustmentDate, "
                    + " ra.description as description, ra.debit_account_id as debitAccountId, "
                    + " debit_gl.name as debitAccountName, debit_gl.gl_code as debitAccountCode, "
                    + " ra.credit_account_id as creditAccountId, credit_gl.name as creditAccountName, "
                    + " credit_gl.gl_code as creditAccountCode, ra.amount as amount, ra.journal_entry_id as journalEntryId, "
                    + " ra.created_by as createdBy, creator.username as createdByUsername, ra.created_date as createdDate "
                    + " FROM acc_reconciliation_adjustment ra "
                    + " LEFT JOIN acc_gl_account debit_gl ON debit_gl.id = ra.debit_account_id "
                    + " LEFT JOIN acc_gl_account credit_gl ON credit_gl.id = ra.credit_account_id "
                    + " LEFT JOIN m_appuser creator ON creator.id = ra.created_by ";
        }

        @Override
        public ReconciliationAdjustmentData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long id = rs.getLong("id");
            final Long importId = rs.getLong("importId");
            final LocalDate adjustmentDate = JdbcSupport.getLocalDate(rs, "adjustmentDate");
            final String description = rs.getString("description");
            final Long debitAccountId = rs.getLong("debitAccountId");
            final String debitAccountName = rs.getString("debitAccountName");
            final String debitAccountCode = rs.getString("debitAccountCode");
            final Long creditAccountId = rs.getLong("creditAccountId");
            final String creditAccountName = rs.getString("creditAccountName");
            final String creditAccountCode = rs.getString("creditAccountCode");
            final BigDecimal amount = rs.getBigDecimal("amount");
            final Long journalEntryId = JdbcSupport.getLong(rs, "journalEntryId");
            final Long createdBy = JdbcSupport.getLong(rs, "createdBy");
            final String createdByUsername = rs.getString("createdByUsername");
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");

            return ReconciliationAdjustmentData.instance(id, importId, adjustmentDate, description, debitAccountId, debitAccountName,
                    debitAccountCode, creditAccountId, creditAccountName, creditAccountCode, amount, journalEntryId, createdBy,
                    createdByUsername, createdDate);
        }
    }

    private static final class UnreconciledGLEntryMapper implements RowMapper<UnreconciledGLEntryData> {

        public String schema() {
            return " je.id as id, je.entry_date as entryDate, je.transaction_id as transactionId, "
                    + " je.description as description, je.amount as amount, je.type_enum as entryType, "
                    + " je.ref_num as referenceNumber FROM acc_gl_journal_entry je "
                    + " WHERE je.account_id = ? AND je.entry_date BETWEEN ? AND ? "
                    + " AND je.reversed = false AND je.id NOT IN "
                    + " (SELECT rm.gl_entry_id FROM acc_reconciliation_match rm WHERE rm.gl_entry_id IS NOT NULL) "
                    + " ORDER BY je.entry_date, je.id ";
        }

        @Override
        public UnreconciledGLEntryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final LocalDate entryDate = JdbcSupport.getLocalDate(rs, "entryDate");
            final String transactionId = rs.getString("transactionId");
            final String description = rs.getString("description");
            final BigDecimal amount = rs.getBigDecimal("amount");
            final Integer entryType = JdbcSupport.getInteger(rs, "entryType");
            final String referenceNumber = rs.getString("referenceNumber");

            return UnreconciledGLEntryData.instance(id, entryDate, transactionId, description, amount, entryType, referenceNumber);
        }
    }

    private static final class ReconciliationRuleMapper implements RowMapper<ReconciliationRuleData> {

        public String schema() {
            return " rr.id as id, rr.gl_account_id as glAccountId, gl.name as glAccountName, gl.gl_code as glAccountCode, "
                    + " rr.rule_name as ruleName, rr.rule_type as ruleType, rr.match_field as matchField, "
                    + " rr.match_pattern as matchPattern, rr.date_tolerance_days as dateToleranceDays, "
                    + " rr.amount_tolerance as amountTolerance, rr.is_active as isActive, rr.priority as priority, "
                    + " rr.created_by as createdBy, creator.username as createdByUsername, rr.created_date as createdDate, "
                    + " rr.last_modified_by as lastModifiedBy, modifier.username as lastModifiedByUsername, "
                    + " rr.last_modified_date as lastModifiedDate " + " FROM acc_reconciliation_rule rr "
                    + " LEFT JOIN acc_gl_account gl ON gl.id = rr.gl_account_id "
                    + " LEFT JOIN m_appuser creator ON creator.id = rr.created_by "
                    + " LEFT JOIN m_appuser modifier ON modifier.id = rr.last_modified_by ";
        }

        @Override
        public ReconciliationRuleData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long glAccountId = rs.getLong("glAccountId");
            final String glAccountName = rs.getString("glAccountName");
            final String glAccountCode = rs.getString("glAccountCode");
            final String ruleName = rs.getString("ruleName");
            final String ruleType = rs.getString("ruleType");
            final String matchField = rs.getString("matchField");
            final String matchPattern = rs.getString("matchPattern");
            final Integer dateToleranceDays = JdbcSupport.getInteger(rs, "dateToleranceDays");
            final BigDecimal amountTolerance = rs.getBigDecimal("amountTolerance");
            final Boolean isActive = rs.getBoolean("isActive");
            final Integer priority = JdbcSupport.getInteger(rs, "priority");
            final Long createdBy = JdbcSupport.getLong(rs, "createdBy");
            final String createdByUsername = rs.getString("createdByUsername");
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");
            final Long lastModifiedBy = JdbcSupport.getLong(rs, "lastModifiedBy");
            final String lastModifiedByUsername = rs.getString("lastModifiedByUsername");
            final LocalDate lastModifiedDate = JdbcSupport.getLocalDate(rs, "lastModifiedDate");

            return ReconciliationRuleData.instance(id, glAccountId, glAccountName, glAccountCode, ruleName, ruleType, matchField,
                    matchPattern, dateToleranceDays, amountTolerance, isActive, priority, createdBy, createdByUsername, createdDate,
                    lastModifiedBy, lastModifiedByUsername, lastModifiedDate);
        }
    }

    @Override
    public Page<BankStatementImportData> retrieveAll(Long glAccountId, LocalDate fromDate, LocalDate toDate, String status,
            SearchParameters searchParameters) {
        final BankStatementImportMapper mapper = new BankStatementImportMapper();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("SELECT ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(" ");
        sqlBuilder.append(mapper.schema());

        final List<Object> paramList = new ArrayList<>();
        sqlBuilder.append(" WHERE 1=1 ");

        if (glAccountId != null) {
            sqlBuilder.append(" AND bsi.gl_account_id = ? ");
            paramList.add(glAccountId);
        }

        if (fromDate != null) {
            sqlBuilder.append(" AND bsi.from_date >= ? ");
            paramList.add(fromDate);
        }

        if (toDate != null) {
            sqlBuilder.append(" AND bsi.to_date <= ? ");
            paramList.add(toDate);
        }

        if (status != null) {
            sqlBuilder.append(" AND bsi.status = ? ");
            paramList.add(status);
        }

        sqlBuilder.append(" ORDER BY bsi.import_date DESC, bsi.id DESC ");

        if (searchParameters != null) {
            if (searchParameters.hasLimit()) {
                sqlBuilder.append(" ");
                sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
            }
        }

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), mapper);
    }

    @Override
    public BankStatementImportData retrieveOne(Long importId) {
        try {
            final BankStatementImportMapper mapper = new BankStatementImportMapper();
            final String sql = "SELECT " + mapper.schema() + " WHERE bsi.id = ?";
            return this.jdbcTemplate.queryForObject(sql, mapper, importId);
        } catch (final EmptyResultDataAccessException e) {
            throw new BankStatementImportNotFoundException(importId);
        }
    }

    @Override
    public List<BankStatementTransactionData> retrieveTransactions(Long importId) {
        final BankStatementTransactionMapper mapper = new BankStatementTransactionMapper();
        final String sql = mapper.schema() + " WHERE bst.import_id = ? ORDER BY bst.transaction_date, bst.id";
        return this.jdbcTemplate.query(sql, mapper, importId);
    }

    @Override
    public List<ReconciliationMatchData> retrieveMatches(Long importId) {
        final ReconciliationMatchMapper mapper = new ReconciliationMatchMapper();
        final String sql = mapper.schema() + " WHERE rm.import_id = ? ORDER BY rm.created_date, rm.id";
        return this.jdbcTemplate.query(sql, mapper, importId);
    }

    @Override
    public List<ReconciliationAdjustmentData> retrieveAdjustments(Long importId) {
        final ReconciliationAdjustmentMapper mapper = new ReconciliationAdjustmentMapper();
        final String sql = mapper.schema() + " WHERE ra.import_id = ? ORDER BY ra.adjustment_date, ra.id";
        return this.jdbcTemplate.query(sql, mapper, importId);
    }

    @Override
    public ReconciliationSummaryData retrieveSummary(Long importId) {
        final BankStatementImport importRecord = this.bankStatementImportRepository.findById(importId)
                .orElseThrow(() -> new BankStatementImportNotFoundException(importId));

        final String matchSql = "SELECT COUNT(*) FROM acc_reconciliation_match WHERE import_id = ?";
        final Integer totalMatches = this.jdbcTemplate.queryForObject(matchSql, Integer.class, importId);

        final String adjustmentSql = "SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM acc_reconciliation_adjustment WHERE import_id = ?";
        final Object[] adjustmentResult = this.jdbcTemplate.queryForObject(adjustmentSql, (rs, rowNum) -> {
            return new Object[] { rs.getInt(1), rs.getBigDecimal(2) };
        }, importId);

        final Integer totalAdjustments = (Integer) adjustmentResult[0];
        final BigDecimal totalAdjustmentAmount = (BigDecimal) adjustmentResult[1];

        return ReconciliationSummaryData.instance(importId, importRecord.getTotalTransactions(), importRecord.getMatchedCount(),
                importRecord.getUnmatchedCount(), totalMatches, totalAdjustments, totalAdjustmentAmount, importRecord.getTotalDebits(),
                importRecord.getTotalCredits(), importRecord.getOpeningBalance(), importRecord.getClosingBalance(),
                importRecord.getStatus());
    }

    @Override
    public List<UnreconciledGLEntryData> retrieveUnreconciledGLEntries(Long glAccountId, LocalDate fromDate, LocalDate toDate) {
        final UnreconciledGLEntryMapper mapper = new UnreconciledGLEntryMapper();
        final String sql = mapper.schema();
        return this.jdbcTemplate.query(sql, mapper, glAccountId, fromDate, toDate);
    }

    @Override
    public List<ReconciliationRuleData> retrieveAllRules(Long glAccountId) {
        final ReconciliationRuleMapper mapper = new ReconciliationRuleMapper();
        final String sql = mapper.schema() + " WHERE rr.gl_account_id = ? ORDER BY rr.priority, rr.id";
        return this.jdbcTemplate.query(sql, mapper, glAccountId);
    }

    @Override
    public ReconciliationRuleData retrieveRule(Long ruleId) {
        try {
            final ReconciliationRuleMapper mapper = new ReconciliationRuleMapper();
            final String sql = "SELECT " + mapper.schema() + " WHERE rr.id = ?";
            return this.jdbcTemplate.queryForObject(sql, mapper, ruleId);
        } catch (final EmptyResultDataAccessException e) {
            throw new ReconciliationRuleNotFoundException(ruleId);
        }
    }
}
