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
package org.apache.fineract.portfolio.note.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.portfolio.note.data.NoteData;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.exception.NoteNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class NoteReadPlatformServiceImpl implements NoteReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    public NoteReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final class NoteMapper implements RowMapper<NoteData> {

        public String schema() {
            return " select n.id as id, n.client_id as clientId, n.group_id as groupId, n.loan_id as loanId, n.loan_transaction_id as transactionId, "
                    + " n.note_type_enum as noteTypeEnum, n.note as note, n.created_date as createdDate, n.created_by as createdById, "
                    + "  n.created_on_utc as createdDateUtc, n.last_modified_on_utc as lastModifiedDateUtc, "
                    + " cb.username as createdBy, n.lastmodified_date as lastModifiedDate, n.last_modified_by as lastModifiedById, mb.username as modifiedBy "
                    + " from m_note n left join m_appuser cb on cb.id=n.created_by left join m_appuser mb on mb.id=n.last_modified_by ";
        }

        @Override
        public NoteData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final Long loanId = JdbcSupport.getLong(rs, "loanId");
            final Long transactionId = JdbcSupport.getLong(rs, "transactionId");
            final Integer noteTypeId = JdbcSupport.getInteger(rs, "noteTypeEnum");
            final EnumOptionData noteType = NoteType.toEnumOptionData(noteTypeId);
            final String note = rs.getString("note");
            final OffsetDateTime createdDateLocal = JdbcSupport.getOffsetDateTime(rs, "createdDate");
            final OffsetDateTime createdDateUtc = JdbcSupport.getOffsetDateTime(rs, "createdDateUtc");
            final Long createdById = JdbcSupport.getLong(rs, "createdById");
            final OffsetDateTime lastModifiedDateLocal = JdbcSupport.getOffsetDateTime(rs, "lastModifiedDate");
            final OffsetDateTime lastModifiedDateUtc = JdbcSupport.getOffsetDateTime(rs, "lastModifiedDateUtc");
            final Long lastModifiedById = JdbcSupport.getLong(rs, "lastModifiedById");
            final String createdByUsername = rs.getString("createdBy");
            final String updatedByUsername = rs.getString("modifiedBy");
            final OffsetDateTime createdDate = createdDateUtc != null ? createdDateUtc : createdDateLocal;
            final OffsetDateTime lastModifiedDate = lastModifiedDateUtc != null ? lastModifiedDateUtc : lastModifiedDateLocal;

            return NoteData.builder().id(id).clientId(clientId).groupId(groupId).loanId(loanId).loanTransactionId(transactionId)
                    .noteType(noteType).note(note).createdOn(createdDate).createdById(createdById).createdByUsername(createdByUsername)
                    .updatedOn(lastModifiedDate).updatedById(lastModifiedById).updatedByUsername(updatedByUsername).build();
        }
    }

    @Override
    public NoteData retrieveNote(final Long noteId, final Long resourceId, final Integer noteTypeId) {
        final NoteType noteType = NoteType.fromInt(noteTypeId);
        try {
            final NoteMapper rm = new NoteMapper();
            List<Object> paramList = new ArrayList<>(Arrays.asList(noteId, resourceId));
            String conditionSql = getResourceCondition(noteType, paramList);
            if (StringUtils.isNotBlank(conditionSql)) {
                conditionSql = " and " + conditionSql;
            }

            final String sql = rm.schema() + " where n.id = ? " + conditionSql + " order by n.created_date DESC";

            return this.jdbcTemplate.queryForObject(sql, rm, paramList.toArray()); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new NoteNotFoundException(noteId, resourceId, noteType.name().toLowerCase(), e);
        }
    }

    @Override
    public Collection<NoteData> retrieveNotesByResource(final Long resourceId, final Integer noteTypeId) {
        final NoteType noteType = NoteType.fromInt(noteTypeId);
        final NoteMapper rm = new NoteMapper();
        List<Object> paramList = new ArrayList<>(Arrays.asList(resourceId));
        final String conditionSql = getResourceCondition(noteType, paramList);

        final String sql = rm.schema() + " where " + conditionSql + " order by n.created_date DESC";

        return this.jdbcTemplate.query(sql, rm, paramList.toArray()); // NOSONAR
    }

    public static String getResourceCondition(final NoteType noteType, List<Object> paramList) {
        String conditionSql = "";
        switch (noteType) {
            case CLIENT:
                paramList.add(NoteType.CLIENT.getValue());
                conditionSql = " n.client_id = ? and note_type_enum = ?";
            break;
            case LOAN:
                paramList.add(NoteType.LOAN.getValue());
                paramList.add(NoteType.LOAN_TRANSACTION.getValue());
                conditionSql = " n.loan_id = ? and ( n.note_type_enum = ? or n.note_type_enum = ? )";
            break;
            case LOAN_TRANSACTION:
                conditionSql = " n.loan_transaction_id = ? ";
            break;
            case SAVING_ACCOUNT:
                paramList.add(NoteType.SAVING_ACCOUNT.getValue());
                paramList.add(NoteType.SAVINGS_TRANSACTION.getValue());
                conditionSql = " n.savings_account_id = ? and ( n.note_type_enum = ? or n.note_type_enum = ? ) ";
            break;
            case SAVINGS_TRANSACTION:
                conditionSql = " n.savings_account_transaction_id = ? ";
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
