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
package org.apache.fineract.infrastructure.dataqueries.service;

import static org.apache.fineract.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer.DATATABLE_NAME_REGEX_PATTERN;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.API_FIELD_NAME;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.TABLE_FIELD_ID;
import static org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant.TABLE_REGISTERED_TABLE;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetRowData;
import org.apache.fineract.infrastructure.dataqueries.exception.DatatableNotFoundException;
import org.apache.fineract.infrastructure.dataqueries.exception.DatatableSystemErrorException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.search.service.SearchUtil;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatatableUtil {

    private static final String APPLICATION_TABLE_NAME = "application_table_name";

    private final SearchUtil searchUtil;
    private final JdbcTemplate jdbcTemplate;
    private final SqlValidator sqlValidator;
    private final PlatformSecurityContext context;
    private final GenericDataService genericDataService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final ColumnValidator columnValidator;

    public boolean isMultirowDatatable(final List<ResultsetColumnHeaderData> columnHeaders) {
        return searchUtil.findFiltered(columnHeaders, e -> e.isNamed(TABLE_FIELD_ID)) != null;
    }

    public String getFKField(EntityTables entityTable) {
        return entityTable.getForeignKeyColumnNameOnDatatable();
    }

    public String validateDatatableRegistered(String datatable) {
        validateDatatableName(datatable);
        if (!isRegisteredDataTable(datatable)) {
            throw new DatatableNotFoundException(datatable);
        }
        return datatable;
    }

    public void validateDatatableName(final String name) {
        if (name == null || name.isEmpty()) {
            throw new PlatformDataIntegrityException("error.msg.datatables.datatable.null.name", "Data table name must not be blank.");
        } else if (!name.matches(DATATABLE_NAME_REGEX_PATTERN)) {
            throw new PlatformDataIntegrityException("error.msg.datatables.datatable.invalid.name.regex", "Invalid data table name.", name);
        }
        sqlValidator.validate(name);
    }

    public EntityTables resolveEntity(final String entityName) {
        EntityTables entityTable = EntityTables.fromEntityName(entityName);
        if (entityTable == null) {
            throw new PlatformDataIntegrityException("error.msg.invalid.application.table", "Invalid Datatable entity: " + entityName,
                    API_FIELD_NAME, entityName);
        }
        return entityTable;
    }

    @NotNull
    public EntityTables queryForApplicationEntity(final String datatable) {
        sqlValidator.validate(datatable);
        final String sql = "SELECT application_table_name FROM x_registered_table where registered_table_name = ?";
        final SqlRowSet rowSet = jdbcTemplate.queryForRowSet(sql, datatable); // NOSONAR

        String applicationTableName;
        if (rowSet.next()) {
            applicationTableName = rowSet.getString(APPLICATION_TABLE_NAME);
        } else {
            throw new DatatableNotFoundException(datatable);
        }
        return resolveEntity(applicationTableName);
    }

    public CommandProcessingResult checkMainResourceExistsWithinScope(@NotNull EntityTables entityTable, final Long appTableId) {
        final String sql = dataScopedSQL(entityTable, appTableId);
        log.debug("data scoped sql: {}", sql);
        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        if (!rs.next()) {
            throw new DatatableNotFoundException(entityTable, appTableId);
        }
        final Long officeId = (Long) rs.getObject("officeId");
        final Long groupId = (Long) rs.getObject("groupId");
        final Long clientId = (Long) rs.getObject("clientId");
        final Long savingsId = (Long) rs.getObject("savingsId");
        final Long loanId = (Long) rs.getObject("loanId");
        final Long transactionId = (Long) rs.getObject("transactionId");
        final Long entityId = (Long) rs.getObject("entityId");

        if (rs.next()) {
            throw new DatatableSystemErrorException("System Error: More than one row returned from data scoping query");
        }

        return new CommandProcessingResultBuilder() //
                .withOfficeId(officeId) //
                .withGroupId(groupId) //
                .withClientId(clientId) //
                .withSavingsId(savingsId) //
                .withLoanId(loanId).withTransactionId(transactionId == null ? null : String.valueOf(transactionId)).withEntityId(entityId)//
                .build();
    }

    public String dataScopedSQL(@NotNull EntityTables entityTable, final Long appTableId) {
        // unfortunately have to, one way or another, be able to restrict data to the users office hierarchy. Here, a
        // few key tables are done. But if additional fields are needed on other tables the same pattern applies
        final AppUser currentUser = this.context.authenticatedUser();
        String officeHierarchy = currentUser.getOffice().getHierarchy();
        // m_loan and m_savings_account are connected to an m_office through either an m_client or an m_group If both it
        // means it relates to an m_client that is in a group (still an m_client account)
        return switch (entityTable) {
            case LOAN -> "select distinct x.* from ( "
                    + "(select o.id as officeId, l.group_id as groupId, l.client_id as clientId, null as savingsId, l.id as loanId, null as transactionId, null as entityId from m_loan l "
                    + getClientOfficeJoinCondition(officeHierarchy, "l") + " where l.id = " + appTableId + ")" + " union all "
                    + "(select o.id as officeId, l.group_id as groupId, l.client_id as clientId, null as savingsId, l.id as loanId, null as transactionId, null as entityId from m_loan l "
                    + getGroupOfficeJoinCondition(officeHierarchy, "l") + " where l.id = " + appTableId + ")" + " ) as x";
            case SAVINGS -> "select distinct x.* from ( "
                    + "(select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, null as transactionId, null as entityId "
                    + "from m_savings_account s " + getClientOfficeJoinCondition(officeHierarchy, "s") + " where s.id = " + appTableId + ")"
                    + " union all "
                    + "(select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, null as transactionId, null as entityId "
                    + "from m_savings_account s " + getGroupOfficeJoinCondition(officeHierarchy, "s") + " where s.id = " + appTableId + ")"
                    + " ) as x";
            case SAVINGS_TRANSACTION -> "select distinct x.* from ( "
                    + "(select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, t.id as transactionId, null as entityId "
                    + "from m_savings_account_transaction t join m_savings_account s on t.savings_account_id = s.id "
                    + getClientOfficeJoinCondition(officeHierarchy, "s") + " where t.id = " + appTableId + ")" + " union all "
                    + "(select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, t.id as transactionId, null as entityId "
                    + "from m_savings_account_transaction t join m_savings_account s on t.savings_account_id = s.id "
                    + getGroupOfficeJoinCondition(officeHierarchy, "s") + " where t.id = " + appTableId + ")" + " ) as x";
            case CLIENT ->
                "select o.id as officeId, null as groupId, c.id as clientId, null as savingsId, null as loanId, null as transactionId, null as entityId from m_client c "
                        + getOfficeJoinCondition(officeHierarchy, "c") + " where c.id = " + appTableId;
            case GROUP, CENTER ->
                "select o.id as officeId, g.id as groupId, null as clientId, null as savingsId, null as loanId, null as transactionId, null as entityId from m_group g "
                        + getOfficeJoinCondition(officeHierarchy, "g") + " where g.id = " + appTableId;
            case OFFICE ->
                "select o.id as officeId, null as groupId, null as clientId, null as savingsId, null as loanId, null as transactionId, null as entityId from m_office o "
                        + "where o.hierarchy like '" + officeHierarchy + "%'" + " and o.id = " + appTableId;
            case LOAN_PRODUCT, SAVINGS_PRODUCT, SHARE_PRODUCT ->
                "select null as officeId, null as groupId, null as clientId, null as savingsId, null as loanId, null as transactionId, p.id as entityId from "
                        + entityTable.getName() + " as p WHERE p.id = " + appTableId;
            default -> throw new PlatformDataIntegrityException("error.msg.invalid.dataScopeCriteria",
                    "Application Table: " + entityTable.getName() + " not catered for in data Scoping");
        };
    }

    public String getOfficeJoinCondition(String officeHierarchy, String joinTableAlias) {
        return " join m_office o on o.id = " + joinTableAlias + ".office_id and o.hierarchy like '" + officeHierarchy + "%' ";
    }

    public String getGroupOfficeJoinCondition(String officeHierarchy, String appTableAlias) {
        return " join m_group g on g.id = " + appTableAlias + ".group_id " + getOfficeJoinCondition(officeHierarchy, "g");
    }

    public String getClientOfficeJoinCondition(String officeHierarchy, String appTableAlias) {
        return " join m_client c on c.id = " + appTableAlias + ".client_id " + getOfficeJoinCondition(officeHierarchy, "c");
    }

    public GenericResultsetData retrieveDataTableGenericResultSet(final EntityTables entityTable, final String dataTableName,
            final Long appTableId, final String order, final Long id) {
        final List<ResultsetColumnHeaderData> columnHeaders = genericDataService.fillResultsetColumnHeaders(dataTableName);
        final boolean multiRow = isMultirowDatatable(columnHeaders);

        String whereClause = getFKField(entityTable) + " = " + appTableId;
        sqlValidator.validate(whereClause);
        String sql = "select * from " + sqlGenerator.escape(dataTableName) + " where " + whereClause;

        // id only used for reading a specific entry that belongs to appTableId (in a one to many datatable)
        if (multiRow && id != null) {
            sql = sql + " and " + TABLE_FIELD_ID + " = " + id;
        }
        if (StringUtils.isNotBlank(order)) {
            columnValidator.validateSqlInjection(sql, order);
            sql = sql + " order by " + order;
        }

        final List<ResultsetRowData> result = genericDataService.fillResultsetRowData(sql, columnHeaders);
        return new GenericResultsetData(columnHeaders, result);
    }

    private boolean isRegisteredDataTable(final String datatable) {
        final String sql = "SELECT COUNT(application_table_name) FROM " + TABLE_REGISTERED_TABLE + " WHERE registered_table_name = ?";
        final Integer count = jdbcTemplate.queryForObject(sql, Integer.class, datatable);
        return count != null && count > 0;
    }

}
