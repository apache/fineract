/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.SQLGrammarException;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.data.DatatableData;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetRowData;
import org.mifosplatform.infrastructure.dataqueries.exception.DatatableNotFoundException;
import org.mifosplatform.infrastructure.dataqueries.exception.DatatableSystemErrorException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@Service
public class ReadWriteNonCoreDataServiceImpl implements ReadWriteNonCoreDataService {

    private final static String DATATABLE_NAME_REGEX_PATTERN = "^[a-zA-Z][a-zA-Z0-9\\-_\\s]{0,48}[a-zA-Z0-9]$";

    private final static Logger logger = LoggerFactory.getLogger(ReadWriteNonCoreDataServiceImpl.class);
    private final static HashMap<String, String> apiTypeToMySQL = new HashMap<String, String>() {

        {
            put("String", "VARCHAR");
            put("Number", "INT");
            put("Decimal", "DECIMAL");
            put("Date", "DATE");
            put("Text", "TEXT");
            put("Dropdown", "INT");
        }
    };

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final JsonParserHelper helper;
    private final GenericDataService genericDataService;
    private final DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public ReadWriteNonCoreDataServiceImpl(final TenantAwareRoutingDataSource dataSource, final PlatformSecurityContext context,
            final FromJsonHelper fromJsonHelper, final GenericDataService genericDataService,
            final DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.helper = new JsonParserHelper();
        this.genericDataService = genericDataService;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Override
    public List<DatatableData> retrieveDatatableNames(final String appTable) {

        String andClause;
        if (appTable == null) {
            andClause = "";
        } else {
            andClause = " and application_table_name = '" + appTable + "'";
        }

        // PERMITTED datatables
        final String sql = "select application_table_name, registered_table_name" + " from x_registered_table " + " where exists"
                + " (select 'f'" + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id"
                + " left join m_role_permission rp on rp.role_id = r.id" + " left join m_permission p on p.id = rp.permission_id"
                + " where ur.appuser_id = " + context.authenticatedUser().getId()
                + " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', registered_table_name))) "
                + andClause + " order by application_table_name, registered_table_name";

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        final List<DatatableData> datatables = new ArrayList<DatatableData>();
        while (rs.next()) {
            final String appTableName = rs.getString("application_table_name");
            final String registeredDatatableName = rs.getString("registered_table_name");
            final List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService
                    .fillResultsetColumnHeaders(registeredDatatableName);

            datatables.add(DatatableData.create(appTableName, registeredDatatableName, columnHeaderData));
        }

        return datatables;
    }

    @Override
    public DatatableData retrieveDatatable(final String datatable) {

        // PERMITTED datatables
        final String sql = "select application_table_name, registered_table_name" + " from x_registered_table " + " where exists"
                + " (select 'f'" + " from m_appuser_role ur " + " join m_role r on r.id = ur.role_id"
                + " left join m_role_permission rp on rp.role_id = r.id" + " left join m_permission p on p.id = rp.permission_id"
                + " where ur.appuser_id = " + context.authenticatedUser().getId() + " and registered_table_name='" + datatable + "'"
                + " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', registered_table_name))) "
                + " order by application_table_name, registered_table_name";

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        DatatableData datatableData = null;
        while (rs.next()) {
            final String appTableName = rs.getString("application_table_name");
            final String registeredDatatableName = rs.getString("registered_table_name");
            final List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService
                    .fillResultsetColumnHeaders(registeredDatatableName);

            datatableData = DatatableData.create(appTableName, registeredDatatableName, columnHeaderData);
        }

        return datatableData;
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        logger.error(dve.getMessage(), dve);
    }

    @Transactional
    @Override
    public void registerDatatable(final String dataTableName, final String applicationTableName) {

        validateAppTable(applicationTableName);

        final String registerDatatableSql = "insert into x_registered_table (registered_table_name, application_table_name) values ('"
                + dataTableName + "', '" + applicationTableName + "')";

        final String createPermission = "'CREATE_" + dataTableName + "'";
        final String createPermissionChecker = "'CREATE_" + dataTableName + "_CHECKER'";
        final String readPermission = "'READ_" + dataTableName + "'";
        final String updatePermission = "'UPDATE_" + dataTableName + "'";
        final String updatePermissionChecker = "'UPDATE_" + dataTableName + "_CHECKER'";
        final String deletePermission = "'DELETE_" + dataTableName + "'";
        final String deletePermissionChecker = "'DELETE_" + dataTableName + "_CHECKER'";

        final String permissionsSql = "insert into m_permission (grouping, code, action_name, entity_name, can_maker_checker) values "
                + "('datatable', "
                + createPermission
                + ", 'CREATE', '"
                + dataTableName
                + "', true),"
                + "('datatable', "
                + createPermissionChecker
                + ", 'CREATE', '"
                + dataTableName
                + "', false),"
                + "('datatable', "
                + readPermission
                + ", 'READ', '"
                + dataTableName
                + "', false),"
                + "('datatable', "
                + updatePermission
                + ", 'UPDATE', '"
                + dataTableName
                + "', true),"
                + "('datatable', "
                + updatePermissionChecker
                + ", 'UPDATE', '"
                + dataTableName
                + "', false),"
                + "('datatable', "
                + deletePermission
                + ", 'DELETE', '"
                + dataTableName
                + "', true),"
                + "('datatable', "
                + deletePermissionChecker
                + ", 'DELETE', '"
                + dataTableName + "', false)";

        try {
            String[] sqlArray = { registerDatatableSql, permissionsSql };
            this.jdbcTemplate.batchUpdate(sqlArray);

        } catch (DataIntegrityViolationException dve) {
            Throwable realCause = dve.getMostSpecificCause();
            // even if duplicate is only due to permission duplicate, okay to
            // show duplicate datatable error msg
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.datatable.registered", "Datatable `" + dataTableName
                            + "` is already registered against an application table.", "dataTableName", dataTableName); }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }

    }

    @Transactional
    @Override
    public void deregisterDatatable(final String datatable) {
        final String permissionList = "('CREATE_" + datatable + "', 'CREATE_" + datatable + "_CHECKER', 'READ_" + datatable + "', 'UPDATE_"
                + datatable + "', 'UPDATE_" + datatable + "_CHECKER', 'DELETE_" + datatable + "', 'DELETE_" + datatable + "_CHECKER')";

        final String deleteRolePermissionsSql = "delete from m_role_permission where m_role_permission.permission_id in (select id from m_permission where code in "
                + permissionList + ")";

        final String deletePermissionsSql = "delete from m_permission where code in " + permissionList;

        final String deleteRegisteredDatatableSql = "delete from x_registered_table where registered_table_name = '" + datatable + "'";

        String[] sqlArray = { deleteRolePermissionsSql, deletePermissionsSql, deleteRegisteredDatatableSql };
        this.jdbcTemplate.batchUpdate(sqlArray);
    }

    @Transactional
    @Override
    public CommandProcessingResult createNewDatatableEntry(final String dataTableName, final Long appTableId, final JsonCommand command) {

        try {
            final String appTable = queryForApplicationTableName(dataTableName);
            CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(appTable, appTableId);

            final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);

            final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
            final Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

            final String sql = getAddSql(columnHeaders, dataTableName, getFKField(appTable), appTableId, dataParams);

            this.jdbcTemplate.update(sql);

            return commandProcessingResult; //

        } catch (ConstraintViolationException dve) {
            // NOTE: jdbctemplate throws a
            // org.hibernate.exception.ConstraintViolationException even though
            // it should be a DataAccessException?
            Throwable realCause = dve.getCause();
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                            + "` and application table with identifier `" + appTableId + "`.", "dataTableName", dataTableName, appTableId); }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        } catch (DataAccessException dve) {
            Throwable realCause = dve.getMostSpecificCause();
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                            + "` and application table with identifier `" + appTableId + "`.", "dataTableName", dataTableName, appTableId); }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    private boolean isRegisteredDataTable(final String name) {
        // PERMITTED datatables
        final String sql = "select if((exists (select 1 from x_registered_table where registered_table_name = ?)) = 1, 'true', 'false')";
        final String isRegisteredDataTable = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { name });
        return new Boolean(isRegisteredDataTable);
    }

    private void validateDatatableName(final String name) {

        if (name == null || name.isEmpty()) {
            throw new PlatformDataIntegrityException("error.msg.datatables.datatable.null.name", "Data table name must not be blank.");
        } else if (!name.matches(DATATABLE_NAME_REGEX_PATTERN)) { throw new PlatformDataIntegrityException(
                "error.msg.datatables.datatable.invalid.name.regex", "Invalid data table name.", name); }
    }

    private String datatableColumnNameToCodeValueName(final String columnName, final String code) {

        return (code + "_cd_" + columnName);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

    private void parseDatatableColumnObjectForCreate(final JsonObject column, StringBuilder sqlBuilder) {

        String name = (column.has("name")) ? column.get("name").getAsString() : null;
        String type = (column.has("type")) ? column.get("type").getAsString() : null;
        Integer length = (column.has("length")) ? column.get("length").getAsInt() : null;
        Boolean mandatory = (column.has("mandatory")) ? column.get("mandatory").getAsBoolean() : false;
        String code = (column.has("code")) ? column.get("code").getAsString() : null;

        if (StringUtils.isNotBlank(code)) {
            name = datatableColumnNameToCodeValueName(name, code);
        }

        String mysqlType = apiTypeToMySQL.get(type);
        sqlBuilder = sqlBuilder.append("`" + name + "` " + mysqlType);

        if (type != null) {
            if (type.equals("String")) {
                sqlBuilder = sqlBuilder.append("(" + length + ")");
            } else if (type.equals("Decimal")) {
                sqlBuilder = sqlBuilder.append("(19,6)");
            } else if (type.equals("Dropdown")) {
                sqlBuilder = sqlBuilder.append("(11)");
            }
        }
        if (mandatory != null) {
            if (mandatory) {
                sqlBuilder = sqlBuilder.append(" NOT NULL");
            } else {
                sqlBuilder = sqlBuilder.append(" DEFAULT NULL");
            }
        }

        sqlBuilder = sqlBuilder.append(", ");
    }

    @Transactional
    @Override
    public CommandProcessingResult createDatatable(final JsonCommand command) {

        String datatableName = null;

        try {
            context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            JsonElement element = this.fromJsonHelper.parse(command.json());
            JsonArray columns = this.fromJsonHelper.extractJsonArrayNamed("columns", element);
            datatableName = this.fromJsonHelper.extractStringNamed("datatableName", element);
            String apptableName = this.fromJsonHelper.extractStringNamed("apptableName", element);
            Boolean multiRow = this.fromJsonHelper.extractBooleanNamed("multiRow", element);

            if (multiRow == null) {
                multiRow = false;
            }

            validateDatatableName(datatableName);
            validateAppTable(apptableName);

            String fkColumnName = apptableName.substring(2) + "_id";
            String fkName = datatableName.toLowerCase().replaceAll("\\s", "_") + "_" + fkColumnName;
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder = sqlBuilder.append("CREATE TABLE `" + datatableName + "` (");

            if (multiRow) {
                sqlBuilder = sqlBuilder.append("`id` BIGINT(20) NOT NULL AUTO_INCREMENT, ").append(
                        "`" + fkColumnName + "` BIGINT(20) NOT NULL, ");
            } else {
                sqlBuilder = sqlBuilder.append("`" + fkColumnName + "` BIGINT(20) NOT NULL, ");
            }

            for (JsonElement column : columns) {
                parseDatatableColumnObjectForCreate(column.getAsJsonObject(), sqlBuilder);
            }

            // Remove trailing comma and space
            sqlBuilder = sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

            if (multiRow) {
                sqlBuilder = sqlBuilder.append(", PRIMARY KEY (`id`)")
                        .append(", KEY `fk_" + apptableName.substring(2) + "_id` (`" + fkColumnName + "`)")
                        .append(", CONSTRAINT `fk_" + fkName + "` ").append("FOREIGN KEY (`" + fkColumnName + "`) ")
                        .append("REFERENCES `" + apptableName + "` (`id`)");
            } else {
                sqlBuilder = sqlBuilder.append(", PRIMARY KEY (`" + fkColumnName + "`)").append(", CONSTRAINT `fk_" + fkName + "` ")
                        .append("FOREIGN KEY (`" + fkColumnName + "`) ").append("REFERENCES `" + apptableName + "` (`id`)");
            }

            sqlBuilder = sqlBuilder.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            this.jdbcTemplate.execute(sqlBuilder.toString());

            registerDatatable(datatableName, apptableName);
        } catch (SQLGrammarException e) {
            Throwable realCause = e.getCause();
            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

            if (realCause.getMessage().toLowerCase().contains("duplicate column name")) {
                baseDataValidator.reset().parameter("name").failWithCode("duplicate.column.name");
            } else if (realCause.getMessage().contains("Table") && realCause.getMessage().contains("already exists")) {
                baseDataValidator.reset().parameter("datatableName").value(datatableName).failWithCode("datatable.already.exists");
            }

            throwExceptionIfValidationWarningsExist(dataValidationErrors);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withResourceIdAsString(datatableName).build();
    }

    private void parseDatatableColumnForUpdate(final JsonObject column,
            final Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition, StringBuilder sqlBuilder) {

        String name = (column.has("name")) ? column.get("name").getAsString() : null;
        String lengthStr = (column.has("length")) ? column.get("length").getAsString() : null;
        Integer length = (StringUtils.isNotBlank(lengthStr)) ? Integer.parseInt(lengthStr) : null;
        String newName = (column.has("newName")) ? column.get("newName").getAsString() : name;
        Boolean mandatory = (column.has("mandatory")) ? column.get("mandatory").getAsBoolean() : false;
        String after = (column.has("after")) ? column.get("after").getAsString() : null;
        String code = (column.has("code")) ? column.get("code").getAsString() : null;
        String newCode = (column.has("newCode")) ? column.get("newCode").getAsString() : null;

        if (StringUtils.isNotBlank(code)) {
            name = datatableColumnNameToCodeValueName(name, code);

            if (StringUtils.isNotBlank(newCode)) {
                newName = datatableColumnNameToCodeValueName(newName, newCode);
            } else {
                newName = datatableColumnNameToCodeValueName(newName, code);
            }
        }

        if (!mapColumnNameDefinition.containsKey(name)) { throw new PlatformDataIntegrityException(
                "error.msg.datatable.column.missing.update.parse", "Column " + name + " does not exist.", name); }

        String type = mapColumnNameDefinition.get(name).getColumnType();
        if (length == null && type.toLowerCase().equals("varchar")) {
            length = mapColumnNameDefinition.get(name).getColumnLength().intValue();
        }

        sqlBuilder = sqlBuilder.append(", CHANGE `" + name + "` `" + newName + "` " + type);
        if (length != null && length > 0) {
            if (type.toLowerCase().equals("decimal")) {
                sqlBuilder.append("(19,6)");
            } else if (type.toLowerCase().equals("varchar")) {
                sqlBuilder.append("(" + length + ")");
            }
        }

        if (mandatory != null) {
            if (mandatory) {
                sqlBuilder = sqlBuilder.append(" NOT NULL");
            } else {
                sqlBuilder = sqlBuilder.append(" DEFAULT NULL");
            }
        }
        if (after != null) {
            sqlBuilder = sqlBuilder.append(" AFTER `" + after + "`");
        }
    }

    private void parseDatatableColumnForAdd(final JsonObject column, StringBuilder sqlBuilder) {

        String name = (column.has("name")) ? column.get("name").getAsString() : null;
        String type = (column.has("type")) ? column.get("type").getAsString() : null;
        Integer length = (column.has("length")) ? column.get("length").getAsInt() : null;
        Boolean mandatory = (column.has("mandatory")) ? column.get("mandatory").getAsBoolean() : false;
        String after = (column.has("after")) ? column.get("after").getAsString() : null;
        String code = (column.has("code")) ? column.get("code").getAsString() : null;

        if (StringUtils.isNotBlank(code)) {
            name = datatableColumnNameToCodeValueName(name, code);
        }

        String mysqlType = apiTypeToMySQL.get(type);
        sqlBuilder = sqlBuilder.append(", ADD `" + name + "` " + mysqlType);

        if (type != null) {
            if (type.equals("String") && length != null) {
                sqlBuilder = sqlBuilder.append("(" + length + ")");
            } else if (type.equals("Decimal")) {
                sqlBuilder = sqlBuilder.append("(19,6)");
            } else if (type.equals("Dropdown")) {
                sqlBuilder = sqlBuilder.append("(11)");
            }
        }
        if (mandatory != null) {
            if (mandatory) {
                sqlBuilder = sqlBuilder.append(" NOT NULL");
            } else {
                sqlBuilder = sqlBuilder.append(" DEFAULT NULL");
            }
        }
        if (after != null) {
            sqlBuilder = sqlBuilder.append(" AFTER `" + after + "`");
        }
    }

    private void parseDatatableColumnForDrop(final JsonObject column, StringBuilder sqlBuilder) {

        String name = (column.has("name")) ? column.get("name").getAsString() : null;

        sqlBuilder = sqlBuilder.append(", DROP COLUMN `" + name + "`");
    }

    @Transactional
    @Override
    public void updateDatatable(final String datatableName, final JsonCommand command) {

        try {
            context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            JsonElement element = this.fromJsonHelper.parse(command.json());
            JsonArray changeColumns = this.fromJsonHelper.extractJsonArrayNamed("changeColumns", element);
            JsonArray addColumns = this.fromJsonHelper.extractJsonArrayNamed("addColumns", element);
            JsonArray dropColumns = this.fromJsonHelper.extractJsonArrayNamed("dropColumns", element);
            String apptableName = this.fromJsonHelper.extractStringNamed("apptableName", element);

            validateDatatableName(datatableName);

            List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService.fillResultsetColumnHeaders(datatableName);
            Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition = new HashMap<String, ResultsetColumnHeaderData>();
            for (ResultsetColumnHeaderData columnHeader : columnHeaderData) {
                mapColumnNameDefinition.put(columnHeader.getColumnName(), columnHeader);
            }

            if (!StringUtils.isBlank(apptableName)) {
                validateAppTable(apptableName);

                String oldApptableName = this.queryForApplicationTableName(datatableName);
                if (!StringUtils.equals(oldApptableName, apptableName)) {
                    String oldFKName = oldApptableName.substring(2) + "_id";
                    String newFKName = apptableName.substring(2) + "_id";
                    String oldConstraintName = datatableName.toLowerCase().replaceAll("\\s", "_") + "_" + oldFKName;
                    String newConstraintName = datatableName.toLowerCase().replaceAll("\\s", "_") + "_" + newFKName;
                    StringBuilder sqlBuilder = new StringBuilder();

                    if (mapColumnNameDefinition.containsKey("id")) {
                        sqlBuilder = sqlBuilder.append("ALTER TABLE `" + datatableName + "` ").append("DROP KEY `fk_" + oldFKName + "`,")
                                .append("DROP FOREIGN KEY `fk_" + oldConstraintName + "`,")
                                .append("CHANGE COLUMN `" + oldFKName + "` `" + newFKName + "` BIGINT(20) NOT NULL,")
                                .append("ADD KEY `fk_" + newFKName + "` (`" + newFKName + "`),")
                                .append("ADD CONSTRAINT `fk_" + newConstraintName + "` ").append("FOREIGN KEY (`" + newFKName + "`) ")
                                .append("REFERENCES `" + apptableName + "` (`id`)");
                    } else {
                        sqlBuilder = sqlBuilder.append("ALTER TABLE `" + datatableName + "` ")
                                .append("DROP FOREIGN KEY `fk_" + oldConstraintName + "`,")
                                .append("CHANGE COLUMN `" + oldFKName + "` `" + newFKName + "` BIGINT(20) NOT NULL,")
                                .append("ADD CONSTRAINT `fk_" + newConstraintName + "` ").append("FOREIGN KEY (`" + newFKName + "`) ")
                                .append("REFERENCES `" + apptableName + "` (`id`)");
                    }

                    this.jdbcTemplate.execute(sqlBuilder.toString());

                    deregisterDatatable(datatableName);
                    registerDatatable(datatableName, apptableName);
                }
            }

            if (changeColumns == null && addColumns == null && dropColumns == null) { return; }

            if (dropColumns != null) {

                StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE `" + datatableName + "`");

                for (JsonElement column : dropColumns) {
                    parseDatatableColumnForDrop(column.getAsJsonObject(), sqlBuilder);
                }

                // Remove the first comma, right after ALTER TABLE `datatable`
                int indexOfFirstComma = sqlBuilder.indexOf(",");
                if (indexOfFirstComma != -1) {
                    sqlBuilder = sqlBuilder.deleteCharAt(indexOfFirstComma);
                }

                this.jdbcTemplate.execute(sqlBuilder.toString());
            }
            if (addColumns != null) {

                StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE `" + datatableName + "`");

                for (JsonElement column : addColumns) {
                    parseDatatableColumnForAdd(column.getAsJsonObject(), sqlBuilder);
                }

                // Remove the first comma, right after ALTER TABLE `datatable`
                int indexOfFirstComma = sqlBuilder.indexOf(",");
                if (indexOfFirstComma != -1) {
                    sqlBuilder = sqlBuilder.deleteCharAt(indexOfFirstComma);
                }

                this.jdbcTemplate.execute(sqlBuilder.toString());
            }
            if (changeColumns != null) {

                StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE `" + datatableName + "`");

                for (JsonElement column : changeColumns) {
                    parseDatatableColumnForUpdate(column.getAsJsonObject(), mapColumnNameDefinition, sqlBuilder);
                }

                // Remove the first comma, right after ALTER TABLE `datatable`
                int indexOfFirstComma = sqlBuilder.indexOf(",");
                if (indexOfFirstComma != -1) {
                    sqlBuilder = sqlBuilder.deleteCharAt(indexOfFirstComma);
                }

                this.jdbcTemplate.execute(sqlBuilder.toString());
            }
        } catch (SQLGrammarException e) {
            Throwable realCause = e.getCause();
            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

            if (realCause.getMessage().toLowerCase().contains("unknown column")) {
                baseDataValidator.reset().parameter("name").failWithCode("does.not.exist");
            } else if (realCause.getMessage().toLowerCase().contains("can't drop")) {
                baseDataValidator.reset().parameter("name").failWithCode("does.not.exist");
            } else if (realCause.getMessage().toLowerCase().contains("duplicate column")) {
                baseDataValidator.reset().parameter("name").failWithCode("column.already.exists");
            }

            throwExceptionIfValidationWarningsExist(dataValidationErrors);
        }
    }

    @Transactional
    @Override
    public void deleteDatatable(final String datatableName) {

        try {
            context.authenticatedUser();
            if (!isRegisteredDataTable(datatableName)) { throw new DatatableNotFoundException(datatableName); }
            validateDatatableName(datatableName);
            deregisterDatatable(datatableName);
            String sql = "DROP TABLE `" + datatableName + "`";
            this.jdbcTemplate.execute(sql);
        } catch (SQLGrammarException e) {
            Throwable realCause = e.getCause();
            List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");
            if (realCause.getMessage().contains("Unknown table")) {
                baseDataValidator.reset().parameter("datatableName").failWithCode("does.not.exist");
            }

            throwExceptionIfValidationWarningsExist(dataValidationErrors);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateDatatableEntryOneToOne(final String dataTableName, final Long appTableId, final JsonCommand command) {

        return updateDatatableEntry(dataTableName, appTableId, null, command);
    }

    @Transactional
    @Override
    public CommandProcessingResult updateDatatableEntryOneToMany(final String dataTableName, final Long appTableId, final Long datatableId,
            final JsonCommand command) {

        return updateDatatableEntry(dataTableName, appTableId, datatableId, command);
    }

    private CommandProcessingResult updateDatatableEntry(final String dataTableName, final Long appTableId, final Long datatableId,
            final JsonCommand command) {

        final String appTable = queryForApplicationTableName(dataTableName);
        CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(appTable, appTableId);

        final GenericResultsetData grs = retrieveDataTableGenericResultSetForUpdate(appTable, dataTableName, appTableId, datatableId);

        if (grs.hasNoEntries()) { throw new DatatableNotFoundException(dataTableName, appTableId); }

        if (grs.hasMoreThanOneEntry()) { throw new PlatformDataIntegrityException("error.msg.attempting.multiple.update",
                "Application table: " + dataTableName + " Foreign key id: " + appTableId); }

        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

        String pkName = "id"; // 1:M datatable
        if (datatableId == null) {
            pkName = getFKField(appTable);
        } // 1:1 datatable

        final Map<String, Object> changes = getAffectedAndChangedColumns(grs, dataParams, pkName);

        if (!changes.isEmpty()) {
            Long pkValue = appTableId;
            if (datatableId != null) {
                pkValue = datatableId;
            }
            final String sql = getUpdateSql(dataTableName, pkName, pkValue, changes);
            logger.info("Update sql: " + sql);
            if (StringUtils.isNotBlank(sql)) {
                this.jdbcTemplate.update(sql);
                changes.put("locale", dataParams.get("locale"));
                changes.put("dateFormat", "yyyy-MM-dd");
            } else {
                logger.info("No Changes");
            }
        }

        return new CommandProcessingResultBuilder() //
                .withOfficeId(commandProcessingResult.getOfficeId()) //
                .withGroupId(commandProcessingResult.getGroupId()) //
                .withClientId(commandProcessingResult.getClientId()) //
                .withSavingsId(commandProcessingResult.getSavingsId()) //
                .withLoanId(commandProcessingResult.getLoanId()) //
                .with(changes) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteDatatableEntries(final String dataTableName, final Long appTableId) {

        final String appTable = queryForApplicationTableName(dataTableName);
        CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(appTable, appTableId);

        final String deleteOneToOneEntrySql = getDeleteEntriesSql(dataTableName, getFKField(appTable), appTableId);

        int rowsDeleted = this.jdbcTemplate.update(deleteOneToOneEntrySql);
        if (rowsDeleted < 1) { throw new DatatableNotFoundException(dataTableName, appTableId); }

        return commandProcessingResult;
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteDatatableEntry(final String dataTableName, final Long appTableId, final Long datatableId) {

        final String appTable = queryForApplicationTableName(dataTableName);
        CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(appTable, appTableId);

        final String sql = getDeleteEntrySql(dataTableName, datatableId);

        this.jdbcTemplate.update(sql);
        return commandProcessingResult;
    }

    @Override
    public GenericResultsetData retrieveDataTableGenericResultSet(final String dataTableName, final Long appTableId, final String order,
            final Long id) {

        final String appTable = queryForApplicationTableName(dataTableName);
        checkMainResourceExistsWithinScope(appTable, appTableId);

        final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);

        String sql = "";

        // id only used for reading a specific entry in a one to many datatable
        // (when updating)
        if (id == null) {
            sql = sql + "select * from `" + dataTableName + "` where " + getFKField(appTable) + " = " + appTableId;
        } else {
            sql = sql + "select * from `" + dataTableName + "` where id = " + id;
        }

        if (order != null) {
            sql = sql + " order by " + order;
        }

        final List<ResultsetRowData> result = fillDatatableResultSetDataRows(sql);

        return new GenericResultsetData(columnHeaders, result);
    }

    private GenericResultsetData retrieveDataTableGenericResultSetForUpdate(final String appTable, final String dataTableName,
            final Long appTableId, final Long id) {

        final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);

        String sql = "";

        // id only used for reading a specific entry in a one to many datatable
        // (when updating)
        if (id == null) {
            sql = sql + "select * from `" + dataTableName + "` where " + getFKField(appTable) + " = " + appTableId;
        } else {
            sql = sql + "select * from `" + dataTableName + "` where id = " + id;
        }

        final List<ResultsetRowData> result = fillDatatableResultSetDataRows(sql);

        return new GenericResultsetData(columnHeaders, result);
    }

    private CommandProcessingResult checkMainResourceExistsWithinScope(final String appTable, final Long appTableId) {

        final String sql = dataScopedSQL(appTable, appTableId);
        logger.info("data scoped sql: " + sql);
        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        if (!rs.next()) { throw new DatatableNotFoundException(appTable, appTableId); }

        Long officeId = getLongSqlRowSet(rs, "officeId");
        Long groupId = getLongSqlRowSet(rs, "groupId");
        Long clientId = getLongSqlRowSet(rs, "clientId");
        Long savingsId = getLongSqlRowSet(rs, "savingsId");
        Long LoanId = getLongSqlRowSet(rs, "loanId");

        if (rs.next()) { throw new DatatableSystemErrorException("System Error: More than one row returned from data scoping query"); }

        return new CommandProcessingResultBuilder() //
                .withOfficeId(officeId) //
                .withGroupId(groupId) //
                .withClientId(clientId) //
                .withSavingsId(savingsId) //
                .withLoanId(LoanId) //
                .build();
    }

    private Long getLongSqlRowSet(final SqlRowSet rs, final String column) {
        Long val = rs.getLong(column);
        if (val == 0) val = null;
        return val;
    }

    private String dataScopedSQL(final String appTable, final Long appTableId) {
        /*
         * unfortunately have to, one way or another, be able to restrict data
         * to the users office hierarchy. Here, a few key tables are done. But
         * if additional fields are needed on other tables the same pattern
         * applies
         */

        AppUser currentUser = context.authenticatedUser();
        String scopedSQL = null;
        /*
         * m_loan and m_savings_account are connected to an m_office thru either
         * an m_client or an m_group If both it means it relates to an m_client
         * that is in a group (still an m_client account)
         */
        if (appTable.equalsIgnoreCase("m_loan")) {
            scopedSQL = "select  distinctrow x.* from ("
                    + " (select o.id as officeId, l.group_id as groupId, l.client_id as clientId, null as savingsId, l.id as loanId from m_loan l "
                    + " join m_client c on c.id = l.client_id "
                    + " join m_office o on o.id = c.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy()
                    + "%'"
                    + " where l.id = "
                    + appTableId
                    + ")"
                    + " union all "
                    + " (select o.id as officeId, l.group_id as groupId, l.client_id as clientId, null as savingsId, l.id as loanId from m_loan l "
                    + " join m_group g on g.id = l.group_id " + " join m_office o on o.id = g.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy() + "%'" + " where l.id = " + appTableId + ")" + " ) x";
        }
        if (appTable.equalsIgnoreCase("m_savings_account")) {
            scopedSQL = "select  distinctrow x.* from ("
                    + " (select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId from m_savings_account s "
                    + " join m_client c on c.id = s.client_id "
                    + " join m_office o on o.id = c.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy()
                    + "%'"
                    + " where s.id = "
                    + appTableId
                    + ")"
                    + " union all "
                    + " (select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId from m_savings_account s "
                    + " join m_group g on g.id = s.group_id " + " join m_office o on o.id = g.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy() + "%'" + " where s.id = " + appTableId + ")" + " ) x";
        }
        if (appTable.equalsIgnoreCase("m_client")) {
            scopedSQL = "select o.id as officeId, null as groupId, c.id as clientId, null as savingsId, null as loanId from m_client c "
                    + " join m_office o on o.id = c.office_id and o.hierarchy like '" + currentUser.getOffice().getHierarchy() + "%'"
                    + " where c.id = " + appTableId;
        }
        if (appTable.equalsIgnoreCase("m_group")) {
            scopedSQL = "select o.id as officeId, g.id as groupId, null as clientId, null as savingsId, null as loanId from m_group g "
                    + " join m_office o on o.id = g.office_id and o.hierarchy like '" + currentUser.getOffice().getHierarchy() + "%'"
                    + " where g.id = " + appTableId;
        }
        if (appTable.equalsIgnoreCase("m_office")) {
            scopedSQL = "select o.id as officeId, null as groupId, null as clientId, null as savingsId, null as loanId from m_office o "
                    + " where o.hierarchy like '" + currentUser.getOffice().getHierarchy() + "%'" + " and o.id = " + appTableId;
        }

        if (scopedSQL == null) { throw new PlatformDataIntegrityException("error.msg.invalid.dataScopeCriteria", "Application Table: "
                + appTable + " not catered for in data Scoping"); }

        return scopedSQL;

    }

    private void validateAppTable(final String appTable) {

        if (appTable.equalsIgnoreCase("m_loan")) return;
        if (appTable.equalsIgnoreCase("m_savings_account")) return;
        if (appTable.equalsIgnoreCase("m_client")) return;
        if (appTable.equalsIgnoreCase("m_group")) return;
        if (appTable.equalsIgnoreCase("m_office")) return;

        throw new PlatformDataIntegrityException("error.msg.invalid.application.table", "Invalid Application Table: " + appTable);
    }

    private List<ResultsetRowData> fillDatatableResultSetDataRows(final String sql) {

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        final List<ResultsetRowData> resultsetDataRows = new ArrayList<ResultsetRowData>();

        final SqlRowSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {
            final List<String> columnValues = new ArrayList<String>();
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                final String columnName = rsmd.getColumnName(i + 1);
                final String columnValue = rs.getString(columnName);
                columnValues.add(columnValue);
            }

            final ResultsetRowData resultsetDataRow = ResultsetRowData.create(columnValues);
            resultsetDataRows.add(resultsetDataRow);
        }

        return resultsetDataRows;
    }

    private String queryForApplicationTableName(final String datatable) {
        final String sql = "SELECT application_table_name FROM x_registered_table where registered_table_name = '" + datatable + "'";

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        String applicationTableName = null;
        if (rs.next()) {
            applicationTableName = rs.getString("application_table_name");
        } else {
            throw new DatatableNotFoundException(datatable);
        }

        return applicationTableName;
    }

    private String getFKField(final String applicationTableName) {

        return applicationTableName.substring(2) + "_id";
    }

    private String getAddSql(final List<ResultsetColumnHeaderData> columnHeaders, final String datatable, final String fkName,
            final Long appTableId, final Map<String, String> queryParams) {

        final Map<String, String> affectedColumns = getAffectedColumns(columnHeaders, queryParams, fkName);

        String pValueWrite = "";
        String addSql = "";
        String singleQuote = "'";

        String insertColumns = "";
        String selectColumns = "";
        String columnName = "";
        String pValue = null;
        for (String key : affectedColumns.keySet()) {
            pValue = affectedColumns.get(key);

            if (StringUtils.isEmpty(pValue)) {
                pValueWrite = "null";
            } else {
                pValueWrite = singleQuote + genericDataService.replace(pValue, singleQuote, singleQuote + singleQuote) + singleQuote;
            }
            columnName = "`" + key + "`";
            insertColumns += ", " + columnName;
            selectColumns += "," + pValueWrite + " as " + columnName;
        }

        addSql = "insert into `" + datatable + "` (`" + fkName + "` " + insertColumns + ")" + " select " + appTableId + " as id"
                + selectColumns;

        return addSql;
    }

    private String getUpdateSql(final String datatable, final String keyFieldName, final Long keyFieldValue,
            final Map<String, Object> changedColumns) {

        // just updating fields that have changed since pre-update read - though
        // its possible these values are different from the page the user was
        // looking at and even different from the current db values (if some
        // other update got in quick) - would need a version field for
        // completeness but its okay to take this risk with additional fields
        // data

        if (changedColumns.size() == 0) return null;

        String pValue = null;
        String pValueWrite = "";
        String singleQuote = "'";
        boolean firstColumn = true;
        String sql = "update `" + datatable + "` ";

        for (String key : changedColumns.keySet()) {
            if (firstColumn) {
                sql += " set ";
                firstColumn = false;
            } else {
                sql += ", ";
            }

            pValue = (String) changedColumns.get(key);
            if (StringUtils.isEmpty(pValue)) {
                pValueWrite = "null";
            } else {
                pValueWrite = singleQuote + genericDataService.replace(pValue, singleQuote, singleQuote + singleQuote) + singleQuote;
            }
            sql += "`" + key + "` = " + pValueWrite;
        }

        sql += " where " + keyFieldName + " = " + keyFieldValue;

        return sql;
    }

    private Map<String, Object> getAffectedAndChangedColumns(final GenericResultsetData grs, final Map<String, String> queryParams,
            final String fkName) {

        final Map<String, String> affectedColumns = getAffectedColumns(grs.getColumnHeaders(), queryParams, fkName);
        final Map<String, Object> affectedAndChangedColumns = new HashMap<String, Object>();

        for (final String key : affectedColumns.keySet()) {
            final String columnValue = affectedColumns.get(key);
            final String colType = grs.getColTypeOfColumnNamed(key);
            if (columnChanged(key, columnValue, colType, grs)) {
                affectedAndChangedColumns.put(key, columnValue);
            }
        }

        return affectedAndChangedColumns;
    }

    private boolean columnChanged(final String key, final String keyValue, final String colType, final GenericResultsetData grs) {

        List<String> columnValues = grs.getData().get(0).getRow();

        String columnValue = null;
        for (int i = 0; i < grs.getColumnHeaders().size(); i++) {

            if (key.equals(grs.getColumnHeaders().get(i).getColumnName())) {
                columnValue = columnValues.get(i);

                if (notTheSame(columnValue, keyValue, colType)) { return true; }
                return false;
            }
        }

        throw new PlatformDataIntegrityException("error.msg.invalid.columnName", "Parameter Column Name: " + key + " not found");
    }

    private Map<String, String> getAffectedColumns(final List<ResultsetColumnHeaderData> columnHeaders,
            final Map<String, String> queryParams, final String keyFieldName) {

        String dateFormat = queryParams.get("dateFormat");
        Locale clientApplicationLocale = null;
        String localeQueryParam = queryParams.get("locale");
        if (!(StringUtils.isBlank(localeQueryParam))) clientApplicationLocale = new Locale(queryParams.get("locale"));

        String underscore = "_";
        String space = " ";
        String pValue = null;
        String queryParamColumnUnderscored;
        String columnHeaderUnderscored;
        boolean notFound;

        Map<String, String> affectedColumns = new HashMap<String, String>();
        Set<String> keys = queryParams.keySet();
        for (String key : keys) {
            // ignores id and foreign key fields
            // also ignores locale and dateformat fields that are used for
            // validating numeric and date data
            if (!((key.equalsIgnoreCase("id")) || (key.equalsIgnoreCase(keyFieldName)) || (key.equals("locale")) || (key
                    .equals("dateFormat")))) {
                notFound = true;
                // matches incoming fields with and without underscores (spaces
                // and underscores considered the same)
                queryParamColumnUnderscored = genericDataService.replace(key, space, underscore);
                for (ResultsetColumnHeaderData columnHeader : columnHeaders) {
                    if (notFound) {
                        columnHeaderUnderscored = genericDataService.replace(columnHeader.getColumnName(), space, underscore);
                        if (queryParamColumnUnderscored.equalsIgnoreCase(columnHeaderUnderscored)) {
                            pValue = queryParams.get(key);
                            pValue = validateColumn(columnHeader, pValue, dateFormat, clientApplicationLocale);
                            affectedColumns.put(columnHeader.getColumnName(), pValue);
                            notFound = false;
                        }
                    }

                }
                if (notFound) { throw new PlatformDataIntegrityException("error.msg.column.not.found", "Column: " + key + " Not Found"); }
            }
        }
        return affectedColumns;
    }

    private String validateColumn(final ResultsetColumnHeaderData columnHeader, final String pValue, final String dateFormat,
            final Locale clientApplicationLocale) {

        String paramValue = pValue;
        if (columnHeader.isDateDisplayType() || columnHeader.isIntegerDisplayType() || columnHeader.isDecimalDisplayType()) {
            paramValue = paramValue.trim();
        }

        if (StringUtils.isEmpty(paramValue) && columnHeader.isMandatory()) {

            final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
            final ApiParameterError error = ApiParameterError.parameterError("error.msg.column.mandatory", "Mandatory",
                    columnHeader.getColumnName());
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }

        if (StringUtils.isNotEmpty(paramValue)) {

            if (columnHeader.hasColumnValues()) {
                if (columnHeader.isCodeValueDisplayType()) {

                    if (columnHeader.isColumnValueNotAllowed(paramValue)) {
                        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
                        final ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.columnValue",
                                "Value not found in Allowed Value list", columnHeader.getColumnName(), paramValue);
                        dataValidationErrors.add(error);
                        throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                                dataValidationErrors);
                    }

                    return paramValue;
                } else if (columnHeader.isCodeLookupDisplayType()) {

                    final Integer codeLookup = Integer.valueOf(paramValue);
                    if (columnHeader.isColumnCodeNotAllowed(codeLookup)) {
                        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
                        final ApiParameterError error = ApiParameterError.parameterError("error.msg.invalid.columnValue",
                                "Value not found in Allowed Value list", columnHeader.getColumnName(), paramValue);
                        dataValidationErrors.add(error);
                        throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                                dataValidationErrors);
                    }

                    return paramValue;
                } else {
                    throw new PlatformDataIntegrityException("error.msg.invalid.columnType.", "Code: " + columnHeader.getColumnName()
                            + " - Invalid Type " + columnHeader.getColumnType() + " (neither varchar nor int)");
                }
            }

            if (columnHeader.isDateDisplayType()) {
                final LocalDate tmpDate = helper.convertFrom(paramValue, columnHeader.getColumnName(), dateFormat, clientApplicationLocale);
                if (tmpDate == null) {
                    paramValue = null;
                } else {
                    paramValue = tmpDate.toString();
                }
            } else if (columnHeader.isIntegerDisplayType()) {
                Integer tmpInt = helper.convertToInteger(paramValue, columnHeader.getColumnName(), clientApplicationLocale);
                if (tmpInt == null) {
                    paramValue = null;
                } else {
                    paramValue = tmpInt.toString();
                }
            } else if (columnHeader.isDecimalDisplayType()) {
                BigDecimal tmpDecimal = helper.convertFrom(paramValue, columnHeader.getColumnName(), clientApplicationLocale);
                if (tmpDecimal == null) {
                    paramValue = null;
                } else {
                    paramValue = tmpDecimal.toString();
                }
            } else if (columnHeader.isString()) {
                if (paramValue.length() > columnHeader.getColumnLength()) {
                    ApiParameterError error = ApiParameterError.parameterError("validation.msg.datatable.entry.column.exceeds.maxlength",
                            "The column `" + columnHeader.getColumnName() + "` exceeds its defined max-length ",
                            columnHeader.getColumnName(), paramValue);
                    List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
                    dataValidationErrors.add(error);
                    throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                            dataValidationErrors);
                }
            }
        }

        return paramValue;
    }

    private String getDeleteEntriesSql(final String datatable, final String FKField, final Long appTableId) {

        return "delete from `" + datatable + "` where `" + FKField + "` = " + appTableId;

    }

    private String getDeleteEntrySql(final String datatable, final Long datatableId) {

        return "delete from `" + datatable + "` where `id` = " + datatableId;

    }

    private boolean notTheSame(final String currValue, final String pValue, final String colType) {
        if (StringUtils.isEmpty(currValue) && StringUtils.isEmpty(pValue)) return false;

        if (StringUtils.isEmpty(currValue)) return true;

        if (StringUtils.isEmpty(pValue)) return true;

        if ("DECIMAL".equalsIgnoreCase(colType)) {
            final BigDecimal currentDecimal = BigDecimal.valueOf(Double.valueOf(currValue));
            final BigDecimal newDecimal = BigDecimal.valueOf(Double.valueOf(pValue));

            return currentDecimal.compareTo(newDecimal) != 0;
        }

        if (currValue.equals(pValue)) return false;

        return true;
    }
}