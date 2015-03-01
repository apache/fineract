/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.dataqueries.service;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.SQLGrammarException;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.mifosplatform.infrastructure.codes.service.CodeReadPlatformService;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.exception.PlatformServiceUnavailableException;
import org.mifosplatform.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.api.DataTableApiConstant;
import org.mifosplatform.infrastructure.dataqueries.data.DataTableValidator;
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
import org.springframework.dao.EmptyResultDataAccessException;
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

    private final static String CODE_VALUES_TABLE = "m_code_value";

    private final static Logger logger = LoggerFactory.getLogger(ReadWriteNonCoreDataServiceImpl.class);
    private final static HashMap<String, String> apiTypeToMySQL = new HashMap<String, String>() {

        {
            put("string", "VARCHAR");
            put("number", "INT");
            put("boolean", "BIT");
            put("decimal", "DECIMAL");
            put("date", "DATE");
            put("datetime", "DATETIME");
            put("text", "TEXT");
            put("dropdown", "INT");
        }
    };

    private final static List<String> stringDataTypes = Arrays.asList("char", "varchar", "blob", "text", "tinyblob", "tinytext",
            "mediumblob", "mediumtext", "longblob", "longtext");

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final JsonParserHelper helper;
    private final GenericDataService genericDataService;
    private final DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final ConfigurationDomainService configurationDomainService;
    private final CodeReadPlatformService codeReadPlatformService;
    private final DataTableValidator dataTableValidator;

    // private final GlobalConfigurationWritePlatformServiceJpaRepositoryImpl
    // configurationWriteService;

    @Autowired(required = true)
    public ReadWriteNonCoreDataServiceImpl(final RoutingDataSource dataSource, final PlatformSecurityContext context,
            final FromJsonHelper fromJsonHelper, final GenericDataService genericDataService,
            final DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer, final CodeReadPlatformService codeReadPlatformService,
            final ConfigurationDomainService configurationDomainService, final DataTableValidator dataTableValidator) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.helper = new JsonParserHelper();
        this.genericDataService = genericDataService;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.codeReadPlatformService = codeReadPlatformService;
        this.configurationDomainService = configurationDomainService;
        this.dataTableValidator = dataTableValidator;
        // this.configurationWriteService = configurationWriteService;
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
                + " where ur.appuser_id = " + this.context.authenticatedUser().getId()
                + " and (p.code in ('ALL_FUNCTIONS', 'ALL_FUNCTIONS_READ') or p.code = concat('READ_', registered_table_name))) "
                + andClause + " order by application_table_name, registered_table_name";

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        final List<DatatableData> datatables = new ArrayList<>();
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
                + " where ur.appuser_id = " + this.context.authenticatedUser().getId() + " and registered_table_name='" + datatable + "'"
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

        Integer category = DataTableApiConstant.CATEGORY_DEFAULT;

        final String permissionSql = this._getPermissionSql(dataTableName);
        this._registerDataTable(applicationTableName, dataTableName, category, permissionSql);

    }

    @Transactional
    @Override
    public void registerDatatable(final JsonCommand command) {

        final String applicationTableName = this.getTableName(command.getUrl());
        final String dataTableName = this.getDataTableName(command.getUrl());

        Integer category = this.getCategory(command);

        this.dataTableValidator.validateDataTableRegistration(command.json());
        final String permissionSql = this._getPermissionSql(dataTableName);
        this._registerDataTable(applicationTableName, dataTableName, category, permissionSql);

    }

    @Transactional
    @Override
    public void registerDatatable(final JsonCommand command, final String permissionSql) {
        final String applicationTableName = this.getTableName(command.getUrl());
        final String dataTableName = this.getDataTableName(command.getUrl());

        Integer category = this.getCategory(command);

        this.dataTableValidator.validateDataTableRegistration(command.json());

        this._registerDataTable(applicationTableName, dataTableName, category, permissionSql);

    }

    @Transactional
    private void _registerDataTable(final String applicationTableName, final String dataTableName, final Integer category,
            final String permissionsSql) {

        validateAppTable(applicationTableName);
        assertDataTableExists(dataTableName);

        final String registerDatatableSql = "insert into x_registered_table (registered_table_name, application_table_name,category) values ('"
                + dataTableName + "', '" + applicationTableName + "', '" + category + "')";

        try {

            final String[] sqlArray = { registerDatatableSql, permissionsSql };
            this.jdbcTemplate.batchUpdate(sqlArray);

            // add the registered table to the config if it is a ppi
            if (this.isSurveyCategory(category)) {
                this.jdbcTemplate.execute("insert into c_configuration (name, value, enabled ) values('" + dataTableName + "', '0','0')");
            }

        }
        /***
         * Strangely, a Hibernate contraint violation exception is thrown
         ****/
        catch (final ConstraintViolationException cve) {
            final Throwable realCause = cve.getCause();
            // even if duplicate is only due to permission duplicate, okay to
            // show duplicate datatable error msg
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.datatable.registered", "Datatable `" + dataTableName
                            + "` is already registered against an application table.", "dataTableName", dataTableName); }
        } catch (final DataIntegrityViolationException dve) {
            final Throwable realCause = dve.getMostSpecificCause();
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

    private String _getPermissionSql(final String dataTableName) {
        final String createPermission = "'CREATE_" + dataTableName + "'";
        final String createPermissionChecker = "'CREATE_" + dataTableName + "_CHECKER'";
        final String readPermission = "'READ_" + dataTableName + "'";
        final String updatePermission = "'UPDATE_" + dataTableName + "'";
        final String updatePermissionChecker = "'UPDATE_" + dataTableName + "_CHECKER'";
        final String deletePermission = "'DELETE_" + dataTableName + "'";
        final String deletePermissionChecker = "'DELETE_" + dataTableName + "_CHECKER'";

        return "insert into m_permission (grouping, code, action_name, entity_name, can_maker_checker) values " + "('datatable', "
                + createPermission + ", 'CREATE', '" + dataTableName + "', true)," + "('datatable', " + createPermissionChecker
                + ", 'CREATE', '" + dataTableName + "', false)," + "('datatable', " + readPermission + ", 'READ', '" + dataTableName
                + "', false)," + "('datatable', " + updatePermission + ", 'UPDATE', '" + dataTableName + "', true)," + "('datatable', "
                + updatePermissionChecker + ", 'UPDATE', '" + dataTableName + "', false)," + "('datatable', " + deletePermission
                + ", 'DELETE', '" + dataTableName + "', true)," + "('datatable', " + deletePermissionChecker + ", 'DELETE', '"
                + dataTableName + "', false)";

    }

    private Integer getCategory(final JsonCommand command) {
        Integer category = command.integerValueOfParameterNamedDefaultToNullIfZero(DataTableApiConstant.categoryParamName);
        if (category == null) category = DataTableApiConstant.CATEGORY_DEFAULT;
        return category;
    }

    private boolean isSurveyCategory(final Integer category) {
        return category.equals(DataTableApiConstant.CATEGORY_PPI);
    }

    @Override
    public String getDataTableName(String url) {

        String[] urlParts = url.split("/");

        return urlParts[3];

    }

    @Override
    public String getTableName(String url) {
        String[] urlParts = url.split("/");
        return urlParts[4];
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

        final String deleteFromConfigurationSql = "delete from c_configuration where name ='" + datatable + "'";

        String[] sqlArray = new String[4];
        sqlArray[0] = deleteRolePermissionsSql;
        sqlArray[1] = deletePermissionsSql;
        sqlArray[2] = deleteRegisteredDatatableSql;
        sqlArray[3] = deleteFromConfigurationSql;

        this.jdbcTemplate.batchUpdate(sqlArray);
    }

    @Transactional
    @Override
    public CommandProcessingResult createNewDatatableEntry(final String dataTableName, final Long appTableId, final JsonCommand command) {

        try {
            final String appTable = queryForApplicationTableName(dataTableName);
            final CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(appTable, appTableId);

            final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);

            final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
            final Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

            final String sql = getAddSql(columnHeaders, dataTableName, getFKField(appTable), appTableId, dataParams);

            this.jdbcTemplate.update(sql);

            return commandProcessingResult; //

        } catch (final ConstraintViolationException dve) {
            // NOTE: jdbctemplate throws a
            // org.hibernate.exception.ConstraintViolationException even though
            // it should be a DataAccessException?
            final Throwable realCause = dve.getCause();
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                            + "` and application table with identifier `" + appTableId + "`.", "dataTableName", dataTableName, appTableId); }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        } catch (final DataAccessException dve) {
            final Throwable realCause = dve.getMostSpecificCause();
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                            + "` and application table with identifier `" + appTableId + "`.", "dataTableName", dataTableName, appTableId); }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        }
    }

    @Override
    public CommandProcessingResult createPPIEntry(final String dataTableName, final Long appTableId, final JsonCommand command) {

        try {
            final String appTable = queryForApplicationTableName(dataTableName);
            final CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(appTable, appTableId);

            final List<ResultsetColumnHeaderData> columnHeaders = this.genericDataService.fillResultsetColumnHeaders(dataTableName);

            final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
            final Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

            final String sql = getAddSqlWithScore(columnHeaders, dataTableName, getFKField(appTable), appTableId, dataParams);

            this.jdbcTemplate.update(sql);

            return commandProcessingResult; //

        } catch (final ConstraintViolationException dve) {
            // NOTE: jdbctemplate throws a
            // org.hibernate.exception.ConstraintViolationException even though
            // it should be a DataAccessException?
            final Throwable realCause = dve.getCause();
            if (realCause.getMessage().contains("Duplicate entry")) { throw new PlatformDataIntegrityException(
                    "error.msg.datatable.entry.duplicate", "An entry already exists for datatable `" + dataTableName
                            + "` and application table with identifier `" + appTableId + "`.", "dataTableName", dataTableName, appTableId); }

            logAsErrorUnexpectedDataIntegrityException(dve);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "Unknown data integrity issue with resource.");
        } catch (final DataAccessException dve) {
            final Throwable realCause = dve.getMostSpecificCause();
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

    private void assertDataTableExists(final String datatableName) {
        final String sql = "select if((exists (select 1 from information_schema.tables where table_schema = schema() and table_name = ?)) = 1, 'true', 'false')";
        final String dataTableExistsString = this.jdbcTemplate.queryForObject(sql, String.class, new Object[] { datatableName });
        final boolean dataTableExists = new Boolean(dataTableExistsString);
        if (!dataTableExists) { throw new PlatformDataIntegrityException("error.msg.invalid.datatable", "Invalid Data Table: "
                + datatableName, "name", datatableName); }
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

    private void parseDatatableColumnObjectForCreate(final JsonObject column, StringBuilder sqlBuilder,
            final StringBuilder constrainBuilder, final String dataTableNameAlias, final Map<String, Long> codeMappings,
            final boolean isConstraintApproach) {

        String name = (column.has("name")) ? column.get("name").getAsString() : null;
        final String type = (column.has("type")) ? column.get("type").getAsString().toLowerCase() : null;
        final Integer length = (column.has("length")) ? column.get("length").getAsInt() : null;
        final Boolean mandatory = (column.has("mandatory")) ? column.get("mandatory").getAsBoolean() : false;
        final String code = (column.has("code")) ? column.get("code").getAsString() : null;

        if (StringUtils.isNotBlank(code)) {
            if (isConstraintApproach) {
                codeMappings.put(dataTableNameAlias + "_" + name, this.codeReadPlatformService.retriveCode(code).getCodeId());
                constrainBuilder.append(", CONSTRAINT `fk_").append(dataTableNameAlias).append("_").append(name).append("` ")
                        .append("FOREIGN KEY (`" + name + "`) ").append("REFERENCES `").append(CODE_VALUES_TABLE).append("` (`id`)");
            } else {
                name = datatableColumnNameToCodeValueName(name, code);
            }
        }

        final String mysqlType = apiTypeToMySQL.get(type);
        sqlBuilder = sqlBuilder.append("`" + name + "` " + mysqlType);

        if (type != null) {
            if (type.equalsIgnoreCase("String")) {
                sqlBuilder = sqlBuilder.append("(" + length + ")");
            } else if (type.equalsIgnoreCase("Decimal")) {
                sqlBuilder = sqlBuilder.append("(19,6)");
            } else if (type.equalsIgnoreCase("Dropdown")) {
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
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final JsonElement element = this.fromJsonHelper.parse(command.json());
            final JsonArray columns = this.fromJsonHelper.extractJsonArrayNamed("columns", element);
            datatableName = this.fromJsonHelper.extractStringNamed("datatableName", element);
            final String apptableName = this.fromJsonHelper.extractStringNamed("apptableName", element);
            Boolean multiRow = this.fromJsonHelper.extractBooleanNamed("multiRow", element);

            /***
             * In cases of tables storing hierarchical entities (like m_group),
             * different entities would end up being stored in the same table.
             * 
             * Ex: Centers are a specific type of group, add abstractions for
             * the same
             ***/
            final String actualAppTableName = mapToActualAppTable(apptableName);

            if (multiRow == null) {
                multiRow = false;
            }

            validateDatatableName(datatableName);
            validateAppTable(apptableName);
            final boolean isConstraintApproach = this.configurationDomainService.isConstraintApproachEnabledForDatatables();
            final String fkColumnName = apptableName.substring(2) + "_id";
            final String dataTableNameAlias = datatableName.toLowerCase().replaceAll("\\s", "_");
            final String fkName = dataTableNameAlias + "_" + fkColumnName;
            StringBuilder sqlBuilder = new StringBuilder();
            final StringBuilder constrainBuilder = new StringBuilder();
            final Map<String, Long> codeMappings = new HashMap<>();
            sqlBuilder = sqlBuilder.append("CREATE TABLE `" + datatableName + "` (");

            if (multiRow) {
                sqlBuilder = sqlBuilder.append("`id` BIGINT(20) NOT NULL AUTO_INCREMENT, ").append(
                        "`" + fkColumnName + "` BIGINT(20) NOT NULL, ");
            } else {
                sqlBuilder = sqlBuilder.append("`" + fkColumnName + "` BIGINT(20) NOT NULL, ");
            }

            for (final JsonElement column : columns) {
                parseDatatableColumnObjectForCreate(column.getAsJsonObject(), sqlBuilder, constrainBuilder, dataTableNameAlias,
                        codeMappings, isConstraintApproach);
            }

            // Remove trailing comma and space
            sqlBuilder = sqlBuilder.delete(sqlBuilder.length() - 2, sqlBuilder.length());

            if (multiRow) {
                sqlBuilder = sqlBuilder.append(", PRIMARY KEY (`id`)")
                        .append(", KEY `fk_" + apptableName.substring(2) + "_id` (`" + fkColumnName + "`)")
                        .append(", CONSTRAINT `fk_" + fkName + "` ").append("FOREIGN KEY (`" + fkColumnName + "`) ")
                        .append("REFERENCES `" + actualAppTableName + "` (`id`)");
            } else {
                sqlBuilder = sqlBuilder.append(", PRIMARY KEY (`" + fkColumnName + "`)").append(", CONSTRAINT `fk_" + fkName + "` ")
                        .append("FOREIGN KEY (`" + fkColumnName + "`) ").append("REFERENCES `" + actualAppTableName + "` (`id`)");
            }

            sqlBuilder.append(constrainBuilder);

            sqlBuilder = sqlBuilder.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8;");
            this.jdbcTemplate.execute(sqlBuilder.toString());

            registerDatatable(datatableName, apptableName);
            registerColumnCodeMapping(codeMappings);
        } catch (final SQLGrammarException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");

            if (realCause.getMessage().toLowerCase().contains("duplicate column name")) {
                baseDataValidator.reset().parameter("name").failWithCode("duplicate.column.name");
            } else if (realCause.getMessage().contains("Table") && realCause.getMessage().contains("already exists")) {
                baseDataValidator.reset().parameter("datatableName").value(datatableName).failWithCode("datatable.already.exists");
            } else if (realCause.getMessage().contains("Column") && realCause.getMessage().contains("big")) {
                baseDataValidator.reset().parameter("column").failWithCode("length.too.big");
            } else if (realCause.getMessage().contains("Row") && realCause.getMessage().contains("large")) {
                baseDataValidator.reset().parameter("row").failWithCode("size.too.large");
            }

            throwExceptionIfValidationWarningsExist(dataValidationErrors);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withResourceIdAsString(datatableName).build();
    }

    private void parseDatatableColumnForUpdate(final JsonObject column,
            final Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition, StringBuilder sqlBuilder, final String datatableName,
            final StringBuilder constrainBuilder, final Map<String, Long> codeMappings, final List<String> removeMappings,
            final boolean isConstraintApproach) {

        String name = (column.has("name")) ? column.get("name").getAsString() : null;
        final String lengthStr = (column.has("length")) ? column.get("length").getAsString() : null;
        Integer length = (StringUtils.isNotBlank(lengthStr)) ? Integer.parseInt(lengthStr) : null;
        String newName = (column.has("newName")) ? column.get("newName").getAsString() : name;
        final Boolean mandatory = (column.has("mandatory")) ? column.get("mandatory").getAsBoolean() : false;
        final String after = (column.has("after")) ? column.get("after").getAsString() : null;
        final String code = (column.has("code")) ? column.get("code").getAsString() : null;
        final String newCode = (column.has("newCode")) ? column.get("newCode").getAsString() : null;
        final String dataTableNameAlias = datatableName.toLowerCase().replaceAll("\\s", "_");
        if (isConstraintApproach) {
            if (StringUtils.isBlank(newName)) {
                newName = name;
            }
            if (!StringUtils.equalsIgnoreCase(code, newCode) || !StringUtils.equalsIgnoreCase(name, newName)) {
                if (StringUtils.equalsIgnoreCase(code, newCode)) {
                    final int codeId = getCodeIdForColumn(dataTableNameAlias, name);
                    if (codeId > 0) {
                        removeMappings.add(dataTableNameAlias + "_" + name);
                        constrainBuilder.append(", DROP FOREIGN KEY `fk_").append(dataTableNameAlias).append("_").append(name).append("` ");
                        codeMappings.put(dataTableNameAlias + "_" + newName, (long) codeId);
                        constrainBuilder.append(",ADD CONSTRAINT  `fk_").append(dataTableNameAlias).append("_").append(newName)
                                .append("` ").append("FOREIGN KEY (`" + newName + "`) ").append("REFERENCES `").append(CODE_VALUES_TABLE)
                                .append("` (`id`)");
                    }

                } else {
                    if (code != null) {
                        removeMappings.add(dataTableNameAlias + "_" + name);
                        if (newCode == null || !StringUtils.equalsIgnoreCase(name, newName)) {
                            constrainBuilder.append(", DROP FOREIGN KEY `fk_").append(dataTableNameAlias).append("_").append(name)
                                    .append("` ");
                        }
                    }
                    if (newCode != null) {
                        codeMappings.put(dataTableNameAlias + "_" + newName, this.codeReadPlatformService.retriveCode(newCode).getCodeId());
                        if (code == null || !StringUtils.equalsIgnoreCase(name, newName)) {
                            constrainBuilder.append(",ADD CONSTRAINT  `fk_").append(dataTableNameAlias).append("_").append(newName)
                                    .append("` ").append("FOREIGN KEY (`" + newName + "`) ").append("REFERENCES `")
                                    .append(CODE_VALUES_TABLE).append("` (`id`)");
                        }
                    }
                }
            }
        } else {
            if (StringUtils.isNotBlank(code)) {
                name = datatableColumnNameToCodeValueName(name, code);
                if (StringUtils.isNotBlank(newCode)) {
                    newName = datatableColumnNameToCodeValueName(newName, newCode);
                } else {
                    newName = datatableColumnNameToCodeValueName(newName, code);
                }
            }
        }
        if (!mapColumnNameDefinition.containsKey(name)) { throw new PlatformDataIntegrityException(
                "error.msg.datatable.column.missing.update.parse", "Column " + name + " does not exist.", name); }
        final String type = mapColumnNameDefinition.get(name).getColumnType();
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

    @SuppressWarnings("deprecation")
    private int getCodeIdForColumn(final String dataTableNameAlias, final String name) {
        final StringBuilder checkColumnCodeMapping = new StringBuilder();
        checkColumnCodeMapping.append("select ccm.code_id from x_table_column_code_mappings ccm where ccm.column_alias_name='")
                .append(dataTableNameAlias).append("_").append(name).append("'");
        int codeId = 0;
        try {
            codeId = this.jdbcTemplate.queryForInt(checkColumnCodeMapping.toString());
        } catch (final EmptyResultDataAccessException e) {
            logger.info(e.getMessage());
        }
        return codeId;
    }

    private void parseDatatableColumnForAdd(final JsonObject column, StringBuilder sqlBuilder, final String dataTableNameAlias,
            final StringBuilder constrainBuilder, final Map<String, Long> codeMappings, final boolean isConstraintApproach) {

        String name = (column.has("name")) ? column.get("name").getAsString() : null;
        final String type = (column.has("type")) ? column.get("type").getAsString().toLowerCase() : null;
        final Integer length = (column.has("length")) ? column.get("length").getAsInt() : null;
        final Boolean mandatory = (column.has("mandatory")) ? column.get("mandatory").getAsBoolean() : false;
        final String after = (column.has("after")) ? column.get("after").getAsString() : null;
        final String code = (column.has("code")) ? column.get("code").getAsString() : null;

        if (StringUtils.isNotBlank(code)) {
            if (isConstraintApproach) {
                codeMappings.put(dataTableNameAlias + "_" + name, this.codeReadPlatformService.retriveCode(code).getCodeId());
                constrainBuilder.append(",ADD CONSTRAINT  `fk_").append(dataTableNameAlias).append("_").append(name).append("` ")
                        .append("FOREIGN KEY (`" + name + "`) ").append("REFERENCES `").append(CODE_VALUES_TABLE).append("` (`id`)");
            } else {
                name = datatableColumnNameToCodeValueName(name, code);
            }
        }

        final String mysqlType = apiTypeToMySQL.get(type);
        sqlBuilder = sqlBuilder.append(", ADD `" + name + "` " + mysqlType);

        if (type != null) {
            if (type.equalsIgnoreCase("String") && length != null) {
                sqlBuilder = sqlBuilder.append("(" + length + ")");
            } else if (type.equalsIgnoreCase("Decimal")) {
                sqlBuilder = sqlBuilder.append("(19,6)");
            } else if (type.equalsIgnoreCase("Dropdown")) {
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

    private void parseDatatableColumnForDrop(final JsonObject column, StringBuilder sqlBuilder, final String datatableName,
            final StringBuilder constrainBuilder, final List<String> codeMappings) {
        final String datatableAlias = datatableName.toLowerCase().replaceAll("\\s", "_");
        final String name = (column.has("name")) ? column.get("name").getAsString() : null;
        sqlBuilder = sqlBuilder.append(", DROP COLUMN `" + name + "`");
        final StringBuilder findFKSql = new StringBuilder();
        findFKSql.append("SELECT count(*)").append("FROM information_schema.TABLE_CONSTRAINTS i")
                .append(" WHERE i.CONSTRAINT_TYPE = 'FOREIGN KEY'").append(" AND i.TABLE_SCHEMA = DATABASE()")
                .append(" AND i.TABLE_NAME = '").append(datatableName).append("' AND i.CONSTRAINT_NAME = 'fk_").append(datatableAlias)
                .append("_").append(name).append("' ");
        @SuppressWarnings("deprecation")
        final int count = this.jdbcTemplate.queryForInt(findFKSql.toString());
        if (count > 0) {
            codeMappings.add(datatableAlias + "_" + name);
            constrainBuilder.append(", DROP FOREIGN KEY `fk_").append(datatableAlias).append("_").append(name).append("` ");
        }
    }

    private void registerColumnCodeMapping(final Map<String, Long> codeMappings) {
        if (codeMappings != null && !codeMappings.isEmpty()) {
            final String[] addSqlList = new String[codeMappings.size()];
            int i = 0;
            for (final Map.Entry<String, Long> mapEntry : codeMappings.entrySet()) {
                addSqlList[i++] = "insert into x_table_column_code_mappings (column_alias_name, code_id) values ('" + mapEntry.getKey()
                        + "'," + mapEntry.getValue() + ");";
            }

            this.jdbcTemplate.batchUpdate(addSqlList);
        }
    }

    private void deleteColumnCodeMapping(final List<String> columnNames) {
        if (columnNames != null && !columnNames.isEmpty()) {
            final String[] deleteSqlList = new String[columnNames.size()];
            int i = 0;
            for (final String columnName : columnNames) {
                deleteSqlList[i++] = "DELETE FROM x_table_column_code_mappings WHERE  column_alias_name='" + columnName + "';";
            }

            this.jdbcTemplate.batchUpdate(deleteSqlList);
        }

    }

    /**
     * Update data table, set column value to empty string where current value
     * is NULL. Run update SQL only if the "mandatory" property is set to true
     * 
     * @param datatableName
     *            Name of data table
     * @param column
     *            JSON encoded array of column properties
     * @see https://mifosforge.jira.com/browse/MIFOSX-1145
     **/
    private void removeNullValuesFromStringColumn(final String datatableName, final JsonObject column,
            final Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition) {
        final Boolean mandatory = (column.has("mandatory")) ? column.get("mandatory").getAsBoolean() : false;
        final String name = (column.has("name")) ? column.get("name").getAsString() : "";
        final String type = (mapColumnNameDefinition.containsKey(name)) ? mapColumnNameDefinition.get(name).getColumnType() : "";

        if (StringUtils.isNotEmpty(type)) {
            if (mandatory && stringDataTypes.contains(type.toLowerCase())) {
                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append("UPDATE `" + datatableName + "` SET `" + name + "` = '' WHERE `" + name + "` IS NULL");

                this.jdbcTemplate.update(sqlBuilder.toString());
            }
        }
    }

    @Transactional
    @Override
    public void updateDatatable(final String datatableName, final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final JsonElement element = this.fromJsonHelper.parse(command.json());
            final JsonArray changeColumns = this.fromJsonHelper.extractJsonArrayNamed("changeColumns", element);
            final JsonArray addColumns = this.fromJsonHelper.extractJsonArrayNamed("addColumns", element);
            final JsonArray dropColumns = this.fromJsonHelper.extractJsonArrayNamed("dropColumns", element);
            final String apptableName = this.fromJsonHelper.extractStringNamed("apptableName", element);

            validateDatatableName(datatableName);

            final List<ResultsetColumnHeaderData> columnHeaderData = this.genericDataService.fillResultsetColumnHeaders(datatableName);
            final Map<String, ResultsetColumnHeaderData> mapColumnNameDefinition = new HashMap<>();
            for (final ResultsetColumnHeaderData columnHeader : columnHeaderData) {
                mapColumnNameDefinition.put(columnHeader.getColumnName(), columnHeader);
            }

            final boolean isConstraintApproach = this.configurationDomainService.isConstraintApproachEnabledForDatatables();

            if (!StringUtils.isBlank(apptableName)) {
                validateAppTable(apptableName);

                final String oldApptableName = queryForApplicationTableName(datatableName);
                if (!StringUtils.equals(oldApptableName, apptableName)) {
                    final String oldFKName = oldApptableName.substring(2) + "_id";
                    final String newFKName = apptableName.substring(2) + "_id";
                    final String actualAppTableName = mapToActualAppTable(apptableName);
                    final String oldConstraintName = datatableName.toLowerCase().replaceAll("\\s", "_") + "_" + oldFKName;
                    final String newConstraintName = datatableName.toLowerCase().replaceAll("\\s", "_") + "_" + newFKName;
                    StringBuilder sqlBuilder = new StringBuilder();

                    if (mapColumnNameDefinition.containsKey("id")) {
                        sqlBuilder = sqlBuilder.append("ALTER TABLE `" + datatableName + "` ").append("DROP KEY `fk_" + oldFKName + "`,")
                                .append("DROP FOREIGN KEY `fk_" + oldConstraintName + "`,")
                                .append("CHANGE COLUMN `" + oldFKName + "` `" + newFKName + "` BIGINT(20) NOT NULL,")
                                .append("ADD KEY `fk_" + newFKName + "` (`" + newFKName + "`),")
                                .append("ADD CONSTRAINT `fk_" + newConstraintName + "` ").append("FOREIGN KEY (`" + newFKName + "`) ")
                                .append("REFERENCES `" + actualAppTableName + "` (`id`)");
                    } else {
                        sqlBuilder = sqlBuilder.append("ALTER TABLE `" + datatableName + "` ")
                                .append("DROP FOREIGN KEY `fk_" + oldConstraintName + "`,")
                                .append("CHANGE COLUMN `" + oldFKName + "` `" + newFKName + "` BIGINT(20) NOT NULL,")
                                .append("ADD CONSTRAINT `fk_" + newConstraintName + "` ").append("FOREIGN KEY (`" + newFKName + "`) ")
                                .append("REFERENCES `" + actualAppTableName + "` (`id`)");
                    }

                    this.jdbcTemplate.execute(sqlBuilder.toString());

                    deregisterDatatable(datatableName);
                    registerDatatable(datatableName, apptableName);
                }
            }

            if (changeColumns == null && addColumns == null && dropColumns == null) { return; }

            if (dropColumns != null) {

                StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE `" + datatableName + "`");
                final StringBuilder constrainBuilder = new StringBuilder();
                final List<String> codeMappings = new ArrayList<>();
                for (final JsonElement column : dropColumns) {
                    parseDatatableColumnForDrop(column.getAsJsonObject(), sqlBuilder, datatableName, constrainBuilder, codeMappings);
                }

                // Remove the first comma, right after ALTER TABLE `datatable`
                final int indexOfFirstComma = sqlBuilder.indexOf(",");
                if (indexOfFirstComma != -1) {
                    sqlBuilder = sqlBuilder.deleteCharAt(indexOfFirstComma);
                }
                sqlBuilder.append(constrainBuilder);
                this.jdbcTemplate.execute(sqlBuilder.toString());
                deleteColumnCodeMapping(codeMappings);
            }
            if (addColumns != null) {

                StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE `" + datatableName + "`");
                final StringBuilder constrainBuilder = new StringBuilder();
                final Map<String, Long> codeMappings = new HashMap<>();
                for (final JsonElement column : addColumns) {
                    parseDatatableColumnForAdd(column.getAsJsonObject(), sqlBuilder, datatableName.toLowerCase().replaceAll("\\s", "_"),
                            constrainBuilder, codeMappings, isConstraintApproach);
                }

                // Remove the first comma, right after ALTER TABLE `datatable`
                final int indexOfFirstComma = sqlBuilder.indexOf(",");
                if (indexOfFirstComma != -1) {
                    sqlBuilder = sqlBuilder.deleteCharAt(indexOfFirstComma);
                }
                sqlBuilder.append(constrainBuilder);
                this.jdbcTemplate.execute(sqlBuilder.toString());
                registerColumnCodeMapping(codeMappings);
            }
            if (changeColumns != null) {

                StringBuilder sqlBuilder = new StringBuilder("ALTER TABLE `" + datatableName + "`");
                final StringBuilder constrainBuilder = new StringBuilder();
                final Map<String, Long> codeMappings = new HashMap<>();
                final List<String> removeMappings = new ArrayList<>();
                for (final JsonElement column : changeColumns) {
                    // remove NULL values from column where mandatory is true
                    removeNullValuesFromStringColumn(datatableName, column.getAsJsonObject(), mapColumnNameDefinition);

                    parseDatatableColumnForUpdate(column.getAsJsonObject(), mapColumnNameDefinition, sqlBuilder, datatableName,
                            constrainBuilder, codeMappings, removeMappings, isConstraintApproach);
                }

                // Remove the first comma, right after ALTER TABLE `datatable`
                final int indexOfFirstComma = sqlBuilder.indexOf(",");
                if (indexOfFirstComma != -1) {
                    sqlBuilder = sqlBuilder.deleteCharAt(indexOfFirstComma);
                }
                sqlBuilder.append(constrainBuilder);
                try {
                    this.jdbcTemplate.execute(sqlBuilder.toString());
                    deleteColumnCodeMapping(removeMappings);
                    registerColumnCodeMapping(codeMappings);
                } catch (final GenericJDBCException e) {
                    if (e.getMessage().contains("Error on rename")) { throw new PlatformServiceUnavailableException(
                            "error.msg.datatable.column.update.not.allowed", "One of the column name modification not allowed"); }
                } catch (final Exception e) {
                    // handle all other exceptions in here

                    // check if exception message contains the
                    // "invalid use of null value" SQL exception message
                    // throw a 503 HTTP error -
                    // PlatformServiceUnavailableException
                    if (e.getMessage().toLowerCase().contains("invalid use of null value")) { throw new PlatformServiceUnavailableException(
                            "error.msg.datatable.column.update.not.allowed", "One of the data table columns contains null values"); }
                }
            }
        } catch (final SQLGrammarException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
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
            this.context.authenticatedUser();
            if (!isRegisteredDataTable(datatableName)) { throw new DatatableNotFoundException(datatableName); }
            validateDatatableName(datatableName);
            assertDataTableEmpty(datatableName);
            deregisterDatatable(datatableName);
            String[] sqlArray = null;
            if (this.configurationDomainService.isConstraintApproachEnabledForDatatables()) {
                final String deleteColumnCodeSql = "delete from x_table_column_code_mappings where column_alias_name like'"
                        + datatableName.toLowerCase().replaceAll("\\s", "_") + "_%'";
                sqlArray = new String[2];
                sqlArray[1] = deleteColumnCodeSql;
            } else {
                sqlArray = new String[1];
            }
            final String sql = "DROP TABLE `" + datatableName + "`";
            sqlArray[0] = sql;
            this.jdbcTemplate.batchUpdate(sqlArray);
        } catch (final SQLGrammarException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("datatable");
            if (realCause.getMessage().contains("Unknown table")) {
                baseDataValidator.reset().parameter("datatableName").failWithCode("does.not.exist");
            }

            throwExceptionIfValidationWarningsExist(dataValidationErrors);
        }
    }

    private void assertDataTableEmpty(final String datatableName) {
        final String sql = "select count(*) from `" + datatableName + "`";
        final int rowCount = this.jdbcTemplate.queryForObject(sql, Integer.class);
        if (rowCount != 0) {
            throw new GeneralPlatformDomainRuleException("error.msg.non.empty.datatable.cannot.be.deleted", "Non-empty datatable cannot be deleted.");
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
        final CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(appTable, appTableId);

        final GenericResultsetData grs = retrieveDataTableGenericResultSetForUpdate(appTable, dataTableName, appTableId, datatableId);

        if (grs.hasNoEntries()) { throw new DatatableNotFoundException(dataTableName, appTableId); }

        if (grs.hasMoreThanOneEntry()) { throw new PlatformDataIntegrityException("error.msg.attempting.multiple.update",
                "Application table: " + dataTableName + " Foreign key id: " + appTableId); }

        final Type typeOfMap = new TypeToken<Map<String, String>>() {}.getType();
        final Map<String, String> dataParams = this.fromJsonHelper.extractDataMap(typeOfMap, command.json());

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
            final String sql = getUpdateSql(grs.getColumnHeaders(), dataTableName, pkName, pkValue, changes);
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
        final CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(appTable, appTableId);

        final String deleteOneToOneEntrySql = getDeleteEntriesSql(dataTableName, getFKField(appTable), appTableId);

        final int rowsDeleted = this.jdbcTemplate.update(deleteOneToOneEntrySql);
        if (rowsDeleted < 1) { throw new DatatableNotFoundException(dataTableName, appTableId); }

        return commandProcessingResult;
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteDatatableEntry(final String dataTableName, final Long appTableId, final Long datatableId) {

        final String appTable = queryForApplicationTableName(dataTableName);
        final CommandProcessingResult commandProcessingResult = checkMainResourceExistsWithinScope(appTable, appTableId);

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

        final Long officeId = getLongSqlRowSet(rs, "officeId");
        final Long groupId = getLongSqlRowSet(rs, "groupId");
        final Long clientId = getLongSqlRowSet(rs, "clientId");
        final Long savingsId = getLongSqlRowSet(rs, "savingsId");
        final Long LoanId = getLongSqlRowSet(rs, "loanId");
        final Long entityId = getLongSqlRowSet(rs, "entityId");

        if (rs.next()) { throw new DatatableSystemErrorException("System Error: More than one row returned from data scoping query"); }

        return new CommandProcessingResultBuilder() //
                .withOfficeId(officeId) //
                .withGroupId(groupId) //
                .withClientId(clientId) //
                .withSavingsId(savingsId) //
                .withLoanId(LoanId).withEntityId(entityId)//
                .build();
    }

    private Long getLongSqlRowSet(final SqlRowSet rs, final String column) {
        Long val = rs.getLong(column);
        if (val == 0) {
            val = null;
        }
        return val;
    }

    private String dataScopedSQL(final String appTable, final Long appTableId) {
        /*
         * unfortunately have to, one way or another, be able to restrict data
         * to the users office hierarchy. Here, a few key tables are done. But
         * if additional fields are needed on other tables the same pattern
         * applies
         */

        final AppUser currentUser = this.context.authenticatedUser();
        String scopedSQL = null;
        /*
         * m_loan and m_savings_account are connected to an m_office thru either
         * an m_client or an m_group If both it means it relates to an m_client
         * that is in a group (still an m_client account)
         */
        if (appTable.equalsIgnoreCase("m_loan")) {
            scopedSQL = "select  distinctrow x.* from ("
                    + " (select o.id as officeId, l.group_id as groupId, l.client_id as clientId, null as savingsId, l.id as loanId, null as entityId from m_loan l "
                    + " join m_client c on c.id = l.client_id "
                    + " join m_office o on o.id = c.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy()
                    + "%'"
                    + " where l.id = "
                    + appTableId
                    + ")"
                    + " union all "
                    + " (select o.id as officeId, l.group_id as groupId, l.client_id as clientId, null as savingsId, l.id as loanId, null as entityId from m_loan l "
                    + " join m_group g on g.id = l.group_id " + " join m_office o on o.id = g.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy() + "%'" + " where l.id = " + appTableId + ")" + " ) x";
        }
        if (appTable.equalsIgnoreCase("m_savings_account")) {
            scopedSQL = "select  distinctrow x.* from ("
                    + " (select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, null as entityId from m_savings_account s "
                    + " join m_client c on c.id = s.client_id "
                    + " join m_office o on o.id = c.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy()
                    + "%'"
                    + " where s.id = "
                    + appTableId
                    + ")"
                    + " union all "
                    + " (select o.id as officeId, s.group_id as groupId, s.client_id as clientId, s.id as savingsId, null as loanId, null as entityId from m_savings_account s "
                    + " join m_group g on g.id = s.group_id " + " join m_office o on o.id = g.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy() + "%'" + " where s.id = " + appTableId + ")" + " ) x";
        }
        if (appTable.equalsIgnoreCase("m_client")) {
            scopedSQL = "select o.id as officeId, null as groupId, c.id as clientId, null as savingsId, null as loanId, null as entityId from m_client c "
                    + " join m_office o on o.id = c.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy()
                    + "%'"
                    + " where c.id = " + appTableId;
        }
        if (appTable.equalsIgnoreCase("m_group") || appTable.equalsIgnoreCase("m_center")) {
            scopedSQL = "select o.id as officeId, g.id as groupId, null as clientId, null as savingsId, null as loanId, null as entityId from m_group g "
                    + " join m_office o on o.id = g.office_id and o.hierarchy like '"
                    + currentUser.getOffice().getHierarchy()
                    + "%'"
                    + " where g.id = " + appTableId;
        }
        if (appTable.equalsIgnoreCase("m_office")) {
            scopedSQL = "select o.id as officeId, null as groupId, null as clientId, null as savingsId, null as loanId, null as entityId from m_office o "
                    + " where o.hierarchy like '" + currentUser.getOffice().getHierarchy() + "%'" + " and o.id = " + appTableId;
        }

        if (appTable.equalsIgnoreCase("m_product_loan") || appTable.equalsIgnoreCase("m_savings_product")) {
            scopedSQL = "select null as officeId, null as groupId, null as clientId, null as savingsId, null as loanId, p.id as entityId from "
                    + appTable + " as p WHERE p.id = " + appTableId;
        }

        if (scopedSQL == null) { throw new PlatformDataIntegrityException("error.msg.invalid.dataScopeCriteria", "Application Table: "
                + appTable + " not catered for in data Scoping"); }

        return scopedSQL;

    }

    private void validateAppTable(final String appTable) {

        if (appTable.equalsIgnoreCase("m_loan")) { return; }
        if (appTable.equalsIgnoreCase("m_savings_account")) { return; }
        if (appTable.equalsIgnoreCase("m_client")) { return; }
        if (appTable.equalsIgnoreCase("m_group")) { return; }
        if (appTable.equalsIgnoreCase("m_center")) { return; }
        if (appTable.equalsIgnoreCase("m_office")) { return; }
        if (appTable.equalsIgnoreCase("m_product_loan")) { return; }
        if (appTable.equalsIgnoreCase("m_savings_product")) { return; }

        throw new PlatformDataIntegrityException("error.msg.invalid.application.table", "Invalid Application Table: " + appTable, "name",
                appTable);
    }

    private String mapToActualAppTable(final String appTable) {
        if (appTable.equalsIgnoreCase("m_center")) { return "m_group"; }
        return appTable;
    }

    private List<ResultsetRowData> fillDatatableResultSetDataRows(final String sql) {

        final SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);

        final List<ResultsetRowData> resultsetDataRows = new ArrayList<>();

        final SqlRowSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {
            final List<String> columnValues = new ArrayList<>();
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
        final String singleQuote = "'";

        String insertColumns = "";
        String selectColumns = "";
        String columnName = "";
        String pValue = null;
        for (final ResultsetColumnHeaderData pColumnHeader : columnHeaders) {
            final String key = pColumnHeader.getColumnName();
            if (affectedColumns.containsKey(key)) {
                pValue = affectedColumns.get(key);
                if (StringUtils.isEmpty(pValue)) {
                    pValueWrite = "null";
                } else {
                    if ("bit".equalsIgnoreCase(pColumnHeader.getColumnType())) {
                        pValueWrite = BooleanUtils.toString(BooleanUtils.toBooleanObject(pValue), "1", "0", "null");
                    } else {
                        pValueWrite = singleQuote + this.genericDataService.replace(pValue, singleQuote, singleQuote + singleQuote)
                                + singleQuote;
                    }

                }
                columnName = "`" + key + "`";
                insertColumns += ", " + columnName;
                selectColumns += "," + pValueWrite + " as " + columnName;
            }
        }

        addSql = "insert into `" + datatable + "` (`" + fkName + "` " + insertColumns + ")" + " select " + appTableId + " as id"
                + selectColumns;

        logger.info(addSql);

        return addSql;
    }

    /**
     * This method is used special for ppi cases Where the score need to be
     * computed
     * 
     * @param columnHeaders
     * @param datatable
     * @param fkName
     * @param appTableId
     * @param queryParams
     * @return
     */
    public String getAddSqlWithScore(final List<ResultsetColumnHeaderData> columnHeaders, final String datatable, final String fkName,
            final Long appTableId, final Map<String, String> queryParams) {

        final Map<String, String> affectedColumns = getAffectedColumns(columnHeaders, queryParams, fkName);

        String pValueWrite = "";
        String scoresId = " ";
        final String singleQuote = "'";

        String insertColumns = "";
        String selectColumns = "";
        String columnName = "";
        String pValue = null;
        for (final String key : affectedColumns.keySet()) {
            pValue = affectedColumns.get(key);

            if (StringUtils.isEmpty(pValue)) {
                pValueWrite = "null";
            } else {
                pValueWrite = singleQuote + this.genericDataService.replace(pValue, singleQuote, singleQuote + singleQuote) + singleQuote;

                scoresId += pValueWrite + " ,";

            }
            columnName = "`" + key + "`";
            insertColumns += ", " + columnName;
            selectColumns += "," + pValueWrite + " as " + columnName;
        }

        scoresId = scoresId.replaceAll(" ,$", "");

        String vaddSql = "insert into `" + datatable + "` (`" + fkName + "` " + insertColumns + ", `score` )" + " select " + appTableId
                + " as id" + selectColumns + " , ( SELECT SUM( code_score ) FROM m_code_value WHERE m_code_value.id IN (" + scoresId
                + " ) ) as score";

        logger.info(vaddSql);

        return vaddSql;
    }

    private String getUpdateSql(List<ResultsetColumnHeaderData> columnHeaders, final String datatable, final String keyFieldName,
            final Long keyFieldValue, final Map<String, Object> changedColumns) {

        // just updating fields that have changed since pre-update read - though
        // its possible these values are different from the page the user was
        // looking at and even different from the current db values (if some
        // other update got in quick) - would need a version field for
        // completeness but its okay to take this risk with additional fields
        // data

        if (changedColumns.size() == 0) { return null; }

        String pValue = null;
        String pValueWrite = "";
        final String singleQuote = "'";
        boolean firstColumn = true;
        String sql = "update `" + datatable + "` ";
        for (final ResultsetColumnHeaderData pColumnHeader : columnHeaders) {
            final String key = pColumnHeader.getColumnName();
            if (changedColumns.containsKey(key)) {
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
                    if ("bit".equalsIgnoreCase(pColumnHeader.getColumnType())) {
                        pValueWrite = BooleanUtils.toString(BooleanUtils.toBooleanObject(pValue), "1", "0", "null");
                    } else {
                        pValueWrite = singleQuote + this.genericDataService.replace(pValue, singleQuote, singleQuote + singleQuote)
                                + singleQuote;
                    }
                }
                sql += "`" + key + "` = " + pValueWrite;
            }
        }

        sql += " where " + keyFieldName + " = " + keyFieldValue;

        return sql;
    }

    private Map<String, Object> getAffectedAndChangedColumns(final GenericResultsetData grs, final Map<String, String> queryParams,
            final String fkName) {

        final Map<String, String> affectedColumns = getAffectedColumns(grs.getColumnHeaders(), queryParams, fkName);
        final Map<String, Object> affectedAndChangedColumns = new HashMap<>();

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

        final List<String> columnValues = grs.getData().get(0).getRow();

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

    public Map<String, String> getAffectedColumns(final List<ResultsetColumnHeaderData> columnHeaders,
            final Map<String, String> queryParams, final String keyFieldName) {

        final String dateFormat = queryParams.get("dateFormat");
        Locale clientApplicationLocale = null;
        final String localeQueryParam = queryParams.get("locale");
        if (!(StringUtils.isBlank(localeQueryParam))) {
            clientApplicationLocale = new Locale(queryParams.get("locale"));
        }

        final String underscore = "_";
        final String space = " ";
        String pValue = null;
        String queryParamColumnUnderscored;
        String columnHeaderUnderscored;
        boolean notFound;

        final Map<String, String> affectedColumns = new HashMap<>();
        final Set<String> keys = queryParams.keySet();
        for (final String key : keys) {
            // ignores id and foreign key fields
            // also ignores locale and dateformat fields that are used for
            // validating numeric and date data
            if (!((key.equalsIgnoreCase("id")) || (key.equalsIgnoreCase(keyFieldName)) || (key.equals("locale")) || (key
                    .equals("dateFormat")))) {
                notFound = true;
                // matches incoming fields with and without underscores (spaces
                // and underscores considered the same)
                queryParamColumnUnderscored = this.genericDataService.replace(key, space, underscore);
                for (final ResultsetColumnHeaderData columnHeader : columnHeaders) {
                    if (notFound) {
                        columnHeaderUnderscored = this.genericDataService.replace(columnHeader.getColumnName(), space, underscore);
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
        if (columnHeader.isDateDisplayType() || columnHeader.isDateTimeDisplayType() || columnHeader.isIntegerDisplayType()
                || columnHeader.isDecimalDisplayType() || columnHeader.isBooleanDisplayType()) {
            // only trim if string is not empty and is not null.
            // throws a NULL pointer exception if the check below is not applied
            paramValue = StringUtils.isNotEmpty(paramValue) ? paramValue.trim() : paramValue;
        }

        if (StringUtils.isEmpty(paramValue) && columnHeader.isMandatory()) {

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
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
                        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
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
                        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
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
                final LocalDate tmpDate = JsonParserHelper.convertFrom(paramValue, columnHeader.getColumnName(), dateFormat,
                        clientApplicationLocale);
                if (tmpDate == null) {
                    paramValue = null;
                } else {
                    paramValue = tmpDate.toString();
                }
            } else if (columnHeader.isDateTimeDisplayType()) {
                final LocalDateTime tmpDateTime = JsonParserHelper.convertDateTimeFrom(paramValue, columnHeader.getColumnName(),
                        dateFormat, clientApplicationLocale);
                if (tmpDateTime == null) {
                    paramValue = null;
                } else {
                    paramValue = tmpDateTime.toString();
                }
            } else if (columnHeader.isIntegerDisplayType()) {
                final Integer tmpInt = this.helper.convertToInteger(paramValue, columnHeader.getColumnName(), clientApplicationLocale);
                if (tmpInt == null) {
                    paramValue = null;
                } else {
                    paramValue = tmpInt.toString();
                }
            } else if (columnHeader.isDecimalDisplayType()) {
                final BigDecimal tmpDecimal = this.helper.convertFrom(paramValue, columnHeader.getColumnName(), clientApplicationLocale);
                if (tmpDecimal == null) {
                    paramValue = null;
                } else {
                    paramValue = tmpDecimal.toString();
                }
            } else if (columnHeader.isBooleanDisplayType()) {

                final Boolean tmpBoolean = BooleanUtils.toBooleanObject(paramValue);
                if (tmpBoolean == null) {
                    final ApiParameterError error = ApiParameterError.parameterError("validation.msg.invalid.boolean.format",
                            "The parameter " + columnHeader.getColumnName() + " has value: " + paramValue
                                    + " which is invalid boolean value.", columnHeader.getColumnName(), paramValue);
                    final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                    dataValidationErrors.add(error);
                    throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                            dataValidationErrors);
                }
                paramValue = tmpBoolean.toString();
            } else if (columnHeader.isString()) {
                if (paramValue.length() > columnHeader.getColumnLength()) {
                    final ApiParameterError error = ApiParameterError.parameterError(
                            "validation.msg.datatable.entry.column.exceeds.maxlength", "The column `" + columnHeader.getColumnName()
                                    + "` exceeds its defined max-length ", columnHeader.getColumnName(), paramValue);
                    final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
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
        if (StringUtils.isEmpty(currValue) && StringUtils.isEmpty(pValue)) { return false; }

        if (StringUtils.isEmpty(currValue)) { return true; }

        if (StringUtils.isEmpty(pValue)) { return true; }

        if ("DECIMAL".equalsIgnoreCase(colType)) {
            final BigDecimal currentDecimal = BigDecimal.valueOf(Double.valueOf(currValue));
            final BigDecimal newDecimal = BigDecimal.valueOf(Double.valueOf(pValue));

            return currentDecimal.compareTo(newDecimal) != 0;
        }

        if (currValue.equals(pValue)) { return false; }

        return true;
    }
}