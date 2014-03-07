package org.mifosplatform.infrastructure.survey.service;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.JsonParserHelper;
import org.mifosplatform.infrastructure.core.service.RoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.data.DataTableValidator;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.mifosplatform.infrastructure.dataqueries.service.GenericDataService;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by Cieyou on 3/13/14.
 */
@Service
public class WriteSurveyServiceImpl implements WriteSurveyService {

    private final static Logger logger = LoggerFactory.getLogger(PovertyLineService.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final JsonParserHelper helper;
    private final GenericDataService genericDataService;
    private final DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final DataTableValidator dataTableValidator;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;

    @Autowired(required = true)
    WriteSurveyServiceImpl(final RoutingDataSource dataSource,
                                  final PlatformSecurityContext context,
                                   final FromJsonHelper fromJsonHelper,
                                   final GenericDataService genericDataService,
                                   final DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer,
                                   final DataTableValidator dataTableValidator,
                                   final ReadWriteNonCoreDataService readWriteNonCoreDataService) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.helper = new JsonParserHelper();
        this.genericDataService = genericDataService;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.dataTableValidator = dataTableValidator;
        this.readWriteNonCoreDataService = readWriteNonCoreDataService;

    }


    @Override
    @Transactional
    public CommandProcessingResult registerSurvey(JsonCommand command)
    {

        final String dataTableName = this.readWriteNonCoreDataService.getDataTableName(command.getUrl());
        final String permissionSql = this._getPermissionSql(dataTableName);
        this.readWriteNonCoreDataService.registerDatatable(command, permissionSql);

        return CommandProcessingResult.commandOnlyResult(command.commandId());

    }

    private String _getPermissionSql(final String dataTableName)
    {
        final String createPermission = "'CREATE_" + dataTableName + "'";
        final String createPermissionChecker = "'CREATE_" + dataTableName + "_CHECKER'";
        final String readPermission = "'READ_" + dataTableName + "'";
        final String updatePermission = "'UPDATE_" + dataTableName + "'";
        final String updatePermissionChecker = "'UPDATE_" + dataTableName + "_CHECKER'";
        final String deletePermission = "'DELETE_" + dataTableName + "'";
        final String deletePermissionChecker = "'DELETE_" + dataTableName + "_CHECKER'";

        return "insert into m_permission (grouping, code, action_name, entity_name, can_maker_checker) values "
                + "('datatable', "
                + createPermission
                + ", 'CREATE', '"
                + dataTableName
                + "', false),"
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
                + "', false),"
                + "('datatable', "
                + updatePermissionChecker
                + ", 'UPDATE', '"
                + dataTableName
                + "', false),"
                + "('datatable', "
                + deletePermission
                + ", 'DELETE', '"
                + dataTableName
                + "', false),"
                + "('datatable', "
                + deletePermissionChecker
                + ", 'DELETE', '"
                + dataTableName + "', false)";
    }

    @Transactional
    @Override
    public CommandProcessingResult fullFillSurvey(final String dataTableName, final Long appTableId, final JsonCommand command) {

        return readWriteNonCoreDataService.createPPIEntry(dataTableName,appTableId,command);
    }


}
