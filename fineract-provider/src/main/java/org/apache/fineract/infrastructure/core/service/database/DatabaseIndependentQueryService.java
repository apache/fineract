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
package org.apache.fineract.infrastructure.core.service.database;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.sql.SQLException;
import java.util.Collection;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

@Component
public class DatabaseIndependentQueryService implements DatabaseQueryService {

    private final Collection<DatabaseQueryService> queryServices;

    @Autowired
    public DatabaseIndependentQueryService(Collection<DatabaseQueryService> queryServices) {
        this.queryServices = queryServices;
    }

    private DatabaseQueryService choose(DataSource dataSource) {
        try {
            DatabaseQueryService result = null;
            if (isNotEmpty(queryServices)) {
                result = queryServices.stream().filter(DatabaseQueryService::isSupported).findAny().orElse(null);
            }
            if (result == null) {
                throw new IllegalStateException("DataSource not supported: " + dataSource.getConnection().getMetaData().getURL());
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Error while trying to choose the proper query service", e);
        }
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public boolean isTablePresent(DataSource dataSource, String tableName) {
        return choose(dataSource).isTablePresent(dataSource, tableName);
    }

    @Override
    public SqlRowSet getTableColumns(DataSource dataSource, String tableName) {
        return choose(dataSource).getTableColumns(dataSource, tableName);
    }
}
