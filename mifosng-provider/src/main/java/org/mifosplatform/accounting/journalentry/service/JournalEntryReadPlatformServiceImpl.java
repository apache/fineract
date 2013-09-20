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
import org.mifosplatform.accounting.journalentry.data.JournalEntryData;
import org.mifosplatform.accounting.journalentry.exception.JournalEntriesNotFoundException;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.portfolio.group.service.SearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class JournalEntryReadPlatformServiceImpl implements JournalEntryReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    private final GLJournalEntryMapper journalEntryMapper = new GLJournalEntryMapper();
    private final PaginationHelper<JournalEntryData> paginationHelper = new PaginationHelper<JournalEntryData>();

    @Autowired
    public JournalEntryReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class GLJournalEntryMapper implements RowMapper<JournalEntryData> {

        public String schema() {
            return " journalEntry.id as id, glAccount.classification_enum as classification ,"
                    + " glAccount.name as glAccountName, glAccount.gl_code as glAccountCode, journalEntry.account_id as glAccountId,"
                    + " journalEntry.office_id as officeId, office.name as officeName, journalEntry.ref_num as referenceNumber, "
                    + " journalEntry.manual_entry as manualEntry,journalEntry.entry_date as transactionDate, "
                    + " journalEntry.type_enum as entryType,journalEntry.amount as amount, journalEntry.transaction_id as transactionId,"
                    + " journalEntry.entity_type_enum as entityType, journalEntry.entity_id as entityId, creatingUser.id as createdByUserId, "
                    + " creatingUser.username as createdByUserName, journalEntry.description as comments, "
                    + " journalEntry.created_date as createdDate, journalEntry.reversed as reversed "
                    + " from acc_gl_journal_entry journalEntry, acc_gl_account glAccount, m_office office, m_appuser creatingUser "
                    + " where journalEntry.account_id = glAccount.id "
                    + " and journalEntry.office_id = office.id and journalEntry.createdby_id = creatingUser.id ";
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

            return new JournalEntryData(id, officeId, officeName, glAccountName, glAccountId, glCode, accountType, transactionDate,
                    entryType, amount, transactionId, manualEntry, entityType, entityId, createdByUserId, createdDate, createdByUserName,
                    comments, reversed, referenceNumber);
        }
    }

    @Override
    public Page<JournalEntryData> retrieveAll(final SearchParameters searchParameters, final Long glAccountId,
            final Boolean onlyManualEntries, final Date fromDate, final Date toDate, final String transactionId) {

        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(this.journalEntryMapper.schema());

        final Object[] objectArray = new Object[5];
        int arrayPos = 0;

        if (StringUtils.isNotBlank(transactionId)) {
            sqlBuilder.append(" and journalEntry.transaction_id = ?");
            objectArray[arrayPos] = transactionId;
            arrayPos = arrayPos + 1;
        }

        if (searchParameters.isOfficeIdPassed()) {
            sqlBuilder.append(" and journalEntry.office_id = ?");
            objectArray[arrayPos] = searchParameters.getOfficeId();
            arrayPos = arrayPos + 1;
        }

        if (glAccountId != null && glAccountId != 0) {
            sqlBuilder.append(" and journalEntry.account_id = ?");
            objectArray[arrayPos] = glAccountId;
            arrayPos = arrayPos + 1;
        }

        if (fromDate != null || toDate != null) {
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String fromDateString = null;
            String toDateString = null;
            if (fromDate != null && toDate != null) {
                sqlBuilder.append(" and journalEntry.entry_date between ? and ? ");
                fromDateString = df.format(fromDate);
                toDateString = df.format(toDate);
                objectArray[arrayPos] = fromDateString;
                arrayPos = arrayPos + 1;
                objectArray[arrayPos] = toDateString;
                arrayPos = arrayPos + 1;
            } else if (fromDate != null) {
                sqlBuilder.append(" and journalEntry.entry_date >= ? ");
                fromDateString = df.format(fromDate);
                objectArray[arrayPos] = fromDateString;
                arrayPos = arrayPos + 1;
            } else if (toDate != null) {
                sqlBuilder.append(" and journalEntry.entry_date <= ? ");
                toDateString = df.format(toDate);
                objectArray[arrayPos] = toDateString;
                arrayPos = arrayPos + 1;
            }
        }

        if (onlyManualEntries != null) {
            if (onlyManualEntries) {
                sqlBuilder.append(" and journalEntry.manual_entry = 1");
            }
        }

        if (searchParameters.isOrderByRequested()) {
            sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());

            if (searchParameters.isSortOrderProvided()) {
                sqlBuilder.append(' ').append(searchParameters.getSortOrder());
            }
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
                this.journalEntryMapper);
    }

    @Override
    public JournalEntryData retrieveGLJournalEntryById(final long glJournalEntryId) {
        try {

            final GLJournalEntryMapper rm = new GLJournalEntryMapper();
            final String sql = "select " + rm.schema() + " and journalEntry.id = ?";

            final JournalEntryData glJournalEntryData = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { glJournalEntryId });

            return glJournalEntryData;
        } catch (final EmptyResultDataAccessException e) {
            throw new JournalEntriesNotFoundException(glJournalEntryId);
        }
    }

}
