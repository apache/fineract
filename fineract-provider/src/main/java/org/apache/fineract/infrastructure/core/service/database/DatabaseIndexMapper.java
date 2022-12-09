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

import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public final class DatabaseIndexMapper {

    private DatabaseIndexMapper() {}

    public static List<IndexDetail> getIndexDetails(SqlRowSet rowset) {
        List<IndexDetail> indexes = new ArrayList<>();
        rowset.beforeFirst();
        while (rowset.next()) {
            indexes.add(new IndexDetail(rowset.getString(1)));
        }
        return indexes;
    }
}
