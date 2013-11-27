/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.service;


import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.accounting.journalentry.data.JournalEntryAssociationParametersData;
import org.mifosplatform.accounting.journalentry.data.JournalEntryData;
import org.mifosplatform.accounting.journalentry.data.TransactionDetailData;
import org.mifosplatform.accounting.journalentry.exception.JournalEntriesNotFoundException;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.portfolio.group.service.SearchParameters;
import org.mifosplatform.portfolio.note.data.NoteData;
import org.mifosplatform.portfolio.paymentdetail.data.PaymentDetailData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class JournalEntryReadPlatformServiceImpl implements JournalEntryReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    private final PaginationHelper<JournalEntryData> paginationHelper = new PaginationHelper<JournalEntryData>();

    @Autowired
    public JournalEntryReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class GLJournalEntryMapper implements RowMapper<JournalEntryData> {

        
        private final JournalEntryAssociationParametersData associationParametersData ;
                
        public GLJournalEntryMapper(final JournalEntryAssociationParametersData associationParametersData ) {
            if(associationParametersData == null){
                this.associationParametersData = new JournalEntryAssociationParametersData();
            }else{
                this.associationParametersData = associationParametersData;
            }
        }
        
        public String schema() {
            StringBuilder sb = new StringBuilder();
              sb.append(" journalEntry.id as id, glAccount.classification_enum as classification ,")
                .append("journalEntry.transaction_id,")
                .append(" glAccount.name as glAccountName, glAccount.gl_code as glAccountCode,glAccount.id as glAccountId, ")
                .append(" journalEntry.office_id as officeId, office.name as officeName, journalEntry.ref_num as referenceNumber, ")
                .append(" journalEntry.manual_entry as manualEntry,journalEntry.entry_date as transactionDate, ")
                .append(" journalEntry.type_enum as entryType,journalEntry.amount as amount, journalEntry.transaction_id as transactionId,")
                .append(" journalEntry.entity_type_enum as entityType, journalEntry.entity_id as entityId, creatingUser.id as createdByUserId, ")
                .append(" creatingUser.username as createdByUserName, journalEntry.description as comments, ")
                .append(" journalEntry.created_date as createdDate, journalEntry.reversed as reversed ");
              if(associationParametersData.isRunningBalanceRequired()){
                  sb.append(" ,journalEntry.is_running_balance_caculated as runningBalanceComputed, ")
                    .append(" journalEntry.office_running_balance as officeRunningBalance, ")
                    .append(" journalEntry.organization_running_balance as organizationRunningBalance ");
              }
              if(associationParametersData.isTransactionDetailsRequired()){
                  sb.append(" ,pd.receipt_number as receiptNumber, ")
                    .append(" pd.check_number as checkNumber, ")
                    .append(" pd.account_number as accountNumber, ")
                    .append(" cdv.code_value as paymentTypeName, ")
                    .append(" pd.payment_type_cv_id as paymentTypeId,")
                    .append(" pd.bank_number as bankNumber, ")
                    .append(" pd.routing_code as routingCode, ")
                    .append(" note.id as noteId, ")
                    .append(" note.note as transactionNote ");
              }
              sb.append(" from acc_gl_journal_entry as journalEntry ")
                .append(" left join acc_gl_account as glAccount on glAccount.id = journalEntry.account_id")
                .append(" left join m_office as office on office.id = journalEntry.office_id")
                .append(" left join m_appuser as creatingUser on creatingUser.id = journalEntry.createdby_id ");
              if(associationParametersData.isTransactionDetailsRequired()){
                  sb.append(" left join m_loan_transaction as lt on journalEntry.loan_transaction_id = lt.id ")
                    .append(" left join m_savings_account_transaction as st on journalEntry.savings_transaction_id = st.id ")
                    .append(" left join m_payment_detail as pd on lt.payment_detail_id = pd.id or st.payment_detail_id = pd.id")
                    .append(" left join m_code_value as cdv on cdv.id = pd.payment_type_cv_id ")
                    .append(" left join m_note as note on lt.id = note.loan_transaction_id or st.id = note.savings_account_transaction_id ");
              }
                return sb.toString();

        }

        @Override
        public JournalEntryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");
            final String glCode = rs.getString("glAccountCode");
            final String glAccountName = rs.getString("glAccountName");
            final Long glAccountId = rs.getLong("glAccountId");
            final int accountTypeId = JdbcSupport.getInteger(rs, "classification");
            final EnumOptionData accountType = AccountingEnumerations.gLAccountType(accountTypeId);
            final LocalDate transactionDate = JdbcSupport.getLocalDate(rs, "transactionDate");
            final Boolean manualEntry = rs.getBoolean("manualEntry");
            final BigDecimal amount = rs.getBigDecimal("amount");
            final int entryTypeId = JdbcSupport.getInteger(rs, "entryType");
            final EnumOptionData entryType = AccountingEnumerations.journalEntryType(entryTypeId);
            final String transactionId = rs.getString("transactionId");
            final Integer entityTypeId = JdbcSupport.getInteger(rs, "entityType");
            EnumOptionData entityType = null;
            if (entityTypeId != null) {
                entityType = AccountingEnumerations.portfolioProductType(entityTypeId);
            }

            final Long entityId = JdbcSupport.getLong(rs, "entityId");
            final Long createdByUserId = rs.getLong("createdByUserId");
            final LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");
            final String createdByUserName = rs.getString("createdByUserName");
            final String comments = rs.getString("comments");
            final Boolean reversed = rs.getBoolean("reversed");
            final String referenceNumber = rs.getString("referenceNumber");
            BigDecimal officeRunningBalance = null;
            BigDecimal organizationRunningBalance =null;
            Boolean runningBalanceComputed =null;
            if(associationParametersData.isRunningBalanceRequired()){
                officeRunningBalance = rs.getBigDecimal("officeRunningBalance");
                organizationRunningBalance = rs.getBigDecimal("organizationRunningBalance");
                runningBalanceComputed = rs.getBoolean("runningBalanceComputed");
            }
            TransactionDetailData transactionDetailData = null;
            
            if(associationParametersData.isTransactionDetailsRequired()){
                PaymentDetailData paymentDetailData = null;
                final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentTypeId");
                if (paymentTypeId != null) {
                    final String typeName = rs.getString("paymentTypeName");
                    final CodeValueData paymentType = CodeValueData.instance(paymentTypeId, typeName);
                    final String accountNumber = rs.getString("accountNumber");
                    final String checkNumber = rs.getString("checkNumber");
                    final String routingCode = rs.getString("routingCode");
                    final String receiptNumber = rs.getString("receiptNumber");
                    final String bankNumber = rs.getString("bankNumber");
                    paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                            bankNumber);
                }
                NoteData noteData =null;
                final Long noteId = JdbcSupport.getLong(rs, "noteId");
                if(noteId!=null){
                    final String note = rs.getString("transactionNote");  
                    noteData = new NoteData(noteId, null, null, null, null, null, null, null, note, null, null, null, null, null, null);
                }
                Long transaction = null;
                if(entityType!=null){
                    transaction = Long.parseLong(transactionId.substring(1).trim());
                }
                transactionDetailData = new TransactionDetailData(transaction, paymentDetailData, noteData);
            }
            return new JournalEntryData(id, officeId, officeName, glAccountName, glAccountId, glCode, accountType, transactionDate,
                    entryType, amount, transactionId, manualEntry, entityType, entityId, createdByUserId, createdDate, createdByUserName,
                    comments, reversed, referenceNumber,officeRunningBalance,organizationRunningBalance,runningBalanceComputed,transactionDetailData);
        }
    }

    @Override
    public Page<JournalEntryData> retrieveAll(final SearchParameters searchParameters, final Long glAccountId,
            final Boolean onlyManualEntries, final Date fromDate, final Date toDate, final String transactionId, final Integer entityType, 
            final JournalEntryAssociationParametersData associationParametersData) {

        GLJournalEntryMapper rm = new GLJournalEntryMapper(associationParametersData);
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(rm.schema());

        final Object[] objectArray = new Object[5];
        int arrayPos = 0;
        String whereClose  = " where ";

        if (StringUtils.isNotBlank(transactionId)) {
            sqlBuilder.append(whereClose+" journalEntry.transaction_id = ?");
            objectArray[arrayPos] = transactionId;
            arrayPos = arrayPos + 1;

            whereClose =" and ";
        }
        
        if (entityType != null && entityType != 0 && (onlyManualEntries == null)) {

            sqlBuilder.append(whereClose+" journalEntry.entity_type_enum = ?");

            objectArray[arrayPos] = entityType;
            arrayPos = arrayPos + 1;

            whereClose =" and ";
        }

        if (searchParameters.isOfficeIdPassed()) {
            sqlBuilder.append(whereClose+" journalEntry.office_id = ?");
            objectArray[arrayPos] = searchParameters.getOfficeId();
            arrayPos = arrayPos + 1;

            whereClose =" and ";
        }

        if (glAccountId != null && glAccountId != 0) {
            sqlBuilder.append(whereClose+" journalEntry.account_id = ?");
            objectArray[arrayPos] = glAccountId;
            arrayPos = arrayPos + 1;

            whereClose =" and ";
        }

        if (fromDate != null || toDate != null) {
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateString = null;
            String toDateString = null;
            if (fromDate != null && toDate != null) {
                sqlBuilder.append(whereClose+" journalEntry.entry_date between ? and ? ");

                whereClose =" and ";

                fromDateString = df.format(fromDate);
                toDateString = df.format(toDate);
                objectArray[arrayPos] = fromDateString;
                arrayPos = arrayPos + 1;
                objectArray[arrayPos] = toDateString;
                arrayPos = arrayPos + 1;
            } else if (fromDate != null) {
                sqlBuilder.append(whereClose+" journalEntry.entry_date >= ? ");
                fromDateString = df.format(fromDate);
                objectArray[arrayPos] = fromDateString;
                arrayPos = arrayPos + 1;
                whereClose =" and ";

            } else if (toDate != null) {
                sqlBuilder.append(whereClose+" journalEntry.entry_date <= ? ");
                toDateString = df.format(toDate);
                objectArray[arrayPos] = toDateString;
                arrayPos = arrayPos + 1;

                whereClose =" and ";
            }
        }

        if (onlyManualEntries != null) {
            if (onlyManualEntries) {
                sqlBuilder.append(whereClose+" journalEntry.manual_entry = 1");

                whereClose =" and ";
            }
        }

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());

            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
            }
        }
        else
        {
            sqlBuilder.append(" order by journalEntry.entry_date, journalEntry.id");
        }

        if (searchParameters.isLimited()) {
            sqlBuilder.append(" limit ").append(searchParameters.getLimit());
            if (searchParameters.isOffset()) {
                sqlBuilder.append(" offset ").append(searchParameters.getOffset());
            }
        }

        final Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), finalObjectArray,
                rm);
    }

    @Override
    public JournalEntryData retrieveGLJournalEntryById(final long glJournalEntryId, JournalEntryAssociationParametersData  associationParametersData) {
        try {

            final GLJournalEntryMapper rm = new GLJournalEntryMapper(associationParametersData);
            final String sql = "select " + rm.schema() + " where journalEntry.id = ?";

            final JournalEntryData glJournalEntryData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { glJournalEntryId });

            return glJournalEntryData;
        } catch (final EmptyResultDataAccessException e) {
            throw new JournalEntriesNotFoundException(glJournalEntryId);
        }
    }

}
