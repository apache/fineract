package org.mifosplatform.portfolio.note.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.portfolio.note.data.NoteData;
import org.mifosplatform.portfolio.note.domain.NoteType;
import org.mifosplatform.portfolio.note.exception.NoteNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class NoteReadPlatformServiceImpl implements NoteReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public NoteReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class NoteMapper implements RowMapper<NoteData> {

        public String schema() {
            return " select n.id as id, n.client_id as clientId, n.group_id as groupId, n.loan_id as loanId, n.loan_transaction_id as transactionId, "
                    + " n.note_type_enum as noteTypeEnum, n.note as note, n.created_date as createdDate, n.createdby_id as createdById, "
                    + " cb.username as createdBy, n.lastmodified_date as lastModifiedDate, n.lastmodifiedby_id as lastModifiedById, mb.username as modifiedBy "
                    + " from m_note n left join m_appuser cb on cb.id=n.createdby_id left join m_appuser mb on mb.id=n.lastmodifiedby_id ";
        }

        @Override
        public NoteData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final Long loanId = JdbcSupport.getLong(rs, "loanId");
            final Long transactionId = JdbcSupport.getLong(rs, "transactionId");
            // final Long depositAccountId = JdbcSupport.getLong(rs,
            // "depositAccountId");
            // final Long savingAccountId = JdbcSupport.getLong(rs,
            // "savingAccountId");
            final Integer noteTypeId = JdbcSupport.getInteger(rs, "noteTypeEnum");
            final EnumOptionData noteType = NoteEnumerations.noteType(noteTypeId);
            final String note = rs.getString("note");
            final DateTime createdDate = JdbcSupport.getDateTime(rs, "createdDate");
            final Long createdById = JdbcSupport.getLong(rs, "createdById");
            final DateTime lastModifiedDate = JdbcSupport.getDateTime(rs, "lastModifiedDate");
            final Long lastModifiedById = JdbcSupport.getLong(rs, "lastModifiedById");
            final String createdByUsername = rs.getString("createdBy");
            final String updatedByUsername = rs.getString("modifiedBy");
            return new NoteData(id, clientId, groupId, loanId, transactionId, null, null, noteType, note,
                    createdDate, createdById, createdByUsername, lastModifiedDate, lastModifiedById, updatedByUsername);
        }
    }

    @Override
    public NoteData retrieveNote(final Long noteId, final Long resourceId, final Integer noteTypeId) {
        final NoteType noteType = NoteType.fromInt(noteTypeId);
        try {
            final NoteMapper rm = new NoteMapper();
            String conditionSql = getResourceCondition(noteType);
            if (StringUtils.isNotBlank(conditionSql)) {
                conditionSql = " and " + conditionSql;
            }

            final String sql = rm.schema() + " where n.id = ? " + conditionSql + " order by n.created_date DESC";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { noteId, resourceId });
        } catch (final EmptyResultDataAccessException e) {
            throw new NoteNotFoundException(noteId, resourceId, noteType.name().toLowerCase());
        }
    }

    @Override
    public Collection<NoteData> retrieveNotesByResource(final Long resourceId, final Integer noteTypeId) {
        final NoteType noteType = NoteType.fromInt(noteTypeId);
        final NoteMapper rm = new NoteMapper();
        String conditionSql = getResourceCondition(noteType);

        final String sql = rm.schema() + " where " + conditionSql + " order by n.created_date DESC";

        return this.jdbcTemplate.query(sql, rm, new Object[] { resourceId });
    }

    public static String getResourceCondition(final NoteType noteType) {
        String conditionSql = "";
        switch (noteType) {
            case CLIENT:
                conditionSql = " n.client_id = ? ";
            break;
            case LOAN:
                conditionSql = " n.loan_id = ? ";
            break;
            case LOAN_TRANSACTION:
                conditionSql = " n.loan_transaction_id = ? ";
            break;
            case SAVING_ACCOUNT:
                conditionSql = " n.saving_account_id = ? ";
            break;
            case GROUP:
                conditionSql = " n.group_id = ? ";
            break;
            default:
            break;
        }

        return conditionSql;
    }
}
