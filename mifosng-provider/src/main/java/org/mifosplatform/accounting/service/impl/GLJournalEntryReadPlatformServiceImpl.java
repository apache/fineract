package org.mifosplatform.accounting.service.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.api.data.GLJournalEntryData;
import org.mifosplatform.accounting.exceptions.GLJournalEntriesNotFoundException;
import org.mifosplatform.accounting.service.AccountingEnumerations;
import org.mifosplatform.accounting.service.GLJournalEntryReadPlatformService;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class GLJournalEntryReadPlatformServiceImpl implements GLJournalEntryReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GLJournalEntryReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class GLJournalEntryMapper implements RowMapper<GLJournalEntryData> {

        public String schema() {
            return " journalEntry.id as journalEntryId, glAccount.classification_enum as classification ,"
                    + " glAccount.name as glAccountName, glAccount.gl_code as glCode, journalEntry.account_id as glAccountId,"
                    + " journalEntry.office_id as officeId, office.name as officeName, "
                    + " journalEntry.portfolio_generated as portfolioGenerated,journalEntry.entry_date as entryDate, "
                    + " journalEntry.type_enum as entryType,journalEntry.amount as amount, journalEntry.transaction_id as transactionId,"
                    + " journalEntry.entity_type as entityType, journalEntry.entity_id as entityId, creatingUser.id as createdByUserId, "
                    + " creatingUser.username as createdByUserName, journalEntry.description as comments, "
                    + " journalEntry.created_date as createdDate, journalEntry.reversed as reversed "
                    + " from acc_gl_journal_entry journalEntry, acc_gl_account glAccount, m_office office, m_appuser creatingUser "
                    + " where journalEntry.account_id = glAccount.id "
                    + " and journalEntry.office_id = office.id and journalEntry.createdby_id = creatingUser.id ";
        }

        @Override
        public GLJournalEntryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            Long journalEntryId = rs.getLong("journalEntryId");
            Long officeId = rs.getLong("officeId");
            String officeName = rs.getString("officeName");
            String glCode = rs.getString("glCode");
            String glAccountName = rs.getString("glAccountName");
            Long glAccountId = rs.getLong("glAccountId");
            final int accountTypeId = JdbcSupport.getInteger(rs, "classification");
            final EnumOptionData accountType = AccountingEnumerations.gLAccountType(accountTypeId);
            LocalDate entryDate = JdbcSupport.getLocalDate(rs, "entryDate");
            Boolean portfolioGenerated = rs.getBoolean("portfolioGenerated");
            BigDecimal amount = rs.getBigDecimal("amount");
            int entryTypeId = JdbcSupport.getInteger(rs, "entryType");
            final EnumOptionData entryType = AccountingEnumerations.journalEntryType(entryTypeId);
            String transactionId = rs.getString("transactionId");
            String entityType = rs.getString("entityType");
            Long entityId = JdbcSupport.getLong(rs, "entityId");
            Long createdByUserId = rs.getLong("createdByUserId");
            LocalDate createdDate = JdbcSupport.getLocalDate(rs, "createdDate");
            String createdByUserName = rs.getString("createdByUserName");
            String comments = rs.getString("comments");
            Boolean reversed = rs.getBoolean("reversed");

            return new GLJournalEntryData(journalEntryId, officeId, officeName, glAccountName, glAccountId, glCode, accountType, entryDate,
                    entryType, amount, transactionId, portfolioGenerated, entityType, entityId, createdByUserId, createdDate,
                    createdByUserName, comments, reversed);
        }
    }

    @Override
    public List<GLJournalEntryData> retrieveAllGLJournalEntries(Long officeId, Long glAccountId, Boolean portfolioGenerated, Date fromDate,
            Date toDate) {
        GLJournalEntryMapper rm = new GLJournalEntryMapper();

        String sql = "select " + rm.schema();
        Object[] objectArray = new Object[4];
        int arrayPos = 0;

        if (officeId != null && officeId != 0) {
            sql += " and journalEntry.office_id = ?";
            objectArray[arrayPos] = officeId;
            arrayPos = arrayPos + 1;
        }

        if (glAccountId != null && glAccountId != 0) {
            sql += " and journalEntry.account_id = ?";
            objectArray[arrayPos] = glAccountId;
            arrayPos = arrayPos + 1;
        }

        if (fromDate != null || toDate != null) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateString = null;
            String toDateString = null;
            if (fromDate != null && toDate != null) {
                sql += " and journalEntry.entry_date between ? and ? ";
                fromDateString = df.format(fromDate);
                toDateString = df.format(toDate);
                objectArray[arrayPos] = fromDateString;
                arrayPos = arrayPos + 1;
                objectArray[arrayPos] = toDateString;
                arrayPos = arrayPos + 1;
            } else if (fromDate != null) {
                sql += " and journalEntry.entry_date >= ? ";
                fromDateString = df.format(fromDate);
                objectArray[arrayPos] = fromDateString;
                arrayPos = arrayPos + 1;
            } else if (toDate != null) {
                sql += " and journalEntry.entry_date <= ? ";
                toDateString = df.format(toDate);
                objectArray[arrayPos] = toDateString;
                arrayPos = arrayPos + 1;
            }
        }

        if (portfolioGenerated != null) {
            if (portfolioGenerated) {
                sql += " and journalEntry.portfolio_generated = 1";
            } else {
                sql += " and journalEntry.portfolio_generated = 0";
            }
        }

        sql += " order by journalEntry.entry_date desc,journalEntry.transaction_id";

        Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);
        return this.jdbcTemplate.query(sql, rm, finalObjectArray);
    }

    @Override
    public GLJournalEntryData retrieveGLJournalEntryById(long glJournalEntryId) {
        try {

            GLJournalEntryMapper rm = new GLJournalEntryMapper();
            String sql = "select " + rm.schema() + " and journalEntry.id = ?";

            GLJournalEntryData glJournalEntryData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { glJournalEntryId });

            return glJournalEntryData;
        } catch (EmptyResultDataAccessException e) {
            throw new GLJournalEntriesNotFoundException(glJournalEntryId);
        }
    }

    @Override
    public List<GLJournalEntryData> retrieveRelatedJournalEntries(String transactionId) {
        try {

            GLJournalEntryMapper rm = new GLJournalEntryMapper();
            String sql = "select " + rm.schema() + " and journalEntry.transaction_id = ?";

            List<GLJournalEntryData> journalEntryDatas = this.jdbcTemplate.query(sql, rm, new Object[] { transactionId });

            return journalEntryDatas;
        } catch (EmptyResultDataAccessException e) {
            throw new GLJournalEntriesNotFoundException(transactionId);
        }
    }

}
