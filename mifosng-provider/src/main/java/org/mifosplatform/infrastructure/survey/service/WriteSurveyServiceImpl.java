/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.survey.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by Cieyou on 3/13/14.
 */
@Service
public class WriteSurveyServiceImpl implements WriteSurveyService {

    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;

    @Autowired(required = true)
    WriteSurveyServiceImpl(final ReadWriteNonCoreDataService readWriteNonCoreDataService) {
        this.readWriteNonCoreDataService = readWriteNonCoreDataService;

    }

    @Override
    @Transactional
    public CommandProcessingResult registerSurvey(JsonCommand command) {

        final String dataTableName = this.readWriteNonCoreDataService.getDataTableName(command.getUrl());
        final String permissionSql = this._getPermissionSql(dataTableName);
        this.readWriteNonCoreDataService.registerDatatable(command, permissionSql);

        return CommandProcessingResult.commandOnlyResult(command.commandId());

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
                + createPermission + ", 'CREATE', '" + dataTableName + "', false)," + "('datatable', " + createPermissionChecker
                + ", 'CREATE', '" + dataTableName + "', false)," + "('datatable', " + readPermission + ", 'READ', '" + dataTableName
                + "', false)," + "('datatable', " + updatePermission + ", 'UPDATE', '" + dataTableName + "', false)," + "('datatable', "
                + updatePermissionChecker + ", 'UPDATE', '" + dataTableName + "', false)," + "('datatable', " + deletePermission
                + ", 'DELETE', '" + dataTableName + "', false)," + "('datatable', " + deletePermissionChecker + ", 'DELETE', '"
                + dataTableName + "', false)";
    }

    @Transactional
    @Override
    public CommandProcessingResult fullFillSurvey(final String dataTableName, final Long appTableId, final JsonCommand command) {

        return readWriteNonCoreDataService.createPPIEntry(dataTableName, appTableId, command);
    }

}
