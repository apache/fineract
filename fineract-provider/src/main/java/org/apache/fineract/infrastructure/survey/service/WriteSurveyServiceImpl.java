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
package org.apache.fineract.infrastructure.survey.service;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
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
