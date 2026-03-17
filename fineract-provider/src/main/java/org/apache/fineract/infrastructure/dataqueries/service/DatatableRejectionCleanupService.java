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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatatableRejectionCleanupService implements CleanupService {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final FromJsonHelper fromJsonHelper;

    @Override
    public void cleanup(CommandSource commandSource) {

        boolean isCreateAction = "CREATE".equals(commandSource.getActionName());
        boolean isDatatableEntity = "DATATABLE".equals(commandSource.getEntityName());
        if (!isCreateAction || !isDatatableEntity) {
            return;
        }

        final String datatableName = fromJsonHelper.parse(commandSource.getCommandAsJson()).getAsJsonObject().get("datatableName")
                .getAsString();

        final String sql = "DROP TABLE IF EXISTS " + sqlGenerator.escape(datatableName);
        log.info("Cleaning up orphaned datatable after rejection: {}", datatableName);
        jdbcTemplate.execute(sql);

    }
}
